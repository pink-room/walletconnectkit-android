package dev.pinkroom.walletconnectkit.data.session

import org.walletconnect.Session

interface SessionManager {
    var session: Session?
    val address: String?
    fun createSession(callback: Session.Callback? = null)
    fun removeSession()
    fun loadSession(callback: Session.Callback? = null)
    val isSessionStored: Boolean
}