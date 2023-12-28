package foo.vide.hslnyt.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import foo.vide.hslnyt.StationsByIdQuery
import foo.vide.hslnyt.StopsByRadiusQuery
import foo.vide.hslnyt.repo.StationStop
import foo.vide.hslnyt.repo.StopsRepository
import foo.vide.hslnyt.type.Mode
import foo.vide.hslnyt.ui.theme.HSLNytTheme
import foo.vide.hslnyt.util.FormatTime
import foo.vide.hslnyt.util.PreviewLightDark
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs

enum class CardType {
    Stop,
    Station
}

@Composable
private fun CardHeader(
    name: String,
    type: CardType,
    distance: Int
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .alignByBaseline()
                .widthIn(max = 200.dp),
            maxLines = 4,
            softWrap = true
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = when (type) {
                CardType.Stop -> "(Stop)"
                CardType.Station -> "(Station)"
            },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alignByBaseline(),
            maxLines = 1
        )
        Spacer(Modifier.weight(1f))
        Text(
            "${distance}m",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.alignByBaseline(),
            maxLines = 1
        )
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
fun StationCard(
    stationStop: StationStop,
    timeProvider: () -> Instant
) {
    val station = stationStop.station
    val stops = stationStop.stops

    ElevatedCard {
        Column(Modifier.padding(10.dp)) {
            CardHeader(
                name = station.name,
                type = CardType.Station,
                distance = stops.first().distance!!
            )

            val platformCodeToStoptimes = stops.flatMap { stop ->
                stop.stop!!.stoptimesWithoutPatterns!!.map { stoptime ->
                    Pair(stoptime, stop.stop.platformCode)
                }
            }.sortedBy { it.first!!.scheduledDeparture }

            platformCodeToStoptimes.forEach { pair ->
                val stoptime = pair.first
                val platformCode = pair.second
                Column {
                    StoptimeRow(
                        stoptime = stoptime!!,
                        timeProvider = timeProvider,
                        platformCode = platformCode ?: ""
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun StopCard(
    stop: StopsByRadiusQuery.Node,
    timeProvider: () -> Instant
) {
    ElevatedCard {
        Column(Modifier.padding(10.dp)) {
            CardHeader(
                name = stop.stop?.name ?: "???",
                type = CardType.Stop,
                distance = stop.distance ?: 0
            )

            stop.stop!!.stoptimesWithoutPatterns!!.forEach { stoptime ->
                Column {
                    StoptimeRow(
                        stoptime = stoptime!!,
                        timeProvider = timeProvider,
                        platformCode = null
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun StoptimeRow(
    stoptime: StopsByRadiusQuery.StoptimesWithoutPattern,
    platformCode: String?,
    timeProvider: () -> Instant
) {
    val mediumText = MaterialTheme.typography.bodyMedium
    val smallText = MaterialTheme.typography.bodySmall
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        platformCode?.let {
            Text(
                text = it,
                style = mediumText,
                modifier = Modifier.width(30.dp)
            )
        }
        Text(
            text = stoptime.trip!!.routeShortName!!,
            modifier = Modifier.width(40.dp),
            style = mediumText
        )
        Text(
            text = "${stoptime.trip.tripHeadsign}",
            style = mediumText
        )
        Spacer(Modifier.weight(1f))

        val isRealTime = stoptime.realtime!!
        val realTime = (stoptime.realtimeDeparture!! + (stoptime.serviceDay as Int)).toLong()
        val realLocalTime = Instant
            .fromEpochSeconds(realTime).toLocalDateTime(TimeZone.currentSystemDefault()).time.toSecondOfDay().toLong()
        val realTimeStr = buildAnnotatedString {
            withStyle(smallText.toSpanStyle().copy(color = LocalContentColor.current.copy(alpha = 0.8f))) {
                if (isRealTime) withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append(FormatTime.timeToHHMMSS(realLocalTime))
                } else {
                    append(FormatTime.timeToHHMMSS(realLocalTime))
                }
            }
        }

        DeltaCountdown(realTime, timeProvider, smallText)
        Spacer(Modifier.width(5.dp))
        Text(realTimeStr, style = smallText)
    }
}

@Composable
private fun DeltaCountdown(realTime: Long, timeProvider: () -> Instant, textStyle: TextStyle) {
    val currentTime = timeProvider().epochSeconds
    val deltaRaw = realTime - currentTime
    val formatter: (Long) -> String =
        if (abs(deltaRaw) < 60 * 60) FormatTime::timeToMMSS
        else FormatTime::timeToHHMMSS

    val delta = when {
        abs(deltaRaw) >= 60 * 60 -> ""
        deltaRaw < 0 -> "-${formatter(-deltaRaw)}"
        deltaRaw >= 0 -> "+${formatter(deltaRaw)}"
        else -> FormatTime.timeToMMSS(deltaRaw)
    }
    Text("$delta ", style = textStyle)
}

@PreviewLightDark
@Composable
private fun PreviewStopCard() = HSLNytTheme {
    StopCard(
        StopsByRadiusQuery.Node(
            distance = 100,
            stop = StopsByRadiusQuery.Stop(
                code = "XX001122",
                name = "Stop",
                vehicleMode = Mode.BUS,
                stoptimesWithoutPatterns = StopsRepository.Preview.stops.value[0].stop?.stoptimesWithoutPatterns,
                platformCode = null,
                parentStation = StopsByRadiusQuery.ParentStation("ADF")
            )
        ),
        timeProvider = { Clock.System.now() }
    )
}

@PreviewLightDark
@Composable
private fun PreviewStationCard() = HSLNytTheme {
    StationCard(
        stationStop = StationStop(
            station = StationsByIdQuery.Station(
                "Station",
                "gtfsId"
            ),
            stops = StopsRepository.Preview.stops.value
        ),
        timeProvider = { Clock.System.now() }
    )
}

@PreviewLightDark
@Composable
private fun PreviewLongHeader() = HSLNytTheme {
    StationCard(
        stationStop = StationStop(
            station = StationsByIdQuery.Station(
                "Long Station Name Very Long",
                "gtfsId"
            ),
            stops = StopsRepository.Preview.stops.value
        ),
        timeProvider = { Clock.System.now() }
    )
}