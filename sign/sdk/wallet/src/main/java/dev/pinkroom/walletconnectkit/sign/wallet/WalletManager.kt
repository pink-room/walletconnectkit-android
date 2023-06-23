package dev.pinkroom.walletconnectkit.sign.wallet

import android.content.Context
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.core.initializeCoreClient
import dev.pinkroom.walletconnectkit.core.sessions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty

internal class WalletManager(
    context: Context,
    config: WalletConnectKitConfig,
) : WalletApi {

    override val events: SharedFlow<Sign.Model?> by lazy { WalletDelegate.events }

    override val activeSessions: Flow<List<Sign.Model.Session>>
        get() = events.filterNotNull().map { sessions }.onEmpty { emit(sessions) }
            .distinctUntilChanged()

    init {
        initializeCoreClient(
            context,
            config.relayServerUrl,
            config.projectId,
            config.connectionType,
            config.metadata,
            config.relay,
            config.keyServerUrl,
            config.networkClientTimeout,
        )
    }

    override fun pair(
        uri: String,
        onSuccess: () -> Unit,
        onError: (Core.Model.Error) -> Unit,
    ) {
        val pairingParams = Core.Params.Pair(uri)
        CoreClient.Pairing.pair(
            pair = pairingParams,
            onSuccess = { onSuccess() },
            onError = { error -> onError(error) },
        )
    }

    override fun approveProposal(
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        if (SignClient.getSessionProposals().isNotEmpty()) {
            val sessionProposal = SignClient.getSessionProposals().last()
            val proposerPublicKey = sessionProposal.proposerPublicKey
            val namespaces = sessionProposal.requiredNamespaces
                .flatMap { (namespace, proposal) ->
                    val chains = proposal.chains ?: emptyList()
                    val accounts = chains.map { chain -> "$chain:$proposerPublicKey" }
                    val methods = proposal.methods
                    val events = proposal.events
                    val session = Sign.Model.Namespace.Session(chains, accounts, methods, events)
                    listOf(namespace to session)
                }.toMap()
            val approveParams = Sign.Params.Approve(proposerPublicKey, namespaces)
            SignClient.approveSession(
                approve = approveParams,
                onSuccess = { onSuccess() },
                onError = { error -> onError(error) },
            )
        }
    }

    override fun rejectProposal(
        reason: String,
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        val sessionProposal = SignClient.getSessionProposals().last()
        val reject = Sign.Params.Reject(
            proposerPublicKey = sessionProposal.proposerPublicKey,
            reason = reason,
        )
        SignClient.rejectSession(
            reject = reject,
            onSuccess = { onSuccess() },
            onError = { error -> onError(error) },
        )
    }

    override fun approveRequest(
        sessionRequest: Sign.Model.SessionRequest,
        result: String,
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        val response = Sign.Params.Response(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = Sign.Model.JsonRpcResponse.JsonRpcResult(
                id = sessionRequest.request.id,
                result = result,
            ),
        )
        SignClient.respond(
            response = response,
            onSuccess = { onSuccess() },
            onError = { error -> onError(error) },
        )

    }

    override fun rejectRequest(
        sessionRequest: Sign.Model.SessionRequest,
        code: Int,
        messageError: String,
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        val response = Sign.Params.Response(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = Sign.Model.JsonRpcResponse.JsonRpcError(
                id = sessionRequest.request.id,
                code = code,
                message = messageError,
            ),
        )
        SignClient.respond(
            response = response,
            onSuccess = { onSuccess() },
            onError = { error -> onError(error) },
        )
    }

    override fun disconnect(
        sessionTopic: String,
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        val disconnectParams = Sign.Params.Disconnect(sessionTopic)
        SignClient.disconnect(
            disconnect = disconnectParams,
            onSuccess = {
                WalletDelegate.deletedSession(sessionTopic)
                onSuccess()
            },
            onError = { error -> onError(error) },
        )
    }

    override fun getSession(topic: String) = SignClient.getActiveSessionByTopic(topic)
}