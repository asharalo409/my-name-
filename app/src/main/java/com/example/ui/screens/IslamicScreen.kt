package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.islamic.BangladeshDistricts
import com.example.data.islamic.DistrictGeo
import com.example.data.islamic.IslamicLibrary
import com.example.data.islamic.PrayerCalculator
import com.example.data.islamic.PrayerSchedule
import com.example.data.model.IslamicJournalEntry
import com.example.ui.theme.NotoSerifBengaliFamily
import com.example.ui.util.formatTaka
import com.example.ui.viewmodel.QuranReaderState
import com.example.ui.viewmodel.WeatherUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IslamicWeatherScreen(
    selectedDistrict: DistrictGeo,
    prayerSchedule: PrayerSchedule,
    hanafiAsr: Boolean,
    weatherState: WeatherUiState,
    quranState: QuranReaderState,
    tasbihCount: Int,
    tasbihTarget: Int,
    journalEntries: List<IslamicJournalEntry>,
    onSelectDistrict: (DistrictGeo) -> Unit,
    onToggleAsrMethod: () -> Unit,
    onRefreshWeather: () -> Unit,
    onLoadSurah: (Int) -> Unit,
    onCloseSurah: () -> Unit,
    onIncrementTasbih: () -> Unit,
    onResetTasbih: () -> Unit,
    onSetTasbihTarget: (Int) -> Unit,
    onSaveJournal: (IslamicJournalEntry) -> Unit
) {
    val context = LocalContext.current
    var subTab by remember { mutableIntStateOf(0) }
    var showDistrictPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        val tabs = listOf("নামাজ ও কিবলা", "আবহাওয়া", "তাসবীহ ও আমল", "যাকাত", "কুরআন ও হাদিস")
        PrimaryScrollableTabRow(selectedTabIndex = subTab, edgePadding = 12.dp) {
            tabs.forEachIndexed { idx, label ->
                Tab(selected = subTab == idx, onClick = { subTab = idx }, text = { Text(label) })
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
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { showDistrictPicker = true }, modifier = Modifier.testTag("district_picker_button")) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("${selectedDistrict.bnName} (${selectedDistrict.division})")
                        }
                        Text(prayerSchedule.hijriDisplayBn, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }

                when (subTab) {
                    0 -> {
                        item {
                            val fmt = DateTimeFormatter.ofPattern("hh:mm a")
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("দৈনিক সালাতের সময়সূচি", style = MaterialTheme.typography.titleMedium)
                                        FilterChip(
                                            selected = hanafiAsr,
                                            onClick = onToggleAsrMethod,
                                            label = { Text(if (hanafiAsr) "হানাফী" else "শাফেয়ী") }
                                        )
                                    }
                                    val slots = listOf(
                                        "ফজর" to prayerSchedule.fajr.format(fmt),
                                        "সূর্যোদয়" to prayerSchedule.sunrise.format(fmt),
                                        "যোহর" to prayerSchedule.dhuhr.format(fmt),
                                        "আসর" to prayerSchedule.asr.format(fmt),
                                        "মাগরিব" to prayerSchedule.maghrib.format(fmt),
                                        "এশা" to prayerSchedule.isha.format(fmt)
                                    )
                                    slots.forEach { (name, time) ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(name, style = MaterialTheme.typography.labelLarge)
                                                Text(time, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            QiblaCompassCard(qiblaBearing = prayerSchedule.qiblaDegrees.toFloat())
                        }
                    }

                    1 -> {
                        item {
                            when (weatherState) {
                                is WeatherUiState.Loading -> {
                                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                                is WeatherUiState.Error -> {
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(weatherState.message, color = MaterialTheme.colorScheme.error)
                                            Button(onClick = onRefreshWeather) { Text("পুনরায় চেষ্টা") }
                                        }
                                    }
                                }
                                is WeatherUiState.Success -> {
                                    val w = weatherState.snapshot
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text("${w.tempC}°C", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
                                                    Text("${w.conditionBn} • অনুভূত ${w.feelsLikeC}°C", style = MaterialTheme.typography.bodyMedium)
                                                }
                                                IconButton(onClick = onRefreshWeather) {
                                                    Icon(Icons.Rounded.Refresh, contentDescription = "রিফ্রেশ")
                                                }
                                            }
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                WeatherMetricPill(Icons.Rounded.WaterDrop, "আর্দ্রতা", "${w.humidityPct}%", Modifier.weight(1f))
                                                WeatherMetricPill(Icons.Rounded.Air, "বাতাস", "${w.windKmh} কিমি", Modifier.weight(1f))
                                                WeatherMetricPill(Icons.Rounded.WbSunny, "AQI / UV", "${w.aqi} / ${w.uvIndex.roundToInt()}", Modifier.weight(1f))
                                            }
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(Modifier.padding(12.dp)) {
                                                    Text("মাঠকর্মী আবহাওয়া সূচক: ${PrayerCalculator.toBnDigits(w.fieldWorkerScore)}/১০০", style = MaterialTheme.typography.labelLarge)
                                                    Text(w.fieldWorkerAdviceBn, style = MaterialTheme.typography.bodyMedium)
                                                }
                                            }
                                            Text("৭ দিনের পূর্বাভাস", style = MaterialTheme.typography.titleMedium)
                                            w.daily.forEach { d ->
                                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(d.dateLabel, style = MaterialTheme.typography.labelLarge)
                                                    Text("${d.minTempC.roundToInt()}° – ${d.maxTempC.roundToInt()}°C", style = MaterialTheme.typography.bodyMedium)
                                                    Text("বৃষ্টি ${d.rainMm} মিমি", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("ডিজিটাল তাসবীহ কাউন্টার", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        PrayerCalculator.toBnDigits(tasbihCount),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text("লক্ষ্য: ${PrayerCalculator.toBnDigits(tasbihTarget)}", style = MaterialTheme.typography.labelSmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(33, 99, 100).forEach { t ->
                                            FilterChip(selected = tasbihTarget == t, onClick = { onSetTasbihTarget(t) }, label = { Text(PrayerCalculator.toBnDigits(t)) })
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            vibrateTick(context)
                                            onIncrementTasbih()
                                        },
                                        modifier = Modifier.size(130.dp).testTag("tasbih_tap_button"),
                                        shape = CircleShape
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Rounded.TouchApp, contentDescription = null, modifier = Modifier.size(32.dp))
                                            Text("জিকির", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    TextButton(onClick = onResetTasbih) { Text("রিসেট করুন") }
                                }
                            }
                        }

                        item {
                            val todayKey = LocalDate.now().toString()
                            val todayEntry = journalEntries.find { it.dateKey == todayKey } ?: IslamicJournalEntry(id = todayKey, dateKey = todayKey)
                            val checklistItems = listOf("ফজর জামাত/সময়মতো", "কুরআন তিলাওয়াত (১০+ মিনিট)", "সকাল-সন্ধ্যার মাসনুন দোয়া", "মা-বাবার খোঁজ / সদকা")
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("দৈনিক আমল চেকলিস্ট ($todayKey)", style = MaterialTheme.typography.titleMedium)
                                    checklistItems.forEach { label ->
                                        val checked = todayEntry.amolItems.contains(label)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val next = if (checked) todayEntry.amolItems - label else todayEntry.amolItems + label
                                                    onSaveJournal(todayEntry.copy(amolItems = next, prayersCompleted = next.size.coerceAtMost(5), zikrCount = tasbihCount))
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(checked = checked, onCheckedChange = null)
                                            Text(label, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        item { ZakatCalculatorCard() }
                    }

                    4 -> {
                        when (quranState) {
                            is QuranReaderState.Loaded -> {
                                val surahMeta = IslamicLibrary.FEATURED_SURAHS.find { it.number == quranState.surahNumber }
                                item {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = onCloseSurah) {
                                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "পেছনে")
                                        }
                                        Text("${surahMeta?.bnName ?: "সূরা ${quranState.surahNumber}"} (${surahMeta?.arabicName ?: ""})", style = MaterialTheme.typography.titleLarge)
                                    }
                                }
                                items(quranState.verses, key = { it.numberInSurah }) { verse ->
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(verse.arabic, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                                            Text("${PrayerCalculator.toBnDigits(verse.numberInSurah)}. ${verse.bengali}", style = MaterialTheme.typography.bodyLarge, fontFamily = NotoSerifBengaliFamily)
                                        }
                                    }
                                }
                            }
                            is QuranReaderState.Loading -> {
                                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                            }
                            else -> {
                                if (quranState is QuranReaderState.Error) {
                                    item { Text(quranState.message, color = MaterialTheme.colorScheme.error) }
                                }
                                item { Text("নির্বাচিত আল-কুরআন সূরা (বাংলা অর্থসহ)", style = MaterialTheme.typography.titleMedium) }
                                items(IslamicLibrary.FEATURED_SURAHS, key = { it.number }) { surah ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable { onLoadSurah(surah.number) },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Column {
                                                    Text("${PrayerCalculator.toBnDigits(surah.number)}. ${surah.bnName}", style = MaterialTheme.typography.labelLarge)
                                                    Text("${surah.revelationType} • ${PrayerCalculator.toBnDigits(surah.ayahCount)} আয়াত", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            Text(surah.arabicName, style = MaterialTheme.typography.titleMedium)
                                        }
                                    }
                                }
                                item { Spacer(Modifier.height(6.dp)); Text("নির্বাচিত সহীহ হাদিস", style = MaterialTheme.typography.titleMedium) }
                                items(IslamicLibrary.CURATED_HADITH) { h ->
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("${h.book} • ${h.reference}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                            Text("“${h.textBn}”", style = MaterialTheme.typography.bodyLarge, fontFamily = NotoSerifBengaliFamily)
                                            Text("বর্ণনায়: ${h.narrators}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    if (showDistrictPicker) {
        DistrictPickerDialog(
            current = selectedDistrict,
            onDismiss = { showDistrictPicker = false },
            onSelect = {
                onSelectDistrict(it)
                showDistrictPicker = false
            }
        )
    }
}

@Composable
private fun WeatherMetricPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QiblaCompassCard(qiblaBearing: Float) {
    val context = LocalContext.current
    var azimuthDeg by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context) {
        val mgr = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotSensor = mgr?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotMat = FloatArray(9)
                val orientation = FloatArray(3)
                SensorManager.getRotationMatrixFromVector(rotMat, event.values)
                SensorManager.getOrientation(rotMat, orientation)
                azimuthDeg = ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (rotSensor != null) mgr.registerListener(listener, rotSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { mgr?.unregisterListener(listener) }
    }

    val needleAngle = qiblaBearing - azimuthDeg
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.Mosque, contentDescription = null, tint = primaryColor)
                Text("কিবলা দিকনির্দেশক (${qiblaBearing.roundToInt()}°)", style = MaterialTheme.typography.titleMedium)
            }
            Canvas(modifier = Modifier.size(150.dp)) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = size.minDimension / 2f - 8f
                drawCircle(color = primaryColor.copy(alpha = 0.12f), radius = r, center = c)
                drawCircle(color = primaryColor, radius = r, center = c, style = Stroke(width = 4f))
                rotate(degrees = needleAngle, pivot = c) {
                    val path = Path().apply {
                        moveTo(c.x, c.y - r + 14f)
                        lineTo(c.x - 14f, c.y + 12f)
                        lineTo(c.x, c.y)
                        lineTo(c.x + 14f, c.y + 12f)
                        close()
                    }
                    drawPath(path, color = primaryColor)
                    drawLine(Color(0xFFD97706), c, Offset(c.x, c.y - r + 18f), strokeWidth = 5f, cap = StrokeCap.Round)
                }
            }
            Text("কাবার দিক বরাবর ফোনটি ঘুরান", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ZakatCalculatorCard() {
    var cash by remember { mutableStateOf("0") }
    var goldTola by remember { mutableStateOf("0") }
    var silverTola by remember { mutableStateOf("0") }
    var investments by remember { mutableStateOf("0") }
    var debt by remember { mutableStateOf("0") }

    val totalAssets = (cash.toDoubleOrNull() ?: 0.0) +
        (goldTola.toDoubleOrNull() ?: 0.0) * 135000.0 +
        (silverTola.toDoubleOrNull() ?: 0.0) * 2200.0 +
        (investments.toDoubleOrNull() ?: 0.0)
    val netWorth = (totalAssets - (debt.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)
    val nisab = 52.5 * 2200.0 // ~115,500 BDT (Silver Nisab)
    val eligible = netWorth >= nisab
    val zakatDue = if (eligible) netWorth * 0.025 else 0.0

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("সহজ যাকাত ক্যালকুলেটর (২.৫%)", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedTextField(value = cash, onValueChange = { cash = it }, label = { Text("নগদ ও ব্যাংক স্থিতি (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = goldTola, onValueChange = { goldTola = it }, label = { Text("স্বর্ণ (ভরি)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = silverTola, onValueChange = { silverTola = it }, label = { Text("রূপা (ভরি)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
            }
            OutlinedTextField(value = investments, onValueChange = { investments = it }, label = { Text("ব্যবসা / বিনিয়োগ পণ্য (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = debt, onValueChange = { debt = it }, label = { Text("তাৎক্ষণিক ঋণ ও দায় (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("নিট সম্পদ: ${formatTaka(netWorth)} (নিসাব ≈ ${formatTaka(nisab)})", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (eligible) "প্রদেয় যাকাত: ${formatTaka(zakatDue)}" else "নিসাব সীমা পূর্ণ হয়নি (যাকাত ফরজ নয়)",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DistrictPickerDialog(
    current: DistrictGeo,
    onDismiss: () -> Unit,
    onSelect: (DistrictGeo) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) BangladeshDistricts.ALL
        else BangladeshDistricts.ALL.filter { it.bnName.contains(query, true) || it.enName.contains(query, true) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("বাংলাদেশের ৬৪ জেলা বাছাই করুন") },
        text = {
            Column(Modifier.height(340.dp)) {
                OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("জেলার নাম খুঁজুন...") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                LazyColumn {
                    items(filtered, key = { it.enName }) { d ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (d.bnName == current.bnName) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { onSelect(d) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(d.bnName, fontWeight = FontWeight.Bold)
                            Text(d.division, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("বন্ধ") } }
    )
}

private fun vibrateTick(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            mgr?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vib?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    } catch (_: Exception) {
    }
}
