package foo.vide.hslnyt.util

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, group = "Light")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, group = "Dark")
annotation class PreviewLightDark