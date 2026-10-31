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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.model.Learner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedLearnersScreen(
    learners: List<Learner>,
    archiveReasons: Map<String, String> = emptyMap(),
    onRestore: (Learner) -> Unit,
    onOpenMenu: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = learners.filter {
        it.fullName.contains(search, ignoreCase = true) ||
            (it.admissionNumber?.contains(search, ignoreCase = true) == true)
    }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Archived learners") },
            navigationIcon = { IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Archive, contentDescription = "Menu") } }
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Archived learners are hidden from active class lists. Restoring a learner preserves their existing academic history.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            RvhFormTextField(
                value = search,
                onValueChange = { search = it },
                label = "Search archived learners",
                helperText = "Search by learner name or admission number.",
                imeAction = androidx.compose.ui.text.input.ImeAction.Search
            )
            Spacer(Modifier.height(12.dp))
            if (filtered.isEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text(if (learners.isEmpty()) "No archived learners" else "No matching learners", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(if (learners.isEmpty()) "When a learner is removed from the active roster, they will appear here." else "Try another name or admission number.")
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.id }) { learner ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(learner.fullName, style = MaterialTheme.typography.titleMedium)
                                    learner.admissionNumber?.let { Text("Admission: $it", style = MaterialTheme.typography.bodySmall) }
                                    archiveReasons[learner.id]?.let { reason ->
                                        Text("Archive reason: $reason", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text("Academic records preserved", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(onClick = { onRestore(learner) }) {
                                    Icon(Icons.Default.Restore, contentDescription = null)
                                    Text(" Restore")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
