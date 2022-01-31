package dev.pinkroom.walletconnectkit.data.factory

import com.github.javafaker.Faker
import dev.pinkroom.walletconnectkit.WalletConnectKitConfig

class WalletConnectKitConfigFactory {

    private val faker = Faker()

    fun build() = WalletConnectKitConfig(
        bridgeUrl = "wss://${faker.internet().domainName()}:${faker.number().digits(4)}",
        appUrl = faker.internet().url(),
        appName = faker.app().name(),
        appDescription = faker.lorem().sentence()
    )
}