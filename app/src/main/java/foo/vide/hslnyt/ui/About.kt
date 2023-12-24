package foo.vide.hslnyt.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import foo.vide.hslnyt.BuildConfig
import foo.vide.hslnyt.R
import foo.vide.hslnyt.ui.theme.HSLNytTheme
import foo.vide.hslnyt.util.PreviewLightDark

@Composable
fun About() = Card {
    Box(
        Modifier
            .padding(20.dp)
            .widthIn(min = 300.dp)) {
        Text(
            style = MaterialTheme.typography.bodyLarge,
            text = buildAnnotatedString {
                withStyle(MaterialTheme.typography.titleLarge.toSpanStyle()) {
                    append(stringResource(id = R.string.app_name))
                    append(" ")
                }
                append(BuildConfig.VERSION_NAME + "\n\n")
                append("Developed by ")
                pushStringAnnotation("", "https://vide.foo")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("https://vide.foo")
                }
                pop()
                append(".")
            }
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview() = HSLNytTheme {
    About()
}