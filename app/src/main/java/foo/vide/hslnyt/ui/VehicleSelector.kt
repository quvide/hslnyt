package foo.vide.hslnyt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import foo.vide.hslnyt.type.Mode
import foo.vide.hslnyt.ui.theme.HSLNytTheme
import foo.vide.hslnyt.util.PreviewLightDark

data class Vehicle(val name: String, val color: Color, val mode: Mode) {
    companion object {
        val Train = Vehicle("Train", Color(131, 75, 149), mode = Mode.RAIL)
        val Bus = Vehicle("Bus", Color(52, 120, 195), mode = Mode.BUS)
        val Tram = Vehicle("Tram", Color(67, 150, 100), mode = Mode.TRAM)
        val Metro = Vehicle("Metro", Color(236, 110, 53), mode = Mode.SUBWAY)
    }
}

private val modes = listOf(
    Vehicle.Train,
    Vehicle.Bus,
    Vehicle.Tram,
    Vehicle.Metro
)

class VehicleSelectorState {
    val selected = mutableStateOf<Vehicle?>(null)
    fun select(vehicle: Vehicle) {
        when (selected.value) {
            vehicle -> selected.value = null
            else -> selected.value = vehicle
        }
    }
}

@Composable
fun rememberVehicleSelectorState() = remember { VehicleSelectorState() }

@Composable
fun VehicleSelector(state: VehicleSelectorState) = Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
) {
    modes.forEach { vehicle ->
        val borderModifier = when (vehicle) {
            state.selected.value -> Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12))
            else -> Modifier
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .weight(1f)
                .background(
                    color = vehicle.color,
                    shape = RoundedCornerShape(percent = 12)
                )
                .clickable { state.select(vehicle) }
                .then(borderModifier),
            contentAlignment = Alignment.Center
        ) {
            if (vehicle != state.selected.value) {
                Text(text = vehicle.name, color = Color.White)
            } else {
                Text(text = "✓", color = Color.White)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() = HSLNytTheme {
    val state = remember {
        VehicleSelectorState().apply {
            selected.value = Vehicle.Bus
        }
    }

    VehicleSelector(state)
}