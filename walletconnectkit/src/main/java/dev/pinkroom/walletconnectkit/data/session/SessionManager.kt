package dev.pinkroom.walletconnectkit.data.session

import org.walletconnect.Session

interface SessionManager {
    var session: Session?
    val address: String?
    val wcUri: String
    fun createSession(callback: Session.Callback)
    fun removeSession()
    fun loadSession(callback: Session.Callback)
    val isSessionStored: Boolean
}