package dev.pinkroom.walletconnectkit.data.wallet

import android.content.Intent
import android.net.Uri
import dev.pinkroom.walletconnectkit.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.common.Dispatchers
import dev.pinkroom.walletconnectkit.common.hasHexPrefix
import dev.pinkroom.walletconnectkit.common.toHex
import dev.pinkroom.walletconnectkit.common.toWei
import dev.pinkroom.walletconnectkit.data.session.SessionRepository
import kotlinx.coroutines.withContext
import org.walletconnect.Session
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

internal class WalletRepository(
    private val walletConnectKitConfig: WalletConnectKitConfig,
    private val sessionRepository: SessionRepository,
    private val dispatchers: Dispatchers,
) : WalletManager {

    override fun openWallet() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("wc:")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        walletConnectKitConfig.context.startActivity(intent)
    }

    override fun requestHandshake() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(sessionRepository.wcUri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        walletConnectKitConfig.context.startActivity(intent)
    }

    override suspend fun performTransaction(
        address: String,
        value: String,
        data: String?,
        nonce: String?,
        gasPrice: String?,
        gasLimit: String?,
    ): Session.MethodCall.Response {
        return withContext(dispatchers.io) {
            suspendCoroutine { continuation ->
                sessionRepository.address?.let { fromAddress ->
                    sessionRepository.session?.let { session ->
                        val id = System.currentTimeMillis()
                        session.performMethodCall(
                            Session.MethodCall.SendTransaction(
                                id,
                                fromAddress,
                                address,
                                nonce,
                                gasPrice,
                                gasLimit,
                                value.toWei().toHex(),
                                data ?: ""
                            )
                        ) { response -> onResponse(id, response, continuation) }
                        openWallet()
                    } ?: continuation.resumeWith(Result.failure(Throwable("Session not found!")))
                } ?: continuation.resumeWith(Result.failure(Throwable("Address not found!")))
            }
        }
    }

    override suspend fun performTransaction(
        address: String,
        value: String,
        nonce: String?,
        gasPrice: String?,
        gasLimit: String?,
    ) = performTransaction(address, value, null, nonce, gasLimit, gasLimit)

    override suspend fun personalSign(message: String): Session.MethodCall.Response {
        return withContext(dispatchers.io) {
            suspendCoroutine { continuation ->
                sessionRepository.address?.let { address ->
                    sessionRepository.session?.let { session ->
                        val id = System.currentTimeMillis()
                        val messageParam = if (message.hasHexPrefix()) message else message.toHex()
                        session.performMethodCall(
                            Session.MethodCall.Custom(
                                id,
                                "personal_sign",
                                listOf(messageParam, address)
                            )
                        ) { onResponse(id, it, continuation) }
                        openWallet()
                    } ?: continuation.resumeWith(Result.failure(Throwable("Session not found!")))
                } ?: continuation.resumeWith(Result.failure(Throwable("Address not found!")))
            }
        }
    }

    private fun onResponse(
        id: Long,
        response: Session.MethodCall.Response,
        continuation: Continuation<Session.MethodCall.Response>
    ) {
        if (id != response.id) {
            val throwable = Throwable("The response id is different from the transaction id!")
            continuation.resumeWith(Result.failure(throwable))
            return
        }
        response.error?.let {
            continuation.resumeWith(Result.failure(Throwable(it.message)))
        } ?: continuation.resumeWith(Result.success(response))
    }
}