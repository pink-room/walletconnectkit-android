package dev.pinkroom.walletconnectkit.sign.wallet.sample.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.sign.client.Sign

@Composable
fun WalletConnectRequestContent(
    sessionRequest: Sign.Model.SessionRequest,
    onReject: () -> Unit,
    onApprove: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(sessionRequest.peerMetaData?.icons?.firstOrNull().toString())
                .build(),
            contentDescription = "DApp Icon",
            contentScale = ContentScale.Fit,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Name")
            Text(text = sessionRequest.peerMetaData?.name.orEmpty())
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Params")
            Text(
                text = sessionRequest.request.params,
                textAlign = TextAlign.Center,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Method")
            Text(
                text = sessionRequest.request.method,
                textAlign = TextAlign.Center,
            )
        }
        Row {
            Button(
                modifier = Modifier.padding(end = 15.dp),
                onClick = onReject,
            ) {
                Text(text = "Decline")
            }
            Button(onClick = onApprove) {
                Text(text = "Approve")
            }
        }
    }
}