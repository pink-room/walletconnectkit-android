package dev.pinkroom.walletconnectkit.sign.dapp

import com.walletconnect.android.Core
import com.walletconnect.sign.client.Sign
import dev.pinkroom.walletconnectkit.core.chains.Chain
import dev.pinkroom.walletconnectkit.core.data.Account
import dev.pinkroom.walletconnectkit.sign.dapp.data.model.PairingMetadata
import dev.pinkroom.walletconnectkit.sign.dapp.data.model.Wallet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface DAppApi {

    val events: SharedFlow<Sign.Model?>

    val activeSessions: Flow<List<Sign.Model.Session>>

    val pairings: List<Core.Model.Pairing>

    var activeAccount: Account?

    suspend fun connect(
        chains: List<Chain>,
        optionalChains: List<Chain> = emptyList(),
        wallet: Wallet? = null,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
    )

    fun connectExistingPairing(
        chains: List<Chain>,
        optionalChains: List<Chain> = emptyList(),
        pairing: Core.Model.Pairing,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
    )

    fun disconnect(
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
    )

    suspend fun performEthSendTransaction(
        toAddress: String,
        data: String = "0x",
        value: String? = null,
        nonce: String? = null,
        gasPrice: String? = null,
        gas: String? = null,
    ): Result<String>

    suspend fun performEthPersonalSign(message: String): Result<String>

    suspend fun performCustomMethodCall(method: String, params: Any): Result<String>

    suspend fun getInstalledWallets(chains: List<String>): List<Wallet>

    suspend fun getPairingsWithMetadata(chains: List<String>): List<PairingMetadata>
}