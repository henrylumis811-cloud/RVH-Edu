package com.henrylumis.rvhgrader.core.database

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the production backup manager against Android's real SQLite implementation. */
@RunWith(AndroidJUnit4::class)
class AcademicDatabaseBackupManagerInstrumentedTest {
    private lateinit var context: Context

    @Before
    fun resetIsolatedTestDatabase() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("rvh_edu.db")
    }

    @After
    fun cleanUpTestDatabase() {
        context.deleteDatabase("rvh_edu.db")
    }

    @Test
    fun tamperedChecksumIsRejectedBeforeExistingDatabaseIsChanged() {
        val backup = AcademicDatabaseBackupManager.buildJson(context)
        val before = schoolRows(context)
        val digestPattern = Regex("\"contentSha256\"\\s*:\\s*\"[0-9a-f]{64}\"")
        val tampered = digestPattern.replace(backup, "\"contentSha256\": \"${"0".repeat(64)}\"")

        assertNotEquals("Test fixture must actually alter the checksum", backup, tampered)
        try {
            AcademicDatabaseBackupManager.restoreJson(context, tampered)
            throw AssertionError("A backup with a tampered checksum must be rejected")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("integrity check failed", ignoreCase = true))
        }

        assertEquals("Preflight rejection must not delete or replace school records", before, schoolRows(context))
        val helper = RvhEduDatabase(context)
        try {
            AcademicDatabaseIntegrity.requireHealthy(helper.readableDatabase)
        } finally {
            helper.close()
        }
    }

    @Test
    fun validBackupCanBeRestoredByProductionManager() {
        val backup = AcademicDatabaseBackupManager.buildJson(context)
        val before = schoolRows(context)

        AcademicDatabaseBackupManager.restoreJson(context, backup)

        assertEquals("Valid restore must preserve the school rows", before, schoolRows(context))
        val helper = RvhEduDatabase(context)
        try {
            AcademicDatabaseIntegrity.requireHealthy(helper.readableDatabase)
        } finally {
            helper.close()
        }
    }

    private fun schoolRows(context: Context): List<List<String>> {
        val helper = RvhEduDatabase(context)
        try {
            val rows = mutableListOf<List<String>>()
            helper.readableDatabase.rawQuery(
                "SELECT id, name, motto, created_at, updated_at FROM school ORDER BY id", null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    rows += listOf(
                        cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getLong(3).toString(), cursor.getLong(4).toString()
                    )
                }
            }
            return rows
        } finally {
            helper.close()
        }
    }
}
