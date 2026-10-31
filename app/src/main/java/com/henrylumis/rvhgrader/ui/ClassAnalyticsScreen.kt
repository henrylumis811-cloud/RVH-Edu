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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.analytics.ClassAnalyticsSummary
import com.henrylumis.rvhgrader.core.analytics.RiskLevel
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassAnalyticsScreen(
    schoolClass: SchoolClass,
    year: Int,
    term: Term,
    section: TermSection,
    summary: ClassAnalyticsSummary,
    learnerNames: Map<String, String>,
    onTermChange: (Term) -> Unit,
    onSectionChange: (TermSection) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Column { Text("Class analytics", fontWeight = FontWeight.SemiBold); Text("PERFORMANCE INSIGHTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
        )
    }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(schoolClass.displayName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Academic year $year · ${term.displayName} · ${section.displayName}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ASSESSMENT PERIOD", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Term.entries.forEach { t -> FilterChip(selected = t == term, onClick = { onTermChange(t) }, label = { Text(t.displayName) }) }
                        }
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TermSection.entries.forEach { s -> FilterChip(selected = s == section, onClick = { onSectionChange(s) }, label = { Text(s.displayName) }) }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AnalyticsMetric("LEARNERS", summary.learnerCount.toString(), Modifier.weight(1f))
                    AnalyticsMetric("CLASS AVG.", summary.averagePercent?.let { "%.1f%%".format(it) } ?: "—", Modifier.weight(1f))
                }
            }
            item {
                Card {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Class snapshot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SnapshotValue("Complete", summary.completeCount.toString())
                            SnapshotValue("Incomplete", summary.incompleteCount.toString())
                            SnapshotValue("Pass rate", summary.passRatePercent?.let { "%.1f%%".format(it) } ?: "—")
                        }
                        if (summary.learnerCount > 0) {
                            LinearProgressIndicator(
                                progress = { (summary.completeCount.toFloat() / summary.learnerCount.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("${summary.completeCount} of ${summary.learnerCount} learners have complete results", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Highest", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Text(summary.highestPercent?.let { "%.1f%%".format(it) } ?: "—", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            Text("Lowest", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Text(summary.lowestPercent?.let { "%.1f%%".format(it) } ?: "—", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Subject performance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Coverage and results among learners assessed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (summary.subjectAnalytics.isEmpty()) {
                item { EmptyAnalyticsCard("No subject data yet", "Subject summaries will appear when marks are available for this assessment period.") }
            } else {
                items(summary.subjectAnalytics, key = { it.subjectCode }) { subject ->
                    Card {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(subject.subjectCode.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(subject.averagePercent?.let { "%.1f%% avg.".format(it) } ?: "No marks", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            }
                            LinearProgressIndicator(progress = { (subject.coveragePercent / 100.0).coerceIn(0.0, 1.0).toFloat() }, modifier = Modifier.fillMaxWidth())
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Coverage", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                Text("${"%.0f".format(subject.coveragePercent)}% · ${subject.learnerCount} assessed · ${subject.missingCount} missing", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                SnapshotValue("AMGP", subject.amgp?.let { "%.2f".format(it) } ?: "—")
                                SnapshotValue("Pass rate", subject.passRatePercent?.let { "%.1f%%".format(it) } ?: "—")
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                SnapshotValue("Highest", subject.highestPercent?.let { "%.1f%%".format(it) } ?: "—")
                                SnapshotValue("Lowest", subject.lowestPercent?.let { "%.1f%%".format(it) } ?: "—")
                            }
                            Text(
                                "D ${subject.distinctionCount}  ·  C ${subject.creditCount}  ·  P ${subject.passCount}  ·  F ${subject.failureCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Learners needing attention", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Flags based on the available academic history", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (summary.riskFlags.isEmpty()) {
                item { EmptyAnalyticsCard("No current risk flags", "There are no learners flagged by the available academic history for this view.") }
            } else {
                items(summary.riskFlags, key = { it.learnerId }) { risk ->
                    val isHigh = risk.level == RiskLevel.HIGH
                    Card(colors = CardDefaults.cardColors(containerColor = if (isHigh) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = if (isHigh) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.tertiary)
                                Column(Modifier.weight(1f)) {
                                    Text(learnerNames[risk.learnerId] ?: "Unknown learner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = if (isHigh) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface)
                                    Text("${risk.level.name} PRIORITY", style = MaterialTheme.typography.labelSmall, color = if (isHigh) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                }
                            }
                            risk.reasons.forEach { reason ->
                                Text("• $reason", style = MaterialTheme.typography.bodyMedium, color = if (isHigh) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun SnapshotValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyAnalyticsCard(title: String, message: String) {
    Card {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
