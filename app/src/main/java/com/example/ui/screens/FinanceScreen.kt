package com.example.ui.screens

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
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.FinanceCategories
import com.example.data.model.Fund
import com.example.data.model.Member
import com.example.data.model.MonthlyFee
import com.example.data.model.OrgSettings
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MonthlyFinanceCanvasChart
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatKpiCard
import com.example.ui.util.formatNumber
import com.example.ui.util.formatTaka
import com.example.ui.util.shareCsvExport
import com.example.ui.util.tr
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    settings: OrgSettings,
    funds: List<Fund>,
    members: List<Member>,
    donations: List<Donation>,
    expenses: List<Expense>,
    fees: List<MonthlyFee>,
    onSaveDonation: (Donation) -> Unit,
    onDeleteDonation: (String) -> Unit,
    onSaveExpense: (Expense) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onSaveMonthlyFee: (MonthlyFee) -> Unit,
    onOpenDonateSheet: () -> Unit
) {
    val context = LocalContext.current
    val lang = settings.language
    var tabIdx by remember { mutableIntStateOf(0) }
    var showExpenseDialog by remember { mutableStateOf(false) }

    val totalDonations = donations.filter { it.status == "verified" }.sumOf { it.amount }
    val totalFees = fees.filter { it.status == "paid" }.sumOf { it.amount }
    val totalIncome = totalDonations + totalFees
    val totalExpense = expenses.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    val curYear = LocalDate.now().year
    val curMonth = LocalDate.now().monthValue
    var selectedMonth by remember { mutableIntStateOf(curMonth) }

    val monthlyIncome = remember(donations, fees, curYear) {
        val buckets = DoubleArray(12)
        donations.filter { it.status == "verified" }.forEach { d ->
            val parts = d.donatedOn.split("-")
            val y = parts.getOrNull(0)?.toIntOrNull() ?: curYear
            val m = (parts.getOrNull(1)?.toIntOrNull() ?: curMonth) - 1
            if (y == curYear && m in 0..11) buckets[m] += d.amount
        }
        fees.filter { it.status == "paid" && it.year == curYear }.forEach { f ->
            val m = f.month - 1
            if (m in 0..11) buckets[m] += f.amount
        }
        buckets.toList()
    }

    val monthlyExpense = remember(expenses, curYear) {
        val buckets = DoubleArray(12)
        expenses.forEach { e ->
            val parts = e.spentOn.split("-")
            val y = parts.getOrNull(0)?.toIntOrNull() ?: curYear
            val m = (parts.getOrNull(1)?.toIntOrNull() ?: curMonth) - 1
            if (y == curYear && m in 0..11) buckets[m] += e.amount
        }
        buckets.toList()
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        val tabs = listOf(
            tr("সারসংক্ষেপ", "Overview", lang),
            tr("অনুদান", "Donations", lang),
            tr("খরচ", "Expenses", lang),
            tr("মাসিক চাঁদা", "Fees", lang)
        )
        PrimaryTabRow(selectedTabIndex = tabIdx) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = tabIdx == idx,
                    onClick = { tabIdx = idx },
                    text = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (tabIdx) {
                    0 -> {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatKpiCard(
                                    label = tr("মোট আয়", "Income", lang),
                                    value = formatTaka(totalIncome, lang),
                                    icon = Icons.Rounded.Savings,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                StatKpiCard(
                                    label = tr("মোট ব্যয়", "Expense", lang),
                                    value = formatTaka(totalExpense, lang),
                                    icon = Icons.Rounded.MoneyOff,
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier.weight(1f)
                                )
                                StatKpiCard(
                                    label = tr("নিট স্থিতি", "Balance", lang),
                                    value = formatTaka(netBalance, lang),
                                    icon = Icons.Rounded.AccountBalance,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                MonthlyFinanceCanvasChart(
                                    monthlyIncome = monthlyIncome,
                                    monthlyExpense = monthlyExpense,
                                    lang = lang,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        item {
                            SectionHeader(
                                title = tr("তহবিলভিত্তিক স্থিতি", "Funds Breakdown", lang),
                                actionLabel = tr("CSV রপ্তানি", "Export CSV", lang),
                                onAction = {
                                    val csv = buildString {
                                        appendLine("Type,TitleOrDonor,Fund,Amount,Date,Method")
                                        donations.forEach { d ->
                                            appendLine("Income,\"${d.donorName}\",\"${d.fundName}\",${d.amount},${d.donatedOn},${d.method}")
                                        }
                                        expenses.forEach { e ->
                                            appendLine("Expense,\"${e.title}\",\"${e.fundName}\",${e.amount},${e.spentOn},${e.category}")
                                        }
                                    }
                                    shareCsvExport(context, "manobseba_ledger_${LocalDate.now()}.csv", csv)
                                }
                            )
                        }

                        items(funds, key = { it.id }) { fund ->
                            val fIn = donations.filter { it.fundId == fund.id && it.status == "verified" }.sumOf { it.amount }
                            val fOut = expenses.filter { it.fundId == fund.id }.sumOf { it.amount }
                            val prog = if (fund.targetAmount > 0) (fIn / fund.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(fund.name, style = MaterialTheme.typography.titleMedium)
                                        Text(formatTaka((fIn - fOut).coerceAtLeast(0.0), lang), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(
                                        "${tr("সংগ্রহ", "In", lang)}: ${formatTaka(fIn, lang)} • ${tr("ব্যয়", "Out", lang)}: ${formatTaka(fOut, lang)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    LinearProgressIndicator(progress = { prog }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)))
                                }
                            }
                        }
                    }

                    1 -> {
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${tr("মোট অনুদান", "Total Donations", lang)}: ${formatTaka(totalDonations, lang)}", style = MaterialTheme.typography.titleMedium)
                                Button(onClick = onOpenDonateSheet, modifier = Modifier.testTag("add_donation_button")) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(tr("নতুন অনুদান", "Add", lang))
                                }
                            }
                        }
                        if (donations.isEmpty()) {
                            item {
                                EmptyStateCard(Icons.Rounded.Savings, "কোনো অনুদান পাওয়া যায়নি", "উপরের বোতামে চাপ দিয়ে প্রথম অনুদান যোগ করুন।")
                            }
                        } else {
                            items(donations, key = { it.id }) { don ->
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(don.donorName, style = MaterialTheme.typography.titleMedium)
                                            Text("${don.fundName} • ${don.method} • ${don.donatedOn}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (!don.trxId.isNullOrBlank()) {
                                                Text("TrxID: ${don.trxId}", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("+${formatTaka(don.amount, lang)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                            IconButton(onClick = { onDeleteDonation(don.id) }) {
                                                Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${tr("মোট খরচ", "Total Expense", lang)}: ${formatTaka(totalExpense, lang)}", style = MaterialTheme.typography.titleMedium)
                                Button(onClick = { showExpenseDialog = true }, modifier = Modifier.testTag("add_expense_button")) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(tr("খরচ যোগ করুন", "Add Expense", lang))
                                }
                            }
                        }
                        if (expenses.isEmpty()) {
                            item {
                                EmptyStateCard(Icons.Rounded.MoneyOff, "কোনো খরচ নথিভুক্ত নেই", "নতুন খরচ যোগ করতে উপরের বাটনে ক্লিক করুন।")
                            }
                        } else {
                            items(expenses, key = { it.id }) { exp ->
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(exp.title, style = MaterialTheme.typography.titleMedium)
                                            Text("${exp.fundName} • ${exp.category} • ${exp.spentOn}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (!exp.recipient.isNullOrBlank()) {
                                                Text("গ্রহীতা: ${exp.recipient}", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("-${formatTaka(exp.amount, lang)}", style = MaterialTheme.typography.titleMedium, color = Color(0xFFE11D48))
                                            IconButton(onClick = { onDeleteExpense(exp.id) }) {
                                                Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${tr("মাসিক চাঁদা ম্যাট্রিক্স", "Monthly Fee Matrix", lang)} (${formatNumber(curYear, lang)})", style = MaterialTheme.typography.titleMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(9, 10, 11, 12).forEach { m ->
                                        FilterChip(
                                            selected = selectedMonth == m,
                                            onClick = { selectedMonth = m },
                                            label = { Text("${formatNumber(m, lang)} নং মাস") }
                                        )
                                    }
                                }
                            }
                        }
                        items(members, key = { it.id }) { member ->
                            val existingFee = fees.find { it.memberId == member.id && it.year == curYear && it.month == selectedMonth }
                            val isPaid = existingFee?.status == "paid"
                            val feeAmt = member.monthlyFeeOverride ?: settings.defaultMonthlyFee
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${member.fullName} (${member.memberCode})", style = MaterialTheme.typography.labelLarge)
                                        Text("${tr("নির্ধারিত চাঁদা", "Fee", lang)}: ${formatTaka(feeAmt, lang)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (isPaid) {
                                        FilledTonalButton(
                                            onClick = {
                                                existingFee?.let {
                                                    onSaveMonthlyFee(it.copy(status = "due"))
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(tr("পরিশোধিত", "Paid", lang))
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                val baseFee = existingFee ?: MonthlyFee(
                                                    id = "fee_${member.id}_${curYear}_$selectedMonth",
                                                    memberId = member.id,
                                                    memberName = member.fullName,
                                                    year = curYear,
                                                    month = selectedMonth,
                                                    amount = feeAmt,
                                                    paidDate = LocalDate.now().toString(),
                                                    method = "নগদ অর্থ",
                                                    status = "paid"
                                                )
                                                onSaveMonthlyFee(baseFee.copy(status = "paid", paidDate = LocalDate.now().toString()))
                                            }
                                        ) {
                                            Text(tr("জমা নিন", "Mark Paid", lang))
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

    if (showExpenseDialog) {
        AddExpenseDialog(
            funds = funds,
            onDismiss = { showExpenseDialog = false },
            onSave = {
                onSaveExpense(it)
                showExpenseDialog = false
            }
        )
    }
}

@Composable
private fun AddExpenseDialog(
    funds: List<Fund>,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var selectedFund by remember { mutableStateOf(funds.firstOrNull()) }
    var selectedCategory by remember { mutableStateOf(FinanceCategories.EXPENSE.first()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন খরচ নথিভুক্ত করুন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("খরচের বিবরণ *") }, singleLine = true)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("টাকার পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(value = recipient, onValueChange = { recipient = it }, label = { Text("গ্রহীতা / স্থান") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    funds.take(2).forEach { f ->
                        FilterChip(selected = selectedFund?.id == f.id, onClick = { selectedFund = f }, label = { Text(f.name, maxLines = 1) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FinanceCategories.EXPENSE.take(2).forEach { c ->
                        FilterChip(selected = selectedCategory == c, onClick = { selectedCategory = c }, label = { Text(c, maxLines = 1) })
                    }
                }
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (title.isBlank() || amt <= 0) {
                    error = "বিবরণ ও সঠিক পরিমাণ দিন।"
                    return@Button
                }
                onSave(
                    Expense(
                        title = title.trim(),
                        fundId = selectedFund?.id ?: "",
                        fundName = selectedFund?.name ?: "সাধারণ তহবিল",
                        amount = amt,
                        category = selectedCategory,
                        spentOn = LocalDate.now().toString(),
                        recipient = recipient.trim().ifBlank { null }
                    )
                )
            }) {
                Text("সংরক্ষণ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
