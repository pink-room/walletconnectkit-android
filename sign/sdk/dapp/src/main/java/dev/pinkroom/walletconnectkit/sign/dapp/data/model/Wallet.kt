package dev.pinkroom.walletconnectkit.sign.dapp.data.model

private const val GET_WALLET_IMAGE_URL =
    "https://explorer-api.walletconnect.com/w3m/v1/getWalletImage/"

data class Wallet(
    val id: String,
    val name: String,
    val imageUrl: String,
    val nativeLink: String?,
    val universalLink: String?,
    val playStoreLink: String,
    val packageName: String,
)

internal fun ExplorerWalletResponse.toWallet(projectId: String) = Wallet(
    id = id,
    name = name,
    imageUrl = "$GET_WALLET_IMAGE_URL$imageId?projectId=$projectId",
    nativeLink = mobile.native,
    universalLink = mobile.universal,
    playStoreLink = app.android,
    packageName = extractPackageId(app.android),
)

private fun extractPackageId(playStoreLink: String): String {
    val regex = "(?<=id=).*?(?=&|\\/|\$)".toRegex()
    return regex.find(playStoreLink)?.value ?: ""
}