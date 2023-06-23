package dev.pinkroom.walletconnectkit.sign.wallet

import com.walletconnect.android.Core
import com.walletconnect.sign.client.Sign
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface WalletApi {

    val events: SharedFlow<Sign.Model?>

    val activeSessions: Flow<List<Sign.Model.Session>>

    fun pair(uri: String, onSuccess: () -> Unit = {}, onError: (Core.Model.Error) -> Unit = {})

    fun approveProposal(onSuccess: () -> Unit, onError: (Sign.Model.Error) -> Unit)

    fun rejectProposal(
        reason: String = "Reject Proposal",
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    )

    fun rejectRequest(
        sessionRequest: Sign.Model.SessionRequest,
        code: Int = 500,
        messageError: String = "Wallet Error",
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    )

    fun approveRequest(
        sessionRequest: Sign.Model.SessionRequest,
        result: String,
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    )

    fun disconnect(
        sessionTopic: String,
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    )

    fun getSession(topic: String): Sign.Model.Session?
}