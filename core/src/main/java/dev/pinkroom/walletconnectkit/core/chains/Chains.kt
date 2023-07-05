package dev.pinkroom.walletconnectkit.core.chains

data class Chain(
    val name: String,
    val namespace: String,
    val reference: String,
    val methods: List<String>,
    val events: List<String>,
) {
    val id: String get() = "$namespace:$reference"
}

object EthMethod {
    const val PERSONAL_SIGN = "personal_sign"
    const val ETH_SIGN = "eth_sign"
    const val ETH_SIGN_TRANSACTION = "eth_signTransaction"
    const val ETH_SIGN_TYPED_DATA = "eth_signTypedData"
    const val ETH_SIGN_TYPED_DATA_V3 = "eth_signTypedData_v3"
    const val ETH_SIGN_TYPED_DATA_V4 = "eth_signTypedData_v4"
    const val ETH_SEND_TRANSACTION = "eth_sendTransaction"
    const val WALLET_SWITCH_ETHEREUM_CHAIN = "wallet_switchEthereumChain"
}

object EthEvent {
    const val ACCOUNTS_CHANGED = "accountsChanged"
    const val CHAIN_CHANGED = "chainChanged"
    const val DISCONNECT = "disconnect"
    const val SESSION_DELETE = "session_delete"
    const val DISPLAY_URI = "display_uri"
    const val CONNECT = "connect"
}

private val ethMethods = listOf(
    EthMethod.PERSONAL_SIGN,
    EthMethod.ETH_SIGN_TYPED_DATA,
    EthMethod.ETH_SIGN_TYPED_DATA_V3,
    EthMethod.ETH_SIGN_TYPED_DATA_V4,
    EthMethod.ETH_SEND_TRANSACTION,
)

private val ethEvents = listOf(
    EthEvent.ACCOUNTS_CHANGED,
    EthEvent.CHAIN_CHANGED,
    EthEvent.DISCONNECT,
    EthEvent.SESSION_DELETE,
    EthEvent.DISPLAY_URI,
    EthEvent.CONNECT,
)

val Ethereum = Chain(
    name = "Ethereum",
    namespace = "eip155",
    reference = "1",
    methods = ethMethods,
    events = ethEvents,
)

val EthereumSepolia = Chain(
    name = "Ethereum Sepolia",
    namespace = "eip155",
    reference = "11155111",
    methods = ethMethods,
    events = ethEvents,
)

val EthereumGoerli = Chain(
    name = "Ethereum Goerli",
    namespace = "eip155",
    reference = "5",
    methods = ethMethods,
    events = ethEvents,
)

val Optimism = Chain(
    name = "Optimism",
    namespace = "eip155",
    reference = "10",
    methods = ethMethods,
    events = ethEvents,
)

val OptimismGoerli = Chain(
    name = "Optimism Goerli",
    namespace = "eip155",
    reference = "420",
    methods = ethMethods,
    events = ethEvents,
)

val Polygon = Chain(
    name = "Polygon",
    namespace = "eip155",
    reference = "137",
    methods = ethMethods,
    events = ethEvents,
)

val PolygonMumbai = Chain(
    name = "Polygon Mumbay",
    namespace = "eip155",
    reference = "80001",
    methods = ethMethods,
    events = ethEvents,
)

val ArbitrumOne = Chain(
    name = "Arbitrum One",
    namespace = "eip155",
    reference = "42161",
    methods = ethMethods,
    events = ethEvents,
)

val ArbitrumNova = Chain(
    name = "Arbitrum Nova",
    namespace = "eip155",
    reference = "42170",
    methods = ethMethods,
    events = ethEvents,
)

val ArbitrumGoerli = Chain(
    name = "Arbitrum Goerli",
    namespace = "eip155",
    reference = "421613",
    methods = ethMethods,
    events = ethEvents,
)

val Celo = Chain(
    name = "Celo",
    namespace = "eip155",
    reference = "42220",
    methods = ethMethods,
    events = ethEvents,
)

val CeloAlfajores = Chain(
    name = "Celo Alfajores",
    namespace = "eip155",
    reference = "44787",
    methods = ethMethods,
    events = ethEvents,
)