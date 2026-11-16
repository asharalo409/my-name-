package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bloodtype
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.islamic.PrayerSchedule
import com.example.data.model.BloodRequest
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.MonthlyFee
import com.example.data.model.Notice
import com.example.data.model.OrgSettings
import com.example.data.model.Project
import com.example.ui.components.BloodBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatKpiCard
import com.example.ui.util.dialPhone
import com.example.ui.util.formatNumber
import com.example.ui.util.formatTaka
import com.example.ui.util.tr
import com.example.ui.viewmodel.WeatherUiState
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    settings: OrgSettings,
    donations: List<Donation>,
    expenses: List<Expense>,
    fees: List<MonthlyFee>,
    projects: List<Project>,
    bloodRequests: List<BloodRequest>,
    notices: List<Notice>,
    memberCount: Int,
    prayerSchedule: PrayerSchedule,
    weatherState: WeatherUiState,
    onOpenDonate: () -> Unit,
    onNavigateFinance: () -> Unit,
    onNavigateBlood: () -> Unit,
    onNavigateIslamic: () -> Unit,
    onNavigateAi: () -> Unit,
    onNavigateCommunity: () -> Unit
) {
    val context = LocalContext.current
    val lang = settings.language
    val totalIncome = donations.filter { it.status == "verified" }.sumOf { it.amount } +
        fees.filter { it.status == "paid" }.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
    val balance = (totalIncome - totalExpense).coerceAtLeast(0.0)
    val goal = settings.goalAmount.coerceAtLeast(1.0)
    val goalProgress = (totalIncome / goal).toFloat().coerceIn(0f, 1f)
    val urgentNotices = notices.filter { it.isUrgent }
    val activeBlood = bloodRequests.filter { !it.isManaged }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (urgentNotices.isNotEmpty()) {
                item {
                    val topUrgent = urgentNotices.first()
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onNavigateCommunity)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.Campaign, contentDescription = null, tint = Color(0xFFDC2626))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(topUrgent.title, style = MaterialTheme.typography.labelLarge, color = Color(0xFF991B1B))
                                Text(topUrgent.body, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF7F1D1D), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner),
                            contentDescription = settings.orgName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x33000000), Color(0xDD04351E))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = settings.orgName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = settings.slogan,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = onOpenDonate,
                                    modifier = Modifier.testTag("home_donate_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24), contentColor = Color(0xFF111827))
                                ) {
                                    Icon(Icons.Rounded.VolunteerActivism, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(tr("অনুদান দিন", "Donate", lang), fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = onNavigateBlood,
                                    modifier = Modifier.testTag("home_blood_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White)
                                ) {
                                    Icon(Icons.Rounded.Bloodtype, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(tr("রক্তের জরুরি", "Blood SOS", lang), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tr("বার্ষিক সংগ্রহ লক্ষ্যমাত্রা", "Annual Collection Goal", lang), style = MaterialTheme.typography.labelLarge)
                                Text(
                                    "${formatTaka(totalIncome, lang)} / ${formatTaka(goal, lang)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                "${formatNumber((goalProgress * 100).toInt(), lang)}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { goalProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatKpiCard(
                        label = tr("বর্তমান তহবিল", "Current Balance", lang),
                        value = formatTaka(balance, lang),
                        icon = Icons.Rounded.AccountBalanceWallet,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatKpiCard(
                        label = tr("মোট খরচ", "Total Spent", lang),
                        value = formatTaka(totalExpense, lang),
                        icon = Icons.Rounded.TrendingUp,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f)
                    )
                    StatKpiCard(
                        label = tr("সক্রিয় সদস্য", "Active Members", lang),
                        value = formatNumber(memberCount, lang),
                        icon = Icons.Rounded.Groups,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SectionHeader(title = tr("দ্রুত সেবা ও বিভাগসমূহ", "Quick Modules", lang))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickTile(Icons.Rounded.AutoAwesome, tr("মানবসেবা AI", "AI Hub", lang), Color(0xFF6D28D9), onNavigateAi, Modifier.weight(1f))
                    QuickTile(Icons.Rounded.Mosque, tr("ইসলামিক ও আবহাওয়া", "Islamic", lang), Color(0xFF0F766E), onNavigateIslamic, Modifier.weight(1f))
                    QuickTile(Icons.Rounded.HowToVote, tr("ভোট ও প্রকল্প", "Community", lang), Color(0xFF0284C7), onNavigateCommunity, Modifier.weight(1f))
                    QuickTile(Icons.Rounded.PhoneInTalk, tr("হটলাইন কল", "Hotline", lang), Color(0xFFDC2626), {
                        dialPhone(context, settings.hotline ?: "999")
                    }, Modifier.weight(1f))
                }
            }

            item {
                val fmt = DateTimeFormatter.ofPattern("hh:mm a")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateIslamic),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("${prayerSchedule.district.bnName} • ${prayerSchedule.hijriDisplayBn}", style = MaterialTheme.typography.labelLarge)
                            Text(
                                "ফজর ${prayerSchedule.fajr.format(fmt)} · যোহর ${prayerSchedule.dhuhr.format(fmt)} · মাগরিব ${prayerSchedule.maghrib.format(fmt)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Icon(Icons.Rounded.WbSunny, contentDescription = null, tint = Color(0xFFD97706))
                            when (weatherState) {
                                is WeatherUiState.Success -> {
                                    Text("${weatherState.snapshot.tempC.toInt()}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(weatherState.snapshot.conditionBn, style = MaterialTheme.typography.labelSmall)
                                }
                                else -> Text("লাইভ আবহাওয়া", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            if (activeBlood.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = tr("জরুরি রক্তের আবেদন", "Urgent Blood Requests", lang),
                        actionLabel = tr("সব দেখুন", "View All", lang),
                        onAction = onNavigateBlood
                    )
                }
                items(activeBlood.take(2), key = { it.id }) { req ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onNavigateBlood)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BloodBadge(req.bloodGroup, urgent = req.urgent)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req.hospital, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text("${req.patientProblem} • ${req.district ?: "ঢাকা"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { dialPhone(context, req.contact) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                            ) {
                                Text("কল")
                            }
                        }
                    }
                }
            }

            if (projects.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = tr("চলমান মানবিক প্রকল্প", "Active Projects", lang),
                        actionLabel = tr("প্রকল্পসমূহ", "All", lang),
                        onAction = onNavigateCommunity
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(projects, key = { it.id }) { proj ->
                            val bannerRes = if (proj.bannerKey == "edu") R.drawable.img_project_edu else R.drawable.img_project_flood
                            val progress = if (proj.goalAmount > 0) (proj.spentAmount / proj.goalAmount).toFloat().coerceIn(0f, 1f) else 0.5f
                            Card(
                                modifier = Modifier
                                    .width(260.dp)
                                    .clickable(onClick = onNavigateCommunity),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column {
                                    Image(
                                        painter = painterResource(id = bannerRes),
                                        contentDescription = proj.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(proj.title, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${proj.area} • ${formatNumber(proj.beneficiaries, lang)} উপকারভোগী", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = tr("সাম্প্রতিক লেনদেন", "Recent Ledger", lang),
                    actionLabel = tr("সম্পূর্ণ হিসাব", "Open Ledger", lang),
                    onAction = onNavigateFinance
                )
            }
            items(donations.take(3), key = { it.id }) { don ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(don.donorName, style = MaterialTheme.typography.labelLarge)
                            Text("${don.fundName} • ${don.method} • ${don.donatedOn}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("+${formatTaka(don.amount, lang)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun QuickTile(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
