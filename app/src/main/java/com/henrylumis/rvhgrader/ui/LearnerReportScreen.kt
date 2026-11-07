package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.reports.ReportCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnerReportScreen(
    report: ReportCard,
    onExport: () -> Unit,
    onBack: () -> Unit
) {
    val learner = report.learner
    val result = report.result
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column {
                    Text("Learner report", fontWeight = FontWeight.SemiBold)
                    Text("INDIVIDUAL ACADEMIC RECORD", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = { IconButton(onClick = onExport) { Icon(Icons.Default.FileUpload, "Export PDF") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 12.dp, 16.dp, 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(report.schoolName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(learner.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${report.schoolClass.displayName}  •  ${report.academicYear}  •  ${report.assessment.term.displayName} · ${report.assessment.section.displayName}",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Admission No.: ${learner.admissionNumber?.takeIf { it.isNotBlank() } ?: "—"}")
                        Text("Age: ${learner.ageYears?.toString() ?: "—"}    Gender: ${learner.gender?.takeIf { it.isNotBlank() } ?: "—"}")
                        Text("Date of birth: ${learner.dateOfBirth?.takeIf { it.isNotBlank() } ?: "—"}    Status: ${learner.status}")
                    }
                }
            }
            item {
                Text("Overall performance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Card {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Metric("TOTAL MARKS", "${result.total}/${result.maxTotal}")
                            Metric("AVERAGE", String.format(Locale.getDefault(), "%.1f%%", result.average))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Metric("AGGREGATES", result.aggregateSum?.toString() ?: "—")
                            Metric("DIVISION", result.division ?: "—")
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Metric("CLASS POSITION", report.classPosition?.let { "$it of ${report.rankedLearnerCount}" } ?: "Not ranked")
                            Metric("SUBJECTS", "${report.subjects.count { it.mark != null }}/${report.subjects.size}")
                        }
                        Text(if (result.complete) "Assessment complete" else "Incomplete assessment · missing ${result.missingSubjects.size} subject(s)",
                            color = if (result.complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold)
                        if (result.missingSubjects.isNotEmpty()) Text("Missing: ${result.missingSubjects.joinToString()}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            item {
                Text("Subject performance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Card {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("SUBJECT", Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("MARK", Modifier.weight(0.8f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("%", Modifier.weight(0.6f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("AGG.", Modifier.weight(0.6f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        report.subjects.forEach { subject ->
                            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(subject.name, Modifier.weight(1.5f), style = MaterialTheme.typography.bodyMedium)
                                Text(subject.mark?.let { "$it/${subject.maxMark}" } ?: "—", Modifier.weight(0.8f), style = MaterialTheme.typography.bodyMedium)
                                Text(subject.mark?.let { String.format(Locale.getDefault(), "%.1f", it.toDouble() / subject.maxMark * 100.0) } ?: "—",
                                    Modifier.weight(0.6f), style = MaterialTheme.typography.bodyMedium)
                                Text(subject.aggregate?.toString() ?: "—", Modifier.weight(0.6f), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            if (!report.teacherComment.isNullOrBlank() || !report.headTeacherComment.isNullOrBlank()) {
                item {
                    Text("Comments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Card {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            report.teacherComment?.takeIf { it.isNotBlank() }?.let { Column {
                                Text("CLASS TEACHER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(it)
                            } }
                            report.headTeacherComment?.takeIf { it.isNotBlank() }?.let { Column {
                                Text("HEADTEACHER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(it)
                            } }
                        }
                    }
                }
            }
            item {
                OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.FileUpload, null); Spacer(Modifier.width(8.dp)); Text("Export learner report as PDF")
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
