package dev.pinkroom.walletconnectkit.api

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.pinkroom.walletconnectkit.R
import dev.pinkroom.walletconnectkit.WalletConnectKit
import dev.pinkroom.walletconnectkit.api.theme.WalletConnectIconBlue
import org.walletconnect.Session

@Composable
fun WalletConnectButton(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier.defaultContentModifier(),
    walletConnectKit: WalletConnectKit,
    onConnected: (address: String) -> Unit,
    onDisconnected: (() -> Unit)? = null,
    sessionCallback: Session.Callback? = null,
    colors: ButtonColors = defaultButtonColors(),
    shape: Shape = defaultButtonShape(),
    border: BorderStroke = defaultBorderStroke(),
    content: @Composable RowScope.() -> Unit = { WalletConnectImage(contentModifier) },
) {
    val viewModel: WCKViewModel =
        createViewModel(walletConnectKit, onConnected, onDisconnected, sessionCallback)
    viewModel.loadSessionIfStored()
    WCKButton(colors, viewModel::onClick, modifier, shape, border, content)
}

@Composable
private fun createViewModel(
    walletConnectKit: WalletConnectKit,
    onConnected: (address: String) -> Unit,
    onDisconnected: (() -> Unit)?,
    sessionCallback: Session.Callback?
): WCKViewModel = viewModel(
    LocalViewModelStoreOwner.current!!,
    null,
    createViewModelFactory(walletConnectKit, onConnected, onDisconnected, sessionCallback)
)

@Composable
private fun createViewModelFactory(
    walletConnectKit: WalletConnectKit,
    onConnected: (address: String) -> Unit,
    onDisconnected: (() -> Unit)?,
    sessionCallback: Session.Callback?
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WCKViewModel(walletConnectKit, onConnected, onDisconnected, sessionCallback) as T
    }
}

@Composable
private fun WCKButton(
    colors: ButtonColors,
    onClick: () -> Unit,
    modifier: Modifier,
    shape: Shape,
    border: BorderStroke,
    content: @Composable (RowScope.() -> Unit)
) = Button(
    colors = colors,
    onClick = onClick,
    modifier = modifier,
    shape = shape,
    border = border,
    content = content
)

private fun Modifier.defaultContentModifier() = this
    .size(232.dp, 48.dp)
    .padding(12.dp)

@Composable
private fun defaultButtonColors() =
    ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background)

@Composable
private fun defaultButtonShape() = RoundedCornerShape(32.dp)

@Composable
private fun defaultBorderStroke() =
    BorderStroke(1.5.dp, WalletConnectIconBlue)

@Composable
private fun WalletConnectImage(modifier: Modifier) {
    Image(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_walletconnect),
        contentDescription = ""
    )
}

@Preview
@Composable
private fun ComposablePreview() {
    WCKButton(
        modifier = Modifier.fillMaxWidth(),
        colors = defaultButtonColors(),
        shape = defaultButtonShape(),
        border = defaultBorderStroke(),
        content = { WalletConnectImage(Modifier.defaultContentModifier()) },
        onClick = {}
    )
}