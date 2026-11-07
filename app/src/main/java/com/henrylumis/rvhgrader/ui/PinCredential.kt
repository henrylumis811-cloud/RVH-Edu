package com.henrylumis.rvhgrader.ui

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Password-based PIN credential primitives. No Android APIs, so the production code is JVM-testable. */
internal object PinCredential {
    private const val SALT_BYTES = 16
    private const val KEY_BITS = 256
    private const val ITERATIONS = 120_000
    private val random = SecureRandom()

    data class Stored(val saltHex: String, val hashHex: String)

    fun create(pin: String): Stored {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        return Stored(salt.toHex(), derive(pin, salt).toHex())
    }

    fun verify(pin: String, stored: Stored): Boolean {
        val salt = stored.saltHex.fromHexOrNull() ?: return false
        val expected = stored.hashHex.fromHexOrNull() ?: return false
        if (salt.size != SALT_BYTES || expected.size != KEY_BITS / 8) return false
        val actual = derive(pin, salt)
        return MessageDigest.isEqual(expected, actual)
    }

    private fun derive(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun String.fromHexOrNull(): ByteArray? {
        if (length % 2 != 0 || any { it !in '0'..'9' && it !in 'a'..'f' }) return null
        return try { chunked(2).map { it.toInt(16).toByte() }.toByteArray() } catch (_: Exception) { null }
    }
}
