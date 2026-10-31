package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.core.importer.ImportReviewRow
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

@Composable
fun ImportReviewScreen(
    rows: List<ImportReviewRow>,
    schoolClass: SchoolClass,
    term: Term,
    section: TermSection,
    onConfirm: (List<ImportReviewRow>) -> Unit,
    onBack: () -> Unit
) {
    var localRows by remember(rows) { mutableStateOf(rows) }
    val subjects = SubjectCatalog.forClass(schoolClass)
    val validRows = localRows.count { it.included && it.name.isNotBlank() && it.candidates.size <= 1 && it.marks.values.any { v -> v != null } }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Review spreadsheet import", style = MaterialTheme.typography.headlineSmall)
        Text("Nothing is written until you confirm. Imported marks become DRAFT.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text("${localRows.size} rows • $validRows ready • ${schoolClass.displayName} • ${term.displayName} • ${section.displayName}")
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(localRows, key = { it.id }) { row ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Include", style = MaterialTheme.typography.labelLarge)
                            Checkbox(checked = row.included, onCheckedChange = { checked ->
                                localRows = localRows.map { if (it.id == row.id) it.copy(included = checked) else it }
                            })
                        }
                        RvhFormTextField(
                            value = row.name,
                            onValueChange = { value ->
                                localRows = localRows.map { if (it.id == row.id) it.copy(name = value) else it }
                            },
                            label = "Learner name",
                            helperText = "Check the name before confirming this import."
                        )
                        val candidateText = when (row.candidates.size) {
                            0 -> "No exact match — a new learner will be created if confirmed."
                            1 -> "Matched to existing learner: ${row.candidates.single().fullName}"
                            else -> "AMBIGUOUS — ${row.candidates.size} exact matches. Exclude or resolve the learner before importing."
                        }
                        Text(candidateText, style = MaterialTheme.typography.bodySmall)
                        subjects.forEach { subject ->
                            val current = row.marks[subject.code]?.toString().orEmpty()
                            RvhFormTextField(
                                value = current,
                                onValueChange = { raw ->
                                    val value = raw.take(3).filter { it.isDigit() }
                                    val parsed = value.toIntOrNull()?.takeIf { it in 0..100 }
                                    val updatedMarks = row.marks.toMutableMap().apply {
                                        if (value.isBlank()) remove(subject.code) else this[subject.code] = parsed
                                    }
                                    localRows = localRows.map { if (it.id == row.id) it.copy(marks = updatedMarks) else it }
                                },
                                label = subject.shortName,
                                helperText = "0–100; leave blank if not assessed.",
                                keyboardType = KeyboardType.Number
                            )
                        }
                        row.warning?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                        if (row.candidates.size > 1) {
                            Text("This row is blocked for safety because the app will never guess between same-name learners.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack, Modifier.weight(1f)) { Text("Cancel") }
            Button(
                onClick = { onConfirm(localRows) },
                enabled = validRows > 0,
                modifier = Modifier.weight(1f)
            ) { Text("Import $validRows row${if (validRows == 1) "" else "s"} as Draft") }
        }
    }
}
