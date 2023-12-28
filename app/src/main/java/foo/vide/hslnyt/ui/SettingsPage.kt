package foo.vide.hslnyt.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import foo.vide.hslnyt.repo.SettingsRepository
import foo.vide.hslnyt.ui.theme.HSLNytTheme
import foo.vide.hslnyt.util.PreviewLightDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(settingsRepository: SettingsRepository) = Scaffold(
    topBar = {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Close settings")
                }
            }
        )
        Divider()
    }
) { paddingValues ->
    Surface(
        Modifier
            .padding(paddingValues)
            .padding(horizontal = 10.dp)
            .consumeWindowInsets(paddingValues)
    ) {
        Column {
            ListItem(
                headlineContent = { Text("Maximum distance") },
                supportingContent = { Text("${settingsRepository.maxRadiusMeters} m") }
            )
            Divider()
            ListItem(
                headlineContent = { Text("Maximum displayed stops") },
                supportingContent = { Text("${settingsRepository.maxStops}") }
            )
            Divider()
            ListItem(
                headlineContent = { Text("Automatic location update frequency") },
                supportingContent = { Text("${settingsRepository.locationUpdateFrequencySeconds} s") }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() = HSLNytTheme {
    SettingsPage(settingsRepository = SettingsRepository.Preview)
}