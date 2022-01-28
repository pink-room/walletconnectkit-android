package dev.pinkroom.walletconnectkit.domain

import org.walletconnect.Session

interface SessionCallback {
    var onConnected: ((address: String) -> Unit)?
    var onDisconnected: (() -> Unit)?
    var sessionCallback: Session.Callback?
}