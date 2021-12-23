package dev.pinkroom.walletconnectkit.data.factory

import android.content.Context
import com.github.javafaker.Faker
import com.nhaarman.mockitokotlin2.mock
import dev.pinkroom.walletconnectkit.WalletConnectKitConfig

class WalletConnectKitConfigFactory {

    private val faker = Faker()
    private val context = mock<Context>()

    fun build() = WalletConnectKitConfig(
        context = context,
        bridgeUrl = "wss://${faker.internet().domainName()}:${faker.number().digits(4)}",
        appUrl = faker.internet().url(),
        appName = faker.app().name(),
        appDescription = faker.lorem().sentence()
    )
}