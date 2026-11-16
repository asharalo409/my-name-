package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.model.FoundationSettings
import com.example.data.security.AppLockManager
import com.example.firebase.signOutGoogle
import com.example.ui.theme.Crimson600
import com.example.ui.theme.OrgPalettes
import com.example.ui.viewmodel.FoundationViewModel

@Composable
fun SettingsProfileScreen(
    vm: FoundationViewModel,
    settings: FoundationSettings,
    isDarkTheme: Boolean,
    userEmail: String,
    userName: String,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val lockManager = remember { AppLockManager(context) }

    var lockConfig by remember { mutableStateOf(lockManager.getSettings()) }
    var showSetPinDialog by remember { mutableStateOf(false) }

    var orgName by remember(settings.orgName) { mutableStateOf(settings.orgName) }
    var slogan by remember(settings.slogan) { mutableStateOf(settings.slogan) }
    var hotline by remember(settings.hotline) { mutableStateOf(settings.hotline) }
    var goalAmt by remember(settings.targetAmount) { mutableStateOf(settings.targetAmount.toLong().toString()) }
    var monthlyFee by remember(settings.monthlyFeeAmount) { mutableStateOf(settings.monthlyFeeAmount.toLong().toString()) }
    var bankDetails by remember(settings.bankDetails) { mutableStateOf(settings.bankDetails) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("লগইন প্রোফাইল", style = MaterialTheme.typography.titleLarge)
                    Text(userName.ifBlank { "সংগঠন অ্যাডমিন" }, style = MaterialTheme.typography.titleMedium)
                    Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            signOutGoogle(credentialManager, scope) {
                                onSignedOut()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Crimson600),
                        modifier = Modifier.testTag("sign_out_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("সাইন-আউট করুন")
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("থিম স্টুডিও (৭টি রঙের প্যালেট)", style = MaterialTheme.typography.titleLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OrgPalettes.forEach { pal ->
                            val selected = settings.paletteKey == pal.key
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(pal.primary, CircleShape)
                                    .clickable {
                                        vm.updateSettings(settings.copy(paletteKey = pal.key))
                                    }
                                    .testTag("palette_${pal.key}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(Icons.Rounded.Check, contentDescription = pal.labelBn, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.DarkMode, contentDescription = null)
                            Spacer(Modifier.width(10.dp))
                            Text("ডার্ক মোড", style = MaterialTheme.typography.titleMedium)
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { vm.toggleDarkMode(isDarkTheme) },
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("অ্যাপ লক ও নিরাপত্তা", style = MaterialTheme.typography.titleLarge)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (lockConfig.enabled) "পিন লক চালু আছে" else "পিন লক বন্ধ আছে", style = MaterialTheme.typography.bodyMedium)
                        if (lockConfig.enabled) {
                            OutlinedButton(onClick = { lockConfig = lockManager.disableLock() }) { Text("বন্ধ করুন") }
                        } else {
                            Button(onClick = { showSetPinDialog = true }) { Text("পিন সেট করুন") }
                        }
                    }
                    if (lockConfig.enabled && lockManager.isBiometricAvailable(context)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Fingerprint, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("ফিঙ্গারপ্রিন্ট / বায়োমেট্রিক")
                            }
                            Switch(
                                checked = lockConfig.biometricEnabled,
                                onCheckedChange = { lockConfig = lockManager.setBiometricEnabled(it) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ফাউন্ডেশন তথ্য ও পেমেন্ট সেটিংস", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(value = orgName, onValueChange = { orgName = it }, label = { Text("ফাউন্ডেশনের নাম") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = slogan, onValueChange = { slogan = it }, label = { Text("স্লোগান") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = hotline, onValueChange = { hotline = it }, label = { Text("জরুরি হটলাইন") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = goalAmt, onValueChange = { goalAmt = it }, label = { Text("লক্ষ্যমাত্রা (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = monthlyFee, onValueChange = { monthlyFee = it }, label = { Text("মাসিক সদস্য চাঁদা হার (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bankDetails, onValueChange = { bankDetails = it }, label = { Text("বিকাশ / নগদ / ব্যাংক বিবরণ") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            vm.updateSettings(
                                settings.copy(
                                    orgName = orgName.ifBlank { settings.orgName },
                                    slogan = slogan,
                                    hotline = hotline,
                                    targetAmount = goalAmt.toDoubleOrNull() ?: settings.targetAmount,
                                    monthlyFeeAmount = monthlyFee.toDoubleOrNull() ?: settings.monthlyFeeAmount,
                                    bankDetails = bankDetails
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_org_settings_btn")
                    ) {
                        Text("সেটিংস সংরক্ষণ করুন")
                    }
                }
            }
        }
    }

    if (showSetPinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSetPinDialog = false },
            title = { Text("৪ সংখ্যার পিন সেট করুন") },
            text = {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) newPin = it },
                    label = { Text("৪ সংখ্যার পিন") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            lockConfig = lockManager.setPin(newPin)
                            showSetPinDialog = false
                        }
                    },
                    enabled = newPin.length == 4
                ) { Text("সেট করুন") }
            },
            dismissButton = { TextButton(onClick = { showSetPinDialog = false }) { Text("বাতিল") } }
        )
    }
}
