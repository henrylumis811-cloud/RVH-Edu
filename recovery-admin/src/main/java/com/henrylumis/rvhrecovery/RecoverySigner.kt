package com.henrylumis.rvhrecovery

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.crypto.util.PrivateKeyFactory
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Imports the administrator PEM once, validates it against the V1 app public key,
 * then stores only AES-GCM ciphertext protected by a non-exportable Android Keystore key. */
object RecoverySigner {
    private const val PREFS = "rvh_recovery_admin_secure"
    private const val CIPHER_TEXT = "private_key_ciphertext"
    private const val IV_TEXT = "private_key_iv"
    private const val ALIAS = "rvh_recovery_admin_aes_v1"
    private const val DOMAIN = "RVH-EDU-PIN-RECOVERY-V1:"
    private const val EXPECTED_PUBLIC = "xVQmVrJljqFSnkUvYm06vRlp_YrRL1McsqYh879SvnE"

    fun hasKey(context: Context): Boolean = context.getSharedPreferences(PREFS, 0).contains(CIPHER_TEXT)

    fun importPem(context: Context, pem: ByteArray) {
        val text = pem.toString(Charsets.US_ASCII)
        require(text.contains("BEGIN PRIVATE KEY") && text.contains("END PRIVATE KEY")) { "Select the RVH recovery PEM private-key file." }
        val body = text.substringAfter("-----BEGIN PRIVATE KEY-----").substringBefore("-----END PRIVATE KEY-----")
            .filterNot { it.isWhitespace() }
        val der = Base64.decode(body, Base64.DEFAULT)
        val parsed = PrivateKeyFactory.createKey(der)
        require(parsed is Ed25519PrivateKeyParameters) { "The selected file is not an Ed25519 private key." }
        val publicRaw = parsed.generatePublicKey().encoded
        require(b64(publicRaw) == EXPECTED_PUBLIC) { "This key does not belong to this RVH Edu recovery build." }

        val aes = getOrCreateAesKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, aes)
        val encrypted = cipher.doFinal(parsed.encoded)
        val prefs = context.getSharedPreferences(PREFS, 0)
        check(prefs.edit().putString(CIPHER_TEXT, b64(encrypted)).putString(IV_TEXT, b64(cipher.iv)).commit())
    }

    fun sign(context: Context, request: String): String {
        val payloadBytes = decode(request)
        val payload = payloadBytes.toString(Charsets.UTF_8)
        val fields = payload.split('|')
        require(fields.size == 5 && fields[0] == "v1") { "This is not a valid RVH Edu recovery request." }
        require(fields[1].matches(Regex("[0-9a-fA-F-]{36}"))) { "The installation identifier is malformed." }
        val issued = fields[2].toLongOrNull() ?: error("The request timestamp is invalid.")
        val expires = fields[3].toLongOrNull() ?: error("The expiry timestamp is invalid.")
        val now = System.currentTimeMillis() / 1000L
        require(issued <= now + 300 && expires > now && expires - issued == 604800L) { "This request has expired or has an invalid validity period. Ask the teacher to generate a new request." }
        require(decode(fields[4]).size == 32) { "The request nonce is invalid." }

        val prefs = context.getSharedPreferences(PREFS, 0)
        val encrypted = decode(prefs.getString(CIPHER_TEXT, null) ?: error("Import the administrator recovery key first."))
        val iv = decode(prefs.getString(IV_TEXT, null) ?: error("The protected key storage is incomplete."))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateAesKey(), GCMParameterSpec(128, iv))
        val seed = cipher.doFinal(encrypted)
        val privateKey = Ed25519PrivateKeyParameters(seed, 0)
        require(b64(privateKey.generatePublicKey().encoded) == EXPECTED_PUBLIC) { "The protected administrator key failed its identity check." }
        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        val message = (DOMAIN + request).toByteArray(Charsets.UTF_8)
        signer.update(message, 0, message.size)
        return b64(signer.generateSignature())
    }

    private fun getOrCreateAesKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }

    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    private fun b64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}
