package com.henrylumis.rvhgrader.core.database

/** Pure-JVM checksum primitives, kept separate so the actual production logic can be tested without Android. */
internal object BackupChecksumPrimitives {
    /** Canonical JSON for a JSON-shaped tree (Map, List, String, Boolean, Number, or null).
     * Object keys are sorted, array order is preserved, and strings are escaped per JSON rules.
     * This is the production serializer used by backup checksum verification.
     */
    fun canonicalJson(value: Any?): String = when (value) {
        null -> "null"
        is Map<*, *> -> {
            require(value.keys.all { it is String }) { "Canonical JSON object keys must be strings." }
            value.keys.map { it as String }.sorted().joinToString(prefix = "{", postfix = "}", separator = ",") { key ->
                quoteJsonString(key) + ":" + canonicalJson(value[key])
            }
        }
        is List<*> -> value.joinToString(prefix = "[", postfix = "]", separator = ",") { canonicalJson(it) }
        is String -> quoteJsonString(value)
        is Boolean -> value.toString()
        is Number -> canonicalNumber(value)
        else -> error("Unsupported value in backup checksum.")
    }

    private fun quoteJsonString(value: String): String = buildString {
        append('"')
        value.forEach { ch ->
            when (ch) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (ch.code < 0x20) append("\\u%04x".format(ch.code)) else append(ch)
            }
        }
        append('"')
    }

    /** Normalize equivalent JSON number spellings such as 1, 1.0 and 1e0. */
    fun canonicalNumber(value: Number): String {
        val parsed = try {
            java.math.BigDecimal(value.toString())
        } catch (failure: NumberFormatException) {
            throw IllegalArgumentException("Non-finite or invalid number in backup checksum.", failure)
        }
        val normalized = parsed.stripTrailingZeros()
        // Keep exponent notation when needed. toPlainString() can expand a short value such as
        // 1e100000000 into an enormous string, creating avoidable memory pressure on import.
        return if (normalized.compareTo(java.math.BigDecimal.ZERO) == 0) "0" else normalized.toString()
    }

    fun sha256(value: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }
}
