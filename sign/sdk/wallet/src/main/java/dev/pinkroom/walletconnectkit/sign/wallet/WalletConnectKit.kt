package dev.pinkroom.walletconnectkit.sign.wallet

import android.content.Context
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.core.appName

class WalletConnectKit
private constructor(walletManager: WalletManager) : WalletApi by walletManager {

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
            if (config.appName.isBlank()) {
                this.config = config.copy(appName = context.appName)
            } else {
                this.config = config
            }
        }

        override fun build(): WalletConnectKit {
            val walletManager = WalletManager(context, config)
            return WalletConnectKit(walletManager)
        }
    }
}