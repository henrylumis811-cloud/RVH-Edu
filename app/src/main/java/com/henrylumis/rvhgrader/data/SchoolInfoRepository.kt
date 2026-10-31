package com.henrylumis.rvhgrader.data

import android.content.ContentValues
import android.content.Context
import com.henrylumis.rvhgrader.core.database.RvhEduDatabase

/** School branding is stored on the selected school; thresholds are kept separately per school. */
object SchoolInfoRepository {
    private const val PREFS = "rvh_school_info"
    private const val KEY_CLASS_PASS_PERCENT = "class_pass_percent"

    fun loadSchoolName(context: Context): String {
        val id = SchoolManagementRepository.activeId(context)
        val helper = RvhEduDatabase(context)
        return try {
            helper.readableDatabase.rawQuery("SELECT name FROM school WHERE id=?", arrayOf(id)).use { c -> if (c.moveToFirst()) c.getString(0).orEmpty() else "" }
        } finally { helper.close() }
    }

    fun loadClassPassPercent(context: Context): Double = context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString("${KEY_CLASS_PASS_PERCENT}_${SchoolManagementRepository.activeId(context)}", null)?.toDoubleOrNull()
        ?.takeIf { it.isFinite() && it in 0.0..100.0 }
        ?: context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_CLASS_PASS_PERCENT, "50")
            ?.toDoubleOrNull()?.takeIf { it.isFinite() && it in 0.0..100.0 } ?: 50.0

    fun saveClassPassPercent(context: Context, percent: Double) {
        require(percent.isFinite() && percent in 0.0..100.0) { "Pass threshold must be between 0 and 100." }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("${KEY_CLASS_PASS_PERCENT}_${SchoolManagementRepository.activeId(context)}", percent.toString()).apply()
    }

    fun saveSchoolName(context: Context, name: String) {
        val id = SchoolManagementRepository.activeId(context)
        val helper = RvhEduDatabase(context)
        try {
            val values = ContentValues().apply { put("name", name.trim()); put("updated_at", System.currentTimeMillis()) }
            check(helper.writableDatabase.update("school", values, "id=?", arrayOf(id)) == 1) { "Active school was not found." }
        } finally { helper.close() }
    }
}
