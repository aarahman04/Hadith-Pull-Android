package online.hadithpull.app.ui.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.hadithpull.app.R
import online.hadithpull.app.ui.components.HadithBackTopBar
import online.hadithpull.app.ui.theme.LocalHadithColors

/** D21: an in-app privacy policy screen, reusing LicensesScreen's readRawText pattern. */
@Composable
fun PrivacyRoute(onBack: () -> Unit) {
    val colors = LocalHadithColors.current
    val context = LocalContext.current
    val text = remember { readRawText(context, R.raw.privacy_policy) }

    Column(Modifier.fillMaxSize()) {
        HadithBackTopBar(title = "Privacy policy", onBack = onBack)
        Text(
            text = text,
            color = colors.textSoft,
            fontSize = 12.8.sp,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        )
    }
}
