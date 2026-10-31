package com.henrylumis.rvhgrader.ui

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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Feedback
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHubScreen(
    onOpenMenu: () -> Unit,
    onSchoolAndGrading: () -> Unit,
    onSchools: () -> Unit,
    onBackup: () -> Unit,
    onArchivedLearners: () -> Unit,
    onClassSetup: () -> Unit,
    onSchoolSubjects: () -> Unit,
    onFeedbackAndUpdates: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("School settings", fontWeight = FontWeight.Bold)
                        Text("CONFIGURATION & DATA", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.1f.sp)
                    }
                },
                navigationIcon = { IconButton(onClick = onOpenMenu) { Icon(Icons.Filled.Tune, contentDescription = "Open navigation") } }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(27.dp))
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Set up your school", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.height(4.dp))
                            Text("Keep academic rules, class structure and data protection in one place.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .84f))
                        }
                    }
                }
            }
            item { SettingsSectionLabel("SCHOOL CONFIGURATION", "Changes here shape how new academic work is recorded.") }
            item { SettingsAction("Schools & school selection", "Register schools and switch the active school", Icons.Filled.Business, onSchools) }
            item { SettingsAction("School details & grading", "School identity, aggregate bands and division thresholds", Icons.Filled.Business, onSchoolAndGrading) }
            item { SettingsAction("School subjects", "Configure the expected subjects for each primary level", Icons.Filled.Category, onSchoolSubjects) }
            item { SettingsAction("Classes & progression", "Review class structure and learner progression", Icons.Filled.Category, onClassSetup) }
            item { SettingsSectionLabel("RECORDS & SAFETY", "Protect records and manage learners who are no longer active.") }
            item { SettingsAction("Backup & restore", "Create a portable backup or restore school records", Icons.Filled.Backup, onBackup) }
            item { SettingsAction("Archived learners", "Review archived records and restore learners", Icons.Filled.Security, onArchivedLearners) }
            item { SettingsSectionLabel("SUPPORT & APP", "Help improve RVH Edu and check for new versions.") }
            item { SettingsAction("Feedback & updates", "Send suggestions or report a problem; check for updates", Icons.Filled.Feedback, onFeedbackAndUpdates) }
            item {
                Spacer(Modifier.height(2.dp))
                Text(
                    "Offline by design · School records remain on this device unless you export them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(title: String, description: String) {
    Column(Modifier.fillMaxWidth().padding(top = 6.dp, start = 2.dp, end = 2.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = .8f.sp)
        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsAction(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(23.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
