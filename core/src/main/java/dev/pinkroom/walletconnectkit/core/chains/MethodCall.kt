package dev.pinkroom.walletconnectkit.core.chains

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

sealed class MethodCall {

    data class SendTransaction(
        val from: String,
        val to: String,
        val data: String,
        val gas: String? = null,
        val gasPrice: String? = null,
        val value: String? = null,
        val nonce: String? = null,
    ) : MethodCall()

    data class PersonalSign(val address: String, val message: String) : MethodCall()

    data class Custom(val params: String) : MethodCall()
}

val gson = Gson()

fun sendTransactionAdapter(value: MethodCall.SendTransaction): String =
    gson.toJson(value)

inline fun <reified T> T.toJson(): String = gson.toJson(this)

inline fun <reified T> Gson.toJsonValue(value: T): T = fromJson(toJson(value), typeToken<T>())

inline fun <reified T> Gson.fromJson(value: String): T = fromJson(value, typeToken<T>())

fun <T> Gson.toJsonValue(value: Any?, classOfT: Class<T>): T = fromJson(toJson(value), classOfT)

inline fun <reified T> typeToken(): Type = object : TypeToken<T>() {}.type