package dev.pinkroom.walletconnectkit.sign.wallet.sample.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun WalletConnectSessions(
    activeSessions: List<Sign.Model.Session>,
    onSessionClicked: (Sign.Model.Session) -> Unit,
) {
    if (activeSessions.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "No active Sessions")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(activeSessions) { session ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSessionClicked(session) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth(0.2f)
                            .padding(end = 20.dp),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(session.metaData?.icons?.firstOrNull())
                            .build(),
                        contentDescription = "Dapp icon",
                        contentScale = ContentScale.Fit,
                    )
                    Text(text = session.metaData?.name.orEmpty())
                }
            }
        }
    }
}