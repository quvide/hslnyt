package foo.vide.hslnyt.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import foo.vide.hslnyt.StopsByRadiusQuery
import foo.vide.hslnyt.repo.StopsRepository
import foo.vide.hslnyt.ui.theme.HSLNytTheme
import foo.vide.hslnyt.util.FormatTime
import foo.vide.hslnyt.util.PreviewLightDark
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs

@Composable
fun StopCard(
    stop: StopsByRadiusQuery.Node,
    timeProvider: () -> Instant
) {
    ElevatedCard {
        Column(Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stop.stop?.name ?: "???",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stop.stop?.code ?: "???",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${stop.distance.toString()}m",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Spacer(Modifier.height(10.dp))
            stop.stop!!.stoptimesWithoutPatterns!!.forEachIndexed { idx, it ->
                Column {
                    StoptimeRow(it!!, timeProvider = timeProvider)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun StoptimeRow(
    stoptime: StopsByRadiusQuery.StoptimesWithoutPattern,
    timeProvider: () -> Instant
) {
    val mediumText = MaterialTheme.typography.bodyMedium
    val smallText = MaterialTheme.typography.bodySmall
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Text(stoptime.trip!!.routeShortName!!, modifier = Modifier.width(40.dp), style = mediumText)
        Text(" ${stoptime.trip.tripHeadsign}", style = mediumText)
        Spacer(Modifier.weight(1f))

        val isRealTime = stoptime.realtime!!
        val realTime = (stoptime.realtimeDeparture!! + (stoptime.serviceDay as Int)).toLong()
        val realLocalTime = Instant.fromEpochSeconds(realTime).toLocalDateTime(TimeZone.currentSystemDefault()).time.toSecondOfDay().toLong()
        val realTimeStr = buildAnnotatedString {
            withStyle(smallText.toSpanStyle().copy(color = LocalContentColor.current.copy(alpha = 0.8f))) {
                if (isRealTime) withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append(FormatTime.timeToHHMMSS(realLocalTime))
                } else {
                    append(FormatTime.timeToHHMMSS(realLocalTime))
                }
            }
        }

        val currentTime = timeProvider().epochSeconds
        val deltaRaw = realTime - currentTime
        val formatter: (Long) -> String =
            if (abs(deltaRaw) < 60 * 60) FormatTime::timeToMMSS else FormatTime::timeToHHMMSS
        val delta = when {
            abs(deltaRaw) >= 60 * 60 -> ""
            deltaRaw < 0 -> "-${formatter(-deltaRaw)}"
            deltaRaw >= 0 -> "+${formatter(deltaRaw)}"
            else -> FormatTime.timeToMMSS(deltaRaw)
        }
        Text(AnnotatedString("$delta  ") + realTimeStr, style = smallText)
    }
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
                stoptimesWithoutPatterns = StopsRepository.Preview.stops.value[0].stop?.stoptimesWithoutPatterns
            )
        ),
        timeProvider = { Clock.System.now() }
    )
}