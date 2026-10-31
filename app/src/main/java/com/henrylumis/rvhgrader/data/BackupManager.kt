package com.henrylumis.rvhgrader.data

import android.content.Context
import android.net.Uri
import com.henrylumis.rvhgrader.grading.GradingScaleRepository
import com.henrylumis.rvhgrader.model.GradingScale
import com.henrylumis.rvhgrader.model.StudentRecord
import org.json.JSONObject

data class BackupPayload(
    val records: List<StudentRecord>,
    val gradingScale: GradingScale,
    val schoolName: String
)

/**
 * Writes/reads a single portable backup file containing the whole gradebook, the teacher's
 * custom grading scale, and the school name — separate from the silent autosave in
 * [GradebookRepository], this is the one meant to be shared, moved to a new phone, or kept
 * somewhere safe. Deliberately contains only school data, grading configuration, and school identity — no external
 * service credentials or device-specific secrets are part of the backup format.
 */
object BackupManager {

    private const val BACKUP_VERSION = 2

    fun buildBackupJson(records: List<StudentRecord>, gradingScale: GradingScale, schoolName: String): String {
        val root = JSONObject()
        root.put("backupVersion", BACKUP_VERSION)
        root.put("exportedAtMillis", System.currentTimeMillis())
        root.put("schoolName", schoolName)
        root.put("records", org.json.JSONArray(GradebookRepository.recordsToJson(records)))
        root.put("gradingScale", JSONObject(GradingScaleRepository.toJson(gradingScale)))
        return root.toString(2)
    }

    fun parseBackupJson(json: String): BackupPayload {
        val root = JSONObject(json)
        val records = GradebookRepository.recordsFromJson(root.getJSONArray("records").toString())
        val gradingScale = GradingScaleRepository.fromJson(root.getJSONObject("gradingScale").toString())
        val schoolName = root.optString("schoolName", "")
        return BackupPayload(records, gradingScale, schoolName)
    }

    fun writeTo(context: Context, uri: Uri, records: List<StudentRecord>, gradingScale: GradingScale, schoolName: String) {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(buildBackupJson(records, gradingScale, schoolName).toByteArray())
        }
    }

    fun readFrom(context: Context, uri: Uri): BackupPayload {
        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            ?: throw IllegalStateException("Could not read the selected file.")
        return parseBackupJson(text)
    }
}
