package com.henrylumis.rvhgrader.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** RVH Edu's durable local academic store. */
class RvhEduDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext, DB_NAME, null, DB_VERSION
) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createCoreTables(db)
        createMultiSchoolTables(db)
        seedLegacySchool(db)
        createSchoolAdmissionNumbersTable(db)
        createSchoolLearnerStatusTable(db)
        createIndexes(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // v1 already allowed nullable admission numbers in fresh installs. This upgrade
            // handles databases created by the earlier foundation checkpoint where the column
            // may have been NOT NULL.
            db.execSQL("ALTER TABLE learner RENAME TO learner_v1")
            createLearnerTable(db)
            db.execSQL("""
                INSERT INTO learner (id, admission_number, full_name, age_years, gender, date_of_birth, photo_uri, status, created_at, updated_at)
                SELECT id, admission_number, full_name, age_years, gender, date_of_birth, photo_uri, status, created_at, updated_at
                FROM learner_v1
            """.trimIndent())
            db.execSQL("DROP TABLE learner_v1")
        }
        if (oldVersion < 3) {
            createLegacyIdentityReviewTable(db)
            createIndexes(db)
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE assessment ADD COLUMN status TEXT NOT NULL DEFAULT 'DRAFT'")
            db.execSQL("ALTER TABLE assessment ADD COLUMN finalized_at INTEGER")
        }
        if (oldVersion < 5) {
            // Existing assessments remain null: we must not pretend today's subject rules were their historical rules.
            db.execSQL("ALTER TABLE assessment ADD COLUMN policy_snapshot_json TEXT")
        }
        if (oldVersion < 6) {
            createLearnerStatusHistoryTable(db)
        }
        if (oldVersion < 7) {
            // Additive migration: preserve all existing learner, enrollment, assessment and mark IDs.
            // Existing records are assigned to one stable legacy school, never copied or merged.
            seedLegacySchool(db)
            db.execSQL("ALTER TABLE enrollment ADD COLUMN school_id TEXT NOT NULL DEFAULT 'legacy-school'")
            createMultiSchoolTables(db)
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_enrollment_school_year_class ON enrollment(school_id, academic_year, school_class)")
        }
        if (oldVersion < 8) {
            // Rebuild the dependent academic tables together so the old global enrollment
            // uniqueness rule can be replaced without losing assessments or marks.
            rebuildAcademicTablesForSchoolScopedUniqueness(db)
        }
        if (oldVersion < 9) {
            // Keep legacy admission numbers, but move their meaning to a school-owned assignment.
            // No learner/enrollment/assessment/mark IDs are changed by this additive migration.
            createSchoolAdmissionNumbersTable(db)
            backfillSchoolAdmissionNumbersFromLegacyColumn(db)
        }
        if (oldVersion < 10) {
            createSchoolLearnerStatusTable(db)
            backfillSchoolLearnerStatuses(db)
            if (oldVersion >= 6) {
                db.execSQL("ALTER TABLE learner_status_history ADD COLUMN school_id TEXT NOT NULL DEFAULT 'legacy-school'")
            }
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_learner_status_history_school ON learner_status_history(school_id, changed_at DESC)")
            // SQLiteOpenHelper wraps onUpgrade in a transaction. Fail the upgrade if the
            // backfill did not produce a complete school-local lifecycle model; this rolls
            // the migration back instead of opening a partially migrated academic database.
            verifySchoolLearnerStatusMigration(db)
        }
    }

    private fun verifySchoolLearnerStatusMigration(db: SQLiteDatabase) {
        val missingEnrollmentStatuses = db.rawQuery("""
            SELECT COUNT(*) FROM enrollment e
            LEFT JOIN school_learner_status sls
              ON sls.school_id = e.school_id AND sls.learner_id = e.learner_id
            WHERE sls.learner_id IS NULL
        """.trimIndent(), null).use { cursor ->
            cursor.moveToFirst()
            cursor.getLong(0)
        }
        check(missingEnrollmentStatuses == 0L) {
            "RVH Edu migration could not create school-specific status for every enrollment. Existing data was not committed."
        }

        val missingLegacyStatuses = db.rawQuery("""
            SELECT COUNT(*) FROM learner l
            WHERE NOT EXISTS (SELECT 1 FROM enrollment e WHERE e.learner_id = l.id)
              AND NOT EXISTS (
                SELECT 1 FROM school_learner_status sls
                WHERE sls.school_id = 'legacy-school' AND sls.learner_id = l.id
              )
        """.trimIndent(), null).use { cursor ->
            cursor.moveToFirst()
            cursor.getLong(0)
        }
        check(missingLegacyStatuses == 0L) {
            "RVH Edu migration could not preserve status for learners without enrollments. Existing data was not committed."
        }

        val historyWithoutSchool = db.rawQuery("""
            SELECT COUNT(*) FROM learner_status_history h
            LEFT JOIN school s ON s.id = h.school_id
            WHERE s.id IS NULL
        """.trimIndent(), null).use { cursor ->
            cursor.moveToFirst()
            cursor.getLong(0)
        }
        check(historyWithoutSchool == 0L) {
            "RVH Edu migration found learner-status history referencing an unknown school. Existing data was not committed."
        }
    }

    private fun createCoreTables(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE school (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL DEFAULT '',
                motto TEXT NOT NULL DEFAULT '',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """.trimIndent())
        createLearnerTable(db)
        db.execSQL("""
            CREATE TABLE enrollment (
                id TEXT PRIMARY KEY NOT NULL,
                school_id TEXT NOT NULL DEFAULT 'legacy-school',
                learner_id TEXT NOT NULL,
                academic_year INTEGER NOT NULL,
                school_class TEXT NOT NULL,
                stream TEXT,
                enrolled_at INTEGER NOT NULL,
                FOREIGN KEY(school_id) REFERENCES school(id),
                FOREIGN KEY(learner_id) REFERENCES learner(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE assessment (
                id TEXT PRIMARY KEY NOT NULL,
                enrollment_id TEXT NOT NULL,
                term TEXT NOT NULL,
                section TEXT NOT NULL,
                recorded_at INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'DRAFT',
                finalized_at INTEGER,
                policy_snapshot_json TEXT,
                UNIQUE(enrollment_id, term, section),
                FOREIGN KEY(enrollment_id) REFERENCES enrollment(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE mark (
                id TEXT PRIMARY KEY NOT NULL,
                assessment_id TEXT NOT NULL,
                subject_code TEXT NOT NULL,
                mark INTEGER NOT NULL,
                max_mark INTEGER NOT NULL DEFAULT 100,
                aggregate INTEGER,
                source TEXT NOT NULL DEFAULT 'MANUAL',
                updated_at INTEGER NOT NULL,
                UNIQUE(assessment_id, subject_code),
                FOREIGN KEY(assessment_id) REFERENCES assessment(id) ON DELETE CASCADE,
                CHECK(mark >= 0), CHECK(max_mark > 0), CHECK(mark <= max_mark)
            )
        """.trimIndent())
        createLegacyIdentityReviewTable(db)
        createLearnerStatusHistoryTable(db)
        createMultiSchoolTables(db)
        seedLegacySchool(db)
    }

    /** Stable identities and time-bounded school relationships; no online service is required. */

    /**
     * Rebuilds only the three dependent academic tables. Child tables are copied first and
     * dropped before their parent; stable IDs and every persisted column are retained.
     * The enclosing SQLiteOpenHelper upgrade transaction makes this all-or-nothing.
     */
    private fun rebuildAcademicTablesForSchoolScopedUniqueness(db: SQLiteDatabase) {
        db.execSQL("CREATE TEMP TABLE mark_v7_copy AS SELECT id, assessment_id, subject_code, mark, max_mark, aggregate, source, updated_at FROM mark")
        db.execSQL("CREATE TEMP TABLE assessment_v7_copy AS SELECT id, enrollment_id, term, section, recorded_at, status, finalized_at, policy_snapshot_json FROM assessment")
        db.execSQL("CREATE TEMP TABLE enrollment_v7_copy AS SELECT id, school_id, learner_id, academic_year, school_class, stream, enrolled_at FROM enrollment")

        db.execSQL("DROP TABLE mark")
        db.execSQL("DROP TABLE assessment")
        db.execSQL("DROP TABLE enrollment")

        db.execSQL("""
            CREATE TABLE enrollment (
                id TEXT PRIMARY KEY NOT NULL,
                school_id TEXT NOT NULL DEFAULT 'legacy-school',
                learner_id TEXT NOT NULL,
                academic_year INTEGER NOT NULL,
                school_class TEXT NOT NULL,
                stream TEXT,
                enrolled_at INTEGER NOT NULL,
                FOREIGN KEY(school_id) REFERENCES school(id),
                FOREIGN KEY(learner_id) REFERENCES learner(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE assessment (
                id TEXT PRIMARY KEY NOT NULL,
                enrollment_id TEXT NOT NULL,
                term TEXT NOT NULL,
                section TEXT NOT NULL,
                recorded_at INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'DRAFT',
                finalized_at INTEGER,
                policy_snapshot_json TEXT,
                UNIQUE(enrollment_id, term, section),
                FOREIGN KEY(enrollment_id) REFERENCES enrollment(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE mark (
                id TEXT PRIMARY KEY NOT NULL,
                assessment_id TEXT NOT NULL,
                subject_code TEXT NOT NULL,
                mark INTEGER NOT NULL,
                max_mark INTEGER NOT NULL DEFAULT 100,
                aggregate INTEGER,
                source TEXT NOT NULL DEFAULT 'MANUAL',
                updated_at INTEGER NOT NULL,
                UNIQUE(assessment_id, subject_code),
                FOREIGN KEY(assessment_id) REFERENCES assessment(id) ON DELETE CASCADE,
                CHECK(mark >= 0), CHECK(max_mark > 0), CHECK(mark <= max_mark)
            )
        """.trimIndent())

        db.execSQL("INSERT INTO enrollment (id, school_id, learner_id, academic_year, school_class, stream, enrolled_at) SELECT id, school_id, learner_id, academic_year, school_class, stream, enrolled_at FROM enrollment_v7_copy")
        db.execSQL("INSERT INTO assessment (id, enrollment_id, term, section, recorded_at, status, finalized_at, policy_snapshot_json) SELECT id, enrollment_id, term, section, recorded_at, status, finalized_at, policy_snapshot_json FROM assessment_v7_copy")
        db.execSQL("INSERT INTO mark (id, assessment_id, subject_code, mark, max_mark, aggregate, source, updated_at) SELECT id, assessment_id, subject_code, mark, max_mark, aggregate, source, updated_at FROM mark_v7_copy")
        db.execSQL("DROP TABLE mark_v7_copy")
        db.execSQL("DROP TABLE assessment_v7_copy")
        db.execSQL("DROP TABLE enrollment_v7_copy")
        createIndexes(db)
    }

    private fun createMultiSchoolTables(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS teacher (
                id TEXT PRIMARY KEY NOT NULL,
                display_name TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS teacher_school_membership (
                id TEXT PRIMARY KEY NOT NULL,
                teacher_id TEXT NOT NULL,
                school_id TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'TEACHER',
                started_at INTEGER NOT NULL,
                ended_at INTEGER,
                status TEXT NOT NULL DEFAULT 'ACTIVE',
                FOREIGN KEY(teacher_id) REFERENCES teacher(id) ON DELETE CASCADE,
                FOREIGN KEY(school_id) REFERENCES school(id) ON DELETE CASCADE,
                CHECK(ended_at IS NULL OR ended_at >= started_at),
                CHECK(status IN ('ACTIVE','ENDED','SUSPENDED'))
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_membership_teacher_status ON teacher_school_membership(teacher_id, status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_membership_school_status ON teacher_school_membership(school_id, status)")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS teaching_assignment (
                id TEXT PRIMARY KEY NOT NULL,
                membership_id TEXT NOT NULL,
                academic_year INTEGER NOT NULL,
                school_class TEXT NOT NULL,
                subject_code TEXT,
                started_at INTEGER NOT NULL,
                ended_at INTEGER,
                FOREIGN KEY(membership_id) REFERENCES teacher_school_membership(id) ON DELETE CASCADE,
                CHECK(ended_at IS NULL OR ended_at >= started_at)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_teaching_assignment_membership_year ON teaching_assignment(membership_id, academic_year)")
    }

    private fun seedLegacySchool(db: SQLiteDatabase) {
        db.execSQL("INSERT OR IGNORE INTO school (id, name, motto, created_at, updated_at) VALUES ('legacy-school', '', '', strftime('%s','now') * 1000, strftime('%s','now') * 1000)")
    }

    private fun createLearnerStatusHistoryTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS learner_status_history (
                id TEXT PRIMARY KEY NOT NULL,
                learner_id TEXT NOT NULL,
                school_id TEXT NOT NULL DEFAULT 'legacy-school',
                from_status TEXT NOT NULL,
                to_status TEXT NOT NULL,
                reason TEXT NOT NULL,
                note TEXT,
                changed_at INTEGER NOT NULL,
                FOREIGN KEY(learner_id) REFERENCES learner(id)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_learner_status_history ON learner_status_history(learner_id, changed_at DESC)")
    }

    private fun createLearnerTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE learner (
                id TEXT PRIMARY KEY NOT NULL,
                admission_number TEXT UNIQUE,
                full_name TEXT NOT NULL,
                age_years INTEGER,
                gender TEXT,
                date_of_birth TEXT,
                photo_uri TEXT,
                status TEXT NOT NULL DEFAULT 'ACTIVE',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """.trimIndent())
    }

    private fun createSchoolAdmissionNumbersTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS school_admission_number (
                id TEXT PRIMARY KEY NOT NULL,
                school_id TEXT NOT NULL,
                learner_id TEXT NOT NULL,
                admission_number TEXT NOT NULL,
                normalized_admission_number TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                UNIQUE(school_id, learner_id),
                FOREIGN KEY(school_id) REFERENCES school(id) ON DELETE CASCADE,
                FOREIGN KEY(learner_id) REFERENCES learner(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_school_admission_learner ON school_admission_number(learner_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_school_admission_normalized ON school_admission_number(school_id, normalized_admission_number)")
    }

    /** Move old shared learner admission values to every school where that learner is enrolled. */
    private fun backfillSchoolAdmissionNumbersFromLegacyColumn(db: SQLiteDatabase) {
        val rows = mutableListOf<Triple<String, String, String>>()
        db.rawQuery("SELECT id, admission_number FROM learner WHERE admission_number IS NOT NULL AND TRIM(admission_number) <> ''", null).use { c ->
            while (c.moveToNext()) rows += Triple(c.getString(0), c.getString(1), c.getString(0))
        }
        rows.forEach { (learnerId, rawAdmission, _) ->
            val admission = rawAdmission.trim().replace(Regex("\\s+"), " ")
            val normalized = admission.uppercase(java.util.Locale.ROOT)
            val schoolIds = mutableListOf<String>()
            db.rawQuery("SELECT DISTINCT school_id FROM enrollment WHERE learner_id = ?", arrayOf(learnerId)).use { c ->
                while (c.moveToNext()) schoolIds += c.getString(0)
            }
            if (schoolIds.isEmpty()) schoolIds += "legacy-school"
            schoolIds.forEach { schoolId ->
                db.execSQL(
                    "INSERT OR IGNORE INTO school_admission_number (id, school_id, learner_id, admission_number, normalized_admission_number, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(java.util.UUID.randomUUID().toString(), schoolId, learnerId, admission, normalized, System.currentTimeMillis(), System.currentTimeMillis())
                )
            }
        }
        // The old UNIQUE column remains for on-disk compatibility but is no longer authoritative.
        db.execSQL("UPDATE learner SET admission_number = NULL WHERE admission_number IS NOT NULL")
    }

    private fun createSchoolLearnerStatusTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS school_learner_status (
                school_id TEXT NOT NULL,
                learner_id TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE','ARCHIVED')),
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY(school_id, learner_id),
                FOREIGN KEY(school_id) REFERENCES school(id) ON DELETE CASCADE,
                FOREIGN KEY(learner_id) REFERENCES learner(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_school_learner_status ON school_learner_status(school_id, status)")
    }

    private fun backfillSchoolLearnerStatuses(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        db.execSQL("""
            INSERT OR IGNORE INTO school_learner_status(school_id, learner_id, status, created_at, updated_at)
            SELECT e.school_id, l.id, l.status, ?, ?
            FROM learner l JOIN enrollment e ON e.learner_id = l.id
            GROUP BY e.school_id, l.id
        """.trimIndent(), arrayOf(now, now))
        db.execSQL("""
            INSERT OR IGNORE INTO school_learner_status(school_id, learner_id, status, created_at, updated_at)
            SELECT 'legacy-school', l.id, l.status, ?, ? FROM learner l
            WHERE NOT EXISTS (SELECT 1 FROM enrollment e WHERE e.learner_id = l.id)
        """.trimIndent(), arrayOf(now, now))
    }

    private fun createLegacyIdentityReviewTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS legacy_identity_review (
                id TEXT PRIMARY KEY NOT NULL,
                normalized_name TEXT NOT NULL,
                learner_id TEXT NOT NULL,
                related_learner_id TEXT NOT NULL,
                reason TEXT NOT NULL,
                resolved INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                UNIQUE(learner_id, related_learner_id, reason),
                FOREIGN KEY(learner_id) REFERENCES learner(id) ON DELETE CASCADE,
                FOREIGN KEY(related_learner_id) REFERENCES learner(id) ON DELETE CASCADE
            )
        """.trimIndent())
    }

    private fun createIndexes(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_enrollment_learner ON enrollment(learner_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_enrollment_school_year_class ON enrollment(school_id, academic_year, school_class)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_enrollment_school_learner_year_class ON enrollment(school_id, learner_id, academic_year, school_class)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_assessment_enrollment ON assessment(enrollment_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mark_assessment ON mark(assessment_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_learner_name ON learner(full_name COLLATE NOCASE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_identity_review_name ON legacy_identity_review(normalized_name)")
    }

    companion object {
        private const val DB_NAME = "rvh_edu.db"
        private const val DB_VERSION = 10
    }
}
