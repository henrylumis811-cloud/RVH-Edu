package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.export.ExportFormat
import com.henrylumis.rvhgrader.grading.GradingLogic
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.SystemMode
import com.henrylumis.rvhgrader.model.StudentRecord
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    selectedClass: SchoolClass,
    onClassChange: (SchoolClass) -> Unit,
    selectedTerm: Term,
    onTermChange: (Term) -> Unit,
    selectedSection: TermSection,
    onSectionChange: (TermSection) -> Unit,
    fieldValues: MutableMap<String, String>,
    nameValue: String,
    onNameChange: (String) -> Unit,
    onFieldChange: (String, String) -> Unit,
    onCommit: () -> Unit,
    records: List<StudentRecord>,
    onOpenMenu: () -> Unit,
    onExportClassList: (ExportFormat) -> Unit
) {
    val mode = selectedClass.mode
    var showExportDialog by remember { mutableStateOf(false) }
    // Always ranked here at display time, regardless of insertion/edit order — positions must
    // never depend on the order records happened to be added or committed in.
    val ranked = remember(records) { records.sortedByDescending { it.total } }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("HENRY LUMIS MAINFRAME") },
                    navigationIcon = {
                        IconButton(onClick = onOpenMenu) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Filled.Share, contentDescription = "Export class list")
                        }
                    }
                )
                Text(
                    "In dedication to Hellen",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClassDropdown(selectedClass, onClassChange, Modifier.weight(1f))
                    TermDropdown(selectedTerm, onTermChange, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 0.dp).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionDropdown(selectedSection, onSectionChange, Modifier.weight(1f))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InputFormCard(
                    mode = mode,
                    nameValue = nameValue,
                    onNameChange = onNameChange,
                    fieldValues = fieldValues,
                    onFieldChange = onFieldChange,
                    onCommit = onCommit
                )
            }
            item {
                Text(
                    "${selectedClass.displayName} — ${selectedTerm.displayName} — ${selectedSection.displayName} STANDINGS",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            if (ranked.isEmpty()) {
                item {
                    Text(
                        "SYSTEM VACANT. AWAITING DATA.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                itemsIndexed(ranked) { index, learner ->
                    StudentCard(position = index + 1, learner = learner, mode = mode)
                }
            }
        }
    }

    if (showExportDialog) {
        ExportFormatDialog(
            title = "Export ${selectedClass.displayName} — ${selectedTerm.displayName} — ${selectedSection.displayName} class list",
            onDismiss = { showExportDialog = false },
            onConfirm = { format ->
                showExportDialog = false
                onExportClassList(format)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassDropdown(selected: SchoolClass, onChange: (SchoolClass) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("CLASS") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SchoolClass.values().forEach { sc ->
                DropdownMenuItem(text = { Text(sc.displayName) }, onClick = { onChange(sc); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TermDropdown(selected: Term, onChange: (Term) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("TERM") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Term.values().forEach { t ->
                DropdownMenuItem(text = { Text(t.displayName) }, onClick = { onChange(t); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionDropdown(selected: TermSection, onChange: (TermSection) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("SECTION") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TermSection.values().forEach { s ->
                DropdownMenuItem(text = { Text(s.displayName) }, onClick = { onChange(s); expanded = false })
            }
        }
    }
}

@Composable
private fun InputFormCard(
    mode: SystemMode,
    nameValue: String,
    onNameChange: (String) -> Unit,
    fieldValues: MutableMap<String, String>,
    onFieldChange: (String, String) -> Unit,
    onCommit: () -> Unit
) {
    val subjects = GradingLogic.subjectsFor(mode)

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("DATA STREAM INPUT", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = nameValue,
                onValueChange = onNameChange,
                label = { Text("LEARNER NAME") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            subjects.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pair.forEach { subject ->
                        OutlinedTextField(
                            value = fieldValues[subject].orEmpty(),
                            onValueChange = { newVal ->
                                onFieldChange(subject, newVal.filter { it.isDigit() }.take(3))
                            },
                            label = { Text(GradingLogic.subjectLabels[subject] ?: subject.uppercase()) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }

            Button(onClick = onCommit, modifier = Modifier.fillMaxWidth()) {
                Text("EXECUTE MATRIX")
            }
        }
    }
}

@Composable
private fun StudentCard(position: Int, learner: StudentRecord, mode: SystemMode) {
    val subjects = GradingLogic.subjectsFor(mode)
    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("POS: $position", style = MaterialTheme.typography.labelLarge)
                Text(learner.name, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                subjects.forEach { subject ->
                    val score = learner.scores[subject]
                    val label = GradingLogic.subjectLabels[subject] ?: subject.uppercase()
                    val aggText = if (learner.graded && score?.agg != null) " (${score.agg})" else ""
                    Text(
                        "$label: ${score?.mark ?: 0}$aggText",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TOT: ${learner.total}", style = MaterialTheme.typography.labelMedium)
                if (learner.graded) {
                    Text("AGG: ${learner.aggSum}", style = MaterialTheme.typography.labelMedium)
                    Text("DIV: ${learner.division}", style = MaterialTheme.typography.labelMedium)
                } else {
                    val avg = if (subjects.isNotEmpty()) learner.total.toFloat() / subjects.size else 0f
                    Text("AVG: ${"%.1f".format(avg)}%", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
