package dev.pinkroom.walletconnectkit.common

import android.content.res.Resources

internal val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()