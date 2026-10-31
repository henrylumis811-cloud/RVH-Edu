package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.data.SchoolManagementRepository
import com.henrylumis.rvhgrader.data.SchoolRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolManagementScreen(onBack: () -> Unit, onSchoolChanged: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var schools by remember { mutableStateOf(SchoolManagementRepository.list(context)) }
    var activeId by remember { mutableStateOf(SchoolManagementRepository.activeId(context)) }
    var showCreate by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var motto by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    fun refresh() { schools = SchoolManagementRepository.list(context); activeId = SchoolManagementRepository.activeId(context) }
    Scaffold(topBar = { TopAppBar(title = { Text("Schools", fontWeight = FontWeight.Bold) }, navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }, actions = { IconButton(onClick = { showCreate = true; error = null }) { Icon(Icons.Filled.AddBusiness, contentDescription = "Add school") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Choose the school you are working in. Switching schools changes the active school identity; it does not move or merge existing academic records.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(schools, key = { it.id }) { school ->
                Card(Modifier.fillMaxWidth().clickable { try { SchoolManagementRepository.select(context, school.id); refresh(); onSchoolChanged() } catch (e: Exception) { error = e.message ?: "Could not select school." } }, colors = CardDefaults.cardColors(containerColor = if (school.id == activeId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(if (school.id == activeId) Icons.Filled.CheckCircle else Icons.Filled.School, contentDescription = null)
                        Column(Modifier.weight(1f)) { Text(school.name.ifBlank { "Existing school records" }, fontWeight = FontWeight.SemiBold); if (school.motto.isNotBlank()) Text(school.motto, style = MaterialTheme.typography.bodySmall); Text(if (school.id == activeId) "ACTIVE SCHOOL" else "Tap to switch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                    }
                }
            }
            item { OutlinedButton(onClick = { showCreate = true; error = null }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.AddBusiness, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Register another school") } }
            if (error != null) item { Text(error!!, color = MaterialTheme.colorScheme.error) }
        }
    }
    if (showCreate) AlertDialog(onDismissRequest = { showCreate = false }, title = { Text("Register school") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedTextField(value = name, onValueChange = { name = it; error = null }, label = { Text("School name") }, singleLine = true); OutlinedTextField(value = motto, onValueChange = { motto = it }, label = { Text("Motto (optional)") }, singleLine = true); if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error) } }, confirmButton = { TextButton(onClick = { try { SchoolManagementRepository.create(context, name, motto); name = ""; motto = ""; showCreate = false; refresh(); onSchoolChanged() } catch (e: Exception) { error = e.message ?: "Could not register school." } }) { Text("Create & select") } }, dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } })
}
