# WalletConnectKit

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.pinkroom/walletconnectkit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/dev.pinkroom/walletconnectkit)
![Status](https://github.com/pink-room/walletconnectkit-android/actions/workflows/android.yml/badge.svg)

`WalletConnectKit` is the Swiss Army toolkit for `WalletConnect`! It will allow you to connect your
DApp with an Ethereum Wallet in a few minutes and start performing transactions right away.

**Note:** Currently, we only support the v1 protocol of `WalletConnect`, since it is the protocol
that most Wallets implement. As soon as Wallets implement the v2 protocol and the`WalletConnect`
library for Kotlin is stable, we will support the v2 protocol.

-------
<p align="center">
    <a href="#installation">Installation</a> &bull;
    <a href="#setup">Setup</a> &bull;
    <a href="#connect-button">Connect Button</a> &bull;
    <a href="#handle-connection">Handle Connection</a> &bull;
    <a href="#transactions">Transactions</a> &bull;
    <a href="#Advanced">Advanced</a>
</p>

-------

## Demo

<p>
   <img src="https://raw.githubusercontent.com/pink-room/walletconnectkit-android/main/demo.gif" width="250"/>
</p>

## Installation

``` groovy
implementation 'dev.pinkroom:walletconnectkit:<last_version>'
```

## Setup

First, you need to create a config:

```kotlin
val config = WalletConnectKitConfig(
    bridgeUrl = "wss://bridge.walletconnect.org",
    appUrl = "walletconnectkit.com",
    appName = "WalletConnectKit",
    appDescription = "WalletConnectKit is the Swiss Army toolkit for WalletConnect!"
)
```

**Note:** You can use your own bridge or any other publicly available.

Then, build the `WalletConnectKit` instance:

```kotlin
val walletConnectKit = WalletConnectKit.builder(context).config(config).build()
```

And you are ready to go! 🚀

## Connect Button

Add the `WalletConnectButton` to your layout:

```xml

<dev.pinkroom.walletconnectkit.WalletConnectButton
    android:id="@+id/walletConnectButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

Start the button with the previously created `WalletConnectKit` instance.

```kotlin
walletConnectButton.start(walletConnectKit)
```

**Note:** `WalletConnectButton` is an `ImageButton` with a default theme that can be overridden by
you!

## Handle Connection

To handle the connection state between your DApp and Wallet, you can set the `onConnected` and
`onDisconnected` callbacks:

```kotlin
walletConnectKit.apply {
    onConnected = ::onConnected
    onDisconnected = ::onDisconnected
}
```

```kotlin
private fun onConnected(address: String) {
    println("You are connected with account: $address")
}

private fun onDisconnected() {
    println("Account disconnected!")
}
```

If you want to be informed about the status of the `WalletConnect` session, you can also set a
`Session.Callback`.

```kotlin
class MyClass : Session.Callback {

    override fun onMethodCall(call: Session.MethodCall) {
        // Handle onMethodCall
    }

    override fun onStatus(status: Session.Status) {
        // Handle session status
    }
}
```

````kotlin
walletConnectkit.sessionCallback = this
````

## Transactions

In order to perform a transaction, you just need to call the `performTransaction` method. This
method has two different implementations. One is based on callbacks and the other is powered by
coroutines.

### With callbacks

````kotlin
walletConnectKit.performTransaction(toAddress, value) { result ->
    if (result.isSuccess) {
        // Handle onSuccess
    } else {
        // Handle onFailure
    }
}
````

### With coroutines

````kotlin
lifecycleScope.launch {
    walletConnectKit.performTransaction(toAddress, value)
        .onSuccess { /* Handle onSuccess */ }
        .onFailure { /* Handle onFailure */ }
}
````

**Note:** If you want to perform a transaction through a smart contract function, you need to pass
the encoded function data of the smart contract to the `data` parameter of the `performTransaction`
function.

## Advanced

Don't want to use the `WalletConnectKitButton`? Want to create your own implementation? You can
still use the `WalletConnectKit` to manage the connection between your DApp and Wallet.

Below are the most relevant methods provided by the `WalletConnectKit` that you need to care about:

<table>
<thead>
<tr>
<th>Method</th>
<th>Description</th>
</tr>
</thead>
<tbody>

<tr>
<td><b>createSession()</b></td>
<td>Creates a session and stores it locally.</td>
</tr>

<tr>
<td><b>removeSession()</b></td>
<td>Removes the current session and cleans everything related to it.</td>
</tr>

<tr>
<td><b>loadSession()</b></td>
<td>Loads the session that is stored locally.</td>
</tr>

<tr>
<td><b>isSessionStored</b></td>
<td>A flag that tells you if there is any session stored locally.</td>
</tr>

<tr>
<td><b>session</b></td>
<td>Returns the current session or null otherwise.</td>
</tr>

<tr>
<td><b>address</b></td>
<td>Returns the approved account address or null otherwise.</td>
</tr>

</tbody>
</table>

## License

    Copyright 2021 Pink Room, Lda

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.