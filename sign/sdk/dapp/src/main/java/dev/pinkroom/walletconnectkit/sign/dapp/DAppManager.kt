package dev.pinkroom.walletconnectkit.sign.dapp

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.net.toUri
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.core.accounts
import dev.pinkroom.walletconnectkit.core.approvedAccounts
import dev.pinkroom.walletconnectkit.core.chains.Chain
import dev.pinkroom.walletconnectkit.core.chains.EthMethod
import dev.pinkroom.walletconnectkit.core.chains.MethodCall
import dev.pinkroom.walletconnectkit.core.chains.toJson
import dev.pinkroom.walletconnectkit.core.data.Account
import dev.pinkroom.walletconnectkit.core.initializeCoreClient
import dev.pinkroom.walletconnectkit.core.sessions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class DAppManager(
    private val context: Context,
    private val config: WalletConnectKitConfig,
) : DAppApi {

    override val events: SharedFlow<Sign.Model?> by lazy { DAppDelegate.events }

    override val activeSessions: Flow<List<Sign.Model.Session>>
        get() = events.filterNotNull()
            .map { sessions }
            .onEmpty { emit(sessions) }
            .distinctUntilChanged()

    override val pairings: List<Core.Model.Pairing>
        get() = CoreClient.Pairing.getPairings().filter { it.peerAppMetaData != null }

    override var activeAccount: Account? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        setSelectedAccount()
    }

    private fun setSelectedAccount() {
        activeAccount = sessions.firstOrNull()?.accounts?.firstOrNull()
        scope.launch {
            events.filter { it is Sign.Model.ApprovedSession }
                .map { it as Sign.Model.ApprovedSession }
                .collect { activeAccount = it.approvedAccounts.firstOrNull() }
        }
    }

    override suspend fun connect(
        chains: List<Chain>,
        optionalChains: List<Chain>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                val pairing = CoreClient.Pairing.create { error -> throw error.throwable }
                    ?.copy(peerAppMetaData = config.metadata)
                    ?: throw Throwable("Null Pairing Error")
                connectToWallet(
                    chains = chains,
                    optionalChains = optionalChains,
                    pairing = pairing,
                    onProposedSequence = { uri ->
                        navigateToWallet(uri)
                        onSuccess()
                    },
                    onConnectError = { error -> throw error.throwable },
                )
            }.onSuccess { onSuccess() }
                .onFailure { e -> onError(e) }
        }
    }

    override fun connectExistingPair(
        chains: List<Chain>,
        optionalChains: List<Chain>,
        pairing: Core.Model.Pairing,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) = connectToWallet(
        chains = chains,
        optionalChains = optionalChains,
        pairing = pairing,
        onProposedSequence = { uri ->
            var redirect = pairing.peerAppMetaData?.redirect
            if (pairing.peerAppMetaData?.redirect?.startsWith("trust://") == true) redirect =
                pairing.peerAppMetaData?.redirect + "wc?uri=" + uri
            navigateToWallet(redirect)
            onSuccess()
        },
        onConnectError = { error -> onError(error.throwable) },
    )

    override fun disconnect(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val disconnectParams = Sign.Params.Disconnect(activeAccount?.topic ?: "")
        SignClient.disconnect(
            disconnect = disconnectParams,
            onSuccess = {
                DAppDelegate.deletedSession(activeAccount?.topic ?: "")
                onSuccess()
            },
            onError = { error -> onError(error.throwable) },
        )
    }

    override suspend fun performEthSendTransaction(
        toAddress: String,
        data: String,
        value: String?,
        nonce: String?,
        gasPrice: String?,
        gas: String?
    ): Result<String> {
        val transaction = MethodCall.SendTransaction(
            from = activeAccount?.address ?: "",
            to = toAddress,
            value = value,
            data = data,
            nonce = nonce,
            gasPrice = gasPrice,
            gas = gas,
        )
        val request = Sign.Params.Request(
            sessionTopic = activeAccount?.topic ?: "",
            method = EthMethod.ETH_SEND_TRANSACTION,
            params = listOf(transaction).toJson(),
            chainId = activeAccount?.chainId ?: "",
        )
        val requestId = request.execute().getOrElse { return Result.failure(it) }.requestId
        return waitForResponse(requestId)
    }

    override suspend fun performEthPersonalSign(message: String): Result<String> {
        val request = Sign.Params.Request(
            sessionTopic = activeAccount?.topic ?: "",
            method = EthMethod.PERSONAL_SIGN,
            params = "[\"$message\", \"${activeAccount?.address ?: ""}\"]",
            chainId = activeAccount?.chainId ?: "",
        )
        val requestId = request.execute().getOrElse { return Result.failure(it) }.requestId
        return waitForResponse(requestId)
    }

    override suspend fun performCustomMethodCall(method: String, params: Any): Result<String> {
        val request = Sign.Params.Request(
            sessionTopic = activeAccount?.topic ?: "",
            method = method,
            params = if (params is String) params else params.toJson(),
            chainId = activeAccount?.chainId ?: "",
        )
        val requestId = request.execute().getOrElse { return Result.failure(it) }.requestId
        return waitForResponse(requestId)
    }

    private fun connectToWallet(
        chains: List<Chain>,
        optionalChains: List<Chain>,
        pairing: Core.Model.Pairing,
        onProposedSequence: (String) -> Unit = {},
        onConnectError: (Sign.Model.Error) -> Unit = {},
    ) {
        val namespaces = chains.groupBy { it.namespace }.map { (key: String, chains: List<Chain>) ->
            key to Sign.Model.Namespace.Proposal(
                chains = chains.map { it.id },
                methods = chains.flatMap { it.methods }.distinct(),
                events = chains.flatMap { it.events }.distinct(),
            )
        }.toMap()
        val optionalNamespaces =
            optionalChains.groupBy { it.namespace }.map { (key: String, chains: List<Chain>) ->
                key to Sign.Model.Namespace.Proposal(
                    chains = chains.map { it.id },
                    methods = chains.flatMap { it.methods }.distinct(),
                    events = chains.flatMap { it.events }.distinct()
                )
            }.toMap()
        val connectParams = Sign.Params.Connect(
            namespaces = namespaces,
            optionalNamespaces = optionalNamespaces,
            properties = null,
            pairing = pairing,
        )
        SignClient.connect(
            connect = connectParams,
            onSuccess = { onProposedSequence(pairing.uri) },
            onError = { error -> onConnectError(error) },
        )
    }

    private fun navigateToWallet(uri: String?) {
        runCatching {
            val deeplinkPairingUri = uri ?: "wc://"
            val intent = Intent(Intent.ACTION_VIEW, deeplinkPairingUri.toUri()).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    private suspend fun Sign.Params.Request.execute(): Result<Sign.Model.SentRequest> =
        suspendCoroutine { continuation ->
            SignClient.request(
                request = this,
                onSuccess = { sentRequest: Sign.Model.SentRequest ->
                    navigateToWallet(SignClient.getActiveSessionByTopic(sessionTopic)?.redirect)
                    continuation.resume(Result.success(sentRequest))
                },
                onError = { continuation.resume(Result.failure(it.throwable)) },
            )
        }

    private suspend fun waitForResponse(requestId: Long): Result<String> {
        val response = events.firstOrNull {
            it is Sign.Model.SessionRequestResponse && it.result.id == requestId
        } ?: return Result.failure(Throwable("Something went wrong!"))
        return when (val result = (response as Sign.Model.SessionRequestResponse).result) {
            is Sign.Model.JsonRpcResponse.JsonRpcResult -> Result.success(result.result)
            is Sign.Model.JsonRpcResponse.JsonRpcError -> Result.failure(Throwable(result.toString()))
        }
    }
}