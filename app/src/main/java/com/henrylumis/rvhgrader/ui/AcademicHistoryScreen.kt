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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.history.LearnerAcademicHistory

@Composable
fun AcademicHistoryScreen(
    histories: List<LearnerAcademicHistory>,
    onOpenLearner: (String) -> Unit,
    onOpenMenu: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = histories.filter { history ->
        val q = query.trim()
        q.isEmpty() || history.learnerName.contains(q, ignoreCase = true) ||
            history.enrollments.any { enrollment ->
                enrollment.academicYear.toString().contains(q, ignoreCase = true) || enrollment.schoolClassName.contains(q, ignoreCase = true) ||
                    enrollment.stream.orEmpty().contains(q, ignoreCase = true)
            }
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Academic history", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("A learner's progress across years and assessments", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onOpenMenu) { Text("MENU") }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HistorySummaryCard("Learners", histories.size.toString(), Modifier.weight(1f))
            HistorySummaryCard("With history", histories.count { h -> h.enrollments.isNotEmpty() }.toString(), Modifier.weight(1f))
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            label = { Text("Search learners, classes or years") },
            trailingIcon = {
                if (query.isNotEmpty()) TextButton(onClick = { query = "" }) { Text("CLEAR") }
            }
        )
        Spacer(Modifier.height(8.dp))
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (histories.isEmpty()) "No academic history yet" else "No matching learners",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(if (histories.isEmpty()) "Learner assessment records will appear here as results are saved."
                        else "Try a different learner name, class or academic year.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    if (query.isNotEmpty()) TextButton(onClick = { query = "" }) { Text("Clear search") }
                }
            }
            return
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("${filtered.size} learner${if (filtered.size == 1) "" else "s"}",
                    Modifier.padding(start = 2.dp, top = 2.dp, bottom = 2.dp),
                    style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(filtered, key = { it.learnerId }) { history ->
                val initials = history.learnerName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                    .take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifBlank { "L" }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center) {
                                Text(initials, color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(history.learnerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("${history.enrollments.size} enrolment${if (history.enrollments.size == 1) "" else "s"} on record",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val progress = history.progress
                            if (progress != null && progress.latestAverage != null) {
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("%.1f%%".format(progress.latestAverage), style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold)
                                    val trend = when { progress.improving -> "Improving"; progress.declining -> "Declining"; else -> "Stable" }
                                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(trend, Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }
                            }
                        }
                        if (history.enrollments.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                history.enrollments.take(2).forEach { enrollment ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(enrollment.schoolClassName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                            Text(enrollment.academicYear.toString() + (enrollment.stream?.let { " • Stream $it" } ?: ""),
                                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("${enrollment.assessments.size} assessment${if (enrollment.assessments.size == 1) "" else "s"}",
                                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (history.enrollments.size > 2) {
                                    Text("+ ${history.enrollments.size - 2} earlier enrolments", style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        } else {
                            Text("No enrolment history recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { onOpenLearner(history.learnerId) }) { Text("VIEW LEARNER RECORD  →") }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun HistorySummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}
