package com.henrylumis.rvhgrader.online

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/** Privacy-conscious optional online hooks. Offline functionality never depends on these services. */
object OnlineServices {
    // Configure HTTPS endpoints before public release. Manifest JSON: versionCode, versionName, message, downloadUrl.
    // Audience endpoint receives only a random install ID, app version code, and platform; never school records.
    const val RELEASE_MANIFEST_URL = ""
    const val AUDIENCE_ENDPOINT_URL = ""

    data class UpdateResult(val configured: Boolean, val updateAvailable: Boolean = false,
                            val versionName: String = "", val message: String = "",
                            val downloadUrl: String = "", val status: String = "")

    fun analyticsEnabled(context: Context): Boolean = prefs(context).getBoolean("anonymous_audience_opt_in", false)
    fun setAnalyticsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("anonymous_audience_opt_in", enabled).apply()
        if (!enabled) prefs(context).edit().remove("anonymous_install_id").apply()
    }

    fun hasInternetConnection(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        return try {
            if (Build.VERSION.SDK_INT >= 23) {
                val network = manager.activeNetwork ?: return false
                val caps = manager.getNetworkCapabilities(network) ?: return false
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } else {
                @Suppress("DEPRECATION") manager.activeNetworkInfo?.isConnected == true
            }
        } catch (_: Exception) { false }
    }

    suspend fun maybeSendAudienceCheckIn(context: Context, versionCode: Int): Boolean = withContext(Dispatchers.IO) {
        if (!analyticsEnabled(context) || !AUDIENCE_ENDPOINT_URL.startsWith("https://") || !hasInternetConnection(context)) return@withContext false
        val lastSent = prefs(context).getLong("last_audience_checkin", 0L)
        if (System.currentTimeMillis() - lastSent < 7L * 24L * 60L * 60L * 1000L) return@withContext false
        var connection: HttpURLConnection? = null
        try {
            val installId = prefs(context).getString("anonymous_install_id", null) ?: UUID.randomUUID().toString().also {
                prefs(context).edit().putString("anonymous_install_id", it).apply()
            }
            connection = (URL(AUDIENCE_ENDPOINT_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"; connectTimeout = 3500; readTimeout = 3500; doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            val payload = JSONObject().put("installId", installId).put("versionCode", versionCode).put("platform", "android").toString()
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val success = connection.responseCode in 200..299
            if (success) prefs(context).edit().putLong("last_audience_checkin", System.currentTimeMillis()).apply()
            success
        } catch (_: Exception) { false } finally { connection?.disconnect() }
    }

    suspend fun checkForUpdate(currentVersionCode: Int): UpdateResult = withContext(Dispatchers.IO) {
        if (!RELEASE_MANIFEST_URL.startsWith("https://")) return@withContext UpdateResult(false, status = "Update checking will activate when the HTTPS release manifest is configured.")
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(RELEASE_MANIFEST_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"; connectTimeout = 4000; readTimeout = 4000
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode !in 200..299) return@withContext UpdateResult(true, status = "Could not check for updates. Try again later.")
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val latest = json.optInt("versionCode", 0)
            if (latest <= 0) return@withContext UpdateResult(true, status = "The update service returned an invalid version.")
            val available = latest > currentVersionCode
            UpdateResult(true, available, json.optString("versionName"), json.optString("message"), json.optString("downloadUrl"),
                if (available) "An update is available." else "RVH Edu is up to date.")
        } catch (_: Exception) {
            UpdateResult(true, status = "Could not check for updates. RVH Edu will keep working normally.")
        } finally { connection?.disconnect() }
    }

    private fun prefs(context: Context) = context.getSharedPreferences("rvh_online_services", Context.MODE_PRIVATE)
}
