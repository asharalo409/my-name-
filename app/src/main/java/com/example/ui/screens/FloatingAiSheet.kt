package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AiMessage
import com.example.data.model.BloodRequest
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.FoundationSettings
import com.example.data.model.Fund
import com.example.data.model.Member
import com.example.data.model.NoticeItem
import com.example.data.model.UiState
import com.example.ui.components.ErrorBanner
import com.example.ui.viewmodel.AiViewModel
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingAiSheet(
    aiVm: AiViewModel,
    settings: FoundationSettings,
    funds: List<Fund>,
    donations: List<Donation>,
    expenses: List<Expense>,
    members: List<Member>,
    bloodRequests: List<BloodRequest>,
    notices: List<NoticeItem>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val messagesState by aiVm.messagesState.collectAsStateWithLifecycle()
    val convId by aiVm.activeConversationId.collectAsStateWithLifecycle()
    val busy by aiVm.chatBusy.collectAsStateWithLifecycle()
    val errorMsg by aiVm.chatError.collectAsStateWithLifecycle()
    val followUps by aiVm.followUps.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }

    fun send(text: String) {
        if (text.isBlank()) return
        aiVm.sendChat(
            userText = text,
            settings = settings,
            funds = funds,
            donations = donations,
            expenses = expenses,
            members = members,
            bloodRequests = bloodRequests,
            notices = notices
        )
        input = ""
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("মানবসেবা AI সহকারী", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "তহবিল, হিসাব ও দ্রুত এন্ট্রি সাহায্য",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(
                        onClick = { aiVm.selectConversation(null) },
                        modifier = Modifier.testTag("ai_new_chat_btn")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "নতুন চ্যাট")
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "বন্ধ করুন")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (errorMsg != null) {
                ErrorBanner(errorMsg!!)
                Spacer(Modifier.height(8.dp))
            }

            val messages = (messagesState as? UiState.Success)?.data.orEmpty()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            )
                        ) {
                            Text(
                                "আসসালামু আলাইকুম! আমি মানবসেবা AI। আপনি ফাউন্ডেশনের মোট তহবিল, সদস্য তালিকা, বকেয়া কিংবা নতুন অনুদান/খরচ যোগ করার জন্য বাংলায় লিখতে পারেন।",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
                items(messages, key = { it.id }) { msg ->
                    AiMessageBubble(
                        message = msg,
                        onApprove = {
                            val id = convId ?: return@AiMessageBubble
                            aiVm.executeActionProposal(id, msg, funds)
                        },
                        onReject = {
                            val id = convId ?: return@AiMessageBubble
                            aiVm.rejectActionProposal(id, msg.id)
                        }
                    )
                }
                if (busy) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("চিন্তা করছি...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                followUps.forEach { chipText ->
                    AssistChip(
                        onClick = { send(chipText) },
                        label = { Text(chipText, maxLines = 1) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("প্রশ্ন লিখুন বা হিসাব যোগ করতে বলুন...") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_sheet_input")
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { send(input) },
                    enabled = !busy && input.isNotBlank(),
                    modifier = Modifier
                        .size(50.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                        .testTag("ai_sheet_send_btn")
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "পাঠান",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AiMessageBubble(
    message: AiMessage,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val isUser = message.role == "user"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(message.content, style = MaterialTheme.typography.bodyMedium)

                if (message.actionStatus == "pending" && message.actionPayloadJson.isNotBlank()) {
                    val summary = remember(message.actionPayloadJson) {
                        try {
                            JSONObject(message.actionPayloadJson).optString("summary", "প্রস্তাবিত এন্ট্রি নিশ্চিত করবেন?")
                        } catch (_: Exception) {
                            "প্রস্তাবিত এন্ট্রি নিশ্চিত করবেন?"
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("প্রস্তাবিত কাজ:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(summary, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onApprove, modifier = Modifier.weight(1f).testTag("ai_action_approve")) {
                                    Text("নিশ্চিত করুন")
                                }
                                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f).testTag("ai_action_reject")) {
                                    Text("বাতিল")
                                }
                            }
                        }
                    }
                } else if (message.actionStatus == "executed") {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("ডাটাবেজে সংরক্ষিত হয়েছে", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
