package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import com.henrylumis.rvhgrader.core.assessment.RosterEntry
import com.henrylumis.rvhgrader.core.repository.PossibleLearnerDuplicate
import com.henrylumis.rvhgrader.model.SchoolClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnerManagementScreen(
    year: Int,
    selectedClass: SchoolClass,
    roster: List<RosterEntry>,
    onYearChange: (Int) -> Unit,
    onClassChange: (SchoolClass) -> Unit,
    onAddLearner: (String, Int?, String?) -> String?,
    onUpdateLearner: (RosterEntry, String, Int?, String?) -> String?,
    onFindDuplicates: (String, Int?, String?, String?) -> List<PossibleLearnerDuplicate>,
    onArchiveLearner: (RosterEntry, String, String?) -> Unit,
    onOpenMarks: (RosterEntry) -> Unit,
    onOpenBulkMarks: () -> Unit,
    onOpenProfile: (RosterEntry) -> Unit,
    onOpenMenu: () -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var learnerToArchive by remember { mutableStateOf<RosterEntry?>(null) }
    var learnerToEdit by remember { mutableStateOf<RosterEntry?>(null) }
    val visibleRoster = roster.filter { it.learner.fullName.contains(search, ignoreCase = true) || (it.learner.admissionNumber?.contains(search, ignoreCase = true) == true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Learners", fontWeight = FontWeight.Bold)
                        Text("STUDENT DIRECTORY", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.1f.sp)
                    }
                },
                navigationIcon = { IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Groups, contentDescription = "Open navigation") } },
                actions = { IconButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, contentDescription = "Add learner") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(46.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(25.dp))
                                    }
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(selectedClass.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Academic year $year", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .8f))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${roster.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("LEARNERS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .8f))
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .14f))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text("CLASS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .75f))
                                    SimpleDropdown(value = selectedClass.displayName, options = SchoolClass.entries, label = "Select class", modifier = Modifier.fillMaxWidth(), optionLabel = { it.displayName }, onSelected = onClassChange)
                                }
                                Column(Modifier.weight(.72f)) {
                                    Text("YEAR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .75f))
                                    var yearDraft by remember(year) { mutableStateOf(year.toString()) }
                                    OutlinedTextField(
                                        value = yearDraft,
                                        onValueChange = { raw ->
                                            yearDraft = raw.filter(Char::isDigit).take(4)
                                            yearDraft.toIntOrNull()?.takeIf { yearDraft.length == 4 && it in 2000..2100 }?.let(onYearChange)
                                        },
                                        label = { Text("Academic year") },
                                        supportingText = {
                                            Text(if (yearDraft.length == 4 && yearDraft.toIntOrNull() in 2000..2100) "Year selected" else "Enter a year from 2000 to 2100")
                                        },
                                        isError = yearDraft.length == 4 && (yearDraft.toIntOrNull() !in 2000..2100),
                                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = search, onValueChange = { search = it },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        label = { Text("Search by name or admission number") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = { if (search.isNotEmpty()) IconButton(onClick = { search = "" }) { Text("Clear", style = MaterialTheme.typography.labelMedium) } }
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Class roster", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("${visibleRoster.size} shown · ${roster.size} enrolled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(onClick = onOpenBulkMarks, enabled = roster.isNotEmpty()) { Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Bulk marks") }
                    }
                }
                if (roster.isEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(64.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(30.dp)) }
                                }
                                Spacer(Modifier.height(14.dp))
                                Text("Your class starts here", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text("Add the first learner to build your roster. Names are required; admission numbers and other details can be added later.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, contentDescription = null); Spacer(Modifier.size(8.dp)); Text("Add first learner") }
                            }
                        }
                    }
                } else if (visibleRoster.isEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No matching learners", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("Try another name or admission number.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(onClick = { search = "" }) { Text("Clear search") }
                            }
                        }
                    }
                } else {
                    items(visibleRoster, key = { it.enrollment.id }) { entry ->
                        LearnerRosterCard(
                            entry = entry,
                            onEdit = { learnerToEdit = entry },
                            onProfile = { onOpenProfile(entry) },
                            onMarks = { onOpenMarks(entry) },
                            onArchive = { learnerToArchive = entry }
                        )
                    }
                }
            }
        }
    }

    learnerToEdit?.let { entry ->
        EditLearnerDialog(
            entry = entry,
            onDismiss = { learnerToEdit = null },
            onFindDuplicates = onFindDuplicates,
            onSave = { name, age, admissionNumber ->
                val error = onUpdateLearner(entry, name, age, admissionNumber)
                if (error == null) learnerToEdit = null
                error
            }
        )
    }

    learnerToArchive?.let { entry ->
        ArchiveLearnerDialog(
            learnerName = entry.learner.fullName,
            onDismiss = { learnerToArchive = null },
            onConfirm = { reason, note -> onArchiveLearner(entry, reason, note); learnerToArchive = null }
        )
    }

    if (showAdd) {
        AddLearnerDialog(
            onDismiss = { showAdd = false },
            onFindDuplicates = onFindDuplicates,
            onSave = { name, age, admissionNumber ->
                val error = onAddLearner(name, age, admissionNumber)
                if (error == null) showAdd = false
                error
            }
        )
    }
}


@Composable
private fun LearnerRosterCard(
    entry: RosterEntry,
    onEdit: () -> Unit,
    onProfile: () -> Unit,
    onMarks: () -> Unit,
    onArchive: () -> Unit
) {
    var menuExpanded by remember(entry.enrollment.id) { mutableStateOf(false) }
    val name = entry.learner.fullName.trim()
    val initials = name.split(Regex("\\s+")).filter { it.isNotBlank() }.take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(initials.ifBlank { "?" }, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    val details = listOfNotNull(entry.learner.ageYears?.let { "Age $it" }, entry.learner.gender, entry.enrollment.stream?.let { "Stream $it" }).joinToString(" · ")
                    if (details.isNotBlank()) Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    entry.learner.admissionNumber?.takeIf { it.isNotBlank() }?.let {
                        Text("Admission no. $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More learner actions") }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("View profile") }, leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }, onClick = { menuExpanded = false; onProfile() })
                        DropdownMenuItem(text = { Text("Edit details") }, leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }, onClick = { menuExpanded = false; onEdit() })
                        DropdownMenuItem(text = { Text("Archive learner") }, leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }, onClick = { menuExpanded = false; onArchive() })
                    }
                }
            }
            Button(onClick = onMarks, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Enter or review marks")
            }
        }
    }
}

@Composable
private fun ArchiveLearnerDialog(
    learnerName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var reason by remember(learnerName) { mutableStateOf("Left school") }
    var note by remember(learnerName) { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Archive learner?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$learnerName will be removed from active class lists. Marks and academic history will be preserved.")
                RvhFormTextField(
                    value = reason, onValueChange = { reason = it; error = null },
                    label = "Reason (required)", helperText = "Examples: left school, transferred, withdrawn, duplicate record",
                    errorText = error
                )
                RvhFormTextField(
                    value = note, onValueChange = { if (it.length <= 500) note = it },
                    label = "Note (optional)", singleLine = false, minLines = 2,
                    helperText = "Up to 500 characters"
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (reason.isBlank()) error = "Enter a reason to keep the archive record auditable."
                else onConfirm(reason.trim(), note.trim().takeIf { it.isNotEmpty() })
            }) { Text("Archive learner") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddLearnerDialog(
    onDismiss: () -> Unit,
    onFindDuplicates: (String, Int?, String?, String?) -> List<PossibleLearnerDuplicate>,
    onSave: (String, Int?, String?) -> String?
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var admissionNumber by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var candidates by remember { mutableStateOf<List<PossibleLearnerDuplicate>>(emptyList()) }
    var pendingReview by remember { mutableStateOf(false) }
    fun attemptSave(force: Boolean = false) {
        val cleanedName = name.trim().replace(Regex("\\s+"), " ")
        if (cleanedName.isBlank()) {
            error = "Full name is required. Enter the learner's name before saving."
            return
        }
        val found = onFindDuplicates(cleanedName, age.toIntOrNull(), admissionNumber.trim().ifBlank { null }, null)
        if (found.isNotEmpty() && !force) {
            candidates = found
            pendingReview = true
        } else {
            error = onSave(cleanedName, age.toIntOrNull(), admissionNumber.trim().ifBlank { null })
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add learner") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Only the name is required. You can add formal details later.")
                RvhFormTextField(
                    name, { name = it; error = null }, label = "Full name *",
                    helperText = if (name.isBlank()) "Required — the learner's name appears on reports and certificates." else "Looks good — this name will appear on reports and certificates."
                )
                RvhFormTextField(
                    age, { age = it.filter(Char::isDigit); error = null }, label = "Age (optional)",
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                )
                RvhFormTextField(
                    admissionNumber, { admissionNumber = it; error = null },
                    label = "Admission number (optional)", imeAction = ImeAction.Done
                )
                error?.let { RvhFeedbackBanner(message = it, kind = RvhFeedbackKind.Error) }
            }
        },
        confirmButton = { Button(onClick = { attemptSave() }) { Text("Save") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
    if (pendingReview && candidates.isNotEmpty()) {
        DuplicateReviewDialog(candidates, onCancel = { pendingReview = false; candidates = emptyList() }, onContinue = {
            pendingReview = false; candidates = emptyList(); attemptSave(force = true)
        })
    }
}

@Composable
private fun EditLearnerDialog(
    entry: RosterEntry,
    onDismiss: () -> Unit,
    onFindDuplicates: (String, Int?, String?, String?) -> List<PossibleLearnerDuplicate>,
    onSave: (String, Int?, String?) -> String?
) {
    var name by remember(entry.learner.id) { mutableStateOf(entry.learner.fullName) }
    var age by remember(entry.learner.id) { mutableStateOf(entry.learner.ageYears?.toString().orEmpty()) }
    var admissionNumber by remember(entry.learner.id) { mutableStateOf(entry.learner.admissionNumber.orEmpty()) }
    var error by remember(entry.learner.id) { mutableStateOf<String?>(null) }
    var candidates by remember(entry.learner.id) { mutableStateOf<List<PossibleLearnerDuplicate>>(emptyList()) }
    var pendingReview by remember(entry.learner.id) { mutableStateOf(false) }
    fun attemptSave(force: Boolean = false) {
        val cleanedName = name.trim().replace(Regex("\\s+"), " ")
        if (cleanedName.isBlank()) {
            error = "Full name is required. Enter the learner's name before saving."
            return
        }
        val found = onFindDuplicates(cleanedName, age.toIntOrNull(), admissionNumber.trim().ifBlank { null }, entry.learner.id)
        if (found.isNotEmpty() && !force) {
            candidates = found
            pendingReview = true
        } else {
            error = onSave(cleanedName, age.toIntOrNull(), admissionNumber.trim().ifBlank { null })
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit learner") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Changes apply to the learner's record and keep their academic history.")
                RvhFormTextField(
                    name, { name = it; error = null }, label = "Full name *",
                    helperText = if (name.isBlank()) "Required — the learner's name appears on reports and certificates." else "Looks good — this name will appear on reports and certificates."
                )
                RvhFormTextField(
                    age, { age = it.filter(Char::isDigit); error = null }, label = "Age (optional)",
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                )
                RvhFormTextField(
                    admissionNumber, { admissionNumber = it; error = null },
                    label = "Admission number (optional)", imeAction = ImeAction.Done
                )
                error?.let { RvhFeedbackBanner(message = it, kind = RvhFeedbackKind.Error) }
            }
        },
        confirmButton = { Button(onClick = { attemptSave() }) { Text("Save changes") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
    if (pendingReview && candidates.isNotEmpty()) {
        DuplicateReviewDialog(candidates, onCancel = { pendingReview = false; candidates = emptyList() }, onContinue = {
            pendingReview = false; candidates = emptyList(); attemptSave(force = true)
        })
    }
}

@Composable
private fun DuplicateReviewDialog(
    candidates: List<PossibleLearnerDuplicate>,
    onCancel: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Possible existing learner") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Review possible matches before continuing. Similar details are warnings, not proof of identity.")
                candidates.take(6).forEach { candidate ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(10.dp)) {
                            Text(candidate.learner.fullName, style = MaterialTheme.typography.titleSmall)
                            candidate.learner.admissionNumber?.let { Text("Admission: $it") }
                            Text(if (candidate.confidence == PossibleLearnerDuplicate.Confidence.HIGH) "Strong match warning" else "Possible match")
                            Text(candidate.reasons.joinToString(" • "), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (candidates.size > 6) Text("And ${candidates.size - 6} more possible matches.")
                Text("No existing record will be merged or changed by this review.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { Button(onClick = onContinue) { Text("Different learner — continue") } },
        dismissButton = { OutlinedButton(onClick = onCancel) { Text("Go back") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SimpleDropdown(
    value: String,
    options: List<T>,
    label: String,
    modifier: Modifier = Modifier,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(value, {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor())
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(optionLabel(option)) }, onClick = { expanded = false; onSelected(option) }) }
        }
    }
}
