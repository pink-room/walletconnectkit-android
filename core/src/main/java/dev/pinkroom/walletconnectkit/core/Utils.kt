package dev.pinkroom.walletconnectkit.core

import android.content.Context
import com.walletconnect.sign.client.Sign

fun Map<String, Sign.Model.Namespace.Proposal>.joinChainsToString(separator: CharSequence = "\n") =
    flatMap { (_, namespace) -> namespace.chains ?: emptyList() }.joinToString(separator)

fun Map<String, Sign.Model.Namespace.Proposal>.joinMethodsToString(separator: CharSequence = "\n") =
    flatMap { (_, namespace) -> namespace.methods }.joinToString(separator)

fun Map<String, Sign.Model.Namespace.Proposal>.joinEventsToString(separator: CharSequence = "\n") =
    flatMap { (_, namespace) -> namespace.events }.joinToString(separator)

val Map<String, Sign.Model.Namespace.Session>.methods
    get() = values.flatMap { it.methods }

val Map<String, Sign.Model.Namespace.Session>.events
    get() = values.flatMap { it.events }

val Map<String, Sign.Model.Namespace.Session>.chains
    get() = values.flatMap { it.chains.orEmpty() }

val Context.appName: String
    get() = applicationInfo.loadLabel(packageManager).toString()