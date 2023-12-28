package foo.vide.hslnyt.repo

import android.location.Location
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import foo.vide.hslnyt.BuildConfig
import foo.vide.hslnyt.StationsByIdQuery
import foo.vide.hslnyt.StopsByRadiusQuery
import foo.vide.hslnyt.type.Mode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

private val apolloClient = ApolloClient.Builder()
    .serverUrl("https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql")
    .addHttpHeader("digitransit-subscription-key", BuildConfig.DIGITRANSIT_PRIMARY)
    .build()

interface StopsRepository {
    val stops: State<List<StopsByRadiusQuery.Node>>
    val stationStops: State<List<StationStop>>
    val refreshing: State<Boolean>
    suspend fun getStops(location: Location)

    companion object {
        val Preview = object : StopsRepository {
            override val refreshing = mutableStateOf(false)
            override suspend fun getStops(location: Location) {}

            override val stationStops = mutableStateOf(emptyList<StationStop>())
            override val stops = mutableStateOf(
                listOf(
                    StopsByRadiusQuery.Node(
                        distance = 100,
                        stop = StopsByRadiusQuery.Stop(
                            code = "XXX0000",
                            name = "Name",
                            vehicleMode = Mode.BUS,
                            platformCode = "11",
                            parentStation = StopsByRadiusQuery.ParentStation("ASDF"),
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
                                        tripHeadsign = "Elielinaukio",
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

data class StationStop(
    val station: StationsByIdQuery.Station,
    val stops: List<StopsByRadiusQuery.Node>
)

class StopsRepositoryImpl(private val settingsRepository: SettingsRepository) : StopsRepository {
    companion object {
        private const val TAG = "StopsRepositoryImpl"
        private const val MOCK_LOCATION = false
    }

    override val stationStops = mutableStateOf(emptyList<StationStop>())
    override val stops = mutableStateOf<List<StopsByRadiusQuery.Node>>(emptyList())
    override val refreshing = mutableStateOf(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val ioSingleParallel = Dispatchers.IO.limitedParallelism(1)

    override suspend fun getStops(location: Location) {
        withContext(ioSingleParallel) {
            if (refreshing.value) {
                return@withContext
            }

            refreshing.value = true
            try {
                val rawStops = apolloClient
                    .query(
                        StopsByRadiusQuery(
                            lat = if (MOCK_LOCATION) 60.1699 else location.latitude,
                            lon = if (MOCK_LOCATION) 24.9384 else location.longitude,
                            radius = settingsRepository.maxRadiusMeters,
                            first = settingsRepository.maxStops
                        )
                    )
                    .execute()
                    .dataAssertNoErrors
                    .stopsByRadius!!
                    .edges!!
                    .map { it!!.node!! }

                val groupedStops = rawStops.groupBy { it.stop?.parentStation }

                val stationIds = groupedStops.keys.filterNotNull().map { it.gtfsId }
                val rawStations = apolloClient
                    .query(StationsByIdQuery(ids = Optional.present(stationIds)))
                    .execute()
                    .dataAssertNoErrors
                    .stations!!

                stops.value = groupedStops[null] ?: emptyList()
                stationStops.value = groupedStops
                    .filterKeys { it != null }
                    .map { (key, value) -> StationStop(rawStations.find { it!!.gtfsId == key!!.gtfsId }!!, value) }

            } catch (e: Exception) {
                Log.e(TAG, "Exception executing StopsByRadiusQuery", e)
            }
            refreshing.value = false
        }
    }
}