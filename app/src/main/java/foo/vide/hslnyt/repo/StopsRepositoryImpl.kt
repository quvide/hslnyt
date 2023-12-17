package foo.vide.hslnyt.repo

import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.apollographql.apollo3.ApolloClient
import foo.vide.hslnyt.BuildConfig
import foo.vide.hslnyt.StopsByRadiusQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

private val apolloClient = ApolloClient.Builder()
    .serverUrl("https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql")
    .addHttpHeader("digitransit-subscription-key", BuildConfig.DIGITRANSIT_PRIMARY)
    .build()

interface StopsRepository {
    val stops: State<List<StopsByRadiusQuery.Node>>
    val refreshing: State<Boolean>
    suspend fun getStops(location: Location)

    companion object {
        val Preview = object : StopsRepository {
            override val refreshing = mutableStateOf(false)
            override suspend fun getStops(location: Location) {}

            override val stops = mutableStateOf(
                listOf(
                    StopsByRadiusQuery.Node(
                        distance = 100,
                        stop = StopsByRadiusQuery.Stop(
                            code = "XXX0000",
                            name = "Name",
                            stoptimesWithoutPatterns = listOf(
                                StopsByRadiusQuery.StoptimesWithoutPattern(
                                    scheduledArrival = 0,
                                    realtime = true,
                                    realtimeArrival = 0,
                                    arrivalDelay = 0,
                                    scheduledDeparture = 0,
                                    realtimeDeparture = (Clock.System.now().epochSeconds + 100).toInt(),
                                    departureDelay = 0,
                                    trip = StopsByRadiusQuery.Trip(
                                        tripHeadsign = "Helsinki",
                                        routeShortName = "000"
                                    ),
                                    serviceDay = 0
                                ),
                                StopsByRadiusQuery.StoptimesWithoutPattern(
                                    scheduledArrival = 0,
                                    realtime = true,
                                    realtimeArrival = 0,
                                    arrivalDelay = 0,
                                    scheduledDeparture = 0,
                                    realtimeDeparture = (Clock.System.now().epochSeconds + 100).toInt(),
                                    departureDelay = 0,
                                    trip = StopsByRadiusQuery.Trip(
                                        tripHeadsign = "Helsinki",
                                        routeShortName = "000"
                                    ),
                                    serviceDay = 0
                                )
                            )
                        )
                    )
                )
            )
        }
    }
}

object StopsRepositoryImpl : StopsRepository {
    override val stops = mutableStateOf<List<StopsByRadiusQuery.Node>>(emptyList())
    override val refreshing = mutableStateOf(false)

    override suspend fun getStops(location: Location) {
        withContext(Dispatchers.IO) {
            refreshing.value = true
            stops.value = apolloClient
                .query(
                    StopsByRadiusQuery(
                        lat = location.latitude,
                        lon = location.longitude,
                        radius = 1000,
                        first = 10
                    )
                )
                .execute()
                .dataAssertNoErrors
                .stopsByRadius!!
                .edges!!
                .map { it!!.node!! }
            refreshing.value = false
        }
    }
}