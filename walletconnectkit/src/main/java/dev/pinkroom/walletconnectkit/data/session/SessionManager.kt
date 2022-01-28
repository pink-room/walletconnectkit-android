package dev.pinkroom.walletconnectkit.data.session

import org.walletconnect.Session

interface SessionManager {
    var session: Session?
    val address: String?
    fun createSession()
    fun removeSession()
    fun loadSession()
    val isSessionStored: Boolean
}