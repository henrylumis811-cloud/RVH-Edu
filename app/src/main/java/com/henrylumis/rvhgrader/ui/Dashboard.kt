package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
    val ranked = remember(records) { records.sortedByDescending { it.total } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(38.dp).clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text("RVH EDU", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("QUICK GRADING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open navigation")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export class list")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SessionSelectionCard(
                    selectedClass = selectedClass,
                    onClassChange = onClassChange,
                    selectedTerm = selectedTerm,
                    onTermChange = onTermChange,
                    selectedSection = selectedSection,
                    onSectionChange = onSectionChange
                )
            }
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Class standings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "${selectedClass.displayName} • ${selectedTerm.displayName} • ${selectedSection.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (ranked.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(46.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Text("No quick-entry results yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Saved learner results will appear here, ranked by total marks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(ranked, key = { _, learner -> learner.id }) { index, learner ->
                    StudentCard(position = index + 1, learner = learner, mode = mode)
                }
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("RVH EDU", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(3.dp))
                    Text("In dedication to Hellen", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
private fun SessionSelectionCard(
    selectedClass: SchoolClass,
    onClassChange: (SchoolClass) -> Unit,
    selectedTerm: Term,
    onTermChange: (Term) -> Unit,
    selectedSection: TermSection,
    onSectionChange: (TermSection) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Marking session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Choose the class and assessment period for this quick entry.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ClassDropdown(selectedClass, onClassChange, Modifier.weight(1f))
                TermDropdown(selectedTerm, onTermChange, Modifier.weight(1f))
            }
            SectionDropdown(selectedSection, onSectionChange, Modifier.fillMaxWidth())
        }
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
            label = { Text("Class") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SchoolClass.values().forEach { schoolClass ->
                DropdownMenuItem(text = { Text(schoolClass.displayName) }, onClick = { onChange(schoolClass); expanded = false })
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
            label = { Text("Term") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Term.values().forEach { term ->
                DropdownMenuItem(text = { Text(term.displayName) }, onClick = { onChange(term); expanded = false })
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
            label = { Text("Assessment period") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TermSection.values().forEach { section ->
                DropdownMenuItem(text = { Text(section.displayName) }, onClick = { onChange(section); expanded = false })
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Quick mark entry", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Enter one learner's marks. All required subjects must be complete before saving.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedTextField(
                value = nameValue,
                onValueChange = onNameChange,
                label = { Text("Learner name") },
                placeholder = { Text("Enter full name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth()
            )
            Text("Subject marks", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            subjects.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    pair.forEach { subject ->
                        OutlinedTextField(
                            value = fieldValues[subject].orEmpty(),
                            onValueChange = { newValue -> onFieldChange(subject, newValue.filter(Char::isDigit).take(3)) },
                            label = { Text(GradingLogic.subjectLabels[subject] ?: subject.uppercase(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            placeholder = { Text("0–100") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Button(
                onClick = onCommit,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save quick-entry result", fontWeight = FontWeight.SemiBold)
            }
            Text(
                "For incomplete work, use the learner mark sheet and save a draft.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StudentCard(position: Int, learner: StudentRecord, mode: SystemMode) {
    val subjects = GradingLogic.subjectsFor(mode)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "#$position",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(learner.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                subjects.forEach { subject ->
                    val score = learner.scores[subject]
                    val label = GradingLogic.subjectLabels[subject] ?: subject.uppercase()
                    val markText = score?.mark?.toString() ?: "—"
                    val aggregateText = if (learner.graded && score?.agg != null) " · A${score.agg}" else ""
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                        Text(
                            "$label: $markText$aggregateText",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("TOTAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(learner.total.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (learner.graded) {
                    SummaryValue("AGGREGATE", learner.aggSum?.toString() ?: "—")
                    SummaryValue("DIVISION", learner.division ?: "—")
                } else {
                    val average = if (subjects.isNotEmpty()) learner.total.toFloat() / subjects.size else 0f
                    SummaryValue("AVERAGE", "${"%.1f".format(average)}%")
                }
            }
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}
