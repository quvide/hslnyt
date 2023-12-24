package foo.vide.hslnyt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.android.gms.location.LocationServices
import foo.vide.hslnyt.repo.LocationRepository
import foo.vide.hslnyt.repo.LocationRepositoryImpl
import foo.vide.hslnyt.repo.StopsRepositoryImpl
import foo.vide.hslnyt.ui.HSLNyt
import foo.vide.hslnyt.ui.theme.HSLNytTheme

class MainActivity : ComponentActivity() {
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            locationRepository.locationPermissionCallback(it)
        }

    private lateinit var locationRepository: LocationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationRepository = LocationRepositoryImpl(
            this,
            LocationServices.getFusedLocationProviderClient(this),
            locationPermissionRequest
        )

        setContent {
            HSLNytTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HSLNyt(
                        stopsRepository = StopsRepositoryImpl,
                        locationRepository = locationRepository,
                    )
                }
            }
        }
    }
}
