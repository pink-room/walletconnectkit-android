package dev.pinkroom.walletconnectkit.common

open class Dispatchers {

    open val io = kotlinx.coroutines.Dispatchers.IO
    open val main = kotlinx.coroutines.Dispatchers.Main
}