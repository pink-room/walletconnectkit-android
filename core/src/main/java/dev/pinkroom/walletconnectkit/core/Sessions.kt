package dev.pinkroom.walletconnectkit.core

import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import dev.pinkroom.walletconnectkit.core.data.Account

/*
* Pairing lifecycle
*
* Inactive Pairing - A pairing is considered inactive if the proposing peer has no proof that any peer
* has paired with it. Inactive pairings should expire 5 minutes after being created.

* Active Pairing - A pairing is considered active if any of peer has a proof that other peer can
* respond to it. Active pairings should have a 30 days expiry period.
* */
// TODO: Should we filter the expired sessions?
val sessions: List<Sign.Model.Session>
    get() = SignClient.getListOfActiveSessions()
        .filter { session -> session.metaData != null }

val Sign.Model.Session.accounts: List<Account>
    get() = namespaces.values.flatMap { it.accounts }.map {
        val (chainNamespace, chainReference, address) = it.split(":")
        Account(
            topic = topic,
            icon = metaData?.icons?.firstOrNull(),
            name = metaData?.name,
            address = address,
            chainNamespace = chainNamespace,
            chainReference = chainReference,
        )
    }

val Sign.Model.ApprovedSession.approvedAccounts: List<Account>
    get() = namespaces.values.flatMap { it.accounts }.map {
        val (chainNamespace, chainReference, address) = it.split(":")
        Account(
            topic = topic,
            icon = metaData?.icons?.firstOrNull(),
            name = metaData?.name,
            address = address,
            chainNamespace = chainNamespace,
            chainReference = chainReference,
        )
    }

