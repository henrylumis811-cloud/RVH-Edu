package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.model.RankedResult
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassResultsScreen(
    schoolClass: SchoolClass,
    year: Int,
    term: Term,
    section: TermSection,
    results: List<RankedResult>,
    learnerNames: Map<String, String>,
    onTermChange: (Term) -> Unit,
    onSectionChange: (TermSection) -> Unit,
    onExport: () -> Unit,
    onReportCard: (String) -> Unit,
    onCertificate: (String) -> Unit,
    onBack: () -> Unit
) {
    val complete = results.filter { it.result.complete }
    val incompleteCount = results.count { !it.result.complete }
    val average = if (complete.isEmpty()) 0.0 else complete.map { it.result.average }.average()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Class results", fontWeight = FontWeight.SemiBold); Text("ACADEMIC PERFORMANCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = { IconButton(onClick = onExport) { Icon(Icons.Default.FileUpload, contentDescription = "Export results") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(schoolClass.displayName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Academic year $year · ${term.displayName} · ${section.displayName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("ASSESSMENT PERIOD", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Term.entries.forEach { item ->
                                FilterChip(selected = item == term, onClick = { onTermChange(item) }, label = { Text(item.displayName) })
                            }
                        }
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TermSection.entries.forEach { item ->
                                FilterChip(selected = item == section, onClick = { onSectionChange(item) }, label = { Text(item.displayName) })
                            }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SummaryMetric("LEARNERS", results.size.toString(), Modifier.weight(1f))
                    SummaryMetric("COMPLETE", complete.size.toString(), Modifier.weight(1f))
                    SummaryMetric("CLASS AVG.", if (complete.isEmpty()) "—" else "${"%.1f".format(average)}%", Modifier.weight(1f))
                }
            }
            if (incompleteCount > 0) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Column(Modifier.weight(1f)) {
                                Text("Incomplete assessments", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("$incompleteCount learner${if (incompleteCount == 1) " has" else "s have"} missing marks and won't be ranked.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Learner results", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Ranked by saved assessment results", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = onExport) { Icon(Icons.Default.FileUpload, null); Spacer(Modifier.width(6.dp)); Text("Export") }
                }
            }
            if (results.isEmpty()) {
                item {
                    Card {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("No results yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Saved results for this class and assessment period will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(results, key = { it.result.learnerId }) { ranked ->
                    val result = ranked.result
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(shape = CircleShape, color = if (result.complete) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer) {
                                    Text(
                                        initials(learnerNames[result.learnerId] ?: "Unknown learner"),
                                        modifier = Modifier.padding(11.dp),
                                        color = if (result.complete) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(learnerNames[result.learnerId] ?: "Unknown learner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text(if (result.complete) "${result.total}/${result.maxTotal} total marks" else "Incomplete · ${result.missingSubjects.size} missing subject(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (result.complete) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (ranked.position <= 3) Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.width(18.dp))
                                            Text("#${ranked.position}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        }
                                        Text("${"%.1f".format(result.average)}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    }
                                } else Text("Pending", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                            }
                            if (result.complete) {
                                androidx.compose.material3.LinearProgressIndicator(progress = (result.average / 100.0).toFloat().coerceIn(0f, 1f), modifier = Modifier.fillMaxWidth())
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (result.graded) "Aggregate ${result.aggregateSum ?: "—"} · Division ${result.division ?: "—"}" else "Assessment complete", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row {
                                        TextButton(onClick = { onReportCard(result.learnerId) }) { Text("Report") }
                                        if (result.graded) TextButton(onClick = { onCertificate(result.learnerId) }) { Text("Certificate") }
                                    }
                                }
                            } else {
                                Text("Missing: ${result.missingSubjects.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

private fun initials(name: String): String = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercaseChar().toString() }.ifBlank { "?" }
