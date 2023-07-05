package dev.pinkroom.walletconnectkit.sign.dapp.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.sign.dapp.BuildConfig
import dev.pinkroom.walletconnectkit.sign.dapp.data.repository.PreferencesRepository
import dev.pinkroom.walletconnectkit.sign.dapp.data.repository.WalletRepository
import dev.pinkroom.walletconnectkit.sign.dapp.data.service.ExplorerService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class DependenciesModule(context: Context, config: WalletConnectKitConfig) {

    val walletRepository by lazy { WalletRepository(context, config, explorerService) }

    val preferencesRepository by lazy { PreferencesRepository(encryptedSharedPreferences, Gson()) }

    private val explorerService by lazy { retrofit.create(ExplorerService::class.java) }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://explorer-api.walletconnect.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(createBaseHttpClientBuilder().build())
            .build()
    }

    private val encryptedSharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secret_shared_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private fun createBaseHttpClientBuilder(): OkHttpClient.Builder {
        val httpClientBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            httpClientBuilder.addInterceptor(loggingInterceptor)
        }
        return httpClientBuilder
    }
}