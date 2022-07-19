package app.hslnyt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.hslnyt.ui.theme.HSLNytTheme
import com.apollographql.apollo3.ApolloClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val TAG = "HSLNyt"

class MainActivity : ComponentActivity() {
    // This is just stupid
    val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { locationRepository.locationPermissionCallback(it) }

    lateinit var locationRepository: LocationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationRepository = LocationRepository(this, LocationServices.getFusedLocationProviderClient(this), locationPermissionRequest)

        setContent {
            HSLNytTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HSLNyt(
                        stopsRepository = StopsRepository,
                        locationRepository = locationRepository,
                    )
                }
            }
        }
    }
}

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql")
    .build()

object StopsRepository {
    val stops = mutableStateOf<List<StopsByRadiusQuery.Node>>(emptyList())
    val refreshing = mutableStateOf(false)

    suspend fun getStops(location: Location) {
        withContext(Dispatchers.IO) {
            refreshing.value = true
            stops.value = apolloClient
                .query(StopsByRadiusQuery(lat = location.latitude, lon = location.longitude, radius = 1000, first = 25))
                .execute()
                .dataAssertNoErrors
                .stopsByRadius!!
                .edges!!
                .map { it!!.node!! }
            refreshing.value = false
        }
    }
}

class LocationRepository(private val context: Context, private val fusedLocationClient: FusedLocationProviderClient, private val locationRequest: ActivityResultLauncher<Array<String>>) {
    enum class LoadingStatus {
        INITIAL,
        LOADING,
        COMPLETE,
        DENIED,
    }

    val location = mutableStateOf<Location?>(null)
    val locationStatus = mutableStateOf<LoadingStatus>(LoadingStatus.INITIAL)

    fun requestLocationUpdate() {
        locationStatus.value = LoadingStatus.LOADING
        if (
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            )
                .addOnSuccessListener {
                    location.value = it
                    locationStatus.value = LoadingStatus.COMPLETE
                }
        } else {
            locationRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    fun locationPermissionCallback(permissions: Map<String, Boolean>) {
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                requestLocationUpdate()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                requestLocationUpdate()
            }
            else -> {
                locationStatus.value = LoadingStatus.DENIED
            }
        }
    }
}

@Composable
fun HSLNyt(stopsRepository: StopsRepository, locationRepository: LocationRepository) {
    val stops = stopsRepository.stops.value
    val location = locationRepository.location.value
    val locationStatus = locationRepository.locationStatus.value
    val composableScope = rememberCoroutineScope()

    // Try to fetch location on init
    LaunchedEffect(true) {
        locationRepository.requestLocationUpdate()
    }

    LaunchedEffect(location) {
        location?.let {
            stopsRepository.getStops(location)
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
    ) {
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                enabled = location != null && !stopsRepository.refreshing.value,
                onClick = {
                    composableScope.launch {
                        location?.let {
                            stopsRepository.getStops(location)
                        }
                    }
                }
            ) {
                Text("Refresh")
            }

            val timestr = remember { mutableStateOf("") }
            LaunchedEffect(true) {
                while (true) {
                    timestr.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    delay(1000)
                }
            }
            Text(timestr.value)

            Button(
                onClick = {
                    locationRepository.requestLocationUpdate()
                },
                enabled = locationStatus != LocationRepository.LoadingStatus.LOADING,
            ) {
                Text("Location")
            }
        }

        Spacer(Modifier.height(10.dp))

        if (stops != null) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                stops.forEach { StopCard(it) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopCard(stop: StopsByRadiusQuery.Node) {
    ElevatedCard {
        Column(Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stop.stop!!.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.alignByBaseline())
                Spacer(Modifier.width(10.dp))
                Text(stop.stop!!.code!!, style = MaterialTheme.typography.titleSmall, modifier = Modifier.alignByBaseline())
                Spacer(Modifier.weight(1f))
                Text("${stop.distance.toString()}m", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.alignByBaseline())
            }


            Spacer(Modifier.height(10.dp))
            stop.stop!!.stoptimesWithoutPatterns!!.forEach { StoptimeRow(it!!) }
        }
    }
}

@Composable
fun StoptimeRow(stoptime: StopsByRadiusQuery.StoptimesWithoutPattern) {
    Row {
        Text("${stoptime.trip!!.routeShortName} ${stoptime.trip!!.tripHeadsign}")
        Spacer(Modifier.weight(1f))
        Text("${ if (stoptime.realtime!!) formatTimeToHMS(stoptime.realtimeDeparture!!) else "" } (${formatTimeToHMS((stoptime.scheduledDeparture!!))})")
    }
}

fun formatTimeToHMS(secondsFromMidnight: Int): String {
    val hours = (secondsFromMidnight / (60*60))
    val minutes = (secondsFromMidnight - hours*60*60) / 60
    val seconds = (secondsFromMidnight - hours*60*60 - minutes*60)

    return "${(hours % 24).toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
