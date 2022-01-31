package dev.pinkroom.walletconnectkit.domain

import android.os.Handler
import android.os.Looper
import dev.pinkroom.walletconnectkit.common.canBeIgnored
import dev.pinkroom.walletconnectkit.data.session.SessionManager
import dev.pinkroom.walletconnectkit.data.session.SessionRepository
import dev.pinkroom.walletconnectkit.data.wallet.WalletManager
import dev.pinkroom.walletconnectkit.data.wallet.WalletRepository
import org.walletconnect.Session

internal class WalletConnectManager(
    private val sessionRepository: SessionRepository,
    walletRepository: WalletRepository,
) : SessionManager by sessionRepository, WalletManager by walletRepository, ConnectionCallback,
    Session.Callback {

    override var onConnected: ((address: String) -> Unit)? = null
        set(value) {
            field = value
            address?.let { field?.invoke(it) }
        }
    override var onDisconnected: (() -> Unit)? = null
    override var sessionCallback: Session.Callback? = null

    init {
        loadSessionIfStored()
    }

    override fun createSession() = sessionRepository.createSession(this)

    override fun loadSession() = sessionRepository.loadSession(this)

    override fun removeSession() {
        sessionRepository.removeSession()
        runOnMainThread { onDisconnected?.invoke() }
    }

    override fun onMethodCall(call: Session.MethodCall) = runOnMainThread {
        sessionCallback?.onMethodCall(call)
    }

    override fun onStatus(status: Session.Status) = runOnMainThread {
        when (status) {
            is Session.Status.Approved -> onSessionApproved()
            is Session.Status.Connected -> onSessionConnected()
            else -> canBeIgnored
        }
        sessionCallback?.onStatus(status)
    }

    private fun onSessionApproved() {
        address?.let { onConnected?.invoke(it) }
    }

    private fun onSessionConnected() {
        address ?: requestHandshake()
    }

    private fun loadSessionIfStored() {
        if (isSessionStored) sessionRepository.loadSession(this)
    }

    private fun runOnMainThread(runnable: Runnable) {
        Handler(Looper.getMainLooper()).post(runnable)
    }
}