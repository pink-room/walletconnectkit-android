package dev.pinkroom.walletconnectkit.sign.dapp.data.service

import dev.pinkroom.walletconnectkit.sign.dapp.data.model.ExplorerResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

internal interface ExplorerService {

    @GET("/w3m/v1/getAndroidListings")
    suspend fun getWallets(
        @Query("projectId") projectId: String,
        @Query("chains") chains: String,
    ): Response<ExplorerResponse>
}