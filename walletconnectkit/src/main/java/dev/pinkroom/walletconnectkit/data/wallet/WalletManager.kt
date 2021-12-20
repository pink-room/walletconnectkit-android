package dev.pinkroom.walletconnectkit.data.wallet

import org.walletconnect.Session

typealias TransactionResponse = (id: Long, Session.MethodCall.Response) -> Unit

interface WalletManager {
    fun openWallet()
    fun requestHandshake()

    suspend fun performTransaction(
        address: String,
        value: String,
        data: String?,
        nonce: String? = null,
        gasPrice: String? = null,
        gasLimit: String? = null,
        transactionResponse: TransactionResponse
    )

    suspend fun performTransaction(
        address: String,
        value: String,
        nonce: String? = null,
        gasPrice: String? = null,
        gasLimit: String? = null,
        transactionResponse: TransactionResponse
    )
}