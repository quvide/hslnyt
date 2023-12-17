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

const val TAG = "HSLNyt"

class MainActivity : ComponentActivity() {
    // This is just stupid
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
                // A surface container using the 'background' color from the theme
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
