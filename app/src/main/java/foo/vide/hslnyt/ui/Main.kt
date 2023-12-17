package foo.vide.hslnyt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import foo.vide.hslnyt.repo.LocationRepository
import foo.vide.hslnyt.repo.StopsRepository
import foo.vide.hslnyt.ui.theme.HSLNytTheme
import foo.vide.hslnyt.util.PreviewLightDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

@Composable
fun HSLNyt(stopsRepository: StopsRepository, locationRepository: LocationRepository) = Surface {
    val stops = stopsRepository.stops.value
    val location = locationRepository.location.value
    val locationStatus = locationRepository.locationStatus.value
    val composableScope = rememberCoroutineScope()

    // Try to fetch location on init
    LaunchedEffect(true) {
        locationRepository.requestLocationUpdate()
    }

    LaunchedEffect(location) {
        while (true) {
            location?.let {
                stopsRepository.getStops(location)
            }
            delay(30.seconds)
        }
    }

    val tickTime = mutableStateOf(Clock.System.now())
    val timestr = remember { mutableStateOf("") }
    LaunchedEffect(true) {
        while (true) {
            tickTime.value = Clock.System.now()
            timestr.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            delay(1000)
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
                        location?.let { stopsRepository.getStops(it) }
                    }
                }
            ) {
                Text("Refresh")
            }

            Text(timestr.value)

            Button(
                onClick = { locationRepository.requestLocationUpdate() },
                enabled = locationStatus != LocationRepository.LoadingStatus.LOADING,
            ) {
                Text("Location")
            }
        }

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            stops.forEach { StopCard(it, timeProvider = { tickTime.value }) }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() = HSLNytTheme {
    HSLNyt(
        stopsRepository = StopsRepository.Preview,
        locationRepository = LocationRepository.Preview
    )
}