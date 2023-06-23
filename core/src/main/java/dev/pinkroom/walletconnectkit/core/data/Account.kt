package dev.pinkroom.walletconnectkit.core.data

data class Account(
    val topic: String,
    val address: String,
    val chainNamespace: String,
    val chainReference: String,
    val name: String? = null,
    val icon: String? = null,
) {
    val chainId: String get() = "$chainNamespace:$chainReference"
}