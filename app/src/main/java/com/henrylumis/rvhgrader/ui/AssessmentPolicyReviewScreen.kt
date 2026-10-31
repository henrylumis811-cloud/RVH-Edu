package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.assessment.AcademicDataService
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

private enum class PolicyReviewFilter(val label: String) {
    ALL("All records"), NEEDS_REVIEW("Needs review"), SAVED("Saved policy")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentPolicyReviewScreen(
    schoolClass: SchoolClass, year: Int, term: Term, section: TermSection,
    entries: List<AcademicDataService.PolicyReviewEntry>, onBack: () -> Unit
) {
    var filter by remember { mutableStateOf(PolicyReviewFilter.ALL) }
    val needsReview = entries.count { it.status != "SAVED POLICY" }
    val saved = entries.size - needsReview
    val visibleEntries = when (filter) {
        PolicyReviewFilter.ALL -> entries
        PolicyReviewFilter.NEEDS_REVIEW -> entries.filter { it.status != "SAVED POLICY" }
        PolicyReviewFilter.SAVED -> entries.filter { it.status == "SAVED POLICY" }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Assessment Policy Review") }, navigationIcon = {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("${schoolClass.displayName} • $year", style = MaterialTheme.typography.titleLarge)
            Text("${term.displayName} • ${section.displayName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard("Assessments", entries.size.toString(), Modifier.weight(1f))
                SummaryCard("Saved policy", saved.toString(), Modifier.weight(1f))
                SummaryCard("Needs review", needsReview.toString(), Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Read-only review: this screen does not alter marks or historical policies. Resolve discrepancies using the original assessment records; do not apply today's rules to old results without an explicit school decision.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PolicyReviewFilter.values().forEach { option ->
                    FilterChip(selected = filter == option, onClick = { filter = option }, label = { Text(option.label) })
                }
            }
            Spacer(Modifier.height(8.dp))
            if (entries.isEmpty()) {
                Text("No saved assessments exist for this selection.", style = MaterialTheme.typography.bodyLarge)
            } else if (visibleEntries.isEmpty()) {
                Text("No records match this filter.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visibleEntries, key = { it.learnerId }) { entry ->
                        val isProblem = entry.status != "SAVED POLICY"
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text(entry.learnerName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    entry.status,
                                    color = if (isProblem) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(entry.detail, style = MaterialTheme.typography.bodyMedium)
                                if (isProblem) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Next step: compare this assessment with the original mark sheet and the school's approved grading rules. Keep the historical record unchanged until verified.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
