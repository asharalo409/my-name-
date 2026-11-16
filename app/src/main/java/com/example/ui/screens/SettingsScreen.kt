package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.OrgSettings
import com.example.ui.theme.ThemePalettes
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: OrgSettings,
    appLockEnabled: Boolean,
    biometricAvailable: Boolean,
    onSaveSettings: (OrgSettings) -> Unit,
    onEnableAppLock: (String, Boolean) -> Unit,
    onDisableAppLock: () -> Unit,
    onSignOut: () -> Unit
) {
    val user = Firebase.auth.currentUser
    var orgName by remember(settings.orgName) { mutableStateOf(settings.orgName) }
    var slogan by remember(settings.slogan) { mutableStateOf(settings.slogan) }
    var hotline by remember(settings.hotline) { mutableStateOf(settings.hotline ?: "") }
    var bankDetails by remember(settings.bankDetails) { mutableStateOf(settings.bankDetails ?: "") }
    var feeText by remember(settings.defaultMonthlyFee) { mutableStateOf(settings.defaultMonthlyFee.toInt().toString()) }
    var goalText by remember(settings.goalAmount) { mutableStateOf(settings.goalAmount.toInt().toString()) }
    var showPinSetup by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.AccountCircle, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(user?.displayName ?: "ফাউন্ডেশন সদস্য", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(user?.email ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier.testTag("sign_out_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("সাইন আউট")
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("থিম স্টুডিও ও ভাষা", style = MaterialTheme.typography.titleMedium)
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ThemePalettes.ALL.forEach { pal ->
                                val selected = settings.themePalette == pal.key
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(pal.primary)
                                        .border(if (selected) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        .clickable { onSaveSettings(settings.copy(themePalette = pal.key)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) Icon(Icons.Rounded.Check, contentDescription = pal.bnName, tint = Color.White)
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("ডার্ক মোড (Dark Theme)", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = settings.darkMode,
                                onCheckedChange = { onSaveSettings(settings.copy(darkMode = it)) }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = settings.language == "bn",
                                onClick = { onSaveSettings(settings.copy(language = "bn")) },
                                label = { Text("বাংলা (BN)") }
                            )
                            FilterChip(
                                selected = settings.language == "en",
                                onClick = { onSaveSettings(settings.copy(language = "en")) },
                                label = { Text("English (EN)") }
                            )
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("অ্যাপ লক ও নিরাপত্তা", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            if (appLockEnabled) "পিন / বায়োমেট্রিক লক সক্রিয় আছে।" else "অ্যাপ খোলার সময় ৪ ডিজিটের পিন বা ফিঙ্গারপ্রিন্ট সুরক্ষা চালু করুন।",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { showPinSetup = true }) {
                                Text(if (appLockEnabled) "পিন পরিবর্তন করুন" else "অ্যাপ লক চালু করুন")
                            }
                            if (appLockEnabled) {
                                OutlinedButton(onClick = onDisableAppLock) { Text("বন্ধ করুন") }
                            }
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("সংগঠনের তথ্য ও ব্যাংকিং", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = orgName, onValueChange = { orgName = it }, label = { Text("সংগঠনের নাম") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = slogan, onValueChange = { slogan = it }, label = { Text("স্লোগান") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = hotline, onValueChange = { hotline = it }, label = { Text("হটলাইন নম্বর") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = bankDetails, onValueChange = { bankDetails = it }, label = { Text("বিকাশ / ব্যাংক তথ্য") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = feeText,
                                onValueChange = { feeText = it },
                                label = { Text("মাসিক চাঁদা (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = goalText,
                                onValueChange = { goalText = it },
                                label = { Text("বার্ষিক লক্ষ্য (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Button(
                            onClick = {
                                onSaveSettings(
                                    settings.copy(
                                        orgName = orgName.trim().ifBlank { "মানবসেবা ফাউন্ডেশন" },
                                        slogan = slogan.trim(),
                                        hotline = hotline.trim().ifBlank { null },
                                        bankDetails = bankDetails.trim().ifBlank { null },
                                        defaultMonthlyFee = feeText.toDoubleOrNull() ?: settings.defaultMonthlyFee,
                                        goalAmount = goalText.toDoubleOrNull() ?: settings.goalAmount
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("save_org_settings_button")
                        ) {
                            Text("সংগঠনের সেটিংস সংরক্ষণ করুন")
                        }
                    }
                }
            }
        }
    }

    if (showPinSetup) {
        var newPin by remember { mutableStateOf("") }
        var bio by remember { mutableStateOf(biometricAvailable) }
        AlertDialog(
            onDismissRequest = { showPinSetup = false },
            title = { Text("৪ ডিজিটের অ্যাপ পিন সেট করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPin = it },
                        label = { Text("৪ ডিজিট পিন") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true
                    )
                    if (biometricAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = bio, onCheckedChange = { bio = it })
                            Text("ফিঙ্গারপ্রিন্ট / বায়োমেট্রিক চালু রাখুন")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPin.length == 4) {
                        onEnableAppLock(newPin, bio)
                        showPinSetup = false
                    }
                }) { Text("সক্রিয় করুন") }
            },
            dismissButton = { TextButton(onClick = { showPinSetup = false }) { Text("বাতিল") } }
        )
    }
}
