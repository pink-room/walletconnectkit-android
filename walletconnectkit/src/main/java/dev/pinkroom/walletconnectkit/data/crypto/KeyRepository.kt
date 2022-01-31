package dev.pinkroom.walletconnectkit.data.crypto

import org.komputing.khex.extensions.toNoPrefixHexString
import java.security.SecureRandom
import javax.crypto.KeyGenerator

internal object KeyRepository {

    private const val KEY_ALGORITHM = "AES"
    private const val KEY_SIZE = 256

    fun generate(): String {
        val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM)
        keyGenerator.init(KEY_SIZE, SecureRandom())
        val secretKey = keyGenerator.generateKey()
        return secretKey.encoded.toNoPrefixHexString()
    }
}