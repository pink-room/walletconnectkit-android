package dev.pinkroom.walletconnectkit.common

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.pinkroom.walletconnectkit.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.data.session.SessionRepository
import dev.pinkroom.walletconnectkit.data.wallet.WalletRepository
import dev.pinkroom.walletconnectkit.domain.WalletConnectManager
import okhttp3.OkHttpClient
import org.walletconnect.impls.FileWCSessionStore
import org.walletconnect.impls.MoshiPayloadAdapter
import org.walletconnect.impls.OkHttpTransport
import java.io.File

internal class WalletConnectKitModule(context: Context, config: WalletConnectKitConfig) {

    val walletConnectManager by lazy { WalletConnectManager(sessionRepository, walletRepository) }

    private val sessionRepository by lazy {
        SessionRepository(payloadAdapter, storage, transporter, config)
    }

    private val walletRepository by lazy {
        WalletRepository(context, sessionRepository, dispatchers)
    }

    private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

    private val payloadAdapter by lazy { MoshiPayloadAdapter(moshi) }

    private val storage by lazy { FileWCSessionStore(file.apply { createNewFile() }, moshi) }

    private val transporter by lazy {
        OkHttpTransport.Builder(OkHttpClient.Builder().build(), moshi)
    }

    private val file by lazy { File(context.cacheDir, "session_store.json") }

    private val dispatchers by lazy { Dispatchers() }
}