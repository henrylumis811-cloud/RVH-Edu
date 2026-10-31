package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.grading.AssessmentWorkflow
import com.henrylumis.rvhgrader.core.assessment.RosterEntry
import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkMarkEntryScreen(
    year: Int,
    schoolClass: com.henrylumis.rvhgrader.model.SchoolClass,
    term: Term,
    section: TermSection,
    roster: List<RosterEntry>,
    onTermChange: (Term) -> Unit,
    onSectionChange: (TermSection) -> Unit,
    loadMarks: (RosterEntry, Term, TermSection) -> List<Mark>,
    saveDraft: (RosterEntry, Term, TermSection, String, Int?) -> String?,
    onBack: () -> Unit
) {
    val subjects = SubjectCatalog.forClass(schoolClass)
    var selectedSubject by remember(schoolClass) { mutableStateOf(subjects.firstOrNull()?.code.orEmpty()) }
    val values = remember { mutableStateMapOf<String, String>() }
    val savedValues = remember { mutableStateMapOf<String, String>() }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
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
        roster.forEach { entry ->
            val value = loadMarks(entry, term, section)
                .firstOrNull { it.subjectCode == selectedSubject }?.mark?.toString().orEmpty()
            values[entry.enrollment.id] = value
            savedValues[entry.enrollment.id] = value
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
    val enteredCount = roster.count { !values[it.enrollment.id].isNullOrBlank() }
    val missingCount = (roster.size - enteredCount).coerceAtLeast(0)
    val changedCount = roster.count { values[it.enrollment.id].orEmpty() != savedValues[it.enrollment.id].orEmpty() }

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
                navigationIcon = { IconButton(onClick = { requestAction(onBack) }) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Assessment period", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        Term.entries.forEach { t ->
                            if (t == term) Button(onClick = {}, enabled = false) { Text(t.displayName) }
                            else OutlinedButton(onClick = { requestAction { onTermChange(t) } }) { Text(t.displayName) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        TermSection.entries.forEach { sec ->
                            if (sec == section) Button(onClick = {}, enabled = false) { Text(sec.displayName) }
                            else OutlinedButton(onClick = { requestAction { onSectionChange(sec) } }) { Text(sec.displayName) }
                        }
                    }
                    Text("Subject", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        subjects.forEach { subject ->
                            if (subject.code == selectedSubject) Button(onClick = {}, enabled = false) { Text(subject.displayName) }
                            else OutlinedButton(onClick = { requestAction { selectedSubject = subject.code } }) { Text(subject.displayName) }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(selectedLabel, style = MaterialTheme.typography.titleLarge)
                    Text("Enter marks from 0 to 100", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text("$enteredCount / ${roster.size}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text("$missingCount missing", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(shape = MaterialTheme.shapes.small, color = if (changedCount == 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    if (changedCount == 0) "All changes saved · Blank marks remain missing" else "$changedCount unsaved change${if (changedCount == 1) "" else "s"} · Blank marks are not zero",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (changedCount == 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(roster, key = { it.enrollment.id }) { entry ->
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
                                    val digits = raw.filter(Char::isDigit).take(3)
                                    val valid = digits.isEmpty() || digits.toIntOrNull()?.let { it <= 100 } == true
                                    if (valid) {
                                        values[entry.enrollment.id] = digits
                                        persistRecoveryDraft()
                                    }
                                    message = null
                                },
                                label = { Text("/100") },
                                trailingIcon = {
                                    if (!values[entry.enrollment.id].isNullOrEmpty()) {
                                        IconButton(onClick = {
                                            values[entry.enrollment.id] = ""
                                            persistRecoveryDraft()
                                        }) { Icon(Icons.Default.Clear, contentDescription = "Clear mark") }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier.weight(.42f)
                            )
                        }
                    }
                }
            }
            message?.let { Text(it, color = if (it.startsWith("Saved")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    // Never write unchanged rows: an unchanged blank must remain untouched,
                    // and a finalized assessment in another row must not block unrelated edits.
                    val changedEntries = roster.filter { entry ->
                        values[entry.enrollment.id].orEmpty() != savedValues[entry.enrollment.id].orEmpty()
                    }
                    var savedCount = 0
                    val errors = mutableListOf<String>()
                    changedEntries.forEach { entry ->
                        val learnerId = entry.enrollment.id
                        val error = saveDraft(
                            entry, term, section, selectedSubject,
                            values[learnerId]?.toIntOrNull()
                        )
                        if (error == null) {
                            savedValues[learnerId] = values[learnerId].orEmpty()
                            savedCount++
                        } else {
                            errors += "${entry.learner.fullName}: $error"
                        }
                    }
                    if (errors.isEmpty()) clearRecoveryDraft() else persistRecoveryDraft()
                    message = when {
                        errors.isEmpty() -> "Saved $savedCount $selectedLabel mark${if (savedCount == 1) "" else "s"}."
                        savedCount > 0 -> "Saved $savedCount; ${errors.size} failed. ${errors.first()}"
                        else -> errors.firstOrNull() ?: "No changed marks to save."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = roster.isNotEmpty() && changedCount > 0
            ) { Text(if (changedCount == 0) "All $selectedLabel marks saved" else "Save $changedCount change${if (changedCount == 1) "" else "s"}") }
        }
    }
}
