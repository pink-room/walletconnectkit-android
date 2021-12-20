package dev.pinkroom.walletconnectkit.data.wallet

import android.content.Intent
import android.net.Uri
import dev.pinkroom.walletconnectkit.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.common.Dispatchers
import dev.pinkroom.walletconnectkit.common.toHex
import dev.pinkroom.walletconnectkit.common.toWei
import dev.pinkroom.walletconnectkit.data.session.SessionRepository
import kotlinx.coroutines.withContext
import org.walletconnect.Session

internal class WalletRepository(
    private val walletConnectKitConfig: WalletConnectKitConfig,
    private val sessionRepository: SessionRepository,
    private val dispatchers: Dispatchers,
) : WalletManager {

    override fun openWallet() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("wc:")
        walletConnectKitConfig.context.startActivity(intent)
    }

    override fun requestHandshake() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(sessionRepository.wcUri)
        walletConnectKitConfig.context.startActivity(intent)
    }

    override suspend fun performTransaction(
        address: String,
        value: String,
        data: String?,
        nonce: String?,
        gasPrice: String?,
        gasLimit: String?,
        transactionResponse: TransactionResponse
    ) {
        withContext(dispatchers.io) {
            sessionRepository.address?.let {
                sessionRepository.session?.let { session ->
                    val id = System.currentTimeMillis()
                    session.performMethodCall(
                        Session.MethodCall.SendTransaction(
                            id,
                            it,
                            address,
                            nonce,
                            gasPrice,
                            gasLimit,
                            value.toWei().toHex(),
                            data ?: ""
                        )
                    ) { response -> transactionResponse(id, response) }
                    openWallet()
                }
            }
        }
    }

    override suspend fun performTransaction(
        address: String,
        value: String,
        nonce: String?,
        gasPrice: String?,
        gasLimit: String?,
        transactionResponse: TransactionResponse
    ) = performTransaction(address, value, null, nonce, gasLimit, gasLimit, transactionResponse)
}