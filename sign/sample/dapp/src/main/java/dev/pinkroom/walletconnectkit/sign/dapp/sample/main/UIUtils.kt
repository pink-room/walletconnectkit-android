package dev.pinkroom.walletconnectkit.sign.dapp.sample.main

import android.content.Context
import android.widget.Toast

fun Context.showToast(
    message: String,
    duration: Int = Toast.LENGTH_SHORT,
) = Toast.makeText(
    this,
    message,
    duration,
).show()

fun String.middleOverflow(overflow: Int = 20, take: Int = 10) =
    if (length <= overflow) this else "${take(take)}…${takeLast(take)}"