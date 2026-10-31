package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    schoolName: String,
    academicYear: Int,
    learnerCount: Int,
    onOpenMenu: () -> Unit,
    onEnrollLearners: () -> Unit,
    onEnterMarks: () -> Unit,
    onBulkMarks: () -> Unit,
    onResults: () -> Unit,
    onAnalytics: () -> Unit,
    onHistory: () -> Unit,
    onImport: () -> Unit,
    onSettings: () -> Unit,
    onBackup: () -> Unit
) {
    val actions = listOf(
        HomeAction("Learners", "Register and manage the class roster", Icons.Filled.Groups, onEnrollLearners),
        HomeAction("Enter marks", "Record marks for one learner", Icons.Filled.Assignment, onEnterMarks),
        HomeAction("Class mark entry", "Enter marks across the class", Icons.Filled.Assignment, onBulkMarks),
        HomeAction("Class results", "Review results and completion", Icons.Filled.QueryStats, onResults),
        HomeAction("Performance", "Explore subject and class trends", Icons.Filled.QueryStats, onAnalytics),
        HomeAction("Academic history", "Find previous learner records", Icons.Filled.History, onHistory),
        HomeAction("Import results", "Review a CSV or Excel file", Icons.Filled.ImportExport, onImport),
        HomeAction("School settings", "Subjects, grading and setup", Icons.Filled.Settings, onSettings),
        HomeAction("Backup & restore", "Protect your school records", Icons.Filled.Backup, onBackup)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(38.dp).clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("RVH EDU", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("SCHOOL MANAGEMENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onOpenMenu) { Icon(Icons.Filled.Menu, contentDescription = "Open navigation") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.fillMaxWidth().padding(22.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("OFFLINE WORKSPACE", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(15.dp))
                        Text(
                            if (schoolName.isBlank()) "Your school, organised." else schoolName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(5.dp))
                        Text("A clearer way to manage learners, assessments and results.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f))
                        Spacer(Modifier.height(22.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            MetricPanel("$learnerCount", "Active learners", Modifier.weight(1f))
                            MetricPanel(academicYear.toString(), "Academic year", Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Your workspace", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Choose a task to continue.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            itemsInRows(actions)
            item {
                Column(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("RVH EDU", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("In dedication to Hellen", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private data class HomeAction(val title: String, val description: String, val icon: ImageVector, val onClick: () -> Unit)

private fun androidx.compose.foundation.lazy.LazyListScope.itemsInRows(actions: List<HomeAction>) {
    items(actions.size / 2 + actions.size % 2) { rowIndex ->
        val firstIndex = rowIndex * 2
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            HomeActionCard(actions[firstIndex], Modifier.weight(1f))
            if (firstIndex + 1 < actions.size) {
                HomeActionCard(actions[firstIndex + 1], Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricPanel(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f))
        }
    }
}

@Composable
private fun HomeActionCard(action: HomeAction, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(154.dp).clickable(onClick = action.onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(38.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(21.dp))
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(14.dp))
            Text(action.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(action.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}
