package foo.vide.hslnyt.repo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.CancellationTokenSource

interface LocationRepository {
    val location: State<Location?>
    val locationStatus: State<LocationStatus>
    fun requestLocationUpdate()
    fun locationPermissionCallback(permissions: Map<String, Boolean>)

    enum class LocationStatus {
        INITIAL,
        LOADING,
        COMPLETE,
        DENIED,
    }

    companion object {
        val Preview = object : LocationRepository {
            override val location = mutableStateOf(null)
            override val locationStatus = mutableStateOf(LocationStatus.COMPLETE)
            override fun requestLocationUpdate() {}
            override fun locationPermissionCallback(permissions: Map<String, Boolean>) {}
        }
    }
}

class LocationRepositoryImpl(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val locationRequest: ActivityResultLauncher<Array<String>>
) : LocationRepository {

    override val location = mutableStateOf<Location?>(null)
    override val locationStatus = mutableStateOf(LocationRepository.LocationStatus.INITIAL)

    private fun hasLocationPermission(): Boolean {
        val fine =
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse =
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    init {
        if (hasLocationPermission()) {
            requestLocationUpdate()
        }
    }

    @SuppressLint("MissingPermission") // checked in function, linter isn't smart enough
    override fun requestLocationUpdate() {
        locationStatus.value = LocationRepository.LocationStatus.LOADING
        if (hasLocationPermission()) {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener {
                location.value = it
                locationStatus.value = LocationRepository.LocationStatus.COMPLETE
            }
        } else {
            locationRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    override fun locationPermissionCallback(permissions: Map<String, Boolean>) {
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                requestLocationUpdate()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                requestLocationUpdate()
            }

            else -> {
                locationStatus.value = LocationRepository.LocationStatus.DENIED
            }
        }
    }
}