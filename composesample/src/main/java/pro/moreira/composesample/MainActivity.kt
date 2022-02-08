package pro.moreira.composesample

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.pinkroom.walletconnectkit.WalletConnectKit
import dev.pinkroom.walletconnectkit.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.api.WalletConnectButton
import kotlinx.coroutines.launch
import pro.moreira.composesample.ui.theme.WalletConnectKitTheme

class MainActivity : ComponentActivity() {

    private val config by lazy {
        WalletConnectKitConfig(
            context = this,
            bridgeUrl = "wss://bridge.aktionariat.com:8887",
            appUrl = "walletconnectkit.com",
            appName = "WalletConnect Kit",
            appDescription = "This is the Swiss Army toolkit for WalletConnect!"
        )
    }
    private val walletConnectKit by lazy { WalletConnectKit.Builder(config).build() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalletConnectKitTheme {
                Surface(color = MaterialTheme.colors.background) {
                    var address: String? by remember { mutableStateOf(walletConnectKit.address) }
                    Sample(
                        address,
                        walletConnectKit,
                        onConnected = { address = it },
                        onDisconnected = { address = null }
                    )
                }
            }
        }
    }
}

@Composable
fun Sample(
    address: String?,
    walletConnectKit: WalletConnectKit,
    onConnected: (String) -> Unit,
    onDisconnected: () -> Unit
) {
    Column {
        if (address == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WalletConnectButton(
                    walletConnectKit = walletConnectKit,
                    onConnected = onConnected,
                    onDisconnected = onDisconnected
                )
            }
        } else {
            Column {
                DropdownMenu(onDisconnected = { walletConnectKit.removeSession() })
                Transaction(walletConnectKit = walletConnectKit)
            }
        }
    }
}

@Composable
fun Transaction(walletConnectKit: WalletConnectKit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Connected with: ${walletConnectKit.address}",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        var toAddress by remember { mutableStateOf("") }
        Spacer(modifier = Modifier.padding(12.dp))
        OutlinedTextField(
            value = toAddress,
            onValueChange = { toAddress = it },
            placeholder = { Text("Address (0xAAA...AAA)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.padding(12.dp))
        var value by remember { mutableStateOf("") }
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            placeholder = { Text("Value (0.01 ETH") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.padding(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val composableScope = rememberCoroutineScope()
            val context = LocalContext.current
            OutlinedButton(onClick = {
                composableScope.launch {
                    runCatching { walletConnectKit.performTransaction(toAddress, value) }
                        .onSuccess { showMessage(context, "Transaction done!") }
                        .onFailure { showMessage(context, it.message ?: it.toString()) }
                }
            }) {
                Text(
                    text = "Perform Transaction",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun DropdownMenu(
    onDisconnected: (() -> Unit),
    showDisconnect: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        actions = {
            if (showDisconnect) {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.MoreVert, null)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(onClick = { onDisconnected() }) {
                        Text(text = "Disconnect")
                    }
                }
            }
        }
    )
}

private fun showMessage(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
