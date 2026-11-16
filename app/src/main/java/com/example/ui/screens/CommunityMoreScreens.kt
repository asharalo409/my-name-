package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Poll
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.islamic.PrayerCalculator
import com.example.data.model.ChatMessage
import com.example.data.model.EventItem
import com.example.data.model.FoundationSettings
import com.example.data.model.Member
import com.example.data.model.NoticeItem
import com.example.data.model.PollItem
import com.example.data.model.ReliefLocation
import com.example.data.model.UiState
import com.example.data.model.roleLabelBn
import com.example.data.weather.WeatherRepository
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ErrorBanner
import com.example.ui.components.LoadingCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.shortDateBn
import com.example.ui.components.takaBn
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald600
import com.example.ui.theme.SkyBlue600
import com.example.ui.viewmodel.FoundationViewModel
import com.example.ui.viewmodel.IslamicWeatherViewModel
import java.time.LocalDate

enum class MoreSection(val label: String) {
    HUB("সকল মডিউল"),
    CALENDAR("ইভেন্ট ক্যালেন্ডার"),
    WEATHER("আবহাওয়া ও বায়ু"),
    RELIEF_MAP("ত্রাণ মানচিত্র"),
    MEMBERS("সদস্য ও আইডি কার্ড"),
    COMMUNITY("নোটিশ, জরিপ ও চ্যাট"),
    SETTINGS("থিম, অ্যাপ লক ও সেটিংস")
}

@Composable
fun MoreHubHostScreen(
    vm: FoundationViewModel,
    islamicVm: IslamicWeatherViewModel,
    settings: FoundationSettings,
    events: List<EventItem>,
    reliefs: List<ReliefLocation>,
    members: List<Member>,
    notices: List<NoticeItem>,
    polls: List<PollItem>,
    chatMessages: List<ChatMessage>,
    isDarkTheme: Boolean,
    userEmail: String,
    userName: String,
    onSignedOut: () -> Unit,
    onOpenIslamic: () -> Unit,
    onBackToHome: () -> Unit
) {
    var section by remember { mutableStateOf(MoreSection.HUB) }

    BackHandler {
        if (section != MoreSection.HUB) section = MoreSection.HUB else onBackToHome()
    }

    if (section != MoreSection.HUB) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { section = MoreSection.HUB }, modifier = Modifier.testTag("more_back_btn")) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "পেছনে")
                }
                Text(section.label, style = MaterialTheme.typography.titleLarge)
            }
            when (section) {
                MoreSection.CALENDAR -> EventCalendarSection(vm, events)
                MoreSection.WEATHER -> WeatherSection(islamicVm)
                MoreSection.RELIEF_MAP -> ReliefMapSection(vm, reliefs)
                MoreSection.MEMBERS -> MembersSection(vm, settings, members)
                MoreSection.COMMUNITY -> CommunitySection(vm, userName, notices, polls, chatMessages)
                MoreSection.SETTINGS -> SettingsProfileScreen(
                    vm = vm,
                    settings = settings,
                    isDarkTheme = isDarkTheme,
                    userEmail = userEmail,
                    userName = userName,
                    onSignedOut = onSignedOut
                )
                MoreSection.HUB -> Unit
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SectionHeader("সকল সেবা ও ব্যবস্থাপনা", "সংগঠন পরিচালনা, ইসলামিক ও কমিউনিটি টুলস")
            }
            item {
                MoreTile(Icons.Rounded.Mosque, "ইসলামিক জোন, নামাজ ও কিবলা", "৬৪ জেলার ওয়াক্ত, তাসবিহ, যাকাত, কুরআন ও হাদিস", MaterialTheme.colorScheme.primary) {
                    onOpenIslamic()
                }
            }
            item {
                MoreTile(Icons.Rounded.CalendarMonth, "ইভেন্ট ক্যালেন্ডার (${PrayerCalculator.bnDigits(events.size)})", "আসন্ন সভা, ত্রাণ ও রক্তদান ক্যাম্পেইন", SkyBlue600) {
                    section = MoreSection.CALENDAR
                }
            }
            item {
                MoreTile(Icons.Rounded.WbSunny, "লাইভ আবহাওয়া ও বায়ুর মান", "Open-Meteo ৭ দিনের পূর্বাভাস ও মাঠ কর্মী নিরাপত্তা", AmberGold) {
                    section = MoreSection.WEATHER
                }
            }
            item {
                MoreTile(Icons.Rounded.Map, "ত্রাণ মানচিত্র (${PrayerCalculator.bnDigits(reliefs.size)})", "সারাদেশে বিতরণকৃত ত্রাণ ক্যাম্প ও পরিবার পরিসংখ্যান", Emerald600) {
                    section = MoreSection.RELIEF_MAP
                }
            }
            item {
                MoreTile(Icons.Rounded.Groups, "সদস্য ও ডিজিটাল আইডি কার্ড", "নিবন্ধিত সদস্য, পদবি ও ডিজিটাল পরিচয়পত্র", MaterialTheme.colorScheme.primary) {
                    section = MoreSection.MEMBERS
                }
            }
            item {
                MoreTile(Icons.Rounded.Forum, "নোটিশ, জরিপ ও লাইভ চ্যাট", "সংগঠনের ঘোষণা, ভোটাভুটি ও দলীয় আলোচনা", Crimson600) {
                    section = MoreSection.COMMUNITY
                }
            }
            item {
                MoreTile(Icons.Rounded.Settings, "থিম স্টুডিও, অ্যাপ লক ও অ্যাকাউন্ট", "৭টি রঙের প্যালেট, পিন/ফিঙ্গারপ্রিন্ট লক ও প্রোফাইল", MaterialTheme.colorScheme.onSurfaceVariant) {
                    section = MoreSection.SETTINGS
                }
            }
        }
    }
}

@Composable
private fun MoreTile(icon: ImageVector, title: String, subtitle: String, tint: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = tint.copy(alpha = 0.14f), shape = RoundedCornerShape(14.dp), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, tint = tint)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EventCalendarSection(vm: FoundationViewModel, events: List<EventItem>) {
    var showAdd by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("নতুন ইভেন্ট যোগ করুন")
            }
        }
        if (events.isEmpty()) {
            item { EmptyStateCard("কোনো ইভেন্ট নেই", "নতুন সভা বা ক্যাম্পেইনের সময়সূচি যোগ করুন।") }
        } else {
            items(events, key = { it.id }) { ev ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatusPill(shortDateBn(ev.eventDate), MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(ev.type, style = MaterialTheme.typography.labelMedium)
                        }
                        Text(ev.title, style = MaterialTheme.typography.titleMedium)
                        Text("স্থান: ${ev.location}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(ev.description, style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = { vm.toggleEventRsvp(ev) }) {
                            Text(if (ev.rsvpGoing) "অংশগ্রহণ নিশ্চিত ✓" else "অংশগ্রহণ করব (RSVP)")
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var loc by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("নতুন ইভেন্ট") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("শিরোনাম") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("স্থান") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("বিবরণ") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        vm.addEvent(EventItem(title = title, type = "meeting", eventDate = LocalDate.now().plusDays(2).toString(), location = loc.ifBlank { "কেন্দ্রীয় কার্যালয়" }, description = desc))
                        showAdd = false
                    }
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("বাতিল") } }
        )
    }
}

@Composable
private fun WeatherSection(vm: IslamicWeatherViewModel) {
    val state by vm.weatherState.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = { vm.refreshWeather() }) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("রিফ্রেশ")
                }
            }
        }
        when (val s = state) {
            is UiState.Loading -> item { LoadingCard("Open-Meteo থেকে লাইভ আবহাওয়া আনা হচ্ছে...") }
            is UiState.Error -> item { ErrorBanner(s.message) }
            is UiState.Success -> {
                val wx = s.data
                val advice = WeatherRepository.fieldSafetyAdvice(wx)
                item {
                    Card(shape = RoundedCornerShape(22.dp)) {
                        Box(
                            modifier = Modifier.fillMaxWidth().background(
                                Brush.linearGradient(listOf(SkyBlue600, MaterialTheme.colorScheme.primary))
                            ).padding(20.dp)
                        ) {
                            Column {
                                Text(wx.districtName, style = MaterialTheme.typography.titleLarge, color = Color.White)
                                Text(
                                    "${PrayerCalculator.bnDigits(wx.temp.toInt())}°C",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White
                                )
                                Text(
                                    "${WeatherRepository.weatherDescBn(wx.code)} • অনুভূত ${PrayerCalculator.bnDigits(wx.feelsLike.toInt())}°C",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "আর্দ্রতা: ${PrayerCalculator.bnDigits(wx.humidity)}% • বাতাস: ${PrayerCalculator.bnDigits(wx.windKmh.toInt())} কিমি/ঘণ্টা" +
                                        if (wx.usAqi != null) " • AQI: ${PrayerCalculator.bnDigits(wx.usAqi)}" else "",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Air, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("স্বেচ্ছাসেবক মাঠ পরামর্শ: ${advice.first}", style = MaterialTheme.typography.titleMedium)
                                Text(advice.second, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                item { Text("৭ দিনের পূর্বাভাস", style = MaterialTheme.typography.titleLarge) }
                items(wx.daily) { d ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(shortDateBn(d.dateIso), style = MaterialTheme.typography.titleMedium)
                                Text("${WeatherRepository.weatherDescBn(d.code)} • বৃষ্টি ${PrayerCalculator.bnDigits(d.rainChance)}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "${PrayerCalculator.bnDigits(d.tempMin.toInt())}° / ${PrayerCalculator.bnDigits(d.tempMax.toInt())}°",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReliefMapSection(vm: FoundationViewModel, reliefs: List<ReliefLocation>) {
    var showAdd by remember { mutableStateOf(false) }
    val pinPrimary = MaterialTheme.colorScheme.primary
    val pinUrgent = Crimson600

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("বাংলাদেশ ত্রাণ কভারেজ মানচিত্র", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Canvas(modifier = Modifier.fillMaxWidth().height(190.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f), RoundedCornerShape(14.dp))) {
                        val minLat = 20.6
                        val maxLat = 26.7
                        val minLng = 88.0
                        val maxLng = 92.8
                        reliefs.forEach { loc ->
                            val x = (((loc.lng - minLng) / (maxLng - minLng)) * size.width).toFloat().coerceIn(20f, size.width - 20f)
                            val y = ((1.0 - (loc.lat - minLat) / (maxLat - minLat)) * size.height).toFloat().coerceIn(20f, size.height - 20f)
                            drawCircle(pinUrgent.copy(alpha = 0.22f), radius = 22.dp.toPx(), center = Offset(x, y))
                            drawCircle(pinPrimary, radius = 7.dp.toPx(), center = Offset(x, y))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "মোট পরিবার: ${PrayerCalculator.bnDigits(reliefs.sumOf { it.familiesHelped })} • মোট বিতরণ: ${takaBn(reliefs.sumOf { it.amountDistributed })}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        item {
            Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                Text("নতুন ত্রাণ ক্যাম্প যোগ করুন")
            }
        }
        items(reliefs, key = { it.id }) { r ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(r.title, style = MaterialTheme.typography.titleMedium)
                        Text("${r.district} (${r.division}) • দুর্যোগ: ${r.disasterType}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("সহায়তা: ${PrayerCalculator.bnDigits(r.familiesHelped)} পরিবার", style = MaterialTheme.typography.labelMedium)
                    }
                    Text(takaBn(r.amountDistributed), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = pinPrimary)
                }
            }
        }
    }

    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var district by remember { mutableStateOf("সিলেট") }
        var families by remember { mutableStateOf("50") }
        var amount by remember { mutableStateOf("25000") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("নতুন ত্রাণ এলাকা") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("ক্যাম্পের নাম") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("জেলা") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = families, onValueChange = { families = it }, label = { Text("পরিবার সংখ্যা") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("টাকার পরিমাণ") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        vm.addReliefLocation(
                            ReliefLocation(
                                title = title,
                                division = "ঢাকা বিভাগ",
                                district = district.ifBlank { "ঢাকা" },
                                disasterType = "ত্রাণ",
                                familiesHelped = families.toIntOrNull() ?: 50,
                                amountDistributed = amount.toDoubleOrNull() ?: 25000.0,
                                distributedDate = LocalDate.now().toString()
                            )
                        )
                        showAdd = false
                    }
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("বাতিল") } }
        )
    }
}

@Composable
private fun MembersSection(
    vm: FoundationViewModel,
    settings: FoundationSettings,
    members: List<Member>
) {
    val context = LocalContext.current
    var showAdd by remember { mutableStateOf(false) }
    var selectedCardMember by remember { mutableStateOf<Member?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("নতুন সদস্য নিবন্ধন")
            }
        }
        items(members, key = { it.id }) { m ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m.fullName, style = MaterialTheme.typography.titleMedium)
                        Text("${m.memberCode} • ${roleLabelBn(m.role)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Text("${m.district} • রক্ত: ${m.bloodGroup}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (!m.phone.isNullOrBlank()) {
                        IconButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${m.phone}"))) }) {
                            Icon(Icons.Rounded.Call, contentDescription = "কল")
                        }
                    }
                    IconButton(onClick = { selectedCardMember = m }) {
                        Icon(Icons.Rounded.Badge, contentDescription = "আইডি কার্ড")
                    }
                }
            }
        }
    }

    selectedCardMember?.let { m ->
        AlertDialog(
            onDismissRequest = { selectedCardMember = null },
            title = { Text("ডিজিটাল সদস্য আইডি কার্ড") },
            text = {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(settings.orgName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                        Text(m.fullName, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
                        Text("পদবি: ${roleLabelBn(m.role)}", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f))
                        Text("কোড: ${m.memberCode} • রক্তের গ্রুপ: ${m.bloodGroup}", color = MaterialTheme.colorScheme.onPrimary)
                        Text("জেলা: ${m.district} • প্রারম্ভিক জের: ${takaBn(m.openingBalance)}", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedCardMember = null }) { Text("বন্ধ করুন") } }
        )
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("MSF-${10 + members.size}") }
        var district by remember { mutableStateOf("ঢাকা") }
        var phone by remember { mutableStateOf("") }
        var blood by remember { mutableStateOf("O+") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("নতুন সদস্য যোগ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("নাম") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("সদস্য কোড") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("জেলা") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("ফোন") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = blood, onValueChange = { blood = it }, label = { Text("রক্তের গ্রুপ") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        vm.addMember(
                            Member(
                                fullName = name,
                                memberCode = code,
                                role = "member",
                                phone = phone.takeIf { it.isNotBlank() },
                                district = district.ifBlank { "ঢাকা" },
                                bloodGroup = blood.ifBlank { "O+" },
                                isActive = true,
                                joinedAt = LocalDate.now().toString()
                            )
                        )
                        showAdd = false
                    }
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("বাতিল") } }
        )
    }
}

@Composable
private fun CommunitySection(
    vm: FoundationViewModel,
    userName: String,
    notices: List<NoticeItem>,
    polls: List<PollItem>,
    messages: List<ChatMessage>
) {
    var chatInput by remember { mutableStateOf("") }
    var showNewNotice by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SectionHeader("নোটিশ ও ঘোষণা", trailing = {
                    TextButton(onClick = { showNewNotice = true }) { Text("+ নতুন") }
                })
            }
            items(notices, key = { it.id }) { n ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(n.title, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(n.body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (polls.isNotEmpty()) {
                item { SectionHeader("সংগঠনের জরিপ ও ভোট") }
                items(polls, key = { it.id }) { poll ->
                    val totalVotes = maxOf(1L, poll.counts.sum())
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Poll, contentDescription = null, tint = AmberGold)
                                Spacer(Modifier.width(8.dp))
                                Text(poll.question, style = MaterialTheme.typography.titleMedium)
                            }
                            poll.options.forEachIndexed { idx, opt ->
                                val cnt = poll.counts.getOrElse(idx) { 0L }
                                val pct = cnt.toFloat() / totalVotes.toFloat()
                                val selected = poll.selectedOption == idx
                                Column(modifier = Modifier.fillMaxWidth().clickable { vm.votePoll(poll, idx) }) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(if (selected) "✓ $opt" else opt, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                        Text("${PrayerCalculator.bnDigits(cnt)} ভোট", style = MaterialTheme.typography.labelMedium)
                                    }
                                    LinearProgressIndicator(progress = { pct }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)))
                                }
                            }
                        }
                    }
                }
            }

            item { SectionHeader("কমিউনিটি চ্যাট (${PrayerCalculator.bnDigits(messages.size)})") }
            items(messages, key = { it.id }) { msg ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${msg.senderName} • ${roleLabelBn(msg.senderRole)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(msg.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("চ্যাটে বার্তা লিখুন...") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        vm.sendChatMessage(userName.ifBlank { "সদস্য" }, "member", "সাধারণ", chatInput)
                        chatInput = ""
                    }
                },
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "পাঠান", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

    if (showNewNotice) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewNotice = false },
            title = { Text("নতুন নোটিশ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("শিরোনাম") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("বিস্তারিত") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        vm.addNotice(NoticeItem(kind = "notice", title = title, body = body))
                        showNewNotice = false
                    }
                }) { Text("প্রকাশ করুন") }
            },
            dismissButton = { TextButton(onClick = { showNewNotice = false }) { Text("বাতিল") } }
        )
    }
}
