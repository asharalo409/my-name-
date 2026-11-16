package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.islamic.PrayerCalculator
import com.example.data.model.BloodDonor
import com.example.data.model.BloodRequest
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Fund
import com.example.data.model.Member
import com.example.data.model.ReliefLocation
import com.example.ui.components.ChartSlice
import com.example.ui.components.FundDonutChart
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.components.MonthlyBarPair
import com.example.ui.components.SectionHeader
import com.example.ui.components.takaBn
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald600
import com.example.ui.theme.SkyBlue600
import java.time.LocalDate

@Composable
fun OverviewScreen(
    funds: List<Fund>,
    donations: List<Donation>,
    expenses: List<Expense>,
    members: List<Member>,
    bloodRequests: List<BloodRequest>,
    bloodDonors: List<BloodDonor>,
    reliefLocations: List<ReliefLocation>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var preset by remember { mutableStateOf("all") }

    val cutoff = remember(preset) {
        val today = LocalDate.now()
        when (preset) {
            "month" -> today.withDayOfMonth(1).toString()
            "3m" -> today.minusMonths(3).toString()
            "year" -> today.withDayOfYear(1).toString()
            else -> ""
        }
    }

    val filteredDonations = remember(donations, cutoff) {
        if (cutoff.isEmpty()) donations else donations.filter { it.donatedOn >= cutoff }
    }
    val filteredExpenses = remember(expenses, cutoff) {
        if (cutoff.isEmpty()) expenses else expenses.filter { it.spentOn >= cutoff }
    }

    val totalIn = filteredDonations.sumOf { it.amount }
    val totalEx = filteredExpenses.sumOf { it.amount }
    val net = totalIn - totalEx
    val efficiencyPct = if (totalIn > 0) ((totalEx / totalIn) * 100).toInt() else 0

    val sliceColors = listOf(MaterialTheme.colorScheme.primary, AmberGold, SkyBlue600, Crimson600, Emerald600)
    val slices = funds.mapIndexed { i, f ->
        ChartSlice(f.name, filteredDonations.filter { it.fundId == f.id }.sumOf { it.amount }, sliceColors[i % sliceColors.size])
    }

    val barPairs = remember(filteredDonations, filteredExpenses) {
        val today = LocalDate.now()
        (3 downTo 0).map { offset ->
            val ym = today.minusMonths(offset.toLong())
            val key = "${ym.year}-${ym.monthValue.toString().padStart(2, '0')}"
            MonthlyBarPair(
                label = PrayerCalculator.bnDigits("${ym.monthValue}/${ym.year % 100}"),
                income = filteredDonations.filter { it.donatedOn.startsWith(key) }.sumOf { it.amount },
                expense = filteredExpenses.filter { it.spentOn.startsWith(key) }.sumOf { it.amount }
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader("সংগঠন ওভারভিউ ও বিশ্লেষণ", "আর্থিক স্বচ্ছতা, তহবিল প্রবাহ ও সেবা সূচক")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("all" to "সব সময়", "month" to "চলতি মাস", "3m" to "গত ৩ মাস", "year" to "এই বছর").forEach { (k, label) ->
                    FilterChip(selected = preset == k, onClick = { preset = k }, label = { Text(label) })
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OverviewKpi("মোট সংগ্রহ", takaBn(totalIn))
                OverviewKpi("মোট বিতরণ", takaBn(totalEx))
                OverviewKpi("হাতে স্থিতি", takaBn(net))
                OverviewKpi("উপকারভোগী পরিবার", PrayerCalculator.bnDigits(reliefLocations.sumOf { it.familiesHelped }))
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("মাসিক আয় বনাম ব্যয় তুলনা", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))
                    IncomeExpenseBarChart(barPairs)
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("তহবিল ভিত্তিক সংগ্রহ অনুপাত", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    FundDonutChart(slices, "মোট", takaBn(totalIn))
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("মূল কার্যকারিতা ও স্বচ্ছতা সূচক", style = MaterialTheme.typography.titleLarge)
                    MetricRow("তহবিল ব্যবহার হার", "${PrayerCalculator.bnDigits(efficiencyPct)}%")
                    HorizontalDivider()
                    MetricRow("মোট অনুদান রসিদ", "${PrayerCalculator.bnDigits(filteredDonations.size)} টি")
                    HorizontalDivider()
                    MetricRow("নিবন্ধিত রক্তদাতা", "${PrayerCalculator.bnDigits(bloodDonors.size)} জন")
                    HorizontalDivider()
                    MetricRow("অমীমাংসিত রক্তের আবেদন", "${PrayerCalculator.bnDigits(bloodRequests.count { !it.isManaged })} টি")
                    HorizontalDivider()
                    MetricRow("মোট সদস্য", "${PrayerCalculator.bnDigits(members.size)} জন (সক্রিয় ${PrayerCalculator.bnDigits(members.count { it.isActive })})")
                }
            }
        }
    }
}

@Composable
private fun OverviewKpi(title: String, value: String) {
    Card(
        modifier = Modifier.width(155.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
