package com.henrylumis.rvhgrader.ui

import android.content.Context
import android.util.Base64
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.SecureRandom
import java.util.UUID

internal object PinRecovery {
    private const val PREFS = "rvh_pin_recovery"
    private const val INSTALL_ID = "install_id"
    private const val USED_REQUEST = "used_request"
    private const val ACTIVE_REQUEST = "active_request"
    private const val DOMAIN = "RVH-EDU-PIN-RECOVERY-V1:"
    private const val PUBLIC_KEY = "xVQmVrJljqFSnkUvYm06vRlp_YrRL1McsqYh879SvnE"
    private const val VALIDITY_SECONDS = 604800L

    data class Request(val encoded: String, val expiresAt: Long)

    /** Restores the still-valid request after process death, app closure or device restart. */
    fun loadActiveRequest(context: Context): Request? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val encoded = prefs.getString(ACTIVE_REQUEST, null) ?: return null
        val payloadBytes = unb64(encoded)
        val payload = payloadBytes?.toString(Charsets.UTF_8)
        val fields = payload?.split('|')
        val now = System.currentTimeMillis() / 1000L
        val validShape = fields != null && fields.size == 5 && fields[0] == "v1" &&
            run {
                val issued = fields[2].toLongOrNull()
                val expires = fields[3].toLongOrNull()
                issued != null && expires != null && issued <= now + 300 &&
                    expires > now && expires - issued == VALIDITY_SECONDS &&
                    (unb64(fields[4])?.size == 32)
            }
        if (!validShape) {
            // Expired or malformed pending requests are discarded; no PIN or school data is touched.
            prefs.edit().remove(ACTIVE_REQUEST).commit()
            return null
        }
        val installId = prefs.getString(INSTALL_ID, null)
        if (installId == null || fields!![1] != installId) {
            prefs.edit().remove(ACTIVE_REQUEST).commit()
            return null
        }
        return Request(encoded, fields!![3].toLong())
    }

    fun createRequest(context: Context): Request {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val installId = prefs.getString(INSTALL_ID, null) ?: UUID.randomUUID().toString().also {
            check(prefs.edit().putString(INSTALL_ID, it).commit())
        }
        val nonce = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val now = System.currentTimeMillis() / 1000L
        val expires = now + VALIDITY_SECONDS
        val payload = "v1|$installId|$now|$expires|${b64(nonce)}"
        val encoded = b64(payload.toByteArray(Charsets.UTF_8))
        check(prefs.edit().putString(ACTIVE_REQUEST, encoded).commit())
        return Request(encoded, expires)
    }

    fun verifyAndConsume(context: Context, request: String, response: String): Boolean {
        if (request.isBlank() || response.isBlank()) return false
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getString(USED_REQUEST, null) == request) return false
        if (prefs.getString(ACTIVE_REQUEST, null) != request) return false
        val payload = String(unb64(request) ?: return false, Charsets.UTF_8)
        val parts = payload.split('|')
        if (parts.size != 5 || parts[0] != "v1") return false
        val installId = prefs.getString(INSTALL_ID, null) ?: return false
        if (parts[1] != installId) return false
        val issued = parts[2].toLongOrNull() ?: return false
        val expires = parts[3].toLongOrNull() ?: return false
        val now = System.currentTimeMillis() / 1000L
        if (issued > now + 300 || expires <= now || expires - issued != VALIDITY_SECONDS) return false
        if (unb64(parts[4])?.size != 32) return false
        val signature = unb64(response) ?: return false
        if (signature.size != 64) return false
        val pub = unb64(PUBLIC_KEY) ?: return false
        val message = (DOMAIN + request).toByteArray(Charsets.UTF_8)
        val verifier = Ed25519Signer()
        verifier.init(false, Ed25519PublicKeyParameters(pub, 0))
        verifier.update(message, 0, message.size)
        if (!verifier.verifySignature(signature)) return false
        return prefs.edit().putString(USED_REQUEST, request).remove(ACTIVE_REQUEST).commit()
    }

    private fun b64(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    private fun unb64(value: String): ByteArray? =
        try { Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING) } catch (_: Exception) { null }
}
