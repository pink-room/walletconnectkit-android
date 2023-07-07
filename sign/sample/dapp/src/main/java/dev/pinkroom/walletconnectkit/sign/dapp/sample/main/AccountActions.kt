package dev.pinkroom.walletconnectkit.sign.dapp.sample.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.pinkroom.walletconnectkit.sign.dapp.WalletConnectKit
import kotlinx.coroutines.launch

@Composable
fun AccountActions(walletConnectKit: WalletConnectKit) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Button(
            onClick = {
                scope.launch {
                    walletConnectKit.performEthSendTransaction("0x670E1F1983CC46933A277dE78e89290a541A7527")
                }
            },
        ) { Text(text = "eth_sendTransaction", color = Color.White) }
        Button(
            modifier = Modifier.padding(top = 8.dp),
            onClick = { scope.launch { walletConnectKit.performEthPersonalSign("Hello World!") } },
        ) { Text(text = "eth_personalSign", color = Color.White) }
        Button(
            modifier = Modifier.padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFCF6E6E)),
            onClick = { walletConnectKit.disconnect() },
        ) { Text(text = "Disconnect", color = Color.White) }
    }
}