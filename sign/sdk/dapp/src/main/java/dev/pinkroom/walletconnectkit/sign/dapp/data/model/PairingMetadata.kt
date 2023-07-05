package dev.pinkroom.walletconnectkit.sign.dapp.data.model

import com.walletconnect.android.Core

data class PairingMetadata(
    val pairing: Core.Model.Pairing,
    val wallet: Wallet?,
)