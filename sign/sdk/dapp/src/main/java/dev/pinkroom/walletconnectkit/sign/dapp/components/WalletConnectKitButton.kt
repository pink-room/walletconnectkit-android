package dev.pinkroom.walletconnectkit.sign.dapp.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.exception.NoRelayConnectionException
import com.walletconnect.android.internal.common.scope
import dev.pinkroom.walletconnectkit.core.chains.Chain
import dev.pinkroom.walletconnectkit.core.chains.Ethereum
import dev.pinkroom.walletconnectkit.sign.dapp.R
import dev.pinkroom.walletconnectkit.sign.dapp.WalletConnectKit
import dev.pinkroom.walletconnectkit.sign.dapp.data.model.PairingMetadata
import dev.pinkroom.walletconnectkit.sign.dapp.data.model.Wallet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MAX_RETRIES = 9 // The max is actually 3, since the onError from the connect function is called 3 times
private const val RETRY_DELAY = 1000L // 1 second
private const val CONNECTING_TIMEOUT = 10000L // 10 seconds

// TODO: We know this is terrible right now, it needs to be refactored ASAP! Look at this code at your own risk :P
@Composable
fun WalletConnectKitButton(
    walletConnectKit: WalletConnectKit,
    modifier: Modifier = Modifier,
    chains: List<Chain> = listOf(Ethereum),
    optionalChains: List<Chain> = emptyList(),
    showAvailablePairings: Boolean = true,
    contentModifier: Modifier = Modifier.defaultContentModifier(),
    colors: ButtonColors = defaultButtonColors(),
    shape: Shape = defaultButtonShape(),
    border: BorderStroke = defaultBorderStroke(),
    content: @Composable RowScope.() -> Unit = { WalletConnectImage(contentModifier) },
    onSuccess: () -> Unit = {},
    onError: (Throwable) -> Unit = {},
) {
    val activeSessions by walletConnectKit.activeSessions.collectAsStateWithLifecycle(initialValue = emptyList())
    var connecting by remember { mutableStateOf(false) }
    var connectionRetryCount by remember { mutableIntStateOf(0) }
    var pairingRetryCount by remember { mutableIntStateOf(0) }
    var selectedWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedPairing by remember { mutableStateOf<Core.Model.Pairing?>(null) }
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }

    // Timeout of 10 seconds if we don't receive any connection response
    OnLifecycleEvent(Lifecycle.Event.ON_RESUME) {
        if (connecting) {
            scope.launch {
                delay(CONNECTING_TIMEOUT)
                connecting = false
                connectionRetryCount = 0
                pairingRetryCount = 0
            }
        }
    }

    // If we have an active session, it means the connection process is done!
    if (activeSessions.isNotEmpty()) {
        connecting = false
        connectionRetryCount = 0
        pairingRetryCount = 0
        onSuccess()
    }

    // Handle new connection with retries
    LaunchedEffect(connectionRetryCount) {
        if (connectionRetryCount in 1..MAX_RETRIES) {
            connecting = true
            if (connectionRetryCount > 1) delay(RETRY_DELAY)
            walletConnectKit.connect(
                chains = chains,
                optionalChains = optionalChains,
                wallet = selectedWallet,
                onError = {
                    if (it is NoRelayConnectionException && connectionRetryCount < MAX_RETRIES) {
                        connectionRetryCount += 1
                    } else {
                        connectionRetryCount = 0
                        onError(it)
                    }
                },
            )
        } else {
            connecting = false
        }
    }

    // Handle pairing connection with retries
    LaunchedEffect(pairingRetryCount) {
        if (pairingRetryCount in 1..MAX_RETRIES) {
            connecting = true
            if (pairingRetryCount > 1) delay(RETRY_DELAY)
            walletConnectKit.connectExistingPairing(
                chains = chains,
                optionalChains = optionalChains,
                pairing = selectedPairing!!,
                onError = {
                    if (it is NoRelayConnectionException && pairingRetryCount < MAX_RETRIES) {
                        pairingRetryCount += 1
                    } else {
                        pairingRetryCount = 0
                        onError(it)
                    }
                },
            )
        } else {
            connecting = false
        }
    }

    if (openBottomSheet) {
        WalletConnectKitBottomSheet(
            walletConnectKit = walletConnectKit,
            chains = chains,
            showAvailablePairings = showAvailablePairings,
            onPairingClick = {
                selectedPairing = it
                pairingRetryCount = 1
                openBottomSheet = false
            },
            onWalletClick = {
                selectedWallet = it
                connectionRetryCount = 1
                openBottomSheet = false
            },
            onDismiss = { openBottomSheet = false },
        )
    }
    Button(
        colors = colors,
        enabled = !connecting,
        onClick = { openBottomSheet = true },
        modifier = modifier,
        shape = shape,
        border = border,
        content = {
            if (connecting) LoadingView()
            else content()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletConnectKitBottomSheet(
    walletConnectKit: WalletConnectKit,
    chains: List<Chain>,
    showAvailablePairings: Boolean,
    onPairingClick: (Core.Model.Pairing) -> Unit,
    onWalletClick: (Wallet?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var showPairings by remember { mutableStateOf(showAvailablePairings && walletConnectKit.pairings.isNotEmpty()) }
    val scope = rememberCoroutineScope()
    var installedWallets by rememberSaveable(chains) { mutableStateOf<List<Wallet>?>(null) }
    var pairingsMetadata by rememberSaveable(chains) { mutableStateOf(emptyList<PairingMetadata>()) }
    LaunchedEffect(chains) {
        scope.launch {
            val chainIds = chains.map { it.id }
            installedWallets = walletConnectKit.getInstalledWallets(chainIds)
            pairingsMetadata = walletConnectKit.getPairingsWithMetadata(chainIds)
            showPairings = showAvailablePairings && pairingsMetadata.isNotEmpty()
        }
    }
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        if (showPairings) {
            SelectPairings(
                pairingsMetadata = pairingsMetadata,
                onPairingClick = {
                    scope.launch {
                        sheetState.hide()
                        onPairingClick(it)
                    }
                },
                onNewConnectionClick = { showPairings = false },
            )
        } else {
            SelectWallet(installedWallets) {
                scope.launch {
                    sheetState.hide()
                    onWalletClick(it)
                }
            }
        }
    }
}

@Composable
private fun SelectWallet(wallets: List<Wallet>?, onWalletClick: (Wallet?) -> Unit) {
    var showAll by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 24.dp),
            text = "Select your Wallet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth(),
            columns = GridCells.Fixed(4),
        ) {
            if (wallets == null) {
                items(4) { LoadingItem() }
            } else if (wallets.isNotEmpty()) {
                if (showAll) {
                    items(wallets) { wallet -> WalletListItem(wallet, onWalletClick) }
                    item { OtherItem { onWalletClick(null) } }
                } else {
                    items(wallets.take(7)) { wallet -> WalletListItem(wallet, onWalletClick) }
                    if (wallets.size > 7) {
                        item { SeeAllItem { showAll = true } }
                    } else {
                        item { OtherItem { onWalletClick(null) } }
                    }
                }
            } else {
                item { OtherItem { onWalletClick(null) } }
            }
        }
    }
}

@Composable
private fun SelectPairings(
    pairingsMetadata: List<PairingMetadata>,
    onPairingClick: (Core.Model.Pairing) -> Unit,
    onNewConnectionClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 24.dp),
            text = "Select previous connection",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyVerticalGrid(
            modifier = Modifier.weight(1F, false),
            columns = GridCells.Fixed(4),
        ) {
            if (pairingsMetadata.isEmpty()) {
                items(4) {
                    LoadingItem()
                }
            } else {
                items(pairingsMetadata) { pairingMetadata ->
                    PairingListItem(pairingMetadata, onPairingClick)
                }
            }
        }
        Button(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = WalletConnectColor),
            onClick = onNewConnectionClick,
            shape = defaultButtonShape(),
            border = defaultBorderStroke(),
            content = {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = "New Connection",
                    color = Color.White,
                )
            },
        )
    }
}

@Composable
private fun defaultButtonColors() = ButtonDefaults.buttonColors(
    backgroundColor = MaterialTheme.colorScheme.background,
    disabledBackgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
        .compositeOver(MaterialTheme.colorScheme.surface),
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

private fun Modifier.defaultContentModifier() = this
    .size(168.dp, 28.dp)
    .padding(4.dp)

private val WalletConnectColor = Color(0xFF3496ff)

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
                .clip(RoundedCornerShape(10.dp)),
        )
        Text(
            text = wallet.name,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PairingListItem(
    pairingMetadata: PairingMetadata,
    onClick: (Core.Model.Pairing) -> Unit,
) {
    Column(
        modifier = Modifier.clickable { onClick(pairingMetadata.pairing) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val walletImageUrl = pairingMetadata.wallet?.imageUrl
            ?: pairingMetadata.pairing.peerAppMetaData?.icons?.firstOrNull()
            ?: ""
        val walletName = pairingMetadata.wallet?.name
            ?: pairingMetadata.pairing.peerAppMetaData?.name
            ?: ""
        WalletImage(
            url = walletImageUrl,
            modifier = Modifier
                .size(80.dp)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
        Text(
            text = walletName,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            color = MaterialTheme.colorScheme.onBackground,
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
                style = MaterialTheme.typography.displayMedium,
            )
        }
        Text(
            text = "Other",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
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
            text = "See All",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LoadingItem() {
    val backgroundColor = if (isSystemInDarkTheme()) {
        Color.DarkGray.copy(0.4f)
    } else {
        Color.LightGray.copy(0.3f)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .shimmer(600)
                .background(backgroundColor),
        )
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(12.dp)
                .shimmer(600)
                .background(backgroundColor),
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WalletImage(url: String, modifier: Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
        contentDescription = null,
        modifier = modifier,
    )
}

@Composable
private fun OnLifecycleEvent(
    vararg filterEvents: Lifecycle.Event,
    onEvent: (Lifecycle.Event) -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (filterEvents.isEmpty()) onEvent(event)
            else if (filterEvents.contains(event)) onEvent(event)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

private fun Modifier.shimmer(
    duration: Int
): Modifier = composed {
    val shimmer = rememberShimmer(
        shimmerBounds = ShimmerBounds.View,
        theme = createCustomTheme(duration),
    )
    shimmer(customShimmer = shimmer)
}

private fun createCustomTheme(duration: Int) = defaultShimmerTheme.copy(
    animationSpec = infiniteRepeatable(
        animation = tween(
            durationMillis = duration,
            delayMillis = 500,
            easing = LinearEasing,
        ),
        repeatMode = RepeatMode.Restart,
    ),
    rotation = 5f,
    shaderColors = listOf(
        Color.Unspecified.copy(alpha = 1.0f),
        Color.Unspecified.copy(alpha = 0.2f),
        Color.Unspecified.copy(alpha = 1.0f),
    ),
    shaderColorStops = null,
    shimmerWidth = 200.dp,
)