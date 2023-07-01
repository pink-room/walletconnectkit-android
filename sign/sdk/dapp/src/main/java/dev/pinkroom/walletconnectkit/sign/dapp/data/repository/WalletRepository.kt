package dev.pinkroom.walletconnectkit.sign.dapp.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.core.net.toUri
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.sign.dapp.data.model.Wallet
import dev.pinkroom.walletconnectkit.sign.dapp.data.model.toWallet
import dev.pinkroom.walletconnectkit.sign.dapp.data.service.ExplorerService


internal class WalletRepository(
    private val context: Context,
    private val walletConnectKitConfig: WalletConnectKitConfig,
    private val explorerService: ExplorerService,
) {

    suspend fun getWalletsInstalled(chains: List<String>): List<Wallet> {
        val installedWalletsIds = getInstalledWalletsIds()
        val allWallets = getAllWallets(chains)?.listings?.map { it.value }?.map {
            it.toWallet(walletConnectKitConfig.projectId)
        } ?: return emptyList()
        return allWallets.filter { installedWalletsIds.contains(it.packageName) }
    }

    private fun getInstalledWalletsIds(): List<String> {
        val installedWallets = queryInstalledWallets()
        return installedWallets.map { it.activityInfo.packageName }
    }

    private fun queryInstalledWallets(): MutableList<ResolveInfo> {
        val intent = Intent(Intent.ACTION_VIEW, "wc://".toUri())
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }
    }

    private suspend fun getAllWallets(chains: List<String>) =
        explorerService.getWallets(walletConnectKitConfig.projectId, chains.joinToString(","))
            .body()
}