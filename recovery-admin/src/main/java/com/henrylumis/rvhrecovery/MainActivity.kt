package com.henrylumis.rvhrecovery

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RvhBlue = Color(0xFF2457C5)
private val RvhBlueDark = Color(0xFF9DBAFF)
private val RvhTeal = Color(0xFF087F8C)
private val RvhTealDark = Color(0xFF72D5D5)

private val RvhLight = lightColorScheme(
    primary = RvhBlue, onPrimary = Color.White,
    primaryContainer = Color(0xFFE2EAFE), onPrimaryContainer = Color(0xFF102F70),
    secondary = RvhTeal, onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1F0F0), onSecondaryContainer = Color(0xFF002F33),
    background = Color(0xFFF5F7FB), surface = Color.White,
    surfaceVariant = Color(0xFFEBEFF6), onBackground = Color(0xFF171C27),
    onSurface = Color(0xFF171C27), onSurfaceVariant = Color(0xFF566174),
    outline = Color(0xFFBBC4D3), outlineVariant = Color(0xFFDCE2EB),
    error = Color(0xFFBA1A1A)
)
private val RvhDark = darkColorScheme(
    primary = RvhBlueDark, onPrimary = Color(0xFF08275F),
    primaryContainer = Color(0xFF173A78), onPrimaryContainer = Color(0xFFE0E8FF),
    secondary = RvhTealDark, onSecondary = Color(0xFF003738),
    background = Color(0xFF10141C), surface = Color(0xFF171C25),
    surfaceVariant = Color(0xFF242B37), onBackground = Color(0xFFE8ECF4),
    onSurface = Color(0xFFE8ECF4), onSurfaceVariant = Color(0xFFB7C0D0),
    outline = Color(0xFF465165), outlineVariant = Color(0xFF303949),
    error = Color(0xFFFFB4AB)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dark = isSystemInDarkTheme()
            MaterialTheme(
                colorScheme = if (dark) RvhDark else RvhLight,
                shapes = Shapes(
                    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
                    medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(24.dp),
                    extraLarge = RoundedCornerShape(30.dp)
                ),
                typography = Typography(
                    headlineMedium = Typography().headlineMedium.copy(fontWeight = FontWeight.Bold),
                    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            ) { RecoveryAdminScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoveryAdminScreen() {
    val context = LocalContext.current
    var hasKey by remember { mutableStateOf(RecoverySigner.hasKey(context)) }
    var request by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Your recovery workspace is ready.") }
    var busy by remember { mutableStateOf(false) }
    var showReplaceKeyDialog by remember { mutableStateOf(false) }
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(status) {
        if (status.isNotBlank()) snackbarHost.showSnackbar(status)
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Unable to read the selected file.")
                RecoverySigner.importPem(context, bytes)
                hasKey = true
                status = "Administrator key imported and protected on this device."
            } catch (e: Exception) {
                status = e.message ?: "Key import failed."
            }
        }
    }

    if (showReplaceKeyDialog) {
        AlertDialog(
            onDismissRequest = { showReplaceKeyDialog = false },
            icon = { Icon(Icons.Default.Key, contentDescription = null) },
            title = { Text("Replace administrator key?") },
            text = { Text("Only import a replacement key supplied through your secure administrator backup. The new key must match this RVH Edu release.") },
            confirmButton = {
                TextButton(onClick = {
                    showReplaceKeyDialog = false
                    picker.launch(arrayOf("application/x-pem-file", "application/octet-stream", "text/plain"))
                }) { Text("Continue") }
            },
            dismissButton = { TextButton(onClick = { showReplaceKeyDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                        BrandMark(Modifier.size(38.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("RVH EDU", style = MaterialTheme.typography.titleMedium, letterSpacing = 1.1.sp)
                            Text("RECOVERY ADMIN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.4.sp)
                        }
                    }
                },
                actions = {
                    Surface(shape = CircleShape, color = if (hasKey) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(if (hasKey) Icons.Default.VerifiedUser else Icons.Default.Lock, null,
                                tint = if (hasKey) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp))
                            Text(if (hasKey) "SECURE" else "SETUP", style = MaterialTheme.typography.labelSmall,
                                color = if (hasKey) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Brand hero
            Surface(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent
            ) {
                Box(Modifier.background(Brush.linearGradient(listOf(
                    if (MaterialTheme.colorScheme.background == RvhDark.background) Color(0xFF173A78) else Color(0xFF2457C5),
                    if (MaterialTheme.colorScheme.background == RvhDark.background) Color(0xFF155B69) else Color(0xFF087F8C)
                )))) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(color = Color.White.copy(alpha = .16f), shape = CircleShape) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Text("ADMINISTRATOR WORKSPACE", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }
                            }
                        }
                        Text("Secure PIN\nrecovery", color = Color.White, style = MaterialTheme.typography.headlineMedium, lineHeight = 34.sp)
                        Text("Issue a request-bound recovery response for an RVH Edu teacher. Signing is performed locally on this device.",
                            color = Color.White.copy(alpha = .88f), style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Security, null, tint = Color.White, modifier = Modifier.size(17.dp))
                            Text("OFFLINE SIGNING  •  ED25519  •  ONE-TIME REQUESTS", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Operational summary
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryTile(Modifier.weight(1f), "SIGNING KEY", if (hasKey) "Protected" else "Not imported", hasKey, Icons.Default.Key)
                SummaryTile(Modifier.weight(1f), "CONNECTION", "Works offline", true, Icons.Default.Security)
            }

            SectionHeading("01", "Administrator key", "Set up the signing identity on this device")
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconBadge(Icons.Default.Key, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(if (hasKey) "Signing key is protected" else "Import your recovery key", style = MaterialTheme.typography.titleMedium)
                            Text(if (hasKey) "Encrypted with Android Keystore on this device." else "Import the PEM file from your secure administrator kit.",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (hasKey) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    Button(
                        onClick = { if (hasKey) showReplaceKeyDialog = true else picker.launch(arrayOf("application/x-pem-file", "application/octet-stream", "text/plain")) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)
                    ) {
                        Icon(if (hasKey) Icons.Default.Refresh else Icons.Default.Key, null)
                        Spacer(Modifier.width(9.dp))
                        Text(if (hasKey) "Replace administrator key" else "Import private key")
                    }
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(17.dp))
                        Text("The key is checked against this RVH Edu release, then encrypted. The original PEM is not retained by the app.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            SectionHeading("02", "Teacher's recovery request", "Paste the complete request received from the teacher")
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    OutlinedTextField(
                        value = request,
                        onValueChange = { request = it.trim(); response = "" },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 6,
                        label = { Text("Recovery request") },
                        placeholder = { Text("Paste the request code here…") },
                        supportingText = { Text("Paste the entire code exactly as received. Any new request replaces the previous pending one on that installation.") },
                        shape = RoundedCornerShape(15.dp)
                    )
                    Button(
                        enabled = hasKey && request.isNotBlank() && !busy,
                        onClick = {
                            busy = true
                            response = ""
                            try {
                                response = RecoverySigner.sign(context, request)
                                                status = "Response generated. Send it only to the teacher who supplied this request."
                            } catch (e: Exception) {
                                                status = e.message ?: "Unable to sign this request."
                            }
                            busy = false
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(15.dp)
                    ) {
                        if (busy) CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.Security, null)
                        Spacer(Modifier.width(9.dp))
                        Text(if (busy) "Signing securely…" else "Generate recovery response")
                        if (!busy) { Spacer(Modifier.weight(1f)); Icon(Icons.Default.ArrowForward, null) }
                    }
                    if (!hasKey) Text("Import your administrator key above to enable signing.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            AnimatedVisibility(visible = response.isNotBlank()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            IconBadge(Icons.Default.CheckCircle, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary)
                            Column(Modifier.weight(1f)) {
                                Text("Recovery response ready", style = MaterialTheme.typography.titleMedium)
                                Text("Generated for this exact request", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
                            SelectionContainer {
                                Text(response, Modifier.fillMaxWidth().padding(14.dp), style = MaterialTheme.typography.bodyMedium, letterSpacing = .3.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(ClipboardManager::class.java)
                                clipboard.setPrimaryClip(ClipData.newPlainText("RVH Edu recovery response", response))
                                                status = "Response copied to clipboard."
                            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                        ) { Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(8.dp)); Text("Copy response") }
                        Button(
                            onClick = {
                                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, response), "Send recovery response"))
                            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                        ) { Icon(Icons.Default.Send, null); Spacer(Modifier.width(8.dp)); Text("Share response with teacher") }
                    }
                }
            }

            // Guidance and trust footer
            Surface(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .65f), shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.VerifiedUser, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Administrator security", style = MaterialTheme.typography.titleSmall)
                        Text("Never share the private key or upload it to GitHub. A response is cryptographically bound to one request; it cannot be used for another installation or request.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            Text("RVH EDU  •  TRUSTED SCHOOL TECHNOLOGY", modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp)
        }
    }
}

@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(RvhBlue, RvhTeal))), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Shield, contentDescription = "RVH Edu", tint = Color.White, modifier = Modifier.padding(8.dp).fillMaxSize())
    }
}

@Composable
private fun IconBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, background: Color, foreground: Color) {
    Surface(shape = RoundedCornerShape(13.dp), color = background, modifier = Modifier.size(44.dp)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = foreground, modifier = Modifier.size(23.dp)) }
    }
}

@Composable
private fun SummaryTile(modifier: Modifier, label: String, value: String, positive: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ElevatedCard(modifier, colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = .6.sp)
            }
            Text(value, style = MaterialTheme.typography.titleMedium, color = if (positive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SectionHeading(number: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) { Text(number, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
