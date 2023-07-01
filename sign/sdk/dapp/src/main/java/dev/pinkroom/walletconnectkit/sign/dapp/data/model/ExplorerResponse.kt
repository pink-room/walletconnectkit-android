package dev.pinkroom.walletconnectkit.sign.dapp.data.model

internal data class ExplorerResponse(
    val listings: Map<String, ExplorerWalletResponse>,
    val count: Int,
    val total: Int
)