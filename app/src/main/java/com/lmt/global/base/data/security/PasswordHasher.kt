package com.lmt.global.base.data.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

interface PasswordHasher {
    fun hash(password: String): String
    fun verify(password: String, encodedHash: String): Boolean
}

class Pbkdf2PasswordHasher(
    private val secureRandom: SecureRandom = SecureRandom()
) : PasswordHasher {
    override fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also(secureRandom::nextBytes)
        val derived = derive(password, salt, ITERATIONS)
        return listOf(
            FORMAT,
            ITERATIONS.toString(),
            encode(salt),
            encode(derived)
        ).joinToString(SEPARATOR)
    }

    override fun verify(password: String, encodedHash: String): Boolean {
        val parts = encodedHash.split(SEPARATOR)
        if (parts.size != PART_COUNT || parts[0] != FORMAT) return false
        val iterations = parts[1].toIntOrNull()?.takeIf { it > 0 } ?: return false
        return runCatching {
            val salt = decode(parts[2])
            val expected = decode(parts[3])
            MessageDigest.isEqual(expected, derive(password, salt, iterations))
        }.getOrDefault(false)
    }

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(value: ByteArray) = Base64.encodeToString(value, Base64.NO_WRAP)
    private fun decode(value: String) = Base64.decode(value, Base64.NO_WRAP)

    private companion object {
        const val FORMAT = "pbkdf2-sha256"
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATIONS = 120_000
        const val KEY_LENGTH_BITS = 256
        const val SALT_BYTES = 16
        const val SEPARATOR = "$"
        const val PART_COUNT = 4
    }
}
