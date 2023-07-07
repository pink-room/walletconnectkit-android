# Fix minifiction issue with WalletConnect SDK
-keepnames public interface kotlinx.coroutines.flow**
# Application classes that will be serialized/deserialized over Gson
-keep class dev.pinkroom.walletconnectkit.core.data.model.** { *; }