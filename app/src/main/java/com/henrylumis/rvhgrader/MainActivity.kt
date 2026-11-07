package com.henrylumis.rvhgrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.migration.LegacyGradebookMigration
import com.henrylumis.rvhgrader.core.assessment.AcademicDataService
import com.henrylumis.rvhgrader.core.assessment.AssessmentPolicyCompatibilityException
import com.henrylumis.rvhgrader.core.assessment.policySnapshotsForExistingAssessments
import com.henrylumis.rvhgrader.core.assessment.RosterEntry
import com.henrylumis.rvhgrader.core.grading.AssessmentWorkflow
import com.henrylumis.rvhgrader.data.SchoolInfoRepository
import com.henrylumis.rvhgrader.export.ExportFormat
import com.henrylumis.rvhgrader.export.ExportManager
import com.henrylumis.rvhgrader.export.AcademicExportManager
import com.henrylumis.rvhgrader.export.AcademicDocumentExportManager
import com.henrylumis.rvhgrader.core.reports.ReportCard
import com.henrylumis.rvhgrader.core.reports.ReportCardBuilder
import com.henrylumis.rvhgrader.core.reports.CertificateBuilder
import com.henrylumis.rvhgrader.grading.GradingLogic
import com.henrylumis.rvhgrader.grading.GradingScaleRepository
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.StudentRecord
import com.henrylumis.rvhgrader.model.SubjectScore
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection
import com.henrylumis.rvhgrader.ui.AppTheme
import com.henrylumis.rvhgrader.ui.BackupScreen
import com.henrylumis.rvhgrader.ui.BulkMarkEntryScreen
import com.henrylumis.rvhgrader.ui.DashboardScreen
import com.henrylumis.rvhgrader.ui.AcademicHistoryScreen
import com.henrylumis.rvhgrader.ui.LearnerManagementScreen
import com.henrylumis.rvhgrader.ui.ClassProgressionScreen
import com.henrylumis.rvhgrader.ui.ArchivedLearnersScreen
import com.henrylumis.rvhgrader.ui.LearnerProfileScreen
import com.henrylumis.rvhgrader.ui.MarkSheetScreen
import com.henrylumis.rvhgrader.ui.ClassResultsScreen
import com.henrylumis.rvhgrader.ui.LearnerReportScreen
import com.henrylumis.rvhgrader.ui.AssessmentPolicyReviewScreen
import com.henrylumis.rvhgrader.ui.ClassAnalyticsScreen
import com.henrylumis.rvhgrader.ui.ImportReviewScreen
import com.henrylumis.rvhgrader.core.importer.AcademicImportParser
import com.henrylumis.rvhgrader.core.importer.ImportReviewBuilder
import com.henrylumis.rvhgrader.core.importer.ImportReviewRow
import com.henrylumis.rvhgrader.ui.PinGate
import com.henrylumis.rvhgrader.ui.Screen
import com.henrylumis.rvhgrader.ui.SchoolGradingSettingsScreen
import com.henrylumis.rvhgrader.ui.HomeScreen
import com.henrylumis.rvhgrader.ui.SettingsHubScreen
import com.henrylumis.rvhgrader.ui.FeedbackAndUpdatesScreen
import com.henrylumis.rvhgrader.ui.SchoolManagementScreen
import com.henrylumis.rvhgrader.ui.SchoolSubjectsSettingsScreen
import com.henrylumis.rvhgrader.core.grading.SubjectCatalogRepository

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {
                    RVHGraderApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RVHGraderApp() {
    val context = LocalContext.current
    var unlocked by remember { mutableStateOf(false) }
    var migrationComplete by remember { mutableStateOf(false) }
    var migrationAttempt by remember { mutableIntStateOf(0) }
    var migrationError by remember { mutableStateOf<String?>(null) }
    remember(context) { SubjectCatalogRepository.loadAndApply(context); true }

    // Do not migrate/open the academic database while the PIN gate is still on screen.
    // The old code launched migration immediately at app startup, so the migration could
    // overlap with database access as soon as the user unlocked the app.
    LaunchedEffect(unlocked, migrationAttempt) {
        if (unlocked && !migrationComplete) {
            migrationError = null
            try {
                withContext(Dispatchers.IO) {
                    LegacyGradebookMigration.migrateIfNeeded(context.applicationContext)
                }
                migrationComplete = true
            } catch (e: Exception) {
                // Keep the legacy JSON untouched and avoid crashing the entire app on startup.
                // Show a retry screen instead of opening the database concurrently after failure.
                migrationError = e.message ?: e.javaClass.simpleName
            }
        }
    }

    if (!unlocked) {
        PinGate(context = context, onUnlocked = { unlocked = true })
        return
    }

    if (!migrationComplete) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text(if (migrationError == null) "Preparing your school records…" else "Could not prepare school records.")
                if (migrationError != null) {
                    Text("Your existing records have not been intentionally deleted. Please retry.")
                    Button(onClick = { migrationError = null; migrationComplete = false; migrationAttempt++ }) {
                        Text("Retry")
                    }
                    Text(migrationError ?: "")
                }
            }
        }
        return
    }

    // Public-release behaviour: no background audience check-ins or update polling.
    // Feedback and manual update checks remain user-initiated in Feedback & Updates.

    // ---- Persisted state: loaded once on unlock, auto-saved after every change ----
    var gradingScale by remember { mutableStateOf(GradingScaleRepository.load(context)) }
    var schoolName by remember { mutableStateOf(SchoolInfoRepository.loadSchoolName(context)) }
    var classPassPercent by remember { mutableStateOf(SchoolInfoRepository.loadClassPassPercent(context)) }

    // New RVH Edu academic core. It coexists with the legacy gradebook until V1 migration is
    // fully surfaced through the UI; closing it with the activity prevents leaked DB handles.
    val academicService = remember { AcademicDataService(context) }
    DisposableEffect(academicService) {
        onDispose { academicService.close() }
    }

    fun persistScale() = GradingScaleRepository.save(context, gradingScale)

    // ---- Class / term / section selection ----
    val navigation = rememberSaveable(saver = com.henrylumis.rvhgrader.ui.AppNavigationState.Saver) { com.henrylumis.rvhgrader.ui.AppNavigationState() }
    var selectedClass by navigation.selectedClass
    var selectedTerm by navigation.selectedTerm
    var selectedSection by navigation.selectedSection
    var academicYear by navigation.academicYear
    var selectedLearnerEntry by remember { mutableStateOf<RosterEntry?>(null) }
    var selectedLearnerHistory by remember { mutableStateOf<com.henrylumis.rvhgrader.core.history.LearnerAcademicHistory?>(null) }
    var markAssessment by remember { mutableStateOf<com.henrylumis.rvhgrader.core.model.AcademicAssessment?>(null) }
    var markExisting by remember { mutableStateOf<List<com.henrylumis.rvhgrader.core.model.Mark>>(emptyList()) }
    var classResults by remember { mutableStateOf<List<com.henrylumis.rvhgrader.core.model.RankedResult>>(emptyList()) }
    var classAnalytics by remember { mutableStateOf<com.henrylumis.rvhgrader.core.analytics.ClassAnalyticsSummary?>(null) }
    var showAcademicExportDialog by remember { mutableStateOf(false) }
    var importRows by remember { mutableStateOf<List<ImportReviewRow>>(emptyList()) }
    var importStatus by remember { mutableStateOf<String?>(null) }
    // Operation feedback is transient. A previous ranking/export error must not masquerade
    // as a live failure after the teacher has moved to an unrelated screen.
    LaunchedEffect(importStatus) {
        if (importStatus != null) {
            delay(8_000)
            importStatus = null
        }
    }
    var academicDataRevision by remember { mutableIntStateOf(0) }
    var activeLearnerReport by remember { mutableStateOf<ReportCard?>(null) }

    var nameValue by remember { mutableStateOf("") }
    val fieldValues = remember { mutableStateMapOf<String, String>() }


    // ---- Navigation ----
    var currentScreen by navigation.currentScreen
    // Keep each screen's saveable UI state (especially LazyColumn/ScrollState positions) while
    // navigating away and back, without keeping every screen composed in memory.
    val screenStateHolder = rememberSaveableStateHolder()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val navScope = rememberCoroutineScope()

    // System Back follows the real route history rather than guessed destinations.
    // A modal drawer is dismissed first; otherwise the previous visited screen is restored.
    BackHandler(enabled = drawerState.isOpen || navigation.canNavigateBack) {
        if (drawerState.isOpen) {
            navScope.launch { drawerState.close() }
        } else {
            navigation.navigateBack()
        }
    }

    // Dashboard standings are now a view of the new RVH Edu database. The legacy StudentRecord
    // type is retained only as a UI compatibility adapter while the old screens are retired.
    // This prevents migrated legacy data and new data from being displayed twice.
    fun currentRecords(): List<StudentRecord> {
        academicDataRevision // intentionally read so Compose invalidates after a save
        val roster = academicService.roster(academicYear, selectedClass)
        return roster.mapNotNull { entry ->
            val assessment = academicService.findAssessment(entry.enrollment, selectedTerm, selectedSection)
                ?: return@mapNotNull null
            val marks = academicService.loadMarks(assessment)
            val result = academicService.calculate(entry.enrollment, assessment)
            StudentRecord(
                id = entry.learner.id,
                name = entry.learner.fullName.uppercase(),
                schoolClass = selectedClass,
                term = selectedTerm,
                section = selectedSection,
                scores = marks.associate { it.subjectCode to SubjectScore(it.mark, it.aggregate) },
                total = result.total,
                aggSum = result.aggregateSum,
                division = result.division,
                graded = result.graded,
                recordedAtMillis = assessment.recordedAtMillis
            )
        }.sortedWith(compareByDescending<StudentRecord> { it.total }.thenBy { it.name })
    }

    fun resetForm() {
        nameValue = ""
        fieldValues.clear()
    }

    // Declare navigation callbacks before the activity-result launcher. Kotlin local functions
    // are not available to earlier local declarations that capture them.
    fun navigateTo(screen: Screen) {
        navigation.navigateTo(screen)
        navScope.launch { drawerState.close() }
    }

    fun goTo(screen: Screen) = navigateTo(screen)

    val importFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val name = context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                    if (c.moveToFirst()) c.getString(0) else null
                }.orEmpty().lowercase()
                context.contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Could not open the selected file." }
                    val sheet = if (name.endsWith(".xlsx")) {
                        AcademicImportParser.parseXlsx(input, selectedClass)
                    } else {
                        AcademicImportParser.parseCsv(input.readBytes().toString(Charsets.UTF_8), selectedClass)
                    }
                    importStatus = sheet.warnings.takeIf { it.isNotEmpty() }?.joinToString("\n")
                    importRows = ImportReviewBuilder.build(sheet, academicService.roster(academicYear, selectedClass).map { it.learner })
                    navigateTo(Screen.IMPORT)
                }
            } catch (e: Exception) {
                importStatus = "IMPORT FAILED: ${e.message ?: "invalid file"}"
            }
        }
    }

    fun openMenu() {
        navScope.launch { drawerState.open() }
    }

    fun onClassOrTermOrSectionChange() {
        resetForm()
    }

    fun loadClassResults() {
        try {
            classResults = academicService.classResults(academicYear, selectedClass, selectedTerm, selectedSection, gradingScale)
            importStatus = null
            navigateTo(Screen.RESULTS)
        } catch (e: AssessmentPolicyCompatibilityException) {
            classResults = emptyList()
            importStatus = "CLASS RESULTS NEED POLICY REVIEW: ${e.message ?: "saved assessment policies conflict"}"
            navigateTo(Screen.POLICY_REVIEW)
        } catch (e: Exception) {
            classResults = emptyList()
            importStatus = "CLASS RESULTS COULD NOT LOAD: ${e.message ?: e.javaClass.simpleName}"
            navigateTo(Screen.HOME)
        } finally {
            navScope.launch { drawerState.close() }
        }
    }

    fun loadClassAnalytics() {
        try {
            classAnalytics = academicService.classAnalytics(academicYear, selectedClass, selectedTerm, selectedSection, gradingScale)
            importStatus = null
            navigateTo(Screen.ANALYTICS)
        } catch (e: AssessmentPolicyCompatibilityException) {
            classAnalytics = null
            importStatus = "CLASS ANALYTICS NEED POLICY REVIEW: ${e.message ?: "saved assessment policies conflict"}"
            navigateTo(Screen.POLICY_REVIEW)
        } catch (e: Exception) {
            classAnalytics = null
            importStatus = "CLASS ANALYTICS COULD NOT LOAD: ${e.message ?: e.javaClass.simpleName}"
            navigateTo(Screen.HOME)
        } finally {
            navScope.launch { drawerState.close() }
        }
    }

    fun loadMarkSheet(entry: RosterEntry) {
        selectedLearnerEntry = entry
        markAssessment = academicService.openAssessment(entry.enrollment, selectedTerm, selectedSection)
        markExisting = academicService.loadMarks(markAssessment!!)
        navigateTo(Screen.MARKS)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("HENRY LUMIS", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "In dedication to Hellen",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(Screen.HOME.label) },
                    selected = currentScreen == Screen.HOME,
                    icon = { Icon(Icons.Filled.Tune, contentDescription = null) },
                    onClick = { goTo(Screen.HOME) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(Screen.DASHBOARD.label) },
                    selected = currentScreen == Screen.DASHBOARD,
                    icon = { Icon(Icons.Filled.Tune, contentDescription = null) },
                    onClick = { goTo(Screen.DASHBOARD) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(Screen.LEARNERS.label) },
                    selected = currentScreen == Screen.LEARNERS,
                    icon = { Icon(Icons.Filled.Groups, contentDescription = null) },
                    onClick = { goTo(Screen.LEARNERS) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(Screen.CLASS_PROGRESSION.label) },
                    selected = currentScreen == Screen.CLASS_PROGRESSION,
                    icon = { Icon(Icons.Filled.Groups, contentDescription = null) },
                    onClick = { goTo(Screen.CLASS_PROGRESSION) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(Screen.ARCHIVED_LEARNERS.label) },
                    selected = currentScreen == Screen.ARCHIVED_LEARNERS,
                    icon = { Icon(Icons.Filled.Groups, contentDescription = null) },
                    onClick = { goTo(Screen.ARCHIVED_LEARNERS) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(Screen.RESULTS.label) },
                    selected = currentScreen == Screen.RESULTS,
                    icon = { Icon(Icons.Filled.Assignment, contentDescription = null) },
                    onClick = { loadClassResults() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(Screen.ANALYTICS.label) },
                    selected = currentScreen == Screen.ANALYTICS,
                    icon = { Icon(Icons.Filled.Assignment, contentDescription = null) },
                    onClick = { loadClassAnalytics() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(Screen.HISTORY.label) },
                    selected = currentScreen == Screen.HISTORY,
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    onClick = { goTo(Screen.HISTORY) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(Screen.SETTINGS.label) },
                    selected = currentScreen == Screen.SETTINGS,
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    onClick = { goTo(Screen.SETTINGS) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(Screen.IMPORT.label) },
                    selected = currentScreen == Screen.IMPORT,
                    icon = { Icon(Icons.Filled.Assignment, contentDescription = null) },
                    onClick = { importFileLauncher.launch(arrayOf("text/csv", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "text/comma-separated-values")) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(Screen.BACKUP.label) },
                    selected = currentScreen == Screen.BACKUP,
                    icon = { Icon(Icons.Filled.Backup, contentDescription = null) },
                    onClick = { goTo(Screen.BACKUP) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Column {
            importStatus?.let { status ->
                Card(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)) {
                        Text(status, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { importStatus = null }) { Text("Dismiss") }
                    }
                }
            }
            screenStateHolder.SaveableStateProvider(key = currentScreen.name) {
            when (currentScreen) {
            Screen.HOME -> HomeScreen(
                schoolName = schoolName,
                academicYear = academicYear,
                learnerCount = academicService.roster(academicYear, selectedClass).size,
                onOpenMenu = { openMenu() },
                onEnrollLearners = { goTo(Screen.LEARNERS) },
                onEnterMarks = { goTo(Screen.LEARNERS) },
                onBulkMarks = { goTo(Screen.BULK_MARKS) },
                onResults = { loadClassResults() },
                onAnalytics = { loadClassAnalytics() },
                onHistory = { goTo(Screen.HISTORY) },
                onImport = { importFileLauncher.launch(arrayOf("text/csv", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "text/comma-separated-values")) },
                onSettings = { goTo(Screen.SETTINGS) },
                onBackup = { goTo(Screen.BACKUP) }
            )

            Screen.DASHBOARD -> DashboardScreen(
                selectedClass = selectedClass,
                onClassChange = { newClass ->
                    selectedClass = newClass
                    onClassOrTermOrSectionChange()
                },
                selectedTerm = selectedTerm,
                onTermChange = { newTerm ->
                    selectedTerm = newTerm
                    onClassOrTermOrSectionChange()
                },
                selectedSection = selectedSection,
                onSectionChange = { newSection ->
                    selectedSection = newSection
                    onClassOrTermOrSectionChange()
                },
                fieldValues = fieldValues,
                nameValue = nameValue,
                onNameChange = { nameValue = it },
                onFieldChange = { field, value -> fieldValues[field] = value },
                onCommit = {
                    try {
                        val cleanName = nameValue.trim()
                        require(cleanName.isNotBlank()) { "Learner name is required." }
                        // Validate the complete mark set BEFORE creating a learner or enrollment.
                        // A failed save must never leave a half-created academic record behind.
                        val expected = com.henrylumis.rvhgrader.core.grading.SubjectCatalog.forClass(selectedClass)
                        val missing = expected.filter { fieldValues[it.code].orEmpty().trim().isBlank() }
                        require(missing.isEmpty()) {
                            "Missing marks: ${missing.joinToString(", ") { it.displayName }}. Use Save Draft from the learner mark sheet for incomplete work."
                        }
                        val invalid = expected.filter { subject ->
                            val raw = fieldValues[subject.code].orEmpty().trim()
                            val value = raw.toIntOrNull()
                            value == null || value !in 0..100
                        }
                        require(invalid.isEmpty()) {
                            "Invalid marks: ${invalid.joinToString(", ") { it.displayName }}. Enter whole numbers from 0 to 100."
                        }
                        val roster = academicService.roster(academicYear, selectedClass)
                        val normalized = cleanName.trim().replace(Regex("\\s+"), " ").lowercase()
                        val candidates = roster.filter {
                            it.learner.fullName.trim().replace(Regex("\\s+"), " ").lowercase() == normalized
                        }
                        require(candidates.size <= 1) {
                            "More than one learner has this name in ${selectedClass.displayName}. Open Learners and use the correct profile."
                        }
                        val entry = if (candidates.size == 1) {
                            candidates.first()
                        } else {
                            val learner = academicService.createLearner(cleanName)
                            academicService.enroll(learner.id, academicYear, selectedClass)
                            academicService.roster(academicYear, selectedClass).first { it.learner.id == learner.id }
                        }
                        val inputs = expected.map { subject ->
                            val raw = fieldValues[subject.code].orEmpty().trim()
                            val value = raw.toIntOrNull() ?: error("Invalid mark for ${subject.displayName}.")
                            AssessmentWorkflow.MarkInput(subject.code, value)
                        }
                        val assessment = academicService.openAssessment(entry.enrollment, selectedTerm, selectedSection)
                        academicService.saveMarks(assessment, entry.enrollment, inputs, gradingScale)
                        academicDataRevision++
                        importStatus = "SAVED TO RVH EDU: ${entry.learner.fullName.uppercase()} • ${selectedTerm.displayName} • ${selectedSection.displayName}"
                        resetForm()
                    } catch (e: Exception) {
                        importStatus = "SAVE FAILED: ${e.message ?: "could not save learner"}"
                    }
                },
                records = currentRecords(),
                onOpenMenu = { openMenu() },
                onExportClassList = { format ->
                    try {
                    val results = academicService.classResults(
                        academicYear, selectedClass, selectedTerm, selectedSection, gradingScale
                    )
                    val roster = academicService.roster(academicYear, selectedClass)
                    val names = roster.associate { it.learner.id to it.learner.fullName }
                    val marks = roster.associate { entry ->
                        entry.learner.id to academicService.findAssessment(entry.enrollment, selectedTerm, selectedSection)
                            ?.let { academicService.loadMarks(it) }.orEmpty()
                    }
                    val file = AcademicExportManager.exportClassResults(
                        context, schoolName, selectedClass, academicYear, selectedTerm, selectedSection,
                        results, names, marks, format,
                        policySnapshotJsons = policySnapshotsForExistingAssessments(results.mapNotNull { ranked ->
                            roster.firstOrNull { it.learner.id == ranked.result.learnerId }?.let { entry ->
                                academicService.findAssessment(entry.enrollment, selectedTerm, selectedSection)
                            }
                        }),
                        currentGradingScale = gradingScale,
                        currentPassPercent = classPassPercent
                    )
                    ExportManager.shareFile(context, file, format.mimeType)
                    } catch (e: Exception) {
                        importStatus = "CLASS EXPORT BLOCKED: ${e.message ?: "review saved assessment policies"}"
                    }
                }
            )

            Screen.LEARNERS -> LearnerManagementScreen(
                year = academicYear,
                selectedClass = selectedClass,
                // Read the revision as Compose state before loading the roster. Every successful
                // add/edit/archive/restore increments it, forcing this screen to query fresh rows.
                roster = run { academicDataRevision; academicService.roster(academicYear, selectedClass) },
                onYearChange = { academicYear = it.coerceIn(2000, 2100) },
                onClassChange = { selectedClass = it },
                onAddLearner = { name, age, admissionNumber ->
                    try {
                        academicService.createAndEnrollLearner(
                            name = name, ageYears = age, admissionNumber = admissionNumber,
                            academicYear = academicYear, schoolClass = selectedClass
                        )
                        academicDataRevision++
                        null
                    } catch (e: Exception) {
                        e.message ?: "Could not add learner"
                    }
                },
                onUpdateLearner = { entry, name, age, admissionNumber ->
                    try {
                        academicService.updateLearnerDetails(entry.learner.id, name, age, admissionNumber)
                        academicDataRevision++
                        null
                    } catch (e: Exception) {
                        e.message ?: "Could not update learner"
                    }
                },
                onFindDuplicates = { name, age, admissionNumber, excludingId ->
                    academicService.findPossibleLearnerDuplicates(name, age, admissionNumber, excludingId)
                },
                onArchiveLearner = { entry, reason, note ->
                    try {
                        academicService.archiveLearner(entry.learner.id, reason, note)
                        academicDataRevision++
                    } catch (e: Exception) {
                        importStatus = "LEARNER ARCHIVE FAILED: ${e.message ?: "review the learner record"}"
                    }
                },
                onOpenMarks = { loadMarkSheet(it) },
                onOpenBulkMarks = { navigateTo(Screen.BULK_MARKS) },
                onOpenProfile = { entry ->
                    selectedLearnerHistory = academicService.learnerHistory(entry.learner.id, gradingScale)
                    navigateTo(Screen.LEARNER_PROFILE)
                },
                onOpenMenu = { openMenu() }
            )

            Screen.CLASS_PROGRESSION -> ClassProgressionScreen(
                sourceYear = academicYear,
                sourceClass = selectedClass,
                roster = run { academicDataRevision; academicService.roster(academicYear, selectedClass) },
                onProgress = { targetYear, targetClass, learnerIds ->
                    try {
                        val count = academicService.progressClass(
                            sourceYear = academicYear,
                            sourceClass = selectedClass,
                            targetYear = targetYear,
                            targetClass = targetClass,
                            learnerIds = learnerIds
                        )
                        academicDataRevision++
                        importStatus = "CLASS PROGRESSION COMPLETE: $count LEARNER(S) ENROLLED FOR $targetYear"
                        null
                    } catch (e: Exception) {
                        e.message ?: "Class progression failed"
                    }
                },
                onOpenMenu = { openMenu() }
            )

            Screen.ARCHIVED_LEARNERS -> ArchivedLearnersScreen(
                learners = run { academicDataRevision; academicService.allLearners().filter { it.status == "ARCHIVED" } },
                archiveReasons = run { academicDataRevision; runCatching { academicService.latestArchiveReasons() }.getOrDefault(emptyMap()) },
                onRestore = { learner ->
                    academicService.restoreLearner(learner.id)
                    academicDataRevision++
                },
                onOpenMenu = { openMenu() }
            )

            Screen.LEARNER_PROFILE -> selectedLearnerHistory?.let { history ->
                LearnerProfileScreen(
                    history = history,
                    onBack = { navigation.navigateBack() }
                )
            }

            Screen.MARKS -> selectedLearnerEntry?.let { entry ->
                MarkSheetScreen(
                    entry = entry,
                    term = selectedTerm,
                    section = selectedSection,
                    assessment = markAssessment,
                    existingMarks = markExisting,
                    onTermChange = { newTerm ->
                        selectedTerm = newTerm
                        markAssessment = academicService.openAssessment(entry.enrollment, newTerm, selectedSection)
                        markExisting = academicService.loadMarks(markAssessment!!)
                    },
                    onSectionChange = { newSection ->
                        selectedSection = newSection
                        markAssessment = academicService.openAssessment(entry.enrollment, selectedTerm, newSection)
                        markExisting = academicService.loadMarks(markAssessment!!)
                    },
                    onLoadAssessment = { loadMarkSheet(entry) },
                    onSaveDraft = { values ->
                        try {
                            val assessment = markAssessment ?: academicService.openAssessment(entry.enrollment, selectedTerm, selectedSection)
                            val inputs = AssessmentWorkflow.subjectsFor(entry.enrollment.schoolClass).map { code ->
                                AssessmentWorkflow.MarkInput(code, values[code]?.trim()?.takeIf { it.isNotEmpty() }?.toIntOrNull())
                            }
                            academicService.saveDraftMarkSet(assessment, entry.enrollment, inputs)
                            markAssessment = academicService.openAssessment(entry.enrollment, selectedTerm, selectedSection)
                            markExisting = academicService.loadMarks(markAssessment!!)
                            null
                        } catch (e: Exception) { e.message ?: "Could not save draft" }
                    },
                    onFinalize = { values ->
                        try {
                            val inputs = AssessmentWorkflow.subjectsFor(entry.enrollment.schoolClass).map { code ->
                                AssessmentWorkflow.MarkInput(code, values[code]?.toIntOrNull())
                            }
                            val assessment = markAssessment ?: academicService.openAssessment(entry.enrollment, selectedTerm, selectedSection)
                            academicService.saveMarks(assessment, entry.enrollment, inputs, gradingScale)
                            markAssessment = academicService.openAssessment(entry.enrollment, selectedTerm, selectedSection)
                            markExisting = academicService.loadMarks(markAssessment!!)
                            null
                        } catch (e: Exception) { e.message ?: "Could not finalize assessment" }
                    },
                    onReopen = {
                        try {
                            val assessment = markAssessment ?: academicService.openAssessment(entry.enrollment, selectedTerm, selectedSection)
                            markAssessment = academicService.reopenAssessment(assessment)
                            markExisting = academicService.loadMarks(markAssessment!!)
                            null
                        } catch (e: Exception) { e.message ?: "Could not reopen assessment" }
                    },
                    onBack = { navigation.navigateBack() }
                )
            }

            Screen.BULK_MARKS -> BulkMarkEntryScreen(
                year = academicYear,
                schoolClass = selectedClass,
                term = selectedTerm,
                section = selectedSection,
                // Cache the roster across ordinary Compose recompositions; invalidate it when
                // the selected class/year or academic data revision changes.
                roster = remember(academicYear, selectedClass, academicDataRevision) {
                    academicService.roster(academicYear, selectedClass)
                },
                onTermChange = { selectedTerm = it },
                onSectionChange = { selectedSection = it },
                loadMarksForRoster = { entries, t, s ->
                    // One school-scoped query for the whole roster; read-only and executed on IO
                    // by BulkMarkEntryScreen, rather than doing N+1 assessment/mark lookups.
                    academicService.loadMarksForRoster(entries, t, s)
                },
                saveDraft = { entry, t, s, subjectCode, value ->
                    try {
                        val assessment = academicService.openAssessment(entry.enrollment, t, s)
                        academicService.saveDraftMarks(
                            assessment, entry.enrollment,
                            listOf(AssessmentWorkflow.MarkInput(subjectCode, value))
                        )
                        null
                    } catch (e: Exception) { e.message ?: "Could not save marks" }
                },
                onBack = { navigation.navigateBack() }
            )

            Screen.RESULTS -> ClassResultsScreen(
                schoolClass = selectedClass,
                year = academicYear,
                term = selectedTerm,
                section = selectedSection,
                results = classResults,
                learnerNames = academicService.allLearners().associateBy({ it.id }, { it.fullName }),
                onTermChange = { newTerm -> selectedTerm = newTerm; loadClassResults() },
                onSectionChange = { newSection -> selectedSection = newSection; loadClassResults() },
                onExport = { showAcademicExportDialog = true },
                onReportCard = { learnerId ->
                    try {
                        val entry = academicService.roster(academicYear, selectedClass).first { it.learner.id == learnerId }
                        val assessment = academicService.findAssessment(entry.enrollment, selectedTerm, selectedSection)
                            ?: error("No assessment exists for this learner.")
                        val marks = academicService.loadMarks(assessment)
                        val result = academicService.calculate(entry.enrollment, assessment, gradingScale)
                        val classRanking = academicService.classResults(academicYear, selectedClass, selectedTerm, selectedSection, gradingScale)
                        val learnerRank = classRanking.firstOrNull { it.result.learnerId == learnerId }
                        val classPosition = learnerRank?.position?.takeIf { it > 0 && result.complete }
                        val rankedLearnerCount = classRanking.count { it.result.complete }
                        activeLearnerReport = ReportCardBuilder.build(
                            schoolName, entry.learner, selectedClass, academicYear, assessment, marks, result,
                            classPosition = classPosition,
                            rankedLearnerCount = rankedLearnerCount.takeIf { classPosition != null }
                        )
                        navigateTo(Screen.LEARNER_REPORT)
                    } catch (e: Exception) {
                        importStatus = "REPORT CARD FAILED: ${e.message ?: "could not generate document"}"
                    }
                },
                onCertificate = { learnerId ->
                    try {
                        val entry = academicService.roster(academicYear, selectedClass).first { it.learner.id == learnerId }
                        val assessment = academicService.findAssessment(entry.enrollment, selectedTerm, selectedSection)
                            ?: error("No assessment exists for this learner.")
                        val result = academicService.calculate(entry.enrollment, assessment)
                        val certificate = CertificateBuilder.build(schoolName, entry.learner, selectedClass, academicYear, result)
                            ?: error("Certificate unavailable until all required marks are complete.")
                        val file = AcademicDocumentExportManager.exportCertificate(context, certificate)
                        ExportManager.shareFile(context, file, "application/pdf")
                    } catch (e: Exception) {
                        importStatus = "CERTIFICATE FAILED: ${e.message ?: "could not generate document"}"
                    }
                },
                onBack = { navigation.navigateBack() }
            )

            Screen.LEARNER_REPORT -> activeLearnerReport?.let { report ->
                LearnerReportScreen(
                    report = report,
                    onExport = {
                        try {
                            val file = AcademicDocumentExportManager.exportReportCard(context, report)
                            ExportManager.shareFile(context, file, "application/pdf")
                        } catch (e: Exception) {
                            importStatus = "REPORT CARD FAILED: ${e.message ?: "could not generate document"}"
                        }
                    },
                    onBack = { navigation.navigateBack() }
                )
            } ?: run { navigation.navigateBack() }

            Screen.POLICY_REVIEW -> AssessmentPolicyReviewScreen(
                schoolClass = selectedClass, year = academicYear, term = selectedTerm, section = selectedSection,
                entries = runCatching { academicService.policyReview(academicYear, selectedClass, selectedTerm, selectedSection) }.getOrDefault(emptyList()),
                onBack = { navigation.navigateBack() }
            )

            Screen.ANALYTICS -> ClassAnalyticsScreen(
                schoolClass = selectedClass,
                year = academicYear,
                term = selectedTerm,
                section = selectedSection,
                summary = classAnalytics ?: academicService.classAnalytics(academicYear, selectedClass, selectedTerm, selectedSection, gradingScale),
                learnerNames = academicService.allLearners().associateBy({ it.id }, { it.fullName }),
                onTermChange = { newTerm -> selectedTerm = newTerm; loadClassAnalytics() },
                onSectionChange = { newSection -> selectedSection = newSection; loadClassAnalytics() },
                onBack = { navigation.navigateBack() }
            )

            Screen.HISTORY -> AcademicHistoryScreen(
                histories = academicService.allLearners().mapNotNull { learner ->
                    academicService.learnerHistory(learner.id, gradingScale)
                }.sortedBy { it.learnerName.lowercase() },
                onOpenLearner = { learnerId ->
                    selectedLearnerEntry = academicService.roster(academicYear, selectedClass)
                        .firstOrNull { it.learner.id == learnerId }
                    selectedLearnerHistory = academicService.learnerHistory(learnerId, gradingScale)
                    navigateTo(Screen.LEARNER_PROFILE)
                },
                onOpenMenu = { openMenu() }
            )

            Screen.SETTINGS -> SettingsHubScreen(
                onOpenMenu = { openMenu() },
                onSchoolAndGrading = { goTo(Screen.GRADING_RULES) },
                onSchools = { goTo(Screen.SCHOOLS) },
                onBackup = { goTo(Screen.BACKUP) },
                onArchivedLearners = { goTo(Screen.ARCHIVED_LEARNERS) },
                onClassSetup = { goTo(Screen.CLASS_PROGRESSION) },
                onSchoolSubjects = { goTo(Screen.SCHOOL_SUBJECTS) },
                onFeedbackAndUpdates = { goTo(Screen.FEEDBACK_UPDATES) }
            )

            Screen.FEEDBACK_UPDATES -> FeedbackAndUpdatesScreen(onBack = { navigation.navigateBack() })

            Screen.SCHOOLS -> SchoolManagementScreen(
                onBack = { navigation.navigateBack() },
                onSchoolChanged = {
                    schoolName = SchoolInfoRepository.loadSchoolName(context)
                    classPassPercent = SchoolInfoRepository.loadClassPassPercent(context)
                    classAnalytics = null
                    classResults = emptyList()
                    academicDataRevision++
                }
            )

            Screen.SCHOOL_SUBJECTS -> SchoolSubjectsSettingsScreen(
                onOpenMenu = { openMenu() },
                onSaved = { classAnalytics = null; classResults = emptyList(); academicDataRevision++ }
            )

            Screen.GRADING_RULES -> SchoolGradingSettingsScreen(
                scale = gradingScale,
                onScaleChange = { newScale ->
                    gradingScale = newScale
                    persistScale()
                },
                schoolName = schoolName,
                classPassPercent = classPassPercent,
                onClassPassPercentChange = { percent ->
                    classPassPercent = percent
                    SchoolInfoRepository.saveClassPassPercent(context, percent)
                    classAnalytics = null
                },
                onSchoolNameChange = { newName ->
                    schoolName = newName
                    SchoolInfoRepository.saveSchoolName(context, newName)
                },
                onOpenMenu = { openMenu() }
            )

            Screen.IMPORT -> ImportReviewScreen(
                rows = importRows,
                schoolClass = selectedClass,
                term = selectedTerm,
                section = selectedSection,
                onConfirm = { rows ->
                    try {
                        val result = academicService.importReviewedSpreadsheetRows(academicYear, selectedClass, selectedTerm, selectedSection, rows)
                        importStatus = "IMPORT COMPLETE: ${result.importedRows} ROW(S) SAVED AS DRAFT • ${result.createdLearners} NEW LEARNER(S) • ${result.ambiguousRows} AMBIGUOUS"
                        importRows = emptyList()
                        navigateTo(Screen.LEARNERS)
                    } catch (e: Exception) {
                        importStatus = "IMPORT FAILED: ${e.message ?: "could not save rows"}"
                    }
                },
                onBack = { navigation.navigateBack() }
            )

            Screen.BACKUP -> BackupScreen(
                onOpenMenu = { openMenu() },
                onDataRestored = { academicDataRevision++ }
            )
            }
            }
        }
    }

    if (showAcademicExportDialog) {
        AlertDialog(
            onDismissRequest = { showAcademicExportDialog = false },
            title = { Text("Export class results") },
            text = { Text("Choose a format. The export uses the new RVH Edu database and preserves incomplete marks as incomplete rather than zero.") },
            confirmButton = {
                TextButton(onClick = {
                    val results = classResults
                    val names = academicService.allLearners().associateBy({ it.id }, { it.fullName })
                    val roster = academicService.roster(academicYear, selectedClass)
                    val marks = results.associate { ranked ->
                        ranked.result.learnerId to (academicService.roster(academicYear, selectedClass).firstOrNull { it.learner.id == ranked.result.learnerId }?.let { entry ->
                            val assessment = academicService.findAssessment(entry.enrollment, selectedTerm, selectedSection)
                            assessment?.let { academicService.loadMarks(it) }.orEmpty()
                        } ?: emptyList())
                    }
                    val file = AcademicExportManager.exportClassResults(context, schoolName, selectedClass, academicYear, selectedTerm, selectedSection, results, names, marks, ExportFormat.PDF, policySnapshotJsons = policySnapshotsForExistingAssessments(results.mapNotNull { ranked -> roster.firstOrNull { it.learner.id == ranked.result.learnerId }?.let { entry -> academicService.findAssessment(entry.enrollment, selectedTerm, selectedSection) } }), currentGradingScale = gradingScale, currentPassPercent = classPassPercent)
                    showAcademicExportDialog = false
                    ExportManager.shareFile(context, file, ExportFormat.PDF.mimeType)
                }) { Text("PDF") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        val results = classResults
                        val names = academicService.allLearners().associateBy({ it.id }, { it.fullName })
                        val roster = academicService.roster(academicYear, selectedClass)
                        val marks = results.associate { ranked ->
                            ranked.result.learnerId to (roster.firstOrNull { it.learner.id == ranked.result.learnerId }?.let { entry -> academicService.findAssessment(entry.enrollment, selectedTerm, selectedSection)?.let { academicService.loadMarks(it) }.orEmpty() } ?: emptyList())
                        }
                        val file = AcademicExportManager.exportClassResults(context, schoolName, selectedClass, academicYear, selectedTerm, selectedSection, results, names, marks, ExportFormat.EXCEL, policySnapshotJsons = policySnapshotsForExistingAssessments(results.mapNotNull { ranked -> roster.firstOrNull { it.learner.id == ranked.result.learnerId }?.let { entry -> academicService.findAssessment(entry.enrollment, selectedTerm, selectedSection) } }), currentGradingScale = gradingScale, currentPassPercent = classPassPercent)
                        showAcademicExportDialog = false
                        ExportManager.shareFile(context, file, ExportFormat.EXCEL.mimeType)
                    }) { Text("Excel") }
                    TextButton(onClick = {
                        val results = classResults
                        val names = academicService.allLearners().associateBy({ it.id }, { it.fullName })
                        val roster = academicService.roster(academicYear, selectedClass)
                        val marks = results.associate { ranked ->
                            ranked.result.learnerId to (roster.firstOrNull { it.learner.id == ranked.result.learnerId }?.let { entry -> academicService.findAssessment(entry.enrollment, selectedTerm, selectedSection)?.let { academicService.loadMarks(it) }.orEmpty() } ?: emptyList())
                        }
                        val file = AcademicExportManager.exportClassResults(context, schoolName, selectedClass, academicYear, selectedTerm, selectedSection, results, names, marks, ExportFormat.CSV, policySnapshotJsons = policySnapshotsForExistingAssessments(results.mapNotNull { ranked -> roster.firstOrNull { it.learner.id == ranked.result.learnerId }?.let { entry -> academicService.findAssessment(entry.enrollment, selectedTerm, selectedSection) } }), currentGradingScale = gradingScale, currentPassPercent = classPassPercent)
                        showAcademicExportDialog = false
                        ExportManager.shareFile(context, file, ExportFormat.CSV.mimeType)
                    }) { Text("CSV") }
                }
            }
        )
    }

}
