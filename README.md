# WalletConnectKit

`WalletConnectKit` is the Swiss Army toolkit for `WalletConnect`. It will allow you to connect your
DApp with a Wallet in a few minutes and start performing method calls right away.

> **Note**
> This SDK uses the WalletConnect v2 protocol and it's currently in the Alpha version. Right now, we
> only support the Sign protocol but aim to add more protocols in the future. Contributions are
> welcome! 🚀

## Sign DApp

[This module](./sign/sdk/dapp) contains the SDK to help you establish a connection with a user's
wallet and perform various operations on the blockchain. The sample DApp code using this SDK can be
found [here](./sign/sample/dapp).

## Sign Wallet

If you want to build a Wallet that supports the WalletConnect v2 Sign protocol
[this](./sign/sdk/wallet) is the SDK for you. In our [sample](./sign/sample/wallet) you can find
the necessary steps to establish a connection with a DApp and handle incoming requests.

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