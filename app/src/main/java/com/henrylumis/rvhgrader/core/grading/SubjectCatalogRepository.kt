package com.henrylumis.rvhgrader.core.grading

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Device-local subject policy storage with an append-only revision history. */
object SubjectCatalogRepository {
    private const val PREFS = "rvh_school_subjects"
    private const val KEY = "catalog_v1" // retained for upgrades from earlier releases
    private const val HISTORY_KEY = "catalog_history_v1"
    private const val MAX_REVISIONS = 50

    data class Revision(
        val revision: Int,
        val savedAtMillis: Long,
        val lower: List<SubjectCatalog.Subject>,
        val upper: List<SubjectCatalog.Subject>
    )

    fun loadAndApply(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, null) ?: return
        runCatching {
            val root = JSONObject(raw)
            val lower = decode(root.getJSONArray("lower"))
            val upper = decode(root.getJSONArray("upper"))
            SubjectCatalog.configure(lower, upper)
        }
    }

    /** Save a new revision only when the effective subject configuration actually changes. */
    @Synchronized
    fun save(context: Context, lower: List<SubjectCatalog.Subject>, upper: List<SubjectCatalog.Subject>) {
        require(SubjectCatalog.validate(lower, upper) == null) {
            SubjectCatalog.validate(lower, upper) ?: "Invalid subject configuration."
        }
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val oldLower = SubjectCatalog.forMode(com.henrylumis.rvhgrader.model.SystemMode.LOWER)
        val oldUpper = SubjectCatalog.forMode(com.henrylumis.rvhgrader.model.SystemMode.UPPER)
        val history = history(context).toMutableList()
        if (history.isEmpty()) {
            // Seed history with the active configuration before recording the first change.
            history += Revision(1, System.currentTimeMillis(), oldLower, oldUpper)
        }
        val unchanged = oldLower == lower && oldUpper == upper
        if (!unchanged) {
            val nextRevision = (history.maxOfOrNull { it.revision } ?: 0) + 1
            history += Revision(nextRevision, System.currentTimeMillis(), lower.toList(), upper.toList())
        }
        val current = JSONObject().put("lower", encode(lower)).put("upper", encode(upper))
        val updatedHistory = JSONArray().also { array ->
            history.takeLast(MAX_REVISIONS).forEach { revision -> array.put(encodeRevision(revision)) }
        }
        check(prefs.edit().putString(KEY, current.toString()).putString(HISTORY_KEY, updatedHistory.toString()).commit()) {
            "Could not save the school subject configuration."
        }
        SubjectCatalog.configure(lower, upper)
    }

    fun history(context: Context): List<Revision> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(HISTORY_KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { index -> decodeRevision(array.getJSONObject(index)) }
                .sortedByDescending { it.revision }
        }.getOrDefault(emptyList())
    }

    /** Restoring is a new revision, preserving the intervening configurations in history. */
    fun restore(context: Context, revision: Int) {
        val selected = history(context).firstOrNull { it.revision == revision }
            ?: error("That subject revision is no longer available.")
        save(context, selected.lower, selected.upper)
    }

    fun reset(context: Context) {
        val lower = SubjectCatalog.defaultSubjects(com.henrylumis.rvhgrader.model.SystemMode.LOWER)
        val upper = SubjectCatalog.defaultSubjects(com.henrylumis.rvhgrader.model.SystemMode.UPPER)
        save(context, lower, upper)
    }

    private fun encode(subjects: List<SubjectCatalog.Subject>) = JSONArray().also { array ->
        subjects.forEach { subject ->
            array.put(JSONObject().put("code", subject.code).put("shortName", subject.shortName).put("displayName", subject.displayName))
        }
    }

    private fun decode(array: JSONArray): List<SubjectCatalog.Subject> = (0 until array.length()).map { index ->
        val item = array.getJSONObject(index)
        SubjectCatalog.Subject(item.getString("code"), item.getString("shortName"), item.getString("displayName"))
    }

    private fun encodeRevision(revision: Revision) = JSONObject()
        .put("revision", revision.revision)
        .put("savedAtMillis", revision.savedAtMillis)
        .put("lower", encode(revision.lower))
        .put("upper", encode(revision.upper))

    private fun decodeRevision(item: JSONObject) = Revision(
        revision = item.getInt("revision"),
        savedAtMillis = item.getLong("savedAtMillis"),
        lower = decode(item.getJSONArray("lower")),
        upper = decode(item.getJSONArray("upper"))
    )
}
