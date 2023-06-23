package dev.pinkroom.walletconnectkit.sign.wallet.sample.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import dev.pinkroom.walletconnectkit.core.joinChainsToString
import dev.pinkroom.walletconnectkit.core.joinEventsToString
import dev.pinkroom.walletconnectkit.core.joinMethodsToString

@Composable
fun WalletConnectProposalSheetContent(
    sessionProposal: Sign.Model.SessionProposal,
    onDeclineClick: () -> Unit,
    onApproveClick: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 20.dp,
                horizontal = 50.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(weight = 1f, fill = false),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(sessionProposal.icons.firstOrNull().toString())
                    .build(),
                contentDescription = "DApp Icon",
                contentScale = ContentScale.Fit,
            )
            Text(text = "Name: ${sessionProposal.name}")
            Text(text = "Url: ${sessionProposal.url}")
            Text(text = "Description")
            Text(text = sessionProposal.description)
            Column(
                Modifier.padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Chain")
                Text(text = sessionProposal.requiredNamespaces.joinChainsToString())
            }
            Column(
                Modifier.padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Methods")
                Text(
                    text = sessionProposal.requiredNamespaces.joinMethodsToString(),
                    textAlign = TextAlign.Center,
                )
            }
            Column(
                Modifier.padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Events")
                Text(
                    text = sessionProposal.requiredNamespaces.joinEventsToString(),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                modifier = Modifier.padding(end = 32.dp),
                onClick = onDeclineClick,
            ) { Text(text = "Decline") }
            Button(onClick = onApproveClick) { Text(text = "Approve") }
        }
    }
}