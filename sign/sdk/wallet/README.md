# Sign Wallet

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.pinkroom.walletconnectkit/sign-wallet/badge.svg)](https://maven-badges.herokuapp.com/maven-central/dev.pinkroom.walletconnectkit/sign-wallet)

The SDK that simplifies the integration of the
[WalletConnect v2 Sign protocol](https://docs.walletconnect.com/2.0/api/sign) in your Wallet.

------- 

<p align="center">  
    <a href="#installation">Installation</a> &bull;  
    <a href="#setup">Setup</a> &bull;  
    <a href="#pair-with-dapp">Pair with DApp</a> &bull;  
    <a href="#sessions">Sessions</a> &bull;
    <a href="#receive-events">Receive Events</a> &bull;  
    <a href="#proposals">Proposals</a> &bull;  
    <a href="#requests">Requests</a>  
</p>  

-------

## Installation

``` groovy
implementation 'dev.pinkroom.walletconnectkit:sign-wallet:<latest_version>'
```

## Setup

First, you need to create a config:

```kotlin  
val config = WalletConnectKitConfig(projectId = "{PROJECT-ID}")  
```

> **Note**
> You can obtain your Project ID from the following [link](https://cloud.walletconnect.com/sign-in).

Next, build the WalletConnectKit instance:

```kotlin  
val walletConnectKit = WalletConnectKit.builder(this).config(config).build()  
```

And you are ready to go! 🚀

## Pair with DApp

To establish a pairing connection with DApp using a provided URI, you can use the `pair`
function provided by `walletConnectKit`. Here's an example:

```kotlin
walletConnectKit.pair(
    uri = uri,
    onSuccess = { /* Handle onSuccess */ },
    onError = { /* Handle onSuccess */ },
)
```

**Note:** This method enables pairing using a DApp URI obtained in different scenarios:

1. **DApp Intent:** Extract the URI from the intent initiated by a DApp and pass it to the pair
   method.
2. **QR Code:** Use a QR code scanning library to retrieve the DApp URI as a string from a QR
   code. Pass it to the pair method.
3. **Direct URI Entry:** Retrieve the manually entered DApp URI as a string from your wallet's
   interface and pass it to the pair function. This allows users to paste or type the URI.

Regardless of how you obtain the DApp URI, you can use the pair function to initiate the
pairing process.

## Sessions

The `sessions` property in `walletConnectKit` provides access to the active sessions established
between a DApp and a wallet. Since the sessions and accounts can change or be updated at any time
they are exposed through a Kotlin `Flow`.

Because we use Flows, make sure you get or collect them inside a coroutine:

```kotlin
scope.launch {
   walletConnectKit.activeSessions.collect { sessions ->
      // Handle the sessions here!
   }
}
```

In compose, you can simply collect them as:

```kotlin
val activeSessions by walletConnectKit.activeSessions.collectAsStateWithLifecycle(initialValue = emptyList())
```

## Receive Events

To receive updates on the status of the WalletConnect sessions, you can access a property called
`events` which is also a `Flow`. This allows you to collect the events and handle them accordingly.

```kotlin
walletConnectKit.events.collect { event ->
    // Handle the event based on its type.
    // Add your own logic here
    // ...
}
```

## Proposals

In the events above, we can receive a `Sign.Model.SessionProposal` event and decide to approve or
reject the proposal.

To approve a proposal, you can use the `approveProposal` method:

```kotlin
 walletConnectKit.approveProposal(
    onSuccess = { /* Handle onSuccess */ },
    onError = { /* Handle onError */ },
)
```

To reject a proposal, you can use the `rejectProposal` method:

```kotlin
walletConnectKit.approveProposal(
    onSuccess = { /* Handle rejectProposal */ },
    onError = { /* Handle onError */ },
)
```

## Requests

And when we receive an `Sign.Model.SessionRequest` event we can accept or reject the request too.

To accept a request, you can use the `approveRequest` method:

```kotlin
walletConnectKit.approveRequest(
    sessionRequest = sessionRequest,
    result = result,
    onSuccess = { /* Handle onSuccess */ },
    onError = { /* Handle onError */ },
)
```

To reject a request, you can use the `rejectRequest` method:

```kotlin
walletConnectKit.rejectRequest(
    sessionRequest = sessionRequest,
    code = code,
    messageError = messageError,
    onSuccess = { /* Handle onSuccess */ },
    onError = { /* Handle onError */ },
)
```

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