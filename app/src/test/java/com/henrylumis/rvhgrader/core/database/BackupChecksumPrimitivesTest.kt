package com.henrylumis.rvhgrader.core.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupChecksumPrimitivesTest {
    @Test
    fun canonicalJsonSortsObjectKeysButPreservesArrayOrder() {
        val first = linkedMapOf<String, Any?>("z" to 1, "a" to listOf("second", "first"))
        val second = linkedMapOf<String, Any?>("a" to listOf("second", "first"), "z" to 1.0)

        assertEquals("{\"a\":[\"second\",\"first\"],\"z\":1}", BackupChecksumPrimitives.canonicalJson(first))
        assertEquals(BackupChecksumPrimitives.canonicalJson(first), BackupChecksumPrimitives.canonicalJson(second))
        assertTrue(
            BackupChecksumPrimitives.canonicalJson(first) !=
                BackupChecksumPrimitives.canonicalJson(linkedMapOf("a" to listOf("first", "second"), "z" to 1))
        )
    }

    @Test
    fun canonicalJsonEscapesQuotesSlashesAndControlCharacters() {
        assertEquals(
            "\"quote:\\\" slash:\\\\ line:\\n tab:\\t\u0001\"".replace("\u0001", "\\u0001"),
            BackupChecksumPrimitives.canonicalJson("quote:\" slash:\\ line:\n tab:\t\u0001")
        )
    }

    @Test
    fun canonicalNumbersNormalizeEquivalentRepresentationsAndNegativeZero() {
        assertEquals("1", BackupChecksumPrimitives.canonicalNumber(1))
        assertEquals("1", BackupChecksumPrimitives.canonicalNumber(1.0))
        assertEquals("1", BackupChecksumPrimitives.canonicalNumber(java.math.BigDecimal("1e0")))
        assertEquals("0", BackupChecksumPrimitives.canonicalNumber(-0.0))
    }

    @Test
    fun canonicalNumberDoesNotExpandExtremeExponentIntoHugeString() {
        assertEquals("1E+100000000", BackupChecksumPrimitives.canonicalNumber(java.math.BigDecimal("1E+100000000")))
    }

    @Test
    fun canonicalJsonRejectsNonStringObjectKeysAndUnsupportedValues() {
        assertTrue(runCatching { BackupChecksumPrimitives.canonicalJson(mapOf(1 to "bad")) }.isFailure)
        assertTrue(runCatching { BackupChecksumPrimitives.canonicalJson(Any()) }.isFailure)
    }

    @Test
    fun sha256MatchesKnownDigest() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            BackupChecksumPrimitives.sha256("abc")
        )
    }
}
