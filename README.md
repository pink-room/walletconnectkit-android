# WalletConnectKit

`WalletConnectKit` is the Swiss Army toolkit for `WalletConnect`! It will allow you to connect your
DApp with an Ethereum Wallet in a few minutes and start performing transactions right away.

**Note:** Currently, we only support the v1 protocol of `WalletConnect`, since it is the protocol
most implemented by Wallets. As soon as Wallets implement the v2 protocol and the`WalletConnect`
library for Kotlin is stable, we will support the v2 protocol.

-------
<p align="center">
    <a href="#installation">Installation</a> &bull;
    <a href="#setup">Setup</a> &bull;
    <a href="#connect-button">Connect Button</a> &bull;
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
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }

    dependencies {
        implementation 'dev.pinkroom:walletconnectkit:<last_version>'
    }
```

## Setup

First, you need to create a config:

```kotlin
val config = WalletConnectKitConfig(
    context = this,
    bridgeUrl = "wss://bridge.aktionariat.com:8887",
    appUrl = "walletconnectkit.com",
    appName = "WalletConnectKit",
    appDescription = "WalletConnectKit is the Swiss Army toolkit for WalletConnect!"
)
```

**Note:** The bridge url provided above is a deployed version of
[this](https://github.com/aktionariat/walletconnect-bridge) repo by its owner. Feel free to use it
or use your own bridge server.

Then, build the `WalletConnectKit` instance:

```kotlin
val walletConnectKit = WalletConnectKit.Builder(config).build()
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

Start the button with the `WalletConnectKit` instance created before. When the account is
successfully connected you will receive the account address.

```kotlin
walletConnectButton.start(walletConnectKit) { address ->
    println("You are connected with account: $address")
}
```

**Note:** `WalletConnectButton` is an `ImageButton` with a default theme that can be overridden by
you!

If you want to be informed about the status of the `WalletConnect` session, you can set a
`Session.Callback` before calling the `start` method.

```kotlin
class MyActivity : AppCompatActivity(), Session.Callback {

    override fun onMethodCall(call: Session.MethodCall) {
        // Handle onMethodCall
    }

    override fun onStatus(status: Session.Status) {
        // Handle session status
    }
}
```

````kotlin
walletConnectButton.sessionCallback = this
````

## Transactions

In order to perform a transaction you just need to call the `performTransaction` method. This method
is a `suspend` function so, you need to call it inside a `coroutine`.

````kotlin
lifecycleScope.launch {
    runCatching { walletConnectKit.performTransaction(toAddress, value) }
        .onSuccess { /* Handle onSuccess */ }
        .onFailure { /* Handle onFailure */ }
}
````

**Note:** If you want to perform a transaction through a smart contract function, you need to pass
the encoded function data of the smart contract to the `data` parameter of the `performTransaction`
function.

## Advanced

If you don't want to use the `WalletConnectKitButton` and want to create your own implementation,
you can still use the `WalletConnectKit` to manage the connection between your DApp and Wallet.

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
<td><b>createSession(callback: Session.Callback)</b></td>
<td>Creates a session and stores it locally. After calling this method you should receive a `Session.Status.Connected` in the passed callback. This is where you should call the `requestHandshake` method (see below).</td>
</tr>

<tr>
<td><b>removeSession()</b></td>
<td>Removes the current session and cleans everything related to it.</td>
</tr>

<tr>
<td><b>loadSession(callback: Session.Callback)</b></td>
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

<tr>
<td><b>requestHandshake()</b></td>
<td>Starts an intent that performs the handshake between your DApp and a Wallet.</td>
</tr>

<tr>
<td><b>openWallet()</b></td>
<td>Starts an intent that opens a Wallet.</td>
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