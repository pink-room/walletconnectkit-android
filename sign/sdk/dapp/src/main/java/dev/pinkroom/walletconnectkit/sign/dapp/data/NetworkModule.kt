package dev.pinkroom.walletconnectkit.sign.dapp.data

import android.content.Context
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.sign.dapp.BuildConfig
import dev.pinkroom.walletconnectkit.sign.dapp.data.repository.WalletRepository
import dev.pinkroom.walletconnectkit.sign.dapp.data.service.ExplorerService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class NetworkModule(context: Context, config: WalletConnectKitConfig) {

    val walletRepository by lazy { WalletRepository(context, config, explorerService) }

    private val explorerService by lazy { retrofit.create(ExplorerService::class.java) }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://explorer-api.walletconnect.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(createBaseHttpClientBuilder().build())
            .build()
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