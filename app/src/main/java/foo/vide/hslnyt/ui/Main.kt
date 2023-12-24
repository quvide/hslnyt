package foo.vide.hslnyt.ui

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import foo.vide.hslnyt.R
import foo.vide.hslnyt.StopsByRadiusQuery
import foo.vide.hslnyt.repo.LocationRepository
import foo.vide.hslnyt.repo.StopsRepository
import foo.vide.hslnyt.ui.theme.HSLNytTheme
import foo.vide.hslnyt.util.PreviewLightDark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

@Composable
fun HSLNyt(
    stopsRepository: StopsRepository,
    locationRepository: LocationRepository
) {
    val stops = stopsRepository.stops.value
    val location = locationRepository.location.value
    val scope = rememberCoroutineScope()
    val lastTime = remember { mutableStateOf(Clock.System.now()) }
    val aboutDialogOpen = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                stopsRepository = stopsRepository,
                locationRepository = locationRepository,
                scope = scope,
                openAboutDialog = { aboutDialogOpen.value = true }
            )
        }
    ) { innerPadding ->
        Surface(
            Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            // Try to fetch location on init
            LaunchedEffect(true) {
                locationRepository.requestLocationUpdate()
            }

            LaunchedEffect(location) {
                while (true) {
                    location?.let { stopsRepository.getStops(it) }
                    delay(30.seconds)
                }
            }

            LaunchedEffect(true) {
                while (true) {
                    val currentTime = Clock.System.now()
                    if ((currentTime - lastTime.value) > 1.seconds) {
                        lastTime.value = currentTime
                    }
                    delay(1000)
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 10.dp),
            ) {
                val vehicleSelectorState = rememberVehicleSelectorState()
                VehicleSelector(vehicleSelectorState)

                Spacer(Modifier.height(10.dp))

                val filteredStops = remember(vehicleSelectorState.selected.value, stops) {
                    stops.filter {
                        val mode = when (vehicleSelectorState.selected.value?.mode) {
                            null -> true
                            it.stop?.vehicleMode -> true
                            else -> false
                        }

                        return@filter mode && it.stop?.stoptimesWithoutPatterns?.isNotEmpty() == true
                    }
                }

                if (filteredStops.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        filteredStops.forEach { StopCard(it, timeProvider = { lastTime.value }) }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_stops),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (aboutDialogOpen.value) {
        Dialog(onDismissRequest = { aboutDialogOpen.value = false }) {
            About()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scope: CoroutineScope,
    stopsRepository: StopsRepository,
    locationRepository: LocationRepository,
    openAboutDialog: () -> Unit
) {
    val location = locationRepository.location.value

    TopAppBar(
        title = {
            Row {
                Text(stringResource(R.string.app_name))
            }
        },
        actions = {
            IconButton(
                onClick = { scope.launch { location?.let { stopsRepository.getStops(it) } } },
                enabled = location != null && !stopsRepository.refreshing.value
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
            IconButton(
                onClick = { locationRepository.requestLocationUpdate() },
                enabled = locationRepository.locationStatus.value != LocationRepository.LoadingStatus.LOADING
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Refresh location")
            }
            val menuVisible = remember { mutableStateOf(false) }
            IconButton(onClick = { menuVisible.value = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Open popup menu")
                AboutMenu(
                    expanded = menuVisible.value,
                    onDismissRequest = { menuVisible.value = !menuVisible.value },
                    openAboutDialog = openAboutDialog
                )
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun Preview() = HSLNytTheme {
    HSLNyt(
        stopsRepository = StopsRepository.Preview,
        locationRepository = LocationRepository.Preview
    )
}

@PreviewLightDark
@Composable
private fun PreviewEmpty() = HSLNytTheme {
    HSLNyt(
        stopsRepository = object : StopsRepository by StopsRepository.Preview {
            override val stops = remember { mutableStateOf(listOf<StopsByRadiusQuery.Node>()) }
        },
        locationRepository = LocationRepository.Preview
    )
}

@Composable
private fun AboutMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    openAboutDialog: () -> Unit
) = DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.about)) },
        onClick = openAboutDialog
    )
}

@Preview
@Composable
private fun PreviewAboutMenu() = HSLNytTheme {
    AboutMenu(true, {}, {})
}