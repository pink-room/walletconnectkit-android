package dev.pinkroom.walletconnectkit.sign.dapp.data.model

import com.google.gson.annotations.SerializedName

internal data class ExplorerWalletResponse(
    val id: String,
    val name: String,
    val description: String?,
    val homePage: String,
    @SerializedName("image_id")
    val imageId: String,
    val mobile: Mobile,
    val app: App,
)

internal data class Mobile(
    val native: String?,
    val universal: String?,
)

internal data class App(
    val android: String,
)