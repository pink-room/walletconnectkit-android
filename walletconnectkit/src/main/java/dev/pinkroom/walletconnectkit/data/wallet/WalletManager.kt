package dev.pinkroom.walletconnectkit.data.wallet

import org.walletconnect.Session

interface WalletManager {
    fun openWallet()
    fun requestHandshake()

    suspend fun performTransaction(
        address: String,
        value: String,
        data: String = "",
        nonce: String? = null,
        gasPrice: String? = null,
        gasLimit: String? = null,
    ): Result<Session.MethodCall.Response>

    fun performTransaction(
        address: String,
        value: String,
        data: String = "",
        nonce: String? = null,
        gasPrice: String? = null,
        gasLimit: String? = null,
        onResult: (Result<Session.MethodCall.Response>) -> Unit
    )
}