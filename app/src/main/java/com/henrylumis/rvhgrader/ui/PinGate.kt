package com.henrylumis.rvhgrader.ui

import android.content.Context
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
                        PinInput(value = input, onValueChange = { input = it; error = null }, label = "Access PIN")
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
                        onClick = {
                            if (creating) {
                                when {
                                    input.length < 4 -> error = "Your PIN must contain at least 4 digits."
                                    input != confirmInput -> error = "Those PINs don't match. Try again."
                                    else -> {
                                        savePin(context, input)
                                        pinState = getPinState(context)
                                        input = ""
                                        confirmInput = ""
                                        onUnlocked()
                                    }
                                }
                            } else if (verifyAndUpgradePin(context, input, pinState)) {
                                onUnlocked()
                            } else {
                                error = "That PIN isn't correct. Please try again."
                                input = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (creating) "Create PIN & continue" else "Unlock RVH Edu",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
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
