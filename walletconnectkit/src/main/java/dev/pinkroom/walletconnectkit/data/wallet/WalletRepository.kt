package dev.pinkroom.walletconnectkit.data.wallet

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.pinkroom.walletconnectkit.common.Dispatchers
import dev.pinkroom.walletconnectkit.common.toHex
import dev.pinkroom.walletconnectkit.common.toWei
import dev.pinkroom.walletconnectkit.data.session.SessionRepository
import kotlinx.coroutines.withContext
import org.walletconnect.Session
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class WalletRepository(
    private val context: Context,
    private val sessionRepository: SessionRepository,
    private val dispatchers: Dispatchers,
) : WalletManager {

    override fun openWallet() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("wc:")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun requestHandshake() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(sessionRepository.wcUri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override suspend fun performTransaction(
        address: String,
        value: String,
        data: String,
        nonce: String?,
        gasPrice: String?,
        gasLimit: String?,
    ): Result<Session.MethodCall.Response> {
        return withContext(dispatchers.io) {
            suspendCoroutine { continuation ->
                performTransaction(
                    address,
                    value,
                    data,
                    nonce,
                    gasPrice,
                    gasLimit,
                    continuation::resume
                )
            }
        }
    }

    override fun performTransaction(
        address: String,
        value: String,
        data: String,
        nonce: String?,
        gasPrice: String?,
        gasLimit: String?,
        onResult: (Result<Session.MethodCall.Response>) -> Unit
    ) {
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
                        data
                    )
                ) { response -> onPerformTransactionResponse(id, response, onResult) }
                openWallet()
            } ?: onResult(Result.failure(Throwable("Session not found!")))
        } ?: onResult(Result.failure(Throwable("Address not found!")))
    }

    private fun onPerformTransactionResponse(
        id: Long,
        response: Session.MethodCall.Response,
        onResult: (Result<Session.MethodCall.Response>) -> Unit
    ) {
        if (id != response.id) {
            val throwable = Throwable("The response id is different from the transaction id!")
            onResult(Result.failure(throwable))
            return
        }
        response.error?.let {
            onResult(Result.failure(Throwable(it.message)))
        } ?: onResult(Result.success(response))
    }
}