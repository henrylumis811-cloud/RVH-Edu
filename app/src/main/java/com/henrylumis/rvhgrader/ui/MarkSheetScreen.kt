package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.assessment.RosterEntry
import com.henrylumis.rvhgrader.core.grading.AssessmentWorkflow
import com.henrylumis.rvhgrader.core.model.AcademicAssessment
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkSheetScreen(
    entry: RosterEntry,
    term: Term,
    section: TermSection,
    assessment: AcademicAssessment?,
    existingMarks: List<Mark>,
    onTermChange: (Term) -> Unit,
    onSectionChange: (TermSection) -> Unit,
    onLoadAssessment: () -> Unit,
    onSaveDraft: (Map<String, String>) -> String?,
    onFinalize: (Map<String, String>) -> String?,
    onReopen: () -> String?,
    onBack: () -> Unit
) {
    val values = remember(entry.enrollment.id, term, section) { mutableStateMapOf<String, String>() }
    var message by remember { mutableStateOf<String?>(null) }
    var savedValues by remember(entry.enrollment.id, term, section) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var pendingNavigation by remember { mutableStateOf<(() -> Unit)?>(null) }
    val currentValues = values.toMap()
    val hasUnsavedChanges = currentValues != savedValues
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(existingMarks, assessment?.id) {
        values.clear()
        existingMarks.forEach { values[it.subjectCode] = it.mark.toString() }
        savedValues = values.toMap()
        message = null
    }

    val subjects = SubjectCatalog.forClass(entry.enrollment.schoolClass)
    val enteredCount = subjects.count { !values[it.code].isNullOrBlank() }
    val missingCount = (subjects.size - enteredCount).coerceAtLeast(0)
    val missingSubjectNames = subjects.filter { values[it.code].isNullOrBlank() }.map { it.displayName }
    fun requestNavigation(action: () -> Unit) {
        if (hasUnsavedChanges && assessment?.status != com.henrylumis.rvhgrader.core.model.AssessmentStatus.FINAL) pendingNavigation = action
        else action()
    }
    Scaffold(topBar = { TopAppBar(title = { Text(entry.learner.fullName) }, navigationIcon = { androidx.compose.material3.IconButton(onClick = { requestNavigation(onBack) }) { androidx.compose.material3.Icon(Icons.Default.ArrowBack, "Back") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("${entry.enrollment.schoolClass.displayName} • ${entry.enrollment.academicYear}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Term.entries.forEach { t -> OutlinedButton(onClick = { requestNavigation { onTermChange(t) } }) { Text(if (t == term) "✓ ${t.displayName}" else t.displayName) } }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                TermSection.entries.forEach { s -> OutlinedButton(onClick = { requestNavigation { onSectionChange(s) } }) { Text(if (s == section) "✓ ${s.displayName}" else s.displayName) } }
            }
            Spacer(Modifier.height(12.dp))
            Text("Enter marks", style = MaterialTheme.typography.titleLarge)
            Text("$enteredCount entered • $missingCount missing", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Missing marks remain missing; they are never silently treated as zero.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                when {
                    assessment?.status == com.henrylumis.rvhgrader.core.model.AssessmentStatus.FINAL -> "FINAL • Locked for editing"
                    missingCount > 0 -> "DRAFT • INCOMPLETE • $missingCount subject(s) missing"
                    else -> "DRAFT • READY TO FINALIZE"
                },
                color = if (assessment?.status == com.henrylumis.rvhgrader.core.model.AssessmentStatus.FINAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.weight(1f)) {
                items(subjects, key = { it.code }) { subject ->
                    OutlinedTextField(
                        value = values[subject.code].orEmpty(),
                        onValueChange = { raw ->
                            val digits = raw.filter(Char::isDigit).take(3)
                            val valid = digits.isEmpty() || digits.toIntOrNull()?.let { it <= 100 } == true
                            if (valid) values[subject.code] = digits
                            message = null
                        },
                        enabled = assessment?.status != com.henrylumis.rvhgrader.core.model.AssessmentStatus.FINAL,
                        label = { Text(subject.displayName) },
                        supportingText = { Text("/ 100") },
                        trailingIcon = {
                            if (!values[subject.code].isNullOrEmpty()) {
                                androidx.compose.material3.IconButton(onClick = { values[subject.code] = "" }) {
                                    androidx.compose.material3.Icon(Icons.Default.Clear, contentDescription = "Clear mark")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (assessment?.status != com.henrylumis.rvhgrader.core.model.AssessmentStatus.FINAL && missingCount > 0) {
                Text("Still missing: ${missingSubjectNames.joinToString(", ")}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (hasUnsavedChanges && assessment?.status != com.henrylumis.rvhgrader.core.model.AssessmentStatus.FINAL) {
                Text("Unsaved changes", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
            }
            message?.let { Text(it, color = if (it.startsWith("Saved") || it.startsWith("Draft saved") || it.startsWith("Assessment finalized") || it.startsWith("Reopened")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(8.dp))
            if (assessment?.status == com.henrylumis.rvhgrader.core.model.AssessmentStatus.FINAL) {
                Button(onClick = { keyboardController?.hide(); focusManager.clearFocus(); message = onReopen() ?: "Reopened for editing." }, modifier = Modifier.fillMaxWidth()) {
                    Text("Reopen for editing")
                }
            } else {
                OutlinedButton(onClick = { keyboardController?.hide(); focusManager.clearFocus(); val error = onSaveDraft(values.toMap()); message = error ?: "Draft saved."; if (error == null) savedValues = values.toMap() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Save draft")
                }
                Button(onClick = { keyboardController?.hide(); focusManager.clearFocus(); val error = onFinalize(values.toMap()); message = error ?: "Assessment finalized."; if (error == null) savedValues = values.toMap() }, enabled = missingCount == 0, modifier = Modifier.fillMaxWidth()) {
                    Text("Finalize assessment")
                }
            }
        }
    }
    if (pendingNavigation != null) {
        RvhConfirmDialog(
            title = "Unsaved marks",
            message = "You have changed marks that have not been saved. Leave this assessment and discard those changes?",
            confirmLabel = "Discard changes",
            dismissLabel = "Keep editing",
            destructive = true,
            onConfirm = { val action = pendingNavigation; pendingNavigation = null; action?.invoke() },
            onDismiss = { pendingNavigation = null }
        )
    }
}
