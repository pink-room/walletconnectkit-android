package dev.pinkroom.walletconnectkit

import dev.pinkroom.walletconnectkit.common.Dispatchers

class TestDispatchers : Dispatchers() {

    override val io = kotlinx.coroutines.Dispatchers.Main
}