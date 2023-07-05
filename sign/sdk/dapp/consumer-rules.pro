# Application classes that will be serialized/deserialized over Gson
-keep class dev.pinkroom.walletconnectkit.sign.dapp.data.model.** { *; }
# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken