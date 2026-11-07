package com.henrylumis.rvhgrader.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "rvh_grader_prefs"
private const val KEY_PIN = "access_pin" // legacy plaintext key; migrated on successful unlock
private const val KEY_PIN_SALT = "access_pin_salt"
private const val KEY_PIN_HASH = "access_pin_hash"

private data class PinState(val credential: PinCredential.Stored?, val legacyPin: String?) {
    val isConfigured: Boolean get() = credential != null || legacyPin != null
}

private fun getPinState(context: Context): PinState {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val salt = prefs.getString(KEY_PIN_SALT, null)
    val hash = prefs.getString(KEY_PIN_HASH, null)
    val credential = if (salt != null && hash != null) PinCredential.Stored(salt, hash) else null
    return PinState(credential, if (credential == null) prefs.getString(KEY_PIN, null) else null)
}

private fun savePin(context: Context, pin: String) {
    val credential = PinCredential.create(pin)
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        .putString(KEY_PIN_SALT, credential.saltHex)
        .putString(KEY_PIN_HASH, credential.hashHex)
        .remove(KEY_PIN)
        .apply()
}

private fun verifyAndUpgradePin(context: Context, pin: String, state: PinState): Boolean {
    state.credential?.let { return PinCredential.verify(pin, it) }
    val legacy = state.legacyPin ?: return false
    if (pin != legacy) return false
    // Preserve existing installations while upgrading away from plaintext storage.
    savePin(context, pin)
    return true
}

/** Local PIN gate: casual-access protection for a shared device, not hardware-backed security. */
@Composable
fun PinGate(context: Context, onUnlocked: () -> Unit) {
    var pinState by remember { mutableStateOf(getPinState(context)) }
    var input by remember { mutableStateOf("") }
    var confirmInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isWorking by remember { mutableStateOf(false) }
    var recoveryRequest by remember { mutableStateOf(PinRecovery.loadActiveRequest(context)?.encoded) }
    var recoveryResponse by remember { mutableStateOf("") }
    var recoveryMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val creating = !pinState.isConfigured

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandHeader()
            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(42.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (creating) "＋" else "⌑", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (creating) "Secure your workspace" else "Welcome back",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                if (creating) "Create a personal access PIN" else "Enter your PIN to continue",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(18.dp))

                    if (creating) {
                        Text(
                            "Choose 4–8 digits you can remember. You'll use this PIN whenever you open RVH Edu.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        PinInput(
                            value = input,
                            onValueChange = { input = it; error = null },
                            label = "Create PIN"
                        )
                        Spacer(Modifier.height(12.dp))
                        PinInput(
                            value = confirmInput,
                            onValueChange = { confirmInput = it; error = null },
                            label = "Confirm PIN"
                        )
                    } else {
                        Text(
                            "Your school workspace is stored on this device. Enter your PIN to continue.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        if (recoveryMode) {
                            Text("Forgot your PIN? Contact the RVH Edu administrator using WhatsApp, Telegram or email. Generate a request first; the request will be attached automatically when you choose a contact method. Your school data remains on this device.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = {
                                recoveryRequest = PinRecovery.createRequest(context).encoded
                                recoveryResponse = ""
                                error = null
                            }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text(if (recoveryRequest == null) "Generate recovery request" else "Generate a replacement request") }
                            recoveryRequest?.let { req ->
                                Spacer(Modifier.height(8.dp))
                                Text("Saved on this device · valid for up to 7 days · you can close RVH Edu and return later", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                OutlinedTextField(
                                    value = req,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Your unique recovery request") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    maxLines = 5
                                )
                                Spacer(Modifier.height(12.dp))
                                Text("Contact RVH Edu Support", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("WhatsApp / Telegram: +256 786 997 653\nEmail: henrylumis811@gmail.com", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(onClick = { sharePinRecoveryRequest(context, "whatsapp", req) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Send request on WhatsApp")
                                }
                                Spacer(Modifier.height(6.dp))
                                OutlinedButton(onClick = { sharePinRecoveryRequest(context, "telegram", req) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Send request on Telegram")
                                }
                                Spacer(Modifier.height(6.dp))
                                OutlinedButton(onClick = { sharePinRecoveryRequest(context, "email", req) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Send request by email")
                                }
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(value = recoveryResponse, onValueChange = { recoveryResponse = it.trim(); error = null }, label = { Text("Administrator recovery response") }, modifier = Modifier.fillMaxWidth())
                            }
                        } else {
                            PinInput(value = input, onValueChange = { input = it; error = null }, label = "Access PIN")
                        }
                    }

                    error?.let {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                it,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Button(
                        enabled = !isWorking,
                        onClick = {
                            if (isWorking) return@Button
                            if (!creating && recoveryMode) {
                                val req = recoveryRequest
                                if (req.isNullOrBlank() || recoveryResponse.isBlank()) {
                                    error = "Generate a request and enter the response from RVH Edu support."
                                } else {
                                    isWorking = true
                                    scope.launch {
                                        val valid = withContext(Dispatchers.Default) {
                                            PinRecovery.verifyAndConsume(context, req, recoveryResponse)
                                        }
                                        isWorking = false
                                        if (valid) {
                                            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                                                .remove(KEY_PIN).remove(KEY_PIN_SALT).remove(KEY_PIN_HASH).apply()
                                            pinState = getPinState(context)
                                            recoveryMode = false
                                            recoveryRequest = null
                                            recoveryResponse = ""
                                            input = ""
                                            error = "Recovery verified. Create your new PIN."
                                        } else {
                                            recoveryResponse = ""
                                            error = "Recovery response is invalid, expired, already used, or belongs to another installation."
                                        }
                                    }
                                }
                            } else if (creating && input.length < 4) {
                                error = "Your PIN must contain at least 4 digits."
                            } else if (creating && input != confirmInput) {
                                error = "Those PINs don't match. Try again."
                            } else {
                                val submittedPin = input
                                isWorking = true
                                error = null
                                scope.launch {
                                    val outcome = withContext(Dispatchers.Default) {
                                        try {
                                            if (creating) {
                                                savePin(context, submittedPin)
                                                true
                                            } else {
                                                verifyAndUpgradePin(context, submittedPin, pinState)
                                            }
                                        } catch (_: Exception) {
                                            // Crypto/provider or preference failures must not crash the app.
                                            false
                                        }
                                    }
                                    isWorking = false
                                    if (outcome) {
                                        if (creating) {
                                            pinState = getPinState(context)
                                            input = ""
                                            confirmInput = ""
                                        }
                                        onUnlocked()
                                    } else {
                                        error = if (creating) {
                                            "Couldn't save your PIN. Please try again."
                                        } else {
                                            "That PIN isn't correct, or verification failed. Please try again."
                                        }
                                        input = ""
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isWorking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (creating) "Create PIN & continue" else if (recoveryMode) "Verify recovery response" else "Unlock RVH Edu",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (!creating && !recoveryMode) {
                        TextButton(onClick = { recoveryRequest = PinRecovery.loadActiveRequest(context)?.encoded; recoveryMode = true; error = null }) {
                            Text("Forgot PIN?")
                        }
                    } else if (!creating && recoveryMode) {
                        TextButton(onClick = { recoveryMode = false; recoveryRequest = null; recoveryResponse = ""; error = null }) {
                            Text("Back to PIN entry")
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("●", color = MaterialTheme.colorScheme.secondary, fontSize = 9.sp)
                        Spacer(Modifier.width(7.dp))
                        Text(
                            "Works offline · PIN stays on this device",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "For privacy, choose a PIN you don't use elsewhere.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Shares the exact installation-bound request through the selected support channel. */
private fun sharePinRecoveryRequest(context: Context, channel: String, request: String) {
    val message = "RVH Edu PIN recovery request\nPlease generate a recovery response for this request:\n$request"
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    try {
        when (channel) {
            "whatsapp" -> {
                val encoded = Uri.encode(message)
                val directChat = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/256786997653?text=$encoded"))
                try {
                    context.startActivity(directChat)
                } catch (_: android.content.ActivityNotFoundException) {
                    context.startActivity(Intent.createChooser(shareIntent, "Send recovery request"))
                }
            }
            "telegram" -> {
                val encoded = Uri.encode(message)
                val directChat = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?phone=256786997653&text=$encoded"))
                try {
                    context.startActivity(directChat)
                } catch (_: android.content.ActivityNotFoundException) {
                    shareIntent.setPackage("org.telegram.messenger")
                    try { context.startActivity(shareIntent) } catch (_: android.content.ActivityNotFoundException) {
                        shareIntent.setPackage(null)
                        context.startActivity(Intent.createChooser(shareIntent, "Send recovery request"))
                    }
                }
            }
            "email" -> {
                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:henrylumis811@gmail.com")).apply {
                    putExtra(Intent.EXTRA_SUBJECT, "RVH Edu PIN Recovery Request")
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                try {
                    context.startActivity(emailIntent)
                } catch (_: android.content.ActivityNotFoundException) {
                    context.startActivity(Intent.createChooser(shareIntent, "Email recovery request"))
                }
            }
            else -> context.startActivity(Intent.createChooser(shareIntent, "Send recovery request"))
        }
    } catch (_: Exception) {
        // Missing or disabled messaging apps must never crash the PIN gate.
        try { context.startActivity(Intent.createChooser(shareIntent.apply { setPackage(null) }, "Send recovery request")) } catch (_: Exception) { }
    }
}

@Composable
private fun PinInput(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { typed -> onValueChange(typed.filter(Char::isDigit).take(8)) },
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        supportingText = { Text("4–8 digits", style = MaterialTheme.typography.labelSmall) }
    )
}

@Composable
private fun BrandHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = com.henrylumis.rvhgrader.R.drawable.rvh_edu_brand),
            contentDescription = "RVH Edu — Learn, Manage, Excel",
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 260.dp)
                .aspectRatio(1f)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "In dedication to Hellen",
            style = MaterialTheme.typography.labelMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
