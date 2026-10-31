package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.model.AggregateBand
import com.henrylumis.rvhgrader.model.DivisionBand
import com.henrylumis.rvhgrader.model.GradingScale

private class EditableAggBand(minMark: String, aggregate: String) {
    var minMark by mutableStateOf(minMark)
    var aggregate by mutableStateOf(aggregate)
}

private class EditableDivBand(maxAggSum: String, label: String) {
    var maxAggSum by mutableStateOf(maxAggSum)
    var label by mutableStateOf(label)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolGradingSettingsScreen(
    scale: GradingScale,
    onScaleChange: (GradingScale) -> Unit,
    schoolName: String,
    classPassPercent: Double,
    onClassPassPercentChange: (Double) -> Unit,
    onSchoolNameChange: (String) -> Unit,
    onOpenMenu: () -> Unit
) {
    var schoolNameField by remember(schoolName) { mutableStateOf(schoolName) }
    var passPercentField by remember(classPassPercent) { mutableStateOf(classPassPercent.toString()) }
    val aggRows = remember(scale) {
        mutableStateListOf(
            *scale.aggregateBands.sortedByDescending { it.minMark }
                .map { EditableAggBand(it.minMark.toString(), it.aggregate.toString()) }
                .toTypedArray()
        )
    }
    val divRows = remember(scale) {
        mutableStateListOf(
            *scale.divisionBands.sortedBy { it.maxAggSum }
                .map { EditableDivBand(it.maxAggSum.toString(), it.label) }
                .toTypedArray()
        )
    }
    var savedMessage by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GRADING SCALE") },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("SCHOOL INFO", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        RvhFormTextField(
                            value = schoolNameField,
                            onValueChange = { schoolNameField = it },
                            label = "SCHOOL NAME",
                            helperText = "Shown on exported reports and class lists."
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Shown on every exported report and class list, and in the learner " +
                                "report view under Student History.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                val cleaned = schoolNameField.trim()
                                savedMessage = when {
                                    cleaned.isBlank() -> "Enter the school name before saving."
                                    else -> runCatching {
                                        onSchoolNameChange(cleaned)
                                        "SCHOOL NAME SAVED"
                                    }.getOrElse { "Could not save school name: ${it.message ?: "please try again."}" }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("SAVE SCHOOL NAME") }
                        Spacer(Modifier.height(12.dp))
                        RvhFormTextField(
                            value = passPercentField,
                            onValueChange = { value ->
                                if (value.matches(Regex("\\d{0,3}(\\.\\d{0,2})?"))) passPercentField = value
                            },
                            label = "CLASS PASS THRESHOLD (%)",
                            helperText = "Used by class and subject analytics; separate from aggregate bands.",
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        )
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                val percent = passPercentField.toDoubleOrNull()
                                savedMessage = when {
                                    percent == null || !percent.isFinite() || percent !in 0.0..100.0 -> "Enter a pass threshold from 0 to 100."
                                    else -> { onClassPassPercentChange(percent); "CLASS PASS THRESHOLD SAVED" }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("SAVE PASS THRESHOLD") }
                    }
                }
            }
            item {
                Text(
                    "These are editable school-specific grading tables, not universal rules. Enter " +
                        "the thresholds and division labels required by this school's policy. Changes " +
                        "apply to calculations made after saving; review existing reports if your " +
                        "school changes its rules mid-year. Subject lists and class grading modes are " +
                        "still configured by the current app version and are a separate setup task.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item {
                Card {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("MARK → AGGREGATE", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        aggRows.forEachIndexed { index, row ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                RvhFormTextField(
                                    value = row.minMark,
                                    onValueChange = { row.minMark = it.filter { c -> c.isDigit() }.take(3) },
                                    label = "MIN MARK",
                                    modifier = Modifier.weight(1f),
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("→")
                                Spacer(Modifier.width(8.dp))
                                RvhFormTextField(
                                    value = row.aggregate,
                                    onValueChange = { row.aggregate = it.filter { c -> c.isDigit() }.take(1) },
                                    label = "AGG",
                                    modifier = Modifier.weight(1f),
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                                IconButton(onClick = { aggRows.removeAt(index) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove band")
                                }
                            }
                        }
                        TextButton(onClick = { aggRows.add(EditableAggBand("", "")) }) {
                            Text("+ ADD BAND")
                        }
                    }
                }
            }
            item {
                Card {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("AGGREGATE SUM → DIVISION", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        divRows.forEachIndexed { index, row ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                RvhFormTextField(
                                    value = row.maxAggSum,
                                    onValueChange = { row.maxAggSum = it.filter { c -> c.isDigit() }.take(3) },
                                    label = "UP TO",
                                    modifier = Modifier.weight(1f),
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("→")
                                Spacer(Modifier.width(8.dp))
                                RvhFormTextField(
                                    value = row.label,
                                    onValueChange = { row.label = it.uppercase().take(6) },
                                    label = "DIVISION",
                                    modifier = Modifier.weight(1f),
                                    helperText = "Label",
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                )
                                IconButton(onClick = { divRows.removeAt(index) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove band")
                                }
                            }
                        }
                        TextButton(onClick = { divRows.add(EditableDivBand("", "")) }) {
                            Text("+ ADD BAND")
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            keyboardController?.hide()
                            savedMessage = "Default grading bands loaded. Press SAVE to apply them."
                            val default = GradingScale.default()
                            aggRows.clear()
                            aggRows.addAll(
                                default.aggregateBands.sortedByDescending { it.minMark }
                                    .map { EditableAggBand(it.minMark.toString(), it.aggregate.toString()) }
                            )
                            divRows.clear()
                            divRows.addAll(
                                default.divisionBands.sortedBy { it.maxAggSum }
                                    .map { EditableDivBand(it.maxAggSum.toString(), it.label) }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("RESET TO DEFAULT") }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            val aggBands = aggRows.mapNotNull { r ->
                                val min = r.minMark.toIntOrNull()
                                val agg = r.aggregate.toIntOrNull()
                                if (min != null && agg != null) AggregateBand(min, agg) else null
                            }
                            val divBands = divRows.mapNotNull { r ->
                                val max = r.maxAggSum.toIntOrNull()
                                if (max != null && r.label.isNotBlank()) DivisionBand(max, r.label) else null
                            }
                            val candidate = GradingScale(aggBands, divBands)
                            val incomplete = aggRows.any { it.minMark.toIntOrNull() == null || it.aggregate.toIntOrNull() == null } ||
                                divRows.any { it.maxAggSum.toIntOrNull() == null || it.label.isBlank() }
                            savedMessage = when {
                                incomplete -> "Complete or remove every unfinished row before saving."
                                candidate.validationError() != null -> candidate.validationError()!!
                                else -> runCatching {
                                    onScaleChange(candidate)
                                    "SCHOOL GRADING RULES SAVED"
                                }.getOrElse { "Could not save grading rules: ${it.message ?: "please try again."}" }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("SAVE") }
                }
            }
            savedMessage?.let { msg ->
                item {
                    RvhFeedbackBanner(
                        message = msg,
                        kind = if (msg.endsWith("SAVED") || msg.startsWith("Default grading bands loaded")) RvhFeedbackKind.Success else RvhFeedbackKind.Error
                    )
                }
            }
        }
    }
}
