package dev.pinkroom.walletconnectkit.sign.dapp.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import dev.pinkroom.walletconnectkit.core.chains.typeToken
import dev.pinkroom.walletconnectkit.sign.dapp.data.model.PairingMetadata

class PreferencesRepository(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
) {

    var pairingsWithMetadata: List<PairingMetadata>
        get() = getFromJson("pairings_metadata") ?: emptyList()
        set(value) = saveAsJson("pairings_metadata", value)

    private inline fun <reified T> getFromJson(key: String): T? {
        val json = sharedPreferences.getString(key, null)
        return json?.let(gson::fromJson) ?: run { null }
    }

    private fun <T> saveAsJson(key: String, value: T?) =
        with(sharedPreferences.edit()) {
            putString(key, gson.toJson(value))
            apply()
        }
}

private inline fun <reified T> Gson.fromJson(value: String): T = fromJson(value, typeToken<T>())