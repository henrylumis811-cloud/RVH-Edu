package com.henrylumis.rvhgrader.core.assessment

import android.content.Context
import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.data.SchoolInfoRepository
import com.henrylumis.rvhgrader.grading.GradingScaleRepository
import com.henrylumis.rvhgrader.model.AggregateBand
import com.henrylumis.rvhgrader.model.DivisionBand
import com.henrylumis.rvhgrader.model.GradingScale
import com.henrylumis.rvhgrader.model.SchoolClass
import org.json.JSONArray
import org.json.JSONObject

/** Immutable, serializable academic-policy record captured when an assessment is first opened. */
data class AssessmentPolicySnapshot(
    val schemaVersion: Int,
    val schoolClass: String,
    val capturedAtMillis: Long,
    val subjects: List<Subject>,
    val aggregateBands: List<AggregateBand> = GradingScale.default().aggregateBands,
    val divisionBands: List<DivisionBand> = GradingScale.default().divisionBands,
    val classPassPercent: Double = 50.0
) {
    data class Subject(val code: String, val shortName: String, val displayName: String)

    fun toJson(): String = JSONObject().apply {
        put("schemaVersion", schemaVersion)
        put("schoolClass", schoolClass)
        put("capturedAtMillis", capturedAtMillis)
        put("subjects", JSONArray().apply { subjects.forEach { put(JSONObject().put("code", it.code).put("shortName", it.shortName).put("displayName", it.displayName)) } })
        put("aggregateBands", JSONArray().apply { aggregateBands.forEach { put(JSONObject().put("minMark", it.minMark).put("aggregate", it.aggregate)) } })
        put("divisionBands", JSONArray().apply { divisionBands.forEach { put(JSONObject().put("maxAggSum", it.maxAggSum).put("label", it.label)) } })
        put("classPassPercent", classPassPercent)
    }.toString()

    /** Schema 1 snapshots are accepted as legacy subject-only snapshots; they are not upgraded silently. */
    val hasCompleteScoringPolicy: Boolean get() = schemaVersion >= 2
    fun gradingScale(): GradingScale = GradingScale(aggregateBands, divisionBands)

    companion object {
        const val CURRENT_SCHEMA_VERSION = 2

        /**
         * Validates whether a class-wide operation can safely combine saved assessment policies.
         * Capture timestamps are intentionally excluded: equivalent policies captured at different
         * times remain compatible. All-legacy classes are allowed, but legacy and pinned policies
         * must never be silently combined.
         */
        fun classCompatibilityError(policyJsons: Collection<String?>): String? {
            val parsed = policyJsons.map { json ->
                if (json.isNullOrBlank()) null
                else runCatching { parse(json) }.getOrElse {
                    return "A learner's saved assessment policy is invalid. Class results were stopped to protect accuracy."
                }
            }
            val pinned = parsed.filterNotNull()
            if (pinned.isEmpty()) return null // Entire class uses legacy, unpinned records.
            if (pinned.size != parsed.size) {
                return "This class contains both legacy assessments and assessments with saved policies. Review the policy differences before combining results."
            }
            fun signature(policy: AssessmentPolicySnapshot) = listOf(
                policy.schoolClass,
                policy.subjects,
                policy.aggregateBands,
                policy.divisionBands,
                policy.classPassPercent,
                policy.hasCompleteScoringPolicy
            )
            if (pinned.map(::signature).distinct().size > 1) {
                return "Learners in this class have different saved academic policies. Combined rankings and analytics were stopped to avoid misleading results."
            }
            return null
        }

        /** Detailed teacher-facing variant that identifies records needing review. */
        fun classCompatibilityErrorForLearners(entries: List<Pair<String, String?>>): String? {
            val baseError = classCompatibilityError(entries.map { it.second }) ?: return null
            // Keep each diagnostic tied to its original row index. Learner names are not unique,
            // so looking records up again by display name can accidentally diagnose the wrong row.
            data class ParsedEntry(val index: Int, val label: String, val raw: String?, val policy: AssessmentPolicySnapshot?)
            val parsed = entries.mapIndexed { index, (label, raw) ->
                val policy = if (raw.isNullOrBlank()) null else runCatching { parse(raw) }.getOrNull()
                ParsedEntry(index, label, raw, policy)
            }
            fun signature(policy: AssessmentPolicySnapshot) = listOf(
                policy.schoolClass, policy.subjects, policy.aggregateBands, policy.divisionBands,
                policy.classPassPercent, policy.hasCompleteScoringPolicy
            )
            val hasPinned = parsed.any { it.policy != null }
            val hasMissingOrInvalid = parsed.any { it.raw.isNullOrBlank() || (it.raw.isNotBlank() && it.policy == null) }
            val named = when {
                hasPinned && hasMissingOrInvalid -> parsed.filter {
                    it.raw.isNullOrBlank() || (it.raw.isNotBlank() && it.policy == null)
                }.map { it.label }
                else -> {
                    val pinned = parsed.filter { it.policy != null }
                    // If multiple signatures exist, every pinned record belongs to a policy group
                    // participating in the conflict, including the first/reference group.
                    if (pinned.map { signature(it.policy!!) }.distinct().size > 1) {
                        pinned.map { it.label }
                    } else emptyList()
                }
            }.distinct()
            return if (named.isEmpty()) baseError else
                "$baseError Learners to review: ${named.take(8).joinToString(", ")}${if (named.size > 8) ", and ${named.size - 8} more" else ""}."
        }

        fun capture(context: Context, schoolClass: SchoolClass, nowMillis: Long = System.currentTimeMillis()): AssessmentPolicySnapshot {
            val scale = GradingScaleRepository.load(context)
            require(scale.validationError() == null) { scale.validationError() ?: "Invalid grading scale." }
            return AssessmentPolicySnapshot(
                schemaVersion = CURRENT_SCHEMA_VERSION,
                schoolClass = schoolClass.name,
                capturedAtMillis = nowMillis,
                subjects = SubjectCatalog.forClass(schoolClass).map { Subject(it.code, it.shortName, it.displayName) },
                aggregateBands = scale.aggregateBands.toList(),
                divisionBands = scale.divisionBands.toList(),
                classPassPercent = SchoolInfoRepository.loadClassPassPercent(context)
            )
        }

        fun parse(json: String): AssessmentPolicySnapshot {
            val root = JSONObject(json)
            val version = root.getInt("schemaVersion")
            require(version in 1..CURRENT_SCHEMA_VERSION) { "Unsupported assessment policy snapshot version: $version" }
            val array = root.getJSONArray("subjects")
            val subjects = (0 until array.length()).map { i ->
                val item = array.getJSONObject(i)
                Subject(item.getString("code").trim(), item.getString("shortName").trim(), item.getString("displayName").trim())
            }
            require(subjects.isNotEmpty()) { "Assessment policy snapshot has no subjects" }
            require(subjects.all { it.code.isNotBlank() && it.shortName.isNotBlank() && it.displayName.isNotBlank() }) {
                "Assessment policy snapshot contains a subject with missing identity or display text"
            }
            require(subjects.map { it.code.uppercase() }.distinct().size == subjects.size) { "Assessment policy snapshot has duplicate subject codes" }
            val defaults = GradingScale.default()
            val aggregates = if (root.has("aggregateBands")) {
                val a = root.getJSONArray("aggregateBands")
                (0 until a.length()).map { i -> a.getJSONObject(i).let { AggregateBand(it.getInt("minMark"), it.getInt("aggregate")) } }
            } else defaults.aggregateBands
            val divisions = if (root.has("divisionBands")) {
                val a = root.getJSONArray("divisionBands")
                (0 until a.length()).map { i -> a.getJSONObject(i).let { DivisionBand(it.getInt("maxAggSum"), it.getString("label")) } }
            } else defaults.divisionBands
            val pass = if (root.has("classPassPercent")) root.getDouble("classPassPercent") else 50.0
            require(pass.isFinite() && pass in 0.0..100.0) { "Invalid pass threshold in policy snapshot" }
            val schoolClass = root.getString("schoolClass").trim()
            require(SchoolClass.values().any { it.name == schoolClass }) { "Unknown school class in assessment policy snapshot" }
            val capturedAt = root.getLong("capturedAtMillis")
            require(capturedAt >= 0L) { "Invalid capture timestamp in assessment policy snapshot" }
            require(aggregates.isNotEmpty()) { "Assessment policy snapshot has no aggregate bands" }
            require(divisions.isNotEmpty()) { "Assessment policy snapshot has no division bands" }
            require(divisions.all { it.label.isNotBlank() }) { "Assessment policy snapshot contains an empty division label" }
            val scale = GradingScale(aggregates, divisions)
            require(scale.validationError() == null) { scale.validationError() ?: "Invalid grading scale in snapshot" }
            return AssessmentPolicySnapshot(version, schoolClass, capturedAt, subjects, aggregates, divisions, pass)
        }
    }
}
