package dev.pinkroom.walletconnectkit.data.session

import dev.pinkroom.walletconnectkit.WalletConnectKitConfig
import org.komputing.khex.extensions.toNoPrefixHexString
import org.walletconnect.Session
import org.walletconnect.impls.OkHttpTransport
import org.walletconnect.impls.WCSession
import org.walletconnect.impls.WCSessionStore
import java.util.*

internal class SessionRepository(
    private val payloadAdapter: Session.PayloadAdapter,
    private val storage: WCSessionStore,
    private val transporter: OkHttpTransport.Builder,
    private val walletConnectKitConfig: WalletConnectKitConfig,
) : SessionManager {

    private var config = buildConfig()
    override var session: Session? = null

    override val address get() = session?.approvedAccounts()?.firstOrNull()
    override val wcUri get() = config.toWCUri()


    override fun createSession(callback: Session.Callback) {
        config = buildConfig()
        session = buildSession().apply {
            addCallback(callback)
            offer()
        }
    }

    override fun removeSession() {
        session?.kill()
        session?.clearCallbacks()
        storage.clean()
        session = null
    }

    override fun loadSession(callback: Session.Callback) {
        storage.list().firstOrNull()?.let {
            config = Session.Config(
                it.config.handshakeTopic,
                it.config.bridge,
                it.config.key,
                it.config.protocol,
                it.config.version
            )
            session = WCSession(
                it.config,
                payloadAdapter,
                storage,
                transporter,
                walletConnectKitConfig.clientMeta
            ).apply { addCallback(callback) }
        }
    }

    override val isSessionStored
        get() = storage.list().firstOrNull()?.approvedAccounts?.firstOrNull() != null

    private fun buildConfig(): Session.Config {
        val handshakeTopic = UUID.randomUUID().toString()
        val key = ByteArray(32).also { Random().nextBytes(it) }.toNoPrefixHexString()
        return Session.Config(handshakeTopic, walletConnectKitConfig.relayUrl, key, "wc", 1)
    }

    private fun buildSession() = WCSession(
        config.toFullyQualifiedConfig(),
        payloadAdapter,
        storage,
        transporter,
        walletConnectKitConfig.clientMeta
    )

    private fun WCSessionStore.clean() = list().forEach { remove(it.config.handshakeTopic) }
}