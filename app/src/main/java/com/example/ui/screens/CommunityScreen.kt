package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.FieldReport
import com.example.data.model.FoundationEvent
import com.example.data.model.FoundationRoles
import com.example.data.model.Member
import com.example.data.model.Notice
import com.example.data.model.OrgSettings
import com.example.data.model.Poll
import com.example.data.model.Project
import com.example.ui.components.BloodBadge
import com.example.ui.components.SectionHeader
import com.example.ui.util.dialPhone
import com.example.ui.util.formatNumber
import com.example.ui.util.formatTaka

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    settings: OrgSettings,
    projects: List<Project>,
    reports: List<FieldReport>,
    members: List<Member>,
    events: List<FoundationEvent>,
    polls: List<Poll>,
    notices: List<Notice>,
    messages: List<ChatMessage>,
    onSaveProject: (Project) -> Unit,
    onSaveReport: (FieldReport) -> Unit,
    onSaveMember: (Member) -> Unit,
    onDeleteMember: (String) -> Unit,
    onSaveEvent: (FoundationEvent) -> Unit,
    onRsvpEvent: (FoundationEvent) -> Unit,
    onSavePoll: (Poll) -> Unit,
    onCastVote: (Poll, Int) -> Unit,
    onSaveNotice: (Notice) -> Unit,
    onReactNotice: (Notice) -> Unit,
    onSendChat: (String) -> Unit,
    onDeleteChat: (String) -> Unit
) {
    val context = LocalContext.current
    val lang = settings.language
    var tab by remember { mutableIntStateOf(0) }
    var showAddMember by remember { mutableStateOf(false) }
    var showAddNotice by remember { mutableStateOf(false) }
    var showAddProject by remember { mutableStateOf(false) }
    var showAddReport by remember { mutableStateOf(false) }
    var showAddEvent by remember { mutableStateOf(false) }
    var chatDraft by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        val tabs = listOf("প্রকল্প ও কাজ", "সদস্য ও কমিটি", "ইভেন্ট", "ভোট ও নোটিশ", "সদস্য চ্যাট")
        PrimaryScrollableTabRow(selectedTabIndex = tab, edgePadding = 12.dp) {
            tabs.forEachIndexed { idx, label ->
                Tab(selected = tab == idx, onClick = { tab = idx }, text = { Text(label) })
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.widthIn(max = 840.dp).fillMaxSize()) {
                if (tab == 4) {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (messages.isEmpty()) {
                            item { Text("এখনও কোনো বার্তা নেই। কমিউনিটি রুমে কথোপকথন শুরু করুন।", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        items(messages, key = { it.id }) { m ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(m.senderName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                        Text(m.body, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    IconButton(onClick = { onDeleteChat(m.id) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                    Surface(tonalElevation = 2.dp) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = chatDraft,
                                onValueChange = { chatDraft = it },
                                placeholder = { Text("সদস্য চ্যাটে বার্তা লিখুন...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = {
                                if (chatDraft.isNotBlank()) {
                                    onSendChat(chatDraft.trim())
                                    chatDraft = ""
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "পাঠান", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (tab) {
                            0 -> {
                                item {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { showAddProject = true }, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("নতুন প্রকল্প")
                                        }
                                        OutlinedButton(onClick = { showAddReport = true }, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("কাজের রিপোর্ট")
                                        }
                                    }
                                }
                                items(projects, key = { it.id }) { p ->
                                    val banner = if (p.bannerKey == "edu") R.drawable.img_project_edu else R.drawable.img_project_flood
                                    val progress = if (p.goalAmount > 0) (p.spentAmount / p.goalAmount).toFloat().coerceIn(0f, 1f) else 0f
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Column {
                                            Image(painter = painterResource(id = banner), contentDescription = p.title, modifier = Modifier.fillMaxWidth().height(130.dp), contentScale = ContentScale.Crop)
                                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(p.title, style = MaterialTheme.typography.titleMedium)
                                                Text("${p.area} • লক্ষ্য: ${formatTaka(p.goalAmount, lang)} • ব্যয়: ${formatTaka(p.spentAmount, lang)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                if (!p.description.isNullOrBlank()) Text(p.description, style = MaterialTheme.typography.bodyMedium)
                                                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)))
                                            }
                                        }
                                    }
                                }
                                item { SectionHeader(title = "মাঠ পর্যায়ের কাজের রিপোর্ট") }
                                items(reports, key = { it.id }) { r ->
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(r.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                                if (r.verified) Icon(Icons.Rounded.Verified, contentDescription = "যাচাইকৃত", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            Text("${r.location} • ${r.reportDate} • ${formatNumber(r.beneficiaryCount, lang)} জন উপকৃত", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(r.summary, style = MaterialTheme.typography.bodyMedium)
                                            if (!r.verified) {
                                                TextButton(onClick = { onSaveReport(r.copy(verified = true)) }) { Text("যাচাইকৃত হিসেবে চিহ্নিত করুন") }
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                item {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("সদস্য ও কার্যকরী পরিষদ (${members.size})", style = MaterialTheme.typography.titleMedium)
                                        Button(onClick = { showAddMember = true }) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("নতুন সদস্য")
                                        }
                                    }
                                }
                                items(members, key = { it.id }) { m ->
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text("${m.fullName} (${m.memberCode})", style = MaterialTheme.typography.titleMedium)
                                                Text("${FoundationRoles.labelOf(m.role, lang)} • ${m.district ?: "ঢাকা"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                                if (!m.occupation.isNullOrBlank()) Text(m.occupation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (!m.bloodGroup.isNullOrBlank()) BloodBadge(m.bloodGroup)
                                                if (!m.phone.isNullOrBlank()) {
                                                    IconButton(onClick = { dialPhone(context, m.phone) }) {
                                                        Icon(Icons.Rounded.Call, contentDescription = "কল", tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                                IconButton(onClick = { onDeleteMember(m.id) }) {
                                                    Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> {
                                item {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("ইভেন্ট ও ক্যাম্প ক্যালেন্ডার", style = MaterialTheme.typography.titleMedium)
                                        Button(onClick = { showAddEvent = true }) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("ইভেন্ট যোগ")
                                        }
                                    }
                                }
                                items(events, key = { it.id }) { ev ->
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Rounded.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Text(ev.title, style = MaterialTheme.typography.titleMedium)
                                            }
                                            Text("${ev.eventDate} ${ev.eventTime ?: ""} • ${ev.location}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (!ev.description.isNullOrBlank()) Text(ev.description, style = MaterialTheme.typography.bodyMedium)
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("অংশগ্রহণকারী: ${formatNumber(ev.rsvpCount, lang)} জন", style = MaterialTheme.typography.labelLarge)
                                                OutlinedButton(onClick = { onRsvpEvent(ev) }) { Text("অংশ নেব (RSVP)") }
                                            }
                                        }
                                    }
                                }
                            }

                            3 -> {
                                item {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("নোটিশ বোর্ড ও মতামত জরিপ", style = MaterialTheme.typography.titleMedium)
                                        Button(onClick = { showAddNotice = true }) {
                                            Icon(Icons.Rounded.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("নতুন নোটিশ")
                                        }
                                    }
                                }
                                items(polls, key = { it.id }) { poll ->
                                    val totalVotes = poll.votes.sum().coerceAtLeast(1L)
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Rounded.HowToVote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Text(poll.question, style = MaterialTheme.typography.titleMedium)
                                            }
                                            poll.options.forEachIndexed { idx, opt ->
                                                val v = poll.votes.getOrElse(idx) { 0L }
                                                val pct = ((v.toDouble() / totalVotes) * 100).toInt()
                                                val chosen = poll.selectedOption == idx
                                                OutlinedButton(
                                                    onClick = { onCastVote(poll, idx) },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(if (chosen) "✓ $opt" else opt, fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal)
                                                        Text("${formatNumber(pct, lang)}%")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                items(notices, key = { it.id }) { notice ->
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text(notice.title, style = MaterialTheme.typography.titleMedium, color = if (notice.isUrgent) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                                TextButton(onClick = { onReactNotice(notice) }) {
                                                    Icon(Icons.Rounded.ThumbUp, contentDescription = "সমর্থন", modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(formatNumber(notice.reactions, lang))
                                                }
                                            }
                                            Text(notice.body, style = MaterialTheme.typography.bodyMedium)
                                            Text("প্রকাশক: ${notice.authorName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    if (showAddMember) {
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("MSF-${106 + members.size}") }
        var phone by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("member") }
        var blood by remember { mutableStateOf("O+") }
        AlertDialog(
            onDismissRequest = { showAddMember = false },
            title = { Text("নতুন সদস্য যোগ করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("পূর্ণ নাম *") }, singleLine = true)
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("সদস্য কোড") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("মোবাইল") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("member", "cashier", "health").forEach { r ->
                            FilterChip(selected = role == r, onClick = { role = r }, label = { Text(FoundationRoles.labelOf(r)) })
                        }
                    }
                    OutlinedTextField(value = blood, onValueChange = { blood = it }, label = { Text("রক্তের গ্রুপ") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        onSaveMember(Member(fullName = name.trim(), memberCode = code.trim(), phone = phone.trim().ifBlank { null }, role = role, bloodGroup = blood.trim()))
                        showAddMember = false
                    }
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { showAddMember = false }) { Text("বাতিল") } }
        )
    }

    if (showAddNotice) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var urgent by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAddNotice = false },
            title = { Text("নতুন নোটিশ প্রকাশ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("শিরোনাম *") }, singleLine = true)
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("বিস্তারিত *") }, minLines = 3)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = urgent, onCheckedChange = { urgent = it })
                        Text("জরুরি ঘোষণা")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        onSaveNotice(Notice(title = title.trim(), body = body.trim(), isUrgent = urgent))
                        showAddNotice = false
                    }
                }) { Text("প্রকাশ") }
            },
            dismissButton = { TextButton(onClick = { showAddNotice = false }) { Text("বাতিল") } }
        )
    }

    if (showAddProject) {
        var title by remember { mutableStateOf("") }
        var area by remember { mutableStateOf("") }
        var goal by remember { mutableStateOf("100000") }
        AlertDialog(
            onDismissRequest = { showAddProject = false },
            title = { Text("নতুন মানবিক প্রকল্প") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("প্রকল্পের নাম *") }, singleLine = true)
                    OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("এলাকা *") }, singleLine = true)
                    OutlinedTextField(value = goal, onValueChange = { goal = it }, label = { Text("বাজেট লক্ষ্য (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onSaveProject(Project(title = title.trim(), area = area.trim().ifBlank { "সারা দেশ" }, goalAmount = goal.toDoubleOrNull() ?: 50000.0))
                        showAddProject = false
                    }
                }) { Text("যোগ করুন") }
            },
            dismissButton = { TextButton(onClick = { showAddProject = false }) { Text("বাতিল") } }
        )
    }

    if (showAddReport) {
        var title by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var summary by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddReport = false },
            title = { Text("মাঠ পর্যায়ের প্রতিবেদন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("শিরোনাম *") }, singleLine = true)
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("স্থান *") }, singleLine = true)
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("বিবরণ *") }, minLines = 3)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && summary.isNotBlank()) {
                        onSaveReport(FieldReport(title = title.trim(), location = location.trim().ifBlank { "ঢাকা" }, summary = summary.trim(), reportDate = java.time.LocalDate.now().toString()))
                        showAddReport = false
                    }
                }) { Text("জমা দিন") }
            },
            dismissButton = { TextButton(onClick = { showAddReport = false }) { Text("বাতিল") } }
        )
    }

    if (showAddEvent) {
        var title by remember { mutableStateOf("") }
        var date by remember { mutableStateOf(java.time.LocalDate.now().plusDays(7).toString()) }
        var location by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddEvent = false },
            title = { Text("নতুন ইভেন্ট যোগ করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("ইভেন্টের নাম *") }, singleLine = true)
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("তারিখ (YYYY-MM-DD)") }, singleLine = true)
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("স্থান *") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && location.isNotBlank()) {
                        onSaveEvent(FoundationEvent(title = title.trim(), eventDate = date.trim(), location = location.trim()))
                        showAddEvent = false
                    }
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { showAddEvent = false }) { Text("বাতিল") } }
        )
    }
}
