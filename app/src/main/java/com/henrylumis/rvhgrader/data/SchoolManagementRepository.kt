package com.henrylumis.rvhgrader.data

import android.content.Context
import android.content.ContentValues
import com.henrylumis.rvhgrader.core.database.RvhEduDatabase
import java.util.UUID

/** Offline school directory and active-school preference. Existing records stay in legacy-school. */
data class SchoolRecord(val id: String, val name: String, val motto: String, val createdAt: Long, val updatedAt: Long)

object SchoolManagementRepository {
    private const val PREFS = "rvh_active_school"
    private const val KEY_ACTIVE_SCHOOL = "active_school_id"
    const val LEGACY_SCHOOL_ID = "legacy-school"

    fun list(context: Context): List<SchoolRecord> {
        val helper = RvhEduDatabase(context)
        return try {
            val db = helper.readableDatabase
            db.rawQuery("SELECT id,name,motto,created_at,updated_at FROM school ORDER BY CASE WHEN id='legacy-school' THEN 0 ELSE 1 END, name COLLATE NOCASE", null).use { c ->
                buildList { while (c.moveToNext()) add(SchoolRecord(c.getString(0), c.getString(1), c.getString(2), c.getLong(3), c.getLong(4))) }
            }
        } finally { helper.close() }
    }

    fun activeId(context: Context): String {
        val requested = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ACTIVE_SCHOOL, LEGACY_SCHOOL_ID)
        return list(context).firstOrNull { it.id == requested }?.id ?: LEGACY_SCHOOL_ID
    }

    /** Creates a school; does not copy, move, or merge existing academic records. */
    fun create(context: Context, name: String, motto: String = ""): SchoolRecord {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Enter a school name." }
        require(cleanName.length <= 120) { "School name must be 120 characters or fewer." }
        require(motto.length <= 240) { "School motto must be 240 characters or fewer." }
        val now = System.currentTimeMillis()
        val school = SchoolRecord(UUID.randomUUID().toString(), cleanName, motto.trim(), now, now)
        val helper = RvhEduDatabase(context)
        try {
            val values = ContentValues().apply { put("id", school.id); put("name", school.name); put("motto", school.motto); put("created_at", now); put("updated_at", now) }
            check(helper.writableDatabase.insertOrThrow("school", null, values) != -1L) { "Could not create school." }
        } finally { helper.close() }
        select(context, school.id)
        return school
    }

    fun select(context: Context, schoolId: String) {
        require(list(context).any { it.id == schoolId }) { "That school does not exist on this device." }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ACTIVE_SCHOOL, schoolId).apply()
    }

    fun update(context: Context, schoolId: String, name: String, motto: String = "") {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Enter a school name." }
        require(cleanName.length <= 120 && motto.length <= 240) { "School details are too long." }
        require(schoolId != LEGACY_SCHOOL_ID || cleanName.isNotEmpty())
        val helper = RvhEduDatabase(context)
        try {
            val values = ContentValues().apply { put("name", cleanName); put("motto", motto.trim()); put("updated_at", System.currentTimeMillis()) }
            check(helper.writableDatabase.update("school", values, "id=?", arrayOf(schoolId)) == 1) { "School not found." }
        } finally { helper.close() }
    }
}
