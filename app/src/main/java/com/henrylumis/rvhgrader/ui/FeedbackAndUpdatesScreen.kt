package com.henrylumis.rvhgrader.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackAndUpdatesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var category by remember { mutableStateOf("Suggestion") }
    var message by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    val privacyPolicyText = remember(context) {
        try { context.assets.open("privacy_policy.txt").bufferedReader().use { it.readText() } }
        catch (_: Exception) { "RVH Edu stores school and learner records locally. Feedback is only shared when you choose to send it through email or WhatsApp. Protect exported backups and do not include learner records in feedback. Contact henrylumis811@gmail.com for privacy questions." }
    }
    val versionInfo = remember {
        try {
            val info = if (Build.VERSION.SDK_INT >= 33) context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0)) else @Suppress("DEPRECATION") context.packageManager.getPackageInfo(context.packageName, 0)
            val code = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode.toInt() else @Suppress("DEPRECATION") info.versionCode
            "${info.versionName ?: "unknown"} (build $code)" to code
        } catch (_: Exception) { "unknown" to 0 }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Feedback & updates", fontWeight = FontWeight.Bold) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Help us improve RVH Edu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Report a problem, suggest a feature, or tell us what is working well. Your message is shared only when you choose to send it.", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Text("SEND FEEDBACK", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Suggestion", "Problem", "Praise").forEach { option ->
                    androidx.compose.material3.FilterChip(selected = category == option, onClick = { category = option }, label = { Text(option) })
                }
            }
            OutlinedTextField(value = message, onValueChange = { message = it }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Your feedback") }, placeholder = { Text("Describe your idea or issue…") }, minLines = 4)
            Text(
                "Choose where to send your feedback. RVH Edu will open your chosen app with a message prepared; nothing is sent until you review and press Send.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val feedbackBody = "RVH Edu feedback\nType: $category\nApp: ${versionInfo.first}\nAndroid: ${Build.VERSION.SDK_INT}\n\n${message.trim()}\n\nPlease remove any learner names or private school records before sending."
            Button(onClick = {
                if (message.isBlank()) {
                    status = "Please enter a message before sending feedback."
                } else try {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("henrylumis811@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "RVH Edu — $category")
                        putExtra(Intent.EXTRA_TEXT, feedbackBody)
                    }
                    context.startActivity(Intent.createChooser(intent, "Send RVH Edu feedback by email"))
                    status = "Email opened. Review the message and press Send in your email app."
                } catch (_: Exception) {
                    status = "No email app is available. You can send feedback using WhatsApp instead."
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Email, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Send feedback by email")
            }
            Button(onClick = {
                if (message.isBlank()) {
                    status = "Please enter a message before sending feedback."
                } else try {
                    val encoded = java.net.URLEncoder.encode(feedbackBody, "UTF-8")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/256786997653?text=$encoded"))
                    context.startActivity(intent)
                    status = "WhatsApp opened. Review the message and press Send there."
                } catch (_: Exception) {
                    status = "Could not open WhatsApp or a browser. Please try email instead."
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Chat, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Send feedback by WhatsApp")
            }
            if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(4.dp))
            Text("APP UPDATES", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Installed version: ${versionInfo.first}", fontWeight = FontWeight.SemiBold)
                    Text("RVH Edu updates will be distributed through Google Play. You do not need a separate update server; check the Play Store listing for available updates.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text("PRIVACY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your school records stay on this device unless you choose to export or share them.", style = MaterialTheme.typography.bodyMedium)
                    Text("RVH Edu has no configured feedback or audience-tracking server in this release. Feedback is sent only when you choose an email or WhatsApp action and press Send in that app.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { showPrivacyPolicy = true }) { Text("Read privacy policy") }
                }
            }

            Text("RVH Edu stays offline-first. Online services are optional and must never prevent access to local school records.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            title = { Text("RVH Edu privacy policy") },
            text = {
                Column(Modifier.height(420.dp).verticalScroll(rememberScrollState())) {
                    Text(privacyPolicyText, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = { TextButton(onClick = { showPrivacyPolicy = false }) { Text("Close") } }
        )
    }
}
