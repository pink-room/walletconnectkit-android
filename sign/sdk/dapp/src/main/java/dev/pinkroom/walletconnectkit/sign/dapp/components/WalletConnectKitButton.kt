package dev.pinkroom.walletconnectkit.sign.dapp.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.exception.NoRelayConnectionException
import dev.pinkroom.walletconnectkit.core.chains.Chain
import dev.pinkroom.walletconnectkit.core.chains.Ethereum
import dev.pinkroom.walletconnectkit.sign.dapp.R
import dev.pinkroom.walletconnectkit.sign.dapp.WalletConnectKit
import kotlinx.coroutines.delay

private const val MAX_RETRIES = 9 // The max is actually 3, since the onError from the connect function is called 3 times
private const val RETRY_DELAY = 1000L // 1 second

// TODO: Refactor code! We know this is terrible right now :P
@Composable
fun WalletConnectKitButton(
    walletConnectKit: WalletConnectKit,
    modifier: Modifier = Modifier,
    chains: List<Chain> = listOf(Ethereum),
    optionalChains: List<Chain> = emptyList(),
    showAvailablePairs: Boolean = true,
    contentModifier: Modifier = Modifier.defaultContentModifier(),
    colors: ButtonColors = defaultButtonColors(),
    shape: Shape = defaultButtonShape(),
    border: BorderStroke = defaultBorderStroke(),
    content: @Composable RowScope.() -> Unit = { WalletConnectImage(contentModifier) },
    onSuccess: () -> Unit = {},
    onError: (Throwable) -> Unit = {},
) {
    var showPairings by remember { mutableStateOf(false) }
    var connecting by remember { mutableStateOf(false) }
    var retryCount by remember { mutableStateOf(0) }
    LaunchedEffect(retryCount) {
        if (retryCount in 1..MAX_RETRIES) {
            connecting = true
            if (retryCount > 1) delay(RETRY_DELAY)
            walletConnectKit.connect(
                chains = chains,
                optionalChains = optionalChains,
                onSuccess = {
                    connecting = false
                    retryCount = 0
                    onSuccess()
                },
                onError = {
                    if (it is NoRelayConnectionException && retryCount < MAX_RETRIES) {
                        retryCount += 1
                    } else {
                        retryCount = 0
                        onError(it)
                    }
                },
            )
        } else {
            connecting = false
        }
    }

    if (showPairings) {
        PairsDialog(
            walletConnectKit = walletConnectKit,
            onDismissClick = { showPairings = false },
            onNewSessionClick = {
                showPairings = false
                retryCount = 1
            },
            chains = chains,
            optionalChains = optionalChains,
            onSuccess = onSuccess,
            onError = onError,
        )
    } else {
        Button(
            colors = colors,
            enabled = !connecting,
            onClick = {
                if (showAvailablePairs && walletConnectKit.pairings.isNotEmpty()) {
                    showPairings = true
                } else {
                    retryCount = 1
                }
            },
            modifier = modifier,
            shape = shape,
            border = border,
            content = {
                if (connecting) LoadingView()
                else content()
            },
        )
    }
}

@Composable
private fun defaultButtonColors() =
    ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.background,
        disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.04f)
            .compositeOver(MaterialTheme.colors.surface),
    )

@Composable
private fun defaultButtonShape() = RoundedCornerShape(32.dp)

@Composable
private fun defaultBorderStroke() = BorderStroke(1.5.dp, WalletConnectColor)

@Composable
private fun WalletConnectImage(modifier: Modifier) = Image(
    modifier = modifier,
    painter = painterResource(R.drawable.ic_walletconnect),
    contentScale = ContentScale.Fit,
    contentDescription = "Wallet Connect",
)

@Composable
private fun LoadingView() = Column(
    modifier = Modifier.defaultContentModifier(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        color = WalletConnectColor,
        strokeWidth = 2.dp,
    )
}

@Composable
private fun PairsDialog(
    walletConnectKit: WalletConnectKit,
    onDismissClick: () -> Unit,
    onNewSessionClick: () -> Unit,
    onSuccess: () -> Unit,
    onError: (Throwable) -> Unit,
    chains: List<Chain>,
    optionalChains: List<Chain>,
) {
    var selectedPairing by remember {
        mutableStateOf(
            Core.Model.Pairing(
                "", 0, null, "", null, "", false, ""
            )
        )
    }
    var connecting by remember { mutableStateOf(false) }
    var retryCount by remember { mutableStateOf(0) }
    LaunchedEffect(retryCount) {
        if (retryCount in 1..MAX_RETRIES) {
            connecting = true
            if (retryCount > 1) delay(RETRY_DELAY)
            walletConnectKit.connectExistingPair(
                chains = chains,
                optionalChains = optionalChains,
                pairing = selectedPairing,
                onSuccess = {
                    connecting = false
                    retryCount = 0
                    onSuccess()
                },
                onError = {
                    if (it is NoRelayConnectionException && retryCount < MAX_RETRIES) {
                        retryCount += 1
                    } else {
                        retryCount = 0
                        onError(it)
                    }
                },
            )
        }
    }
    Dialog(
        onDismissRequest = onDismissClick,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(vertical = 24.dp)
                .fillMaxSize(),
        ) {
            Card(
                shape = MaterialTheme.shapes.medium,
                elevation = 4.dp,
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.padding(start = 12.dp),
                            text = "Previous pairs",
                            style = Typography().h6,
                            color = Color.Black,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = onDismissClick) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, false),
                    ) {
                        items(walletConnectKit.pairings) { pairing ->
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPairing = pairing
                                        retryCount = 1
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth(0.15f)
                                        .padding(end = 10.dp),
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(pairing.peerAppMetaData?.icons?.firstOrNull())
                                        .build(),
                                    contentDescription = "Wallet icon",
                                    contentScale = ContentScale.Fit,
                                )
                                Text(text = "${pairing.peerAppMetaData?.name}")
                            }
                            Spacer(Modifier.padding(bottom = 4.dp))
                        }
                    }
                    Button(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = WalletConnectColor),
                        onClick = onNewSessionClick,
                        shape = defaultButtonShape(),
                        border = defaultBorderStroke(),
                        content = { Text(text = "New Connection", color = Color.White) },
                    )
                }
            }
        }
    }
}

private fun Modifier.defaultContentModifier() = this
    .size(168.dp, 28.dp)
    .padding(4.dp)

private val WalletConnectColor = Color(0xFF3B99FC)