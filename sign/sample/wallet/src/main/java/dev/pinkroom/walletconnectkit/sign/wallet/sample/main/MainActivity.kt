package dev.pinkroom.walletconnectkit.sign.wallet.sample.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.walletconnect.sign.client.Sign
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.sign.wallet.WalletConnectKit
import dev.pinkroom.walletconnectkit.sign.wallet.sample.BuildConfig
import dev.pinkroom.walletconnectkit.sign.wallet.sample.theme.WalletConnectKitTheme
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    private lateinit var walletConnectKit: WalletConnectKit

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = WalletConnectKitConfig(projectId = BuildConfig.PROJECT_ID)
        walletConnectKit = WalletConnectKit.builder(this).config(config).build()
        setContent {
            WalletConnectKitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    intent.handle()
                    Content(walletConnectKit)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.handle()
    }

    @ExperimentalMaterialApi
    @Composable
    private fun Content(walletConnectKit: WalletConnectKit) {
        val context = LocalContext.current
        val event by walletConnectKit.events.collectAsStateWithLifecycle(initialValue = null)
        var actionRequiredEvent by remember { mutableStateOf<Sign.Model?>(null) }
        val activeSessions =
            walletConnectKit.activeSessions.collectAsStateWithLifecycle(initialValue = emptyList())
        val modalSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true,
        )
        val scope = rememberCoroutineScope()
        if (event is Sign.Model.SessionProposal || event is Sign.Model.SessionRequest) {
            actionRequiredEvent = event
            LaunchedEffect(actionRequiredEvent) { modalSheetState.show() }
        }
        ModalBottomSheetLayout(
            sheetState = modalSheetState,
            sheetShape = RoundedCornerShape(
                topStart = 30.dp,
                topEnd = 30.dp,
            ),
            sheetBackgroundColor = MaterialTheme.colorScheme.surface,
            sheetContent = {
                if (actionRequiredEvent is Sign.Model.SessionProposal) {
                    val sessionProposal = actionRequiredEvent as Sign.Model.SessionProposal
                    WalletConnectProposalSheetContent(
                        sessionProposal = sessionProposal,
                        onApproveClick = {
                            walletConnectKit.approveProposal(
                                onSuccess = { navigateBack(sessionProposal.redirect) },
                                onError = { error -> context.showToast("Approve Proposal: $error") },
                            )
                            scope.launch { modalSheetState.hide() }
                        },
                        onDeclineClick = {
                            walletConnectKit.rejectProposal(
                                onSuccess = { navigateBack(sessionProposal.redirect) },
                                onError = { error -> context.showToast("Reject Proposal: $error") },
                            )
                            scope.launch { modalSheetState.hide() }
                        },
                    )
                } else if (actionRequiredEvent is Sign.Model.SessionRequest) {
                    val sessionRequest = actionRequiredEvent as Sign.Model.SessionRequest
                    WalletConnectRequestContent(
                        sessionRequest = sessionRequest,
                        onApprove = {
                            walletConnectKit.approveRequest(
                                sessionRequest = sessionRequest,
                                result = "Transaction successful!",
                                onSuccess = { navigateBack(sessionRequest.peerMetaData?.redirect) },
                                onError = { error -> context.showToast("Approve Request: $error") },
                            )
                            scope.launch { modalSheetState.hide() }
                        },
                        onReject = {
                            walletConnectKit.rejectRequest(
                                sessionRequest,
                                onSuccess = { navigateBack(sessionRequest.peerMetaData?.redirect) },
                                onError = { error -> context.showToast("Reject Request: $error") },
                            )
                            scope.launch { modalSheetState.hide() }
                        },
                    )
                }
            },
        ) {
            HandleSessions(activeSessions.value)
        }
    }

    @Composable
    private fun HandleSessions(activeSessions: List<Sign.Model.Session>) {
        var onClickSessionDetails by remember { mutableStateOf(false) }
        var session: Sign.Model.Session? by remember { mutableStateOf(null) }
        if (!onClickSessionDetails) {
            WalletConnectSessions(
                activeSessions = activeSessions,
                onSessionClicked = { activeSession ->
                    session = walletConnectKit.getSession(activeSession.topic)
                    onClickSessionDetails = true
                },
            )
        } else {
            WalletConnectSessionDetails(
                session = session,
                onBackBtn = { onClickSessionDetails = false },
                onDisconnect = {
                    session?.let {
                        walletConnectKit.disconnect(
                            sessionTopic = it.topic,
                            onSuccess = { },
                            onError = { },
                        )
                        onClickSessionDetails = false
                    }
                },
            )
        }
    }

    // TODO: Add redirect
    private fun Activity.navigateBack(redirect: String?) {
        redirect?.toUri()?.let { deepLinkUri ->
            startActivity(Intent(Intent.ACTION_VIEW, deepLinkUri))
        } ?: run {
            finishAndRemoveTask()
            exitProcess(0)
        }
    }

    private fun Intent.handle() {
        val uri = dataString.toString()
        walletConnectKit.pair(uri)
    }
}

private fun Context.showToast(
    message: String,
    duration: Int = Toast.LENGTH_SHORT,
) = Toast.makeText(
    this,
    message,
    duration,
).show()