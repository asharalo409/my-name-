package com.example.ui.screens

import androidx.biometric.BiometricPrompt
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.islamic.PrayerCalculator
import com.example.data.security.AppLockManager

@Composable
fun AppLockScreen(
    lockManager: AppLockManager,
    biometricEnabled: Boolean,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun triggerBiometric() {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onUnlocked()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        errorText = errString.toString()
                    }
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("মানবসেবা ফাউন্ডেশন")
            .setSubtitle("অ্যাপ আনলক করতে আপনার বায়োমেট্রিক যাচাই করুন")
            .setNegativeButtonText("পিন ব্যবহার করুন")
            .build()
        prompt.authenticate(info)
    }

    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled && lockManager.isBiometricAvailable(context)) {
            triggerBiometric()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "অ্যাপ লক",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Text("অ্যাপ লক সুরক্ষিত", style = MaterialTheme.typography.headlineSmall)
            Text(
                "চালিয়ে যেতে ৪ সংখ্যার গোপন পিন দিন",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(4) { idx ->
                    val filled = idx < pin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                    )
                }
            }

            if (errorText != null) {
                Text(errorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { digit ->
                            FilledTonalButton(
                                onClick = {
                                    if (pin.length < 4) {
                                        val next = pin + digit
                                        pin = next
                                        errorText = null
                                        if (next.length == 4) {
                                            if (lockManager.verifyPin(next)) {
                                                onUnlocked()
                                            } else {
                                                errorText = "ভুল পিন! আবার চেষ্টা করুন।"
                                                pin = ""
                                            }
                                        }
                                    }
                                },
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(68.dp)
                                    .testTag("pin_key_$digit")
                            ) {
                                Text(
                                    PrayerCalculator.bnDigits(digit),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (biometricEnabled && lockManager.isBiometricAvailable(context)) {
                        OutlinedButton(
                            onClick = { triggerBiometric() },
                            shape = CircleShape,
                            modifier = Modifier
                                .size(68.dp)
                                .testTag("biometric_unlock_button")
                        ) {
                            Icon(Icons.Rounded.Fingerprint, contentDescription = "বায়োমেট্রিক")
                        }
                    } else {
                        Spacer(Modifier.size(68.dp))
                    }
                    FilledTonalButton(
                        onClick = {
                            if (pin.length < 4) {
                                val next = pin + "0"
                                pin = next
                                if (next.length == 4) {
                                    if (lockManager.verifyPin(next)) {
                                        onUnlocked()
                                    } else {
                                        errorText = "ভুল পিন! আবার চেষ্টা করুন।"
                                        pin = ""
                                    }
                                }
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(68.dp)
                            .testTag("pin_key_0")
                    ) {
                        Text(PrayerCalculator.bnDigits("0"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(68.dp)
                            .testTag("pin_backspace")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "মুছুন")
                    }
                }
            }
        }
    }
}
