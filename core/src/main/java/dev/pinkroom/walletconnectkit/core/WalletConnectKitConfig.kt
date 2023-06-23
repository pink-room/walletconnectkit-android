package dev.pinkroom.walletconnectkit.core

import android.app.Application
import android.content.Context
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.NetworkClientTimeout
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient

data class WalletConnectKitConfig(
    val projectId: String,
    val relayServerUrl: String = "relay.walletconnect.com",
    val appName: String = "",
    val appUrl: String = "",
    val appDescription: String = "",
    val icons: List<String> = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
    val redirect: String? = null,
    val connectionType: ConnectionType = ConnectionType.AUTOMATIC,
    val verifyUrl: String? = null,
    val relay: RelayConnectionInterface? = null,
    val keyServerUrl: String? = null,
    val networkClientTimeout: NetworkClientTimeout? = null,
) {

    val metadata = Core.Model.AppMetaData(
        name = appName,
        description = appDescription,
        url = appUrl,
        icons = icons,
        redirect = redirect,
        verifyUrl = verifyUrl,
    )
}

fun initializeCoreClient(
    context: Context,
    relayServerUrl: String,
    projectId: String,
    connectionType: ConnectionType,
    metadata: Core.Model.AppMetaData,
    relay: RelayConnectionInterface?,
    keyServerUrl: String?,
    networkClientTimeout: NetworkClientTimeout?,
) {
    CoreClient.initialize(
        relayServerUrl = "wss://$relayServerUrl?projectId=$projectId",
        connectionType = connectionType,
        application = context.applicationContext as Application,
        metaData = metadata,
        relay = relay,
        keyServerUrl = keyServerUrl,
        networkClientTimeout = networkClientTimeout,
        onError = { },
    )
    val initParams = Sign.Params.Init(CoreClient)
    SignClient.initialize(
        init = initParams,
        onError = { },
    )
}