package dev.pinkroom.walletconnectkit.sign.dapp.sample.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)

@Composable
fun WalletConnectKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
    SystemUiTheme(darkTheme)
}


@Composable
private fun SystemUiTheme(darkTheme: Boolean) {
    val systemUiController = rememberSystemUiController()
    val color = if (darkTheme) MaterialTheme.colorScheme.onBackground
    else MaterialTheme.colorScheme.background
    systemUiController.setSystemBarsColor(
        color = color,
        darkIcons = !darkTheme,
    )
    systemUiController.setNavigationBarColor(
        color = color,
        darkIcons = !darkTheme,
    )
}