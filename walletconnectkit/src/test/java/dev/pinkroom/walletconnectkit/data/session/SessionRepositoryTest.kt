package dev.pinkroom.walletconnectkit.data.session

import com.github.javafaker.Faker
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.pinkroom.walletconnectkit.data.factory.WalletConnectKitConfigFactory
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.walletconnect.Session
import org.walletconnect.impls.MoshiPayloadAdapter
import org.walletconnect.impls.OkHttpTransport
import org.walletconnect.impls.WCSessionStore

class SessionRepositoryTest {

    private val storage = mock<WCSessionStore>()
    private val sessionCallback = mock<Session.Callback>()

    private val faker = Faker()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val payloadAdapter = MoshiPayloadAdapter(moshi)
    private val transporter = OkHttpTransport.Builder(OkHttpClient.Builder().build(), moshi)
    private val config = WalletConnectKitConfigFactory().build()

    private lateinit var sessionRepository: SessionRepository

    @Before
    fun setUp() {
        sessionRepository = SessionRepository(
            payloadAdapter,
            storage,
            transporter,
            config
        )
    }

    @Test
    fun `Given a callback, when createSession() is called, then session is not null`() {
        sessionRepository.createSession(sessionCallback)
        assertNotNull(sessionRepository.session)
    }

    @Test
    fun `Given a session, when removeSession() is called, then session is null`() {
        val session = mock<Session>()
        sessionRepository.session = session
        sessionRepository.removeSession()
        assertNull(sessionRepository.session)
    }

    @Test
    fun `Given a session, when removeSession() is called, then kill() is called`() {
        val session = mock<Session>()
        sessionRepository.session = session
        sessionRepository.removeSession()
        verify(session).kill()
    }

    @Test
    fun `Given a session, when removeSession() is called, then clearCallbacks() is called`() {
        val session = mock<Session>()
        sessionRepository.session = session
        sessionRepository.removeSession()
        verify(session).clearCallbacks()
    }

    @Test
    fun `Given a stored session, when loadSession() is called, then session is not null`() {
        stubStateConfig()
        sessionRepository.loadSession(sessionCallback)
        assertNotNull(sessionRepository.session)
    }

    @Test
    fun `Given a stored session, when isSessionStored is called, then return true`() {
        stubStateConfig()
        assertTrue(sessionRepository.isSessionStored)
    }

    @Test
    fun `Given no session, when isSessionStored is called, then return false`() {
        assertFalse(sessionRepository.isSessionStored)
    }

    @Test
    fun `Given a session with an approved account, when address is called, then address is returned`() {
        val session = mock<Session>()
        sessionRepository.session = session
        val address = faker.random().hex()
        whenever(session.approvedAccounts()).thenReturn(listOf(address))
        assertEquals(address, sessionRepository.address)
    }

    @Test
    fun `Given a session without an approved account, when address is called, then null is returned`() {
        val session = mock<Session>()
        sessionRepository.session = session
        whenever(session.approvedAccounts()).thenReturn(emptyList())
        assertNull(sessionRepository.address)
    }

    @Test
    fun `Given no session, when address is called, then null is returned`() {
        assertNull(sessionRepository.address)
    }

    private fun stubStateConfig() {
        val state = mock<WCSessionStore.State>()
        val handshakeTopic = faker.internet().uuid()
        val key = faker.random().hex()
        whenever(state.config).thenReturn(
            Session.Config(
                handshakeTopic,
                config.bridgeUrl,
                key,
                "wc",
                1
            ).toFullyQualifiedConfig()
        )
        whenever(storage.list()).thenReturn(listOf(state))
    }
}
