# Sign DApp

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.pinkroom.walletconnectkit/sign-dapp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/dev.pinkroom.walletconnectkit/sign-dapp)

The SDK that simplifies the integration of the
[WalletConnect v2 Sign protocol](https://docs.walletconnect.com/2.0/api/sign) for Android apps.

-------  
<p align="center">  
    <a href="#installation">Installation</a> &bull;  
    <a href="#setup">Setup</a> &bull;  
    <a href="#connect-button">Connect Button</a> &bull;  
    <a href="#session-and-accounts">Session and Accounts</a> &bull;
    <a href="#methods">Methods</a> &bull;  
    <a href="#receive-events">Receive Events</a> &bull;  
    <a href="#advanced">Advanced</a> 
</p>  
  
-------

## Installation

``` groovy
implementation 'dev.pinkroom.walletconnectkit:sign-dapp:<latest_version>'
```

## Setup

First, you need to create a config:

```kotlin  
val config = WalletConnectKitConfig(projectId = "{PROJECT-ID}")  
```

> **Note**
> You can obtain your Project ID from the following [link](https://cloud.walletconnect.com/sign-in).

Next, build the `WalletConnectKit` instance:

```kotlin  
val walletConnectKit = WalletConnectKit.builder(this).config(config).build()  
```

And you are ready to go! 🚀

## Connect Button

The simplest way to connect a DApp to a Wallet is to use the `WalletConnectKitButton`. This is a
composable component that you can use to connect existing pairs or new sessions. Using Jetpack
Compose you only need to do this:

```Kotlin  
WalletConnectKitButton(walletConnectKit)  
```

Even tho this component is not fully customisable yet, you can check the accepted parameters to
change the button style or simply copy paste the component code and write your own implementation.

Also, by default, the `WalletConnectKitButton` is configured to use the Ethereum chain with some
default methods and events. For more information on how to change this, please refer to the
<a href="#advanced">advanced</a> section.

## Session and Accounts

`Sessions` represent the connections established between a DApp and a Wallet. `Accounts` refer to
the public addresses associated with each session. Since the sessions and accounts can change or
be updated at any time they are exposed through a Kotlin `Flow`.

Because we use Flows, make sure you get or collect them inside a coroutine:

```kotlin
scope.launch {
    val currentActiveSessions = walletConnectKit.activeSessions.lastOrNull()
    // or if you want to always have the active sessions up to date
    walletConnectKit.activeSessions.collect { sessions ->
        // Handle the sessions here! Each session contains a list of accounts.
        sessions.forEach { it.accounts }
    }
}
```

In compose, you can simply collect them as:

```kotlin
val activeSessions by walletConnectKit.activeSessions.collectAsStateWithLifecycle(initialValue = emptyList())
```

**Important note:** by default, the SDK automatically stores the active account that you use to
perform method calls and operations. If you have access to multiple accounts and want to override
the active account just set the `activeAccount` variable:

```kotlin  
walletConnectKit.activeAccount = /* Account */
```

## Methods

Within this SDK, we have integrated the `send_transaction` and `personal_sign` methods specifically
for Ethereum.

All the method calls are `suspend functions` and return a `Result` so you can easily handle the
success and failure states.

### Transactions

To initiate a transaction, simply call the `performEthSendTransaction` method.

```kotlin
walletConnectKit.performEthSendTransaction(
    toAddress = toAddress,
    value = value
).onSuccess { /* Handle the success state */ }
    .onFailure { /* Handle the failure state */ }
```

### Personal Sign

If you solely require signing a message, you can use the `performEthPersonalSign` method.

```kotlin
walletConnectKit.performEthPersonalSign(message)
    .onSuccess { /* Handle the success state */ }
    .onFailure { /* Handle the failure state */ }
```

### Custom Method Call

Any other RPC method that you want to call, simply use the `performCustomMethodCall` function. This
function receives the method name that you want to call and the params. You can send any class
containing the params or a String representing the params in a JSON format.

## Receive Events

To receive updates on the status of the WalletConnect session, you can access a property
called `events` which is a `Flow`. This allows you to collect the events and handle them
accordingly.

```kotlin
walletConnectKit.events.collect { event ->
    // Handle the event based on its type.
    // Add your own logic here
    // ...
}
```

## Advanced

### Connect Button

To use a different method/chain when connecting with the `WalletConnectKitButton`, you can make the
following adjustments.

Use another chain already defined
[here](https://github.com/pink-room/walletconnectkit-android/tree/v2/core/src/main/java/dev/pinkroom/walletconnectkit/core/chains/Chains.kt):

```kotlin  
WalletConnectKitButton(
    walletConnectKit = walletConnectKit,
    chains = listOf(Optimism),
)  
```

Changing methods or events for a defined chain:

```kotlin  
WalletConnectKitButton(
    walletConnectKit = walletConnectKit,
    chains = listOf(
        Ethereum.copy(
            methods = listOf(
                EthMethod.ETH_SIGN,
                EthMethod.ETH_SIGN_TRANSACTION,
            ),
            events = listOf(
                EthEvent.connect,
                EthEvent.disconnect,
            ),
        )
    )
)
```

Setting up a new chain:

```kotlin  
val Cosmos = Chain(
    name = "Cosmos",
    namespace = "cosmos",
    reference = "cosmoshub-4",
    methods = listOf("chainChanged", "accountsChanged"),
    events = listOf("cosmos_signDirect", "cosmos_signAmino"),
)
WalletConnectKitButton(
    walletConnectKit = walletConnectKit,
    chains = listOf(Cosmos),
)
```

### Connect without the WalletConnectKitButton

If you don't want to use the `WalletConnectKitButton` you just need to call the connect function to
connect with a Wallet.

```kotlin  
walletConnectKit.connect(  
    chains = chains,  
    optionalChains = optionalChains,  
    onSuccess = { /* Handle onSuccess */ },  
    onError = { /* Handle onSuccess */ },  
)  
```

### Pairs

To obtain the list of pairs currently available simple get them:

```Kotlin  
walletConnectKit.pairings
```  

If you have an existing pair, you can reuse it and connect to that specific wallet:

```kotlin  
walletConnectKit.connectExistingPair(
    chains = chains,
    optionalChains = optionalChains,
    pairing = selectedPairing,
    onSuccess = { /* Handle onSuccess */ },
    onError = { /* Handle onSuccess */ },
)  
```

**Note:** Even when using an existing pair, you still need to pass the chains and optional chains.
This is because you only have the pair and metadata of the previously paired wallet, not the session
information. If the redirect is not set on the previous wallet, a pop-up will be opened to choose
the desired wallet for pairing. If the redirect is set, the wallet will be automatically opened.

## License

    Copyright 2023 Pink Room, Lda

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.