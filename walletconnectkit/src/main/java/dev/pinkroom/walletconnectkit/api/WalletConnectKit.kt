@file:Suppress("PackageDirectoryMismatch") // Because library imports are prettier this way!
package dev.pinkroom.walletconnectkit

import android.content.Context
import dev.pinkroom.walletconnectkit.common.WalletConnectKitModule
import dev.pinkroom.walletconnectkit.data.session.SessionManager
import dev.pinkroom.walletconnectkit.data.wallet.WalletManager
import dev.pinkroom.walletconnectkit.domain.SessionCallback
import dev.pinkroom.walletconnectkit.domain.WalletConnectManager

class WalletConnectKit private constructor(
    walletConnectManager: WalletConnectManager,
) : SessionManager by walletConnectManager, WalletManager by walletConnectManager,
    SessionCallback by walletConnectManager {

    interface Configuration {
        fun config(config: WalletConnectKitConfig): Build
    }

    interface Build {
        fun build(): WalletConnectKit
    }

    companion object {
        @JvmStatic
        fun builder(context: Context): Configuration = Builder(context)
    }

    private class Builder(context: Context) : Configuration, Build {

        private val context = context.applicationContext
        private lateinit var config: WalletConnectKitConfig

        override fun config(config: WalletConnectKitConfig) = apply {
            this.config = config
        }

        override fun build(): WalletConnectKit {
            val module = WalletConnectKitModule(context, config)
            return WalletConnectKit(module.walletConnectManager)
        }
    }
}