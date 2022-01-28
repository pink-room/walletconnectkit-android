@file:Suppress("PackageDirectoryMismatch") // Because library imports are prettier this way!
package dev.pinkroom.walletconnectkit

import org.walletconnect.Session

class WalletConnectKitConfig(
    val bridgeUrl: String,
    private val appUrl: String,
    private val appName: String,
    private val appDescription: String,
    private val appIcons: List<String>? = listOf(),
) {
    internal val clientMeta by lazy { Session.PeerMeta(appUrl, appName, appDescription, appIcons) }
}