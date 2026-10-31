package com.henrylumis.rvhgrader.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.henrylumis.rvhgrader.core.database.AcademicDatabaseBackupManager
import com.henrylumis.rvhgrader.core.database.BackupPreview
import java.text.DateFormat
import java.util.Date

/** Portable backup/restore for the authoritative RVH Edu academic database. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onOpenMenu: () -> Unit,
    onDataRestored: () -> Unit
) {
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingRestore by remember { mutableStateOf<Uri?>(null) }
    var showRestoreConfirmation by remember { mutableStateOf(false) }
    var restorePreview by remember { mutableStateOf<BackupPreview?>(null) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                AcademicDatabaseBackupManager.writeTo(context, uri)
                statusMessage = "RVH EDU DATABASE BACKUP SAVED."
            } catch (e: Exception) {
                statusMessage = "DATABASE BACKUP FAILED: ${e.message ?: "unknown error"}"
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                // Read-only preview checks the backup envelope and checksum before asking the
                // teacher to confirm replacement. Full database validation still occurs on restore.
                restorePreview = AcademicDatabaseBackupManager.previewFrom(context, uri)
                pendingRestore = uri
                showRestoreConfirmation = true
            } catch (e: Exception) {
                pendingRestore = null
                restorePreview = null
                statusMessage = "BACKUP PREVIEW FAILED: ${e.message ?: "invalid backup"}"
            }
        }
    }

    if (showRestoreConfirmation && pendingRestore != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmation = false; pendingRestore = null; restorePreview = null },
            title = { Text("Replace local academic data?") },
            text = {
                val preview = restorePreview
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (preview != null) {
                        Text("Backup date: ${DateFormat.getDateTimeInstance().format(Date(preview.exportedAtMillis))}")
                        Text("Learners: ${preview.learnerCount} · Enrolments: ${preview.enrollmentCount}")
                        Text("Assessments: ${preview.assessmentCount} · Marks: ${preview.markCount}")
                        Text(if (preview.checksumVerified) "Integrity checksum: verified" else "Integrity checksum: unavailable (legacy backup)")
                        Text("Backup format: v${preview.backupVersion} · Database schema: v${preview.schemaVersion}")
                    }
                    Text(
                        "This preview checks the file and checksum where available. Full academic-data and database-schema validation runs before restoration. " +
                            "The current database is replaced only if validation succeeds; a failed transaction is rolled back."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingRestore
                    showRestoreConfirmation = false
                    pendingRestore = null
                    restorePreview = null
                    if (uri != null) {
                        try {
                            AcademicDatabaseBackupManager.restoreFrom(context, uri)
                            statusMessage = "RVH EDU DATABASE RESTORED SUCCESSFULLY."
                            onDataRestored()
                        } catch (e: Exception) {
                            statusMessage = "RESTORE FAILED — EXISTING DATA WAS NOT REPLACED: ${e.message ?: "invalid backup"}"
                        }
                    }
                }) { Text("RESTORE") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmation = false; pendingRestore = null; restorePreview = null }) {
                    Text("CANCEL")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BACKUP & RESTORE") },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("RVH EDU DATABASE", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "This is the authoritative school-data backup. It includes learners, enrollments, " +
                            "assessments, marks and identity-review records from the RVH Edu database. " +
                            "It is the backup you should keep and move to a replacement phone.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { createBackupLauncher.launch("rvh_edu_database_backup.json") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("EXPORT RVH EDU BACKUP") }
                }
            }

            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("RESTORE RVH EDU BACKUP", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Choose an RVH Edu database backup. The app validates it and performs the restore " +
                            "transactionally; a failed restore cannot leave a half-restored database.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { openBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("IMPORT RVH EDU BACKUP") }
                }
            }

            statusMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
