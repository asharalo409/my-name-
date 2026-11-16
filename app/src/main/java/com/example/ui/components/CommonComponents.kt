package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Donation
import com.example.data.model.FinanceCategories
import com.example.data.model.Fund
import com.example.data.model.OrgSettings
import com.example.ui.util.formatNumber
import com.example.ui.util.formatTaka
import com.example.ui.util.tr
import java.time.LocalDate

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun StatKpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    tip: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                tip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BloodBadge(bloodGroup: String, urgent: Boolean = false) {
    val bg = if (urgent) Color(0xFFDC2626) else Color(0xFFFEE2E2)
    val fg = if (urgent) Color.White else Color(0xFFB91C1C)
    Surface(
        color = bg,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = bloodGroup,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun MonthlyFinanceCanvasChart(
    monthlyIncome: List<Double>,
    monthlyExpense: List<Double>,
    lang: String,
    modifier: Modifier = Modifier
) {
    val maxVal = (monthlyIncome + monthlyExpense).maxOrNull()?.coerceAtLeast(1000.0) ?: 1000.0
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.78f),
        label = "chartBars"
    )
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = Color(0xFFE11D48)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    val monthLabels = if (lang == "en") {
        listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
    } else {
        listOf("জা", "ফে", "মা", "এ", "মে", "জু", "জু", "আ", "সে", "অ", "ন", "ডি")
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                tr("মাসিক আয় ও ব্যয় বিশ্লেষণ", "Monthly Income vs Expense", lang),
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                LegendDot(incomeColor, tr("আয়", "Income", lang))
                LegendDot(expenseColor, tr("ব্যয়", "Expense", lang))
            }
        }
        Spacer(Modifier.height(12.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            val slotW = size.width / 12f
            val barW = (slotW * 0.30f).coerceAtLeast(5f)
            val usableH = size.height - 10f

            for (i in 0..3) {
                val y = usableH * (i / 3f)
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }

            for (m in 0 until 12) {
                val xCenter = slotW * m + slotW / 2f
                val incRatio = ((monthlyIncome.getOrElse(m) { 0.0 }) / maxVal).toFloat().coerceIn(0f, 1f)
                val expRatio = ((monthlyExpense.getOrElse(m) { 0.0 }) / maxVal).toFloat().coerceIn(0f, 1f)

                val incH = usableH * incRatio * animProgress
                val expH = usableH * expRatio * animProgress

                if (incH > 1f) {
                    drawRoundRect(
                        color = incomeColor,
                        topLeft = Offset(xCenter - barW - 1.5f, usableH - incH),
                        size = Size(barW, incH),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
                if (expH > 1f) {
                    drawRoundRect(
                        color = expenseColor,
                        topLeft = Offset(xCenter + 1.5f, usableH - expH),
                        size = Size(barW, expH),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            monthLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${tr("সর্বোচ্চ মাস", "Peak scale", lang)}: ${formatTaka(maxVal, lang)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateQuickBottomSheet(
    settings: OrgSettings,
    funds: List<Fund>,
    onDismiss: () -> Unit,
    onSubmitDonation: (Donation) -> Unit
) {
    var donorName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("500") }
    var selectedFund by remember(funds) { mutableStateOf(funds.firstOrNull()) }
    var method by remember { mutableStateOf(FinanceCategories.METHODS.first()) }
    var trxId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("অনুদান জমা দিন", style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "বন্ধ করুন")
                }
            }

            if (!settings.bankDetails.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = settings.bankDetails,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Text("তহবিল বাছাই করুন", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                funds.take(3).forEach { fund ->
                    FilterChip(
                        selected = selectedFund?.id == fund.id,
                        onClick = { selectedFund = fund },
                        label = { Text(fund.name, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            OutlinedTextField(
                value = donorName,
                onValueChange = { donorName = it },
                label = { Text("দাতার নাম *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("টাকার পরিমাণ (৳) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FinanceCategories.METHODS.take(4).forEach { m ->
                    FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                }
            }
            OutlinedTextField(
                value = trxId,
                onValueChange = { trxId = it },
                label = { Text("ট্রানজেকশন নম্বর / রেফারেন্স") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("মন্তব্য (ঐচ্ছিক)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (errorMsg != null) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (donorName.isBlank()) {
                        errorMsg = "অনুগ্রহ করে দাতার নাম লিখুন।"
                        return@Button
                    }
                    if (amt <= 0) {
                        errorMsg = "সঠিক টাকার পরিমাণ লিখুন।"
                        return@Button
                    }
                    onSubmitDonation(
                        Donation(
                            donorName = donorName.trim(),
                            fundId = selectedFund?.id ?: "",
                            fundName = selectedFund?.name ?: "সাধারণ তহবিল",
                            amount = amt,
                            category = "সাধারণ অনুদান",
                            method = method,
                            trxId = trxId.trim().ifBlank { null },
                            donatedOn = LocalDate.now().toString(),
                            notes = notes.trim().ifBlank { null },
                            status = "verified"
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_donation_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("অনুদান নিশ্চিত করুন", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
