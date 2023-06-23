package dev.pinkroom.walletconnectkit.sign.wallet.sample.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.sign.client.Sign
import dev.pinkroom.walletconnectkit.core.chains
import dev.pinkroom.walletconnectkit.core.events
import dev.pinkroom.walletconnectkit.core.methods

@Composable
fun WalletConnectSessionDetails(
    session: Sign.Model.Session?,
    onBackBtn: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Button(
        modifier = Modifier.padding(
            top = 12.dp,
            start = 12.dp,
        ),
        onClick = onBackBtn,
    ) { Text(text = "<- Back") }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth(0.2f)
                .padding(end = 20.dp),
            model = ImageRequest.Builder(LocalContext.current)
                .data(session?.metaData?.icons?.firstOrNull())
                .build(),
            contentDescription = "Dapp icon",
            contentScale = ContentScale.Fit,
        )
        Row(verticalAlignment = Alignment.CenterVertically) { Text(text = "Dapp Name: ${session?.metaData?.name}") }
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { Text(text = "Url: ${session?.metaData?.url}") }
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Methods:",
        )
        session?.namespaces?.methods?.forEach { method -> Text(text = method) }
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Events:",
        )
        session?.namespaces?.events?.forEach { event -> Text(text = event) }
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Chains:",
        )
        session?.namespaces?.chains?.forEach { chain -> Text(text = chain) }
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onDisconnect,
        ) { Text(text = "Disconnect") }
    }
}