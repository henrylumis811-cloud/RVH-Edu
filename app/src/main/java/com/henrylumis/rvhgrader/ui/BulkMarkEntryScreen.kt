package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import org.json.JSONObject
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import com.henrylumis.rvhgrader.core.grading.AssessmentWorkflow
import com.henrylumis.rvhgrader.core.assessment.RosterEntry
import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun BulkMarkEntryScreen(
    year: Int,
    schoolClass: com.henrylumis.rvhgrader.model.SchoolClass,
    term: Term,
    section: TermSection,
    roster: List<RosterEntry>,
    onTermChange: (Term) -> Unit,
    onSectionChange: (TermSection) -> Unit,
    loadMarksForRoster: (List<RosterEntry>, Term, TermSection) -> Map<String, List<Mark>>,
    saveDraft: (RosterEntry, Term, TermSection, String, Int?) -> String?,
    onBack: () -> Unit
) {
    val subjects = SubjectCatalog.forClass(schoolClass)
    var selectedSubject by remember(schoolClass) { mutableStateOf(subjects.firstOrNull()?.code.orEmpty()) }
    var showAssessmentPicker by remember { mutableStateOf(false) }
    var pickerTerm by remember(term) { mutableStateOf(term) }
    var pickerSection by remember(section) { mutableStateOf(section) }
    var pickerSubject by remember(schoolClass, selectedSubject) { mutableStateOf(selectedSubject) }
    val values = remember { mutableStateMapOf<String, String>() }
    val savedValues = remember { mutableStateMapOf<String, String>() }
    var message by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val screenScope = rememberCoroutineScope()
    var learnerSearch by remember { mutableStateOf("") }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val context = LocalContext.current
    val recoveryPreferences = remember(context) {
        context.getSharedPreferences("rvh_bulk_mark_recovery", android.content.Context.MODE_PRIVATE)
    }
    val recoveryKey = "draft_${year}_${schoolClass.name}_${term.name}_${section.name}_$selectedSubject"

    fun persistRecoveryDraft() {
        val snapshot = JSONObject()
        roster.forEach { entry ->
            snapshot.put(entry.enrollment.id, values[entry.enrollment.id].orEmpty())
        }
        recoveryPreferences.edit().putString(recoveryKey, snapshot.toString()).apply()
    }

    fun clearRecoveryDraft() {
        recoveryPreferences.edit().remove(recoveryKey).apply()
    }

    LaunchedEffect(roster, term, section, selectedSubject, recoveryKey) {
        values.clear()
        savedValues.clear()
        // Assessment lookup and SQLite reads must not run on Compose's main dispatcher.
        // Load the roster snapshot on IO, then publish the completed values together on main.
        val loadedValues = withContext(Dispatchers.IO) {
            val marksByEnrollment = loadMarksForRoster(roster, term, section)
            roster.map { entry ->
                val value = marksByEnrollment[entry.enrollment.id].orEmpty()
                    .firstOrNull { it.subjectCode == selectedSubject }?.mark?.toString().orEmpty()
                entry.enrollment.id to value
            }
        }
        loadedValues.forEach { (enrollmentId, value) ->
            values[enrollmentId] = value
            savedValues[enrollmentId] = value
        }
        // Restore unsaved input after process death/restart. Only IDs in the current roster
        // are applied, and the snapshot is scoped to year/class/term/section/subject.
        val recovered = recoveryPreferences.getString(recoveryKey, null)
        if (!recovered.isNullOrBlank()) {
            runCatching {
                val snapshot = JSONObject(recovered)
                roster.forEach { entry ->
                    if (snapshot.has(entry.enrollment.id)) {
                        values[entry.enrollment.id] = snapshot.optString(entry.enrollment.id, "")
                    }
                }
            }.onFailure { clearRecoveryDraft() }
        }
        message = if (recovered.isNullOrBlank()) null else "Recovered unsaved marks from this device. Review and save them."
    }

    val selectedLabel = subjects.firstOrNull { it.code == selectedSubject }?.displayName ?: selectedSubject
    val visibleRoster = remember(roster, learnerSearch) {
        val query = learnerSearch.trim()
        if (query.isBlank()) roster else roster.filter { entry ->
            entry.learner.fullName.contains(query, ignoreCase = true) ||
                entry.enrollment.id.contains(query, ignoreCase = true)
        }
    }
    // Count all mark states in one derived pass. The previous three independent roster scans
    // ran on every keystroke/recomposition, which becomes noticeable in large classes.
    val markCounts by remember(roster) {
        derivedStateOf {
            var entered = 0
            var changed = 0
            roster.forEach { entry ->
                val id = entry.enrollment.id
                if (!values[id].isNullOrBlank()) entered++
                if (values[id].orEmpty() != savedValues[id].orEmpty()) changed++
            }
            Triple(entered, (roster.size - entered).coerceAtLeast(0), changed)
        }
    }
    val enteredCount = markCounts.first
    val missingCount = markCounts.second
    val changedCount = markCounts.third

    // Serializing the entire draft and writing SharedPreferences on every key event adds avoidable
    // work to the UI thread. Debounce recovery snapshots; explicit save/error paths remain immediate.
    // Build these O(roster-size) snapshots only when their observable inputs change.
    // Avoid calling values.toMap()/savedValues.toMap() as remember/effect keys on every
    // recomposition, which would copy the entire class roster during unrelated UI updates.
    val draftSnapshot by remember(roster) {
        derivedStateOf { roster.associate { it.enrollment.id to values[it.enrollment.id].orEmpty() } }
    }
    val savedSnapshot by remember(roster) {
        derivedStateOf { roster.associate { it.enrollment.id to savedValues[it.enrollment.id].orEmpty() } }
    }
    LaunchedEffect(recoveryKey, draftSnapshot, savedSnapshot) {
        delay(300)
        val hasUnsavedChanges = draftSnapshot.any { (learnerId, mark) ->
            mark != savedSnapshot[learnerId].orEmpty()
        }
        if (!hasUnsavedChanges) {
            recoveryPreferences.edit().remove(recoveryKey).apply()
        } else {
            val snapshot = JSONObject()
            draftSnapshot.forEach { (learnerId, mark) -> snapshot.put(learnerId, mark) }
            recoveryPreferences.edit().putString(recoveryKey, snapshot.toString()).apply()
        }
    }

    fun requestAction(action: () -> Unit) {
        if (changedCount > 0) pendingAction = action else action()
    }

    if (pendingAction != null) {
        RvhConfirmDialog(
            title = "Unsaved marks",
            message = "You have $changedCount unsaved change${if (changedCount == 1) "" else "s"}. Discard them and continue?",
            confirmLabel = "Discard changes",
            dismissLabel = "Keep editing",
            destructive = true,
            onConfirm = {
                val action = pendingAction
                pendingAction = null
                clearRecoveryDraft()
                action?.invoke()
            },
            onDismiss = { pendingAction = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bulk marks") },
                navigationIcon = { IconButton(onClick = { requestAction(onBack) }, enabled = !isSaving) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!keyboardVisible) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("MARK ENTRY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text("$schoolClass", style = MaterialTheme.typography.headlineSmall)
                        Text("Academic year $year", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("${roster.size}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("LEARNERS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
            if (!keyboardVisible) Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("CURRENT MARKING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text("${term.displayName} · ${section.displayName}", style = MaterialTheme.typography.titleSmall)
                        Text(selectedLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = {
                        pickerTerm = term
                        pickerSection = section
                        pickerSubject = selectedSubject
                        requestAction { showAssessmentPicker = true }
                    }, enabled = !isSaving) {
                        Text("Change")
                    }
                }
            }
            if (!keyboardVisible) Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(selectedLabel, style = MaterialTheme.typography.titleLarge)
                    Text("Enter marks from 0 to 100", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text("$enteredCount / ${roster.size}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text("$missingCount missing", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text("$selectedLabel · $enteredCount/${roster.size} entered · $missingCount missing", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (!keyboardVisible) Surface(shape = MaterialTheme.shapes.small, color = if (changedCount == 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    if (changedCount == 0) "All changes saved · Blank marks remain missing" else "$changedCount unsaved change${if (changedCount == 1) "" else "s"} · Blank marks are not zero",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (changedCount == 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            if (!keyboardVisible) OutlinedTextField(
                value = learnerSearch,
                onValueChange = { learnerSearch = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search selected learners") },
                placeholder = { Text("Type a learner name or ID") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search learners") },
                trailingIcon = {
                    if (learnerSearch.isNotEmpty()) {
                        IconButton(onClick = { learnerSearch = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear learner search")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
            )
            if (!keyboardVisible) Text(
                if (learnerSearch.isBlank()) "Showing all ${roster.size} learners" else "Showing ${visibleRoster.size} of ${roster.size} learners",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                items(visibleRoster, key = { it.enrollment.id }) { entry ->
                    val markBringIntoView = remember(entry.enrollment.id) { BringIntoViewRequester() }
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(38.dp)) {
                                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Text(entry.learner.fullName.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString(""), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(entry.learner.fullName, style = MaterialTheme.typography.bodyLarge)
                                Text("Learner", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedTextField(
                                value = values[entry.enrollment.id].orEmpty(),
                                onValueChange = { raw ->
                                    if (isSaving) return@OutlinedTextField
                                    val digits = raw.filter(Char::isDigit).take(3)
                                    val valid = digits.isEmpty() || digits.toIntOrNull()?.let { it <= 100 } == true
                                    if (valid) {
                                        values[entry.enrollment.id] = digits
                                    }
                                    message = null
                                },
                                label = { Text("/100") },
                                trailingIcon = {
                                    if (!values[entry.enrollment.id].isNullOrEmpty()) {
                                        IconButton(onClick = {
                                            values[entry.enrollment.id] = ""
                                        }, enabled = !isSaving) { Icon(Icons.Default.Clear, contentDescription = "Clear mark") }
                                    }
                                },
                                singleLine = true,
                                enabled = !isSaving,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier.width(112.dp)
                                    .bringIntoViewRequester(markBringIntoView)
                                    .onFocusEvent { state ->
                                        if (state.isFocused) screenScope.launch {
                                            delay(120)
                                            markBringIntoView.bringIntoView()
                                        }
                                    }
                            )
                        }
                    }
                }
                if (visibleRoster.isEmpty()) {
                    item {
                        Text(
                            "No learners match ‘$learnerSearch’. Try another name or ID.",
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            message?.let { Text(it, color = if (it.startsWith("Saved")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    if (!isSaving) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        // Capture the UI state before switching to IO. Never write unchanged rows.
                        val changedEntries = roster.mapNotNull { entry ->
                            val id = entry.enrollment.id
                            val current = values[id].orEmpty()
                            if (current == savedValues[id].orEmpty()) null else Triple(entry, id, current)
                        }
                        val saveTerm = term
                        val saveSection = section
                        val saveSubject = selectedSubject
                        val saveLabel = selectedLabel
                        isSaving = true
                        message = "Saving ${changedEntries.size} mark${if (changedEntries.size == 1) "" else "s"}…"
                        screenScope.launch {
                            val outcomes = withContext(Dispatchers.IO) {
                                changedEntries.map { (entry, id, current) ->
                                    val error = runCatching {
                                        saveDraft(entry, saveTerm, saveSection, saveSubject, current.toIntOrNull())
                                    }.getOrElse { it.message ?: "Could not save marks" }
                                    Triple(id, current, error)
                                }
                            }
                            var savedCount = 0
                            val errors = mutableListOf<String>()
                            outcomes.forEachIndexed { index, (id, current, error) ->
                                if (error == null) {
                                    savedValues[id] = current
                                    savedCount++
                                } else {
                                    errors += "${changedEntries[index].first.learner.fullName}: $error"
                                }
                            }
                            // Edits may still be made while the database batch is running.
                            // Do not erase recovery data if the live UI now contains newer edits.
                            val hasNewerEdits = roster.any { entry ->
                                values[entry.enrollment.id].orEmpty() != savedValues[entry.enrollment.id].orEmpty()
                            }
                            if (errors.isEmpty() && !hasNewerEdits) clearRecoveryDraft() else persistRecoveryDraft()
                            message = when {
                                errors.isEmpty() -> "Saved $savedCount $saveLabel mark${if (savedCount == 1) "" else "s"}."
                                savedCount > 0 -> "Saved $savedCount; ${errors.size} failed. ${errors.first()}"
                                else -> errors.firstOrNull() ?: "No changed marks to save."
                            }
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = roster.isNotEmpty() && changedCount > 0 && !isSaving
            ) { Text(if (isSaving) "Saving marks…" else if (changedCount == 0) "All $selectedLabel marks saved" else "Save $changedCount change${if (changedCount == 1) "" else "s"}") }
        }
    }

    if (showAssessmentPicker) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val pickerScroll = rememberScrollState()
        val pickerTermLabel = pickerTerm.displayName
        val pickerSectionLabel = pickerSection.displayName
        val pickerSubjectLabel = subjects.firstOrNull { it.code == pickerSubject }?.displayName ?: pickerSubject
        ModalBottomSheet(
            onDismissRequest = { showAssessmentPicker = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 6.dp,
            dragHandle = {
                androidx.compose.material3.BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.outline
                )
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(pickerScroll)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Marking session", style = MaterialTheme.typography.titleLarge)
                            Text("Choose what you are entering marks for.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("CURRENT SELECTION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .78f))
                            Text("$pickerTermLabel · $pickerSectionLabel", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(pickerSubjectLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("$schoolClass · $year", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .78f))
                        }
                    }

                    PickerSection(title = "TERM", helper = "Select the school term") {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Term.entries.forEach { option ->
                                FilterChip(
                                    selected = pickerTerm == option,
                                    onClick = { pickerTerm = option },
                                    label = { Text(option.displayName) },
                                    leadingIcon = if (pickerTerm == option) ({ Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }) else null
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    PickerSection(title = "ASSESSMENT PERIOD", helper = "Choose when the assessment takes place") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TermSection.entries.forEach { option ->
                                val selected = pickerSection == option
                                Surface(
                                    onClick = { pickerSection = option },
                                    shape = MaterialTheme.shapes.medium,
                                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        androidx.compose.material3.RadioButton(selected = selected, onClick = { pickerSection = option })
                                        Text(option.displayName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge,
                                            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface)
                                        if (selected) Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    PickerSection(title = "SUBJECT", helper = "Choose the subject whose marks you are entering") {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            subjects.forEach { option ->
                                FilterChip(
                                    selected = pickerSubject == option.code,
                                    onClick = { pickerSubject = option.code },
                                    label = { Text(option.displayName) },
                                    leadingIcon = if (pickerSubject == option.code) ({ Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }) else null
                                )
                            }
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showAssessmentPicker = false },
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            requestAction {
                                onTermChange(pickerTerm)
                                onSectionChange(pickerSection)
                                selectedSubject = pickerSubject
                                showAssessmentPicker = false
                            }
                        },
                        modifier = Modifier.weight(1.35f).heightIn(min = 52.dp),
                        enabled = !isSaving
                    ) { Text("Apply selection") }
                }
            }
        }
    }
}

@Composable
private fun PickerSection(
    title: String,
    helper: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(helper, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        content()
    }
}
