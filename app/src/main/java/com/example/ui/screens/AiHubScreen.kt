package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ai.AiActionDraft
import com.example.data.ai.LedgerOcrEntry
import com.example.data.ai.LegacyMemberImportEntry
import com.example.data.model.AiChatMessage
import com.example.data.model.AiConversation
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Fund
import com.example.data.model.Member
import com.example.data.model.OrgSettings
import com.example.ui.util.formatTaka
import com.example.ui.viewmodel.AiStreamState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHubScreen(
    settings: OrgSettings,
    funds: List<Fund>,
    members: List<Member>,
    donations: List<Donation>,
    expenses: List<Expense>,
    conversations: List<AiConversation>,
    activeConversationId: String?,
    messages: List<AiChatMessage>,
    chatStreamState: AiStreamState,
    pendingAction: AiActionDraft?,
    auditState: AiStreamState,
    ocrBusy: Boolean,
    ocrSummary: String,
    ocrEntries: List<LedgerOcrEntry>,
    ocrError: String?,
    legacyBusy: Boolean,
    legacyEntries: List<LegacyMemberImportEntry>,
    legacyError: String?,
    onSelectConversation: (String?) -> Unit,
    onNewConversation: () -> Unit,
    onSendPrompt: (String, String) -> Unit,
    onExecuteAction: (AiActionDraft) -> Unit,
    onDismissAction: () -> Unit,
    onRunAudit: (String) -> Unit,
    onAnalyzePhoto: (Bitmap) -> Unit,
    onImportOcr: (List<LedgerOcrEntry>) -> Unit,
    onParseLegacy: (String) -> Unit,
    onImportLegacy: (List<LegacyMemberImportEntry>) -> Unit
) {
    val context = LocalContext.current
    var aiTab by remember { mutableIntStateOf(0) }
    var promptText by remember { mutableStateOf("") }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var legacyRawText by remember {
        mutableStateOf("রফিকুল ইসলাম, MSF-201, 01711223344, সদস্য, 1500\nসুমাইয়া আক্তার, MSF-202, 01822334455, কোষাধ্যক্ষ, 2500")
    }

    val totalIn = donations.filter { it.status == "verified" }.sumOf { it.amount }
    val totalOut = expenses.sumOf { it.amount }
    val orgContextSummary = remember(settings, funds, members, donations, expenses) {
        """
        Organization: ${settings.orgName}
        Hotline: ${settings.hotline ?: ""}
        Default Monthly Fee: ${settings.defaultMonthlyFee} BDT
        Verified Donations Total: $totalIn BDT (${donations.size} entries)
        Total Expenses: $totalOut BDT (${expenses.size} entries)
        Net Cash Balance: ${totalIn - totalOut} BDT
        Members Count: ${members.size}
        Funds: ${funds.joinToString { "${it.name} (Target ${it.targetAmount})" }}
        """.trimIndent()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bmp = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (_: Exception) {
                null
            }
            if (bmp != null) {
                selectedBitmap = bmp
                onAnalyzePhoto(bmp)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        val tabs = listOf("AI সহকারী", "খাতা রিডার", "আর্থিক অডিট", "লিগ্যাসি ইমপোর্ট")
        PrimaryScrollableTabRow(selectedTabIndex = aiTab, edgePadding = 12.dp) {
            tabs.forEachIndexed { idx, label ->
                Tab(selected = aiTab == idx, onClick = { aiTab = idx }, text = { Text(label) })
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize()
            ) {
                when (aiTab) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item {
                                        FilledTonalButton(onClick = onNewConversation) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("নতুন চ্যাট")
                                        }
                                    }
                                    items(conversations.take(5), key = { it.id }) { conv ->
                                        FilterChip(
                                            selected = activeConversationId == conv.id,
                                            onClick = { onSelectConversation(conv.id) },
                                            label = { Text(conv.title.take(22)) }
                                        )
                                    }
                                }
                            }

                            if (messages.isEmpty() && chatStreamState is AiStreamState.Idle) {
                                item {
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))) {
                                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Text("মানবসেবা AI সহকারী", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            }
                                            Text("তহবিলের হিসাব জিজ্ঞাসা করুন অথবা সরাসরি নির্দেশ দিন—যেমন: 'রহিম ভাই ৫০০ টাকা বিকাশে অনুদান দিয়েছেন নোট করুন'।")
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                AssistChip(onClick = { onSendPrompt("বর্তমান মোট তহবিল ও খরচ কত?", orgContextSummary) }, label = { Text("মোট তহবিল?") })
                                                AssistChip(onClick = { onSendPrompt("জরুরি ত্রাণ তহবিলের পরামর্শ দাও", orgContextSummary) }, label = { Text("ত্রাণ পরামর্শ") })
                                            }
                                        }
                                    }
                                }
                            }

                            items(messages, key = { it.id }) { m ->
                                val isUser = m.role == "user"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        contentColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                        shape = RoundedCornerShape(16.dp),
                                        tonalElevation = 1.dp,
                                        modifier = Modifier.widthIn(max = 520.dp)
                                    ) {
                                        Text(m.content, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                                    }
                                }
                            }

                            when (chatStreamState) {
                                is AiStreamState.Loading -> {
                                    item {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                            Text("মানবসেবা AI চিন্তা করছে...", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                                is AiStreamState.Streaming -> {
                                    item {
                                        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp)) {
                                            Text(chatStreamState.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                                        }
                                    }
                                }
                                is AiStreamState.Error -> {
                                    item {
                                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                                            Text(chatStreamState.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
                                        }
                                    }
                                }
                                else -> {}
                            }

                            if (pendingAction != null) {
                                item {
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("প্রস্তাবিত এন্ট্রি: ${pendingAction.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(pendingAction.summary, style = MaterialTheme.typography.bodyMedium)
                                            Text("পরিমাণ: ${formatTaka(pendingAction.amount)} • খাত: ${pendingAction.fundName}", style = MaterialTheme.typography.labelLarge)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(onClick = { onExecuteAction(pendingAction) }) {
                                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    Text("অনুমোদন ও সংরক্ষণ")
                                                }
                                                TextButton(onClick = onDismissAction) { Text("বাতিল") }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Surface(tonalElevation = 3.dp) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = promptText,
                                    onValueChange = { promptText = it },
                                    placeholder = { Text("প্রশ্ন লিখুন বা হিসাব এন্ট্রি বলুন...") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("ai_chat_input"),
                                    singleLine = true
                                )
                                IconButton(
                                    onClick = {
                                        val txt = promptText.trim()
                                        if (txt.isNotEmpty()) {
                                            promptText = ""
                                            onSendPrompt(txt, orgContextSummary)
                                        }
                                    },
                                    modifier = Modifier.testTag("ai_send_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "পাঠান", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    1 -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Rounded.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("হাতে লেখা খাতা ও রসিদ রিডার (AI OCR)", style = MaterialTheme.typography.titleMedium)
                                        }
                                        Text("খাতার পাতার ছবি নির্বাচন করুন। Gemini 3.8 Flash স্বয়ংক্রিয়ভাবে নাম, টাকার পরিমাণ এবং তহবিল আলাদা করবে।", style = MaterialTheme.typography.bodyMedium)
                                        Button(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("pick_ledger_photo_button")
                                        ) {
                                            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("গ্যালারি থেকে খাতার ছবি বাছাই করুন")
                                        }
                                    }
                                }
                            }

                            if (selectedBitmap != null) {
                                item {
                                    Image(
                                        bitmap = selectedBitmap!!.asImageBitmap(),
                                        contentDescription = "নির্বাচিত খাতার ছবি",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(16.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            if (ocrBusy) {
                                item {
                                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator()
                                        Spacer(Modifier.width(12.dp))
                                        Text("হাতে লেখা হিসাব পড়া হচ্ছে...")
                                    }
                                }
                            }

                            if (!ocrError.isNullOrBlank()) {
                                item { Text(ocrError, color = MaterialTheme.colorScheme.error) }
                            }

                            if (ocrSummary.isNotBlank()) {
                                item {
                                    Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                        Text(ocrSummary, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }

                            items(ocrEntries, key = { "${it.rowNumber}_${it.personOrTitle}" }) { entry ->
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(entry.personOrTitle, style = MaterialTheme.typography.labelLarge)
                                            Text("${entry.entryType} • ${entry.fundName} • ${entry.method}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(formatTaka(entry.amount), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            if (ocrEntries.isNotEmpty()) {
                                item {
                                    Button(onClick = { onImportOcr(ocrEntries) }, modifier = Modifier.fillMaxWidth()) {
                                        Text("সবগুলো (${ocrEntries.size}) হিসাবের খাতায় যোগ করুন")
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Rounded.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("AI আর্থিক স্বাস্থ্য ও স্বচ্ছতা অডিট", style = MaterialTheme.typography.titleMedium)
                                        }
                                        Text("লাইভ আয়, ব্যয়, তহবিল ভারসাম্য এবং সদস্য সক্রিয়তার ওপর ভিত্তি করে অডিট রিপোর্ট।", style = MaterialTheme.typography.bodyMedium)
                                        Button(
                                            onClick = { onRunAudit(orgContextSummary) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("run_ai_audit_button")
                                        ) {
                                            Text("অডিট বিশ্লেষণ শুরু করুন")
                                        }
                                    }
                                }
                            }
                            when (auditState) {
                                is AiStreamState.Loading -> item { CircularProgressIndicator() }
                                is AiStreamState.Streaming -> item {
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Text(auditState.text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                                is AiStreamState.Success -> item {
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Text(auditState.text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                                is AiStreamState.Error -> item {
                                    Text(auditState.message, color = MaterialTheme.colorScheme.error)
                                }
                                else -> {}
                            }
                        }
                    }

                    3 -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Rounded.UploadFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("পুরনো সদস্য তালিকা ইমপোর্ট (AI Parser)", style = MaterialTheme.typography.titleMedium)
                                        }
                                        OutlinedTextField(
                                            value = legacyRawText,
                                            onValueChange = { legacyRawText = it },
                                            minLines = 4,
                                            label = { Text("CSV বা টেক্সট পেস্ট করুন") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Button(onClick = { onParseLegacy(legacyRawText) }, enabled = !legacyBusy, modifier = Modifier.fillMaxWidth()) {
                                            Text(if (legacyBusy) "বিশ্লেষণ হচ্ছে..." else "AI দিয়ে সদস্য তালিকা পার্স করুন")
                                        }
                                    }
                                }
                            }
                            if (!legacyError.isNullOrBlank()) {
                                item { Text(legacyError, color = MaterialTheme.colorScheme.error) }
                            }
                            items(legacyEntries, key = { it.memberCode + it.fullName }) { item ->
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("${item.fullName} (${item.memberCode})", style = MaterialTheme.typography.labelLarge)
                                            Text("${item.phone} • ${item.role}", style = MaterialTheme.typography.labelSmall)
                                        }
                                        Text(formatTaka(item.openingBalance), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            if (legacyEntries.isNotEmpty()) {
                                item {
                                    OutlinedButton(onClick = { onImportLegacy(legacyEntries) }, modifier = Modifier.fillMaxWidth()) {
                                        Text("সবগুলো (${legacyEntries.size}) সদস্য ডাটাবেসে যোগ করুন")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
