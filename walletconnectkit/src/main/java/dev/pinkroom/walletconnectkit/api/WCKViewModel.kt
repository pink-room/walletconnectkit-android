package dev.pinkroom.walletconnectkit.api

import android.content.ActivityNotFoundException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pinkroom.walletconnectkit.WalletConnectKit
import dev.pinkroom.walletconnectkit.common.NoWalletFoundException
import dev.pinkroom.walletconnectkit.common.canBeIgnored
import kotlinx.coroutines.launch
import org.walletconnect.Session

internal class WCKViewModel constructor(
    private val walletConnectKit: WalletConnectKit,
    private val onConnected: (address: String) -> Unit,
    private val onDisconnected: (() -> Unit)?,
    private val sessionCallback: Session.Callback?
) : ViewModel(), Session.Callback {

    override fun onStatus(status: Session.Status) {
        viewModelScope.launch {
            when (status) {
                is Session.Status.Approved -> onSessionApproved()
                is Session.Status.Connected -> onSessionConnected()
                is Session.Status.Closed -> onSessionDisconnected()
                else -> canBeIgnored
            }
            sessionCallback?.onStatus(status)
        }
    }

    override fun onMethodCall(call: Session.MethodCall) {
        viewModelScope.launch { sessionCallback?.onMethodCall(call) }
    }

    fun onClick() {
        if (walletConnectKit.isSessionStored) {
            walletConnectKit.removeSession()
        }
        walletConnectKit.createSession(this)
    }

    fun loadSessionIfStored() {
        if (walletConnectKit.isSessionStored) {
            walletConnectKit.loadSession(this)
            walletConnectKit.address?.let(onConnected)
        }
    }

    private fun onSessionApproved() {
        walletConnectKit.address?.let(onConnected)
    }

    private fun onSessionConnected() {
        walletConnectKit.address ?: runCatching { walletConnectKit.requestHandshake() }
            .onFailure { throwable ->
                if (throwable is ActivityNotFoundException)
                    throw NoWalletFoundException("No wallet was found on the device.")
                else throw throwable
            }
    }

    private fun onSessionDisconnected() {
        onDisconnected?.invoke()
    }
}