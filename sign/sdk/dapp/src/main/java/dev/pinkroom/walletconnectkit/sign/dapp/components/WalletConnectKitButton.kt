package dev.pinkroom.walletconnectkit.sign.dapp.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.exception.NoRelayConnectionException
import com.walletconnect.android.internal.common.scope
import dev.pinkroom.walletconnectkit.core.chains.Chain
import dev.pinkroom.walletconnectkit.core.chains.Ethereum
import dev.pinkroom.walletconnectkit.sign.dapp.R
import dev.pinkroom.walletconnectkit.sign.dapp.WalletConnectKit
import dev.pinkroom.walletconnectkit.sign.dapp.data.model.Wallet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MAX_RETRIES =
    9 // The max is actually 3, since the onError from the connect function is called 3 times
private const val RETRY_DELAY = 1000L // 1 second

// TODO: Refactor code! We know this is terrible right now :P
@OptIn(ExperimentalMaterial3Api::class)
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
    var retryCount by remember { mutableIntStateOf(0) }
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
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
        if (openBottomSheet) {
            BottomSheet(walletConnectKit) {
                openBottomSheet = false
            }
        }
        Button(
            colors = colors,
            enabled = !connecting,
            onClick = {
                openBottomSheet = true
                scope.launch {
                    // scope.launch { sheetState.expand() }
                    //val cenas = walletConnectKit.getInstalledWallets(chains.map { it.id })
                }
                /* if (showAvailablePairs && walletConnectKit.pairings.isNotEmpty()) {
                    showPairings = true
                } else {
                    retryCount = 1
                }*/
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheet(walletConnectKit: WalletConnectKit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var wallets by remember { mutableStateOf(emptyList<Wallet>()) }
    LaunchedEffect(sheetState) {
        scope.launch {
            wallets = walletConnectKit.getInstalledWallets(listOf(Ethereum.id))
        }
    }
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.padding(bottom = 24.dp),
                text = "Select your Wallet",
                style = MaterialTheme.typography.h5,
            )
            if (wallets.isNotEmpty()) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxWidth(),
                    columns = GridCells.Fixed(4),
                ) {
                    items(wallets.take(7)) { wallet ->
                        WalletListItem(wallet) {
                            Log.e("CENAS", wallet.name)
                        }
                    }
                    if (wallets.size > 7) {
                        // Show See All

                    } else {
                        item {
                            // OtherItem { Log.e("CENAS", "Other") }
                            SeeAllItem { Log.e("CENAS", "See all") }
                        }
                    }
                }
            }
        }
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

@Composable
private fun WalletListItem(
    wallet: Wallet,
    onWalletItemClick: (Wallet) -> Unit,
) {
    Column(
        modifier = Modifier.clickable { onWalletItemClick(wallet) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WalletImage(
            url = wallet.imageUrl,
            modifier = Modifier
                .size(80.dp)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Text(
            text = wallet.name,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OtherItem(onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF3C464F))
                .wrapContentSize(Alignment.Center),
        ) {
            Text(
                modifier = Modifier.offset(y = (-12).dp),
                text = "...",
                color = Color.White,
                style = MaterialTheme.typography.h3,
            )
        }
        Text(
            text = "Other",
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SeeAllItem(onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF3C464F))
                .wrapContentSize(Alignment.Center),
        ) {
            Image(
                painterResource(R.drawable.ic_short_walletconnect),
                modifier = Modifier.size(40.dp),
                contentDescription = "",
            )
        }
        Text(
            text = "See all",
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WalletImage(url: String, modifier: Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier,
    )
}