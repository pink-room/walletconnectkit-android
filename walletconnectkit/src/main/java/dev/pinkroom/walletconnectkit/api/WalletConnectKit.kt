@file:Suppress("PackageDirectoryMismatch") // Because library imports are prettier this way!
package dev.pinkroom.walletconnectkit

import dev.pinkroom.walletconnectkit.common.WalletConnectKitModule
import dev.pinkroom.walletconnectkit.data.session.SessionManager
import dev.pinkroom.walletconnectkit.data.wallet.WalletManager

class WalletConnectKit private constructor(
    sessionManager: SessionManager,
    walletManager: WalletManager,
) : SessionManager by sessionManager, WalletManager by walletManager {

    class Builder(config: WalletConnectKitConfig) {

        private val walletConnectKitModule = WalletConnectKitModule(config.context, config)

        fun build() = WalletConnectKit(
            walletConnectKitModule.sessionRepository, walletConnectKitModule.walletRepository
        )
    }
}