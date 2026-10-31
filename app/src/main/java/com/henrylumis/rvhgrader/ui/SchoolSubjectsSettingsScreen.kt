package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.core.grading.SubjectCatalogRepository
import com.henrylumis.rvhgrader.model.SystemMode

private class EditableSubject(subject: SubjectCatalog.Subject) {
    var code by mutableStateOf(subject.code)
    var shortName by mutableStateOf(subject.shortName)
    var displayName by mutableStateOf(subject.displayName)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolSubjectsSettingsScreen(onOpenMenu: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val lowerRows = remember { mutableStateListOf<EditableSubject>().apply { addAll(SubjectCatalog.forMode(SystemMode.LOWER).map(::EditableSubject)) } }
    val upperRows = remember { mutableStateListOf<EditableSubject>().apply { addAll(SubjectCatalog.forMode(SystemMode.UPPER).map(::EditableSubject)) } }
    var message by remember { mutableStateOf<String?>(null) }
    var revisions by remember { mutableStateOf(SubjectCatalogRepository.history(context)) }

    Scaffold(topBar = { TopAppBar(title = { Text("SCHOOL SUBJECTS") }, navigationIcon = {
        IconButton(onClick = onOpenMenu) { Icon(Icons.Filled.Menu, contentDescription = "Open navigation") }
    }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Configure the subjects expected for each class group. Subject codes are permanent identifiers used by marks and reports; do not rename a code after results have been recorded.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                Text("Before changing subjects for a school with existing marks, make a backup. Removing a subject from the active list can make earlier assessments appear incomplete; this does not delete the stored marks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            item { SubjectGroupEditor("LOWER PRIMARY (P1–P3)", lowerRows) }
            item { SubjectGroupEditor("UPPER PRIMARY (P4–P7)", upperRows) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        keyboardController?.hide()
                        lowerRows.clear(); lowerRows.addAll(SubjectCatalog.defaultSubjects(SystemMode.LOWER).map(::EditableSubject))
                        upperRows.clear(); upperRows.addAll(SubjectCatalog.defaultSubjects(SystemMode.UPPER).map(::EditableSubject))
                        message = "Default lists loaded. Press Save to apply them."
                    }, modifier = Modifier.weight(1f)) { Text("LOAD DEFAULTS") }
                    Button(onClick = {
                        keyboardController?.hide()
                        val lower = lowerRows.map { SubjectCatalog.Subject(it.code.trim().lowercase(), it.shortName.trim(), it.displayName.trim()) }
                        val upper = upperRows.map { SubjectCatalog.Subject(it.code.trim().lowercase(), it.shortName.trim(), it.displayName.trim()) }
                        val error = validate(lower, upper)
                        if (error != null) message = error else runCatching {
                            SubjectCatalogRepository.save(context, lower, upper)
                            revisions = SubjectCatalogRepository.history(context)
                            onSaved()
                            message = "SUBJECT CONFIGURATION SAVED"
                        }.onFailure { message = it.message ?: "Could not save subject configuration." }
                    }, modifier = Modifier.weight(1f)) { Text("SAVE SUBJECTS") }
                }
            }
            item {
                HorizontalDivider()
                Text("SUBJECT CONFIGURATION HISTORY", style = MaterialTheme.typography.titleMedium)
                Text("Each saved change is kept as a local revision. Restoring a revision creates a new revision; it does not rewrite historical marks or guarantee that old reports are pinned to the rules used at the time.", style = MaterialTheme.typography.bodySmall)
            }
            if (revisions.isEmpty()) {
                item { Text("No saved revisions yet. Save your current subject setup to start the history.", style = MaterialTheme.typography.bodySmall) }
            } else {
                items(revisions.size) { index ->
                    val revision = revisions[index]
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Revision ${revision.revision}", style = MaterialTheme.typography.titleSmall)
                                val date = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(revision.savedAtMillis))
                                Text(date, style = MaterialTheme.typography.bodySmall)
                                Text("Lower: ${revision.lower.size} subjects • Upper: ${revision.upper.size} subjects", style = MaterialTheme.typography.bodySmall)
                            }
                            OutlinedButton(onClick = {
                                keyboardController?.hide()
                                runCatching {
                                    SubjectCatalogRepository.restore(context, revision.revision)
                                    lowerRows.clear(); lowerRows.addAll(SubjectCatalog.forMode(SystemMode.LOWER).map(::EditableSubject))
                                    upperRows.clear(); upperRows.addAll(SubjectCatalog.forMode(SystemMode.UPPER).map(::EditableSubject))
                                    revisions = SubjectCatalogRepository.history(context)
                                    onSaved()
                                    message = "RESTORED REVISION ${revision.revision} AS THE ACTIVE SUBJECT SET"
                                }.onFailure { message = it.message ?: "Could not restore this revision." }
                            }) { Text("RESTORE") }
                        }
                    }
                }
            }
            message?.let { msg -> item {
                RvhFeedbackBanner(
                    message = msg,
                    kind = if (msg.startsWith("SUBJECT CONFIGURATION SAVED") || msg.startsWith("RESTORED REVISION") || msg.startsWith("Default lists loaded")) RvhFeedbackKind.Success else RvhFeedbackKind.Error
                )
            } }
        }
    }
}

@Composable
private fun SubjectGroupEditor(title: String, rows: androidx.compose.runtime.snapshots.SnapshotStateList<EditableSubject>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            rows.forEachIndexed { index, row ->
                RvhFormTextField(value = row.displayName, onValueChange = { row.displayName = it }, label = "SUBJECT NAME", helperText = "Name shown to teachers and on reports.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RvhFormTextField(value = row.code, onValueChange = { row.code = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' }.take(20) }, label = "CODE", modifier = Modifier.weight(1f), helperText = "Permanent ID")
                    RvhFormTextField(value = row.shortName, onValueChange = { row.shortName = it.take(10) }, label = "SHORT NAME", modifier = Modifier.weight(1f), helperText = "Up to 10 characters")
                    TextButton(onClick = { if (rows.size > 1) rows.removeAt(index) }) { Text("REMOVE") }
                }
                HorizontalDivider()
            }
            TextButton(onClick = { rows.add(EditableSubject(SubjectCatalog.Subject("sub${rows.size + 1}", "SUB", "New Subject"))) }) { Text("+ ADD SUBJECT") }
        }
    }
}

private fun validate(lower: List<SubjectCatalog.Subject>, upper: List<SubjectCatalog.Subject>): String? {
    if (lower.isEmpty() || upper.isEmpty()) return "Each class group must have at least one subject."
    for ((label, subjects) in listOf("Lower-primary" to lower, "Upper-primary" to upper)) {
        if (subjects.any { it.code.isBlank() || it.shortName.isBlank() || it.displayName.isBlank() }) return "$label: complete every subject field."
        if (subjects.any { !it.code.matches(Regex("[a-z][a-z0-9_]{1,19}")) }) return "$label: codes must start with a letter and use lowercase letters, digits or underscores."
        if (subjects.map { it.code }.distinct().size != subjects.size) return "$label: subject codes must be unique."
        if (subjects.map { it.displayName.trim().lowercase() }.distinct().size != subjects.size) return "$label: subject names must be unique."
    }
    return null
}
