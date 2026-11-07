package com.henrylumis.rvhgrader.core.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.henrylumis.rvhgrader.core.database.RvhEduDatabase
import com.henrylumis.rvhgrader.core.model.LegacyIdentityReview

class LegacyIdentityReviewRepository(private val database: RvhEduDatabase) {
    fun getUnresolved(): List<LegacyIdentityReview> = database.readableDatabase.query(
        "legacy_identity_review", null, "resolved = 0", null, null, null, "normalized_name ASC"
    ).use { c ->
        buildList {
            while (c.moveToNext()) {
                add(LegacyIdentityReview(
                    id = c.getString(c.getColumnIndexOrThrow("id")),
                    normalizedName = c.getString(c.getColumnIndexOrThrow("normalized_name")),
                    learnerId = c.getString(c.getColumnIndexOrThrow("learner_id")),
                    relatedLearnerId = c.getString(c.getColumnIndexOrThrow("related_learner_id")),
                    reason = c.getString(c.getColumnIndexOrThrow("reason")),
                    resolved = c.getInt(c.getColumnIndexOrThrow("resolved")) != 0
                ))
            }
        }
    }

    fun markResolved(id: String) {
        val values = ContentValues().apply { put("resolved", 1) }
        database.writableDatabase.update("legacy_identity_review", values, "id = ?", arrayOf(id))
    }
}
