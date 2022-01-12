package dev.pinkroom.walletconnectkit.api

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.pinkroom.walletconnectkit.R
import dev.pinkroom.walletconnectkit.WalletConnectKit
import dev.pinkroom.walletconnectkit.WalletConnectKitConfig
import org.walletconnect.Session

@Composable
fun WalletConnectButton(
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier
        .size(232.dp, 48.dp)
        .padding(12.dp),
    walletConnectKit: WalletConnectKit,
    onConnected: (address: String) -> Unit,
    onDisconnected: (() -> Unit)? = null,
    sessionCallback: Session.Callback? = null
) {
    val viewModel = WCKViewModel(walletConnectKit, onConnected, onDisconnected, sessionCallback)
    viewModel.loadSessionIfStored()
    Button( // TODO fix background colors (no dark mode?)
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.design_default_color_background)),
        onClick = viewModel::onClick,
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.5.dp, colorResource(id = R.color.walletconnect_icon))
    ) {
        Image(
            modifier = imageModifier,
            painter = painterResource(id = R.drawable.ic_walletconnect),
            contentDescription = ""
        )
    }
}

@Preview
@Composable
private fun ComposablePreview() {
    val config = WalletConnectKitConfig(
        context = LocalContext.current,
        bridgeUrl = "wss://bridge.aktionariat.com:8887",
        appUrl = "walletconnectkit.com",
        appName = "WalletConnect Kit",
        appDescription = "This is the Swiss Army toolkit for WalletConnect!"
    )
    val walletConnectKit = WalletConnectKit.Builder(config).build()
    WalletConnectButton(
        modifier = Modifier.fillMaxWidth(),
        walletConnectKit = walletConnectKit,
        onConnected = {},
    )
}