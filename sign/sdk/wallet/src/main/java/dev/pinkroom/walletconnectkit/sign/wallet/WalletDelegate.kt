package dev.pinkroom.walletconnectkit.sign.wallet

import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal object WalletDelegate : SignClient.WalletDelegate {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _events = MutableSharedFlow<Sign.Model?>()

    val events = _events.asSharedFlow()

    init {
        SignClient.setWalletDelegate(this)
    }

    override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
        scope.launch { _events.emit(sessionProposal) }
    }

    override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest) {
        scope.launch { _events.emit(sessionRequest) }
    }

    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
        scope.launch { _events.emit(deletedSession) }
    }

    override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
        scope.launch { _events.emit(settleSessionResponse) }
    }

    override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {
        scope.launch { _events.emit(sessionUpdateResponse) }
    }

    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        scope.launch { _events.emit(state) }
    }

    override fun onError(error: Sign.Model.Error) {
        scope.launch { _events.emit(error) }
    }

    fun deletedSession(sessionTopic: String) =
        onSessionDelete(Sign.Model.DeletedSession.Success(sessionTopic, "Delete Session"))
}