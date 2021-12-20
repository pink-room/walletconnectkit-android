package dev.pinkroom.walletconnectkit.common

import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal

fun String.toWei(unit: Convert.Unit = Convert.Unit.ETHER): BigDecimal = Convert.toWei(this, unit)

fun BigDecimal.toHex(): String = Numeric.toHexStringWithPrefixSafe(this.toBigInteger())