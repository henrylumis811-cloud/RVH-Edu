package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.assessment.RosterEntry
import com.henrylumis.rvhgrader.model.SchoolClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassProgressionScreen(
    sourceYear: Int,
    sourceClass: SchoolClass,
    roster: List<RosterEntry>,
    onProgress: (targetYear: Int, targetClass: SchoolClass, learnerIds: Set<String>) -> String?,
    onOpenMenu: () -> Unit
) {
    var targetYearText by remember(sourceYear) { mutableStateOf((sourceYear + 1).toString()) }
    var targetClass by remember(sourceClass) {
        mutableStateOf(SchoolClass.values().getOrNull(sourceClass.ordinal + 1) ?: sourceClass)
    }
    var classMenuExpanded by remember { mutableStateOf(false) }
    var confirmProgress by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val selectedIds = remember(sourceYear, sourceClass, roster) {
        mutableStateListOf<String>().apply { addAll(roster.map { it.learner.id }) }
    }
    val targetYear = targetYearText.toIntOrNull()
    val canProgress = targetYear != null && targetYear in 2000..2100 && targetYear > sourceYear && selectedIds.isNotEmpty()
    val placementKind = when {
        targetClass.ordinal == sourceClass.ordinal -> "Repeat the same class"
        targetClass.ordinal == sourceClass.ordinal + 1 -> "Promote to the next class"
        targetClass.ordinal < sourceClass.ordinal -> "Move to a lower class — review carefully"
        else -> "Skip one or more classes — review placement carefully"
    }
    val placementWarning = when {
        targetClass.ordinal < sourceClass.ordinal -> "The destination is below the source class. Confirm this is an intentional placement decision before continuing."
        targetClass.ordinal > sourceClass.ordinal + 1 -> "This skips one or more classes. Confirm the learner's approved placement before continuing."
        sourceClass == SchoolClass.P7 && targetClass == SchoolClass.P7 -> "Primary Seven has no higher primary class. School-leaver handling is separate from progression."
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Class Progression") },
                navigationIcon = { IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Groups, contentDescription = "Open menu") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Move learners into a new academic year", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text("Source: ${sourceClass.displayName} • $sourceYear", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            RvhFormTextField(
                value = targetYearText,
                onValueChange = { value -> if (value.length <= 4 && value.all { it.isDigit() }) targetYearText = value },
                label = "Target academic year",
                helperText = "Enter a year after $sourceYear.",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                errorText = if (targetYearText.isNotBlank() && (targetYear == null || targetYear !in 2000..2100 || targetYear <= sourceYear)) "Choose a year from 2000–2100 that is later than $sourceYear." else null
            )
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = classMenuExpanded, onExpandedChange = { classMenuExpanded = it }) {
                OutlinedTextField(
                    value = targetClass.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Destination class") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = classMenuExpanded, onDismissRequest = { classMenuExpanded = false }) {
                    SchoolClass.values().forEach { schoolClass ->
                        DropdownMenuItem(text = { Text(schoolClass.displayName) }, onClick = {
                            targetClass = schoolClass
                            classMenuExpanded = false
                        })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Placement preview", style = MaterialTheme.typography.titleSmall)
                    Text(placementKind, style = MaterialTheme.typography.bodyLarge)
                    Text("${selectedIds.size} learner(s) selected • $sourceYear → ${targetYearText.ifBlank { "—" }}", style = MaterialTheme.typography.bodySmall)
                    placementWarning?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Select the learners to move. Unselected learners stay in their existing records; this operation never deletes or edits past marks. School leavers should be handled separately through learner archiving.", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { selectedIds.clear(); selectedIds.addAll(roster.map { it.learner.id }) }) { Text("Select all") }
                TextButton(onClick = { selectedIds.clear() }) { Text("Clear selection") }
            }
            Text("Selected: ${selectedIds.size} of ${roster.size}", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(roster, key = { it.learner.id }) { entry ->
                    val checked = entry.learner.id in selectedIds
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = checked, onCheckedChange = { selected ->
                                if (selected) { if (entry.learner.id !in selectedIds) selectedIds.add(entry.learner.id) }
                                else selectedIds.remove(entry.learner.id)
                            })
                            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                                Text(entry.learner.fullName, style = MaterialTheme.typography.bodyLarge)
                                Text(entry.learner.admissionNumber ?: "No admission number", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            errorMessage?.let { RvhFeedbackBanner(it, RvhFeedbackKind.Error) }
            successMessage?.let { RvhFeedbackBanner(it, RvhFeedbackKind.Success) }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { confirmProgress = true }, enabled = canProgress, modifier = Modifier.fillMaxWidth()) {
                Text("Review and progress ${selectedIds.size} learner(s)")
            }
        }
    }

    if (confirmProgress) {
        AlertDialog(
            onDismissRequest = { confirmProgress = false },
            title = { Text("Confirm class progression") },
            text = {
                Text(buildString {
                    append("$placementKind: create ${selectedIds.size} enrollment(s) in ${targetClass.displayName} for $targetYear? ")
                    if (placementWarning != null) append("Important: $placementWarning ")
                    append("Existing academic records will be preserved. If any selected learner already has an enrollment in the target year, or a source record has changed, the whole operation will be rejected without partial changes.")
                })
            },
            confirmButton = {
                Button(onClick = {
                    confirmProgress = false
                    val result = onProgress(targetYear!!, targetClass, selectedIds.toSet())
                    if (result == null) {
                        successMessage = "Progression completed for ${selectedIds.size} learner(s)."
                        errorMessage = null
                    } else {
                        errorMessage = result
                        successMessage = null
                    }
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { confirmProgress = false }) { Text("Cancel") } }
        )
    }
}
