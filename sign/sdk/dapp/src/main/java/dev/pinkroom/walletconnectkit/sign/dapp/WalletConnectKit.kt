package dev.pinkroom.walletconnectkit.sign.dapp

import android.content.Context
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.core.appName
import dev.pinkroom.walletconnectkit.sign.dapp.data.DependenciesModule

class WalletConnectKit
private constructor(dAppManager: DAppManager) : DAppApi by dAppManager {

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
            val dependenciesModule = DependenciesModule(context, config)
            val dappManager = DAppManager(
                context,
                config,
                dependenciesModule.walletRepository,
                dependenciesModule.preferencesRepository,
            )
            return WalletConnectKit(dappManager)
        }
    }
}