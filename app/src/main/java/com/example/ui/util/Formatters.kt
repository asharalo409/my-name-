package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.islamic.PrayerCalculator
import java.io.File
import java.text.NumberFormat
import java.util.Locale

fun tr(bn: String, en: String, lang: String): String = if (lang == "en") en else bn

fun formatTaka(amount: Double, lang: String = "bn"): String {
    val rounded = kotlin.math.round(amount).toLong()
    return if (lang == "en") {
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(rounded)
        "৳$formatted"
    } else {
        val formatted = NumberFormat.getNumberInstance(Locale.forLanguageTag("bn-BD")).format(rounded)
        "৳$formatted"
    }
}

fun formatNumber(num: Number, lang: String = "bn"): String {
    return if (lang == "en") {
        NumberFormat.getNumberInstance(Locale.US).format(num)
    } else {
        PrayerCalculator.toBnDigits(num.toInt())
    }
}

fun dialPhone(context: Context, phone: String) {
    val clean = phone.filter { it.isDigit() || it == '+' }
    if (clean.isBlank()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun shareText(context: Context, title: String, body: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

fun shareCsvExport(context: Context, fileName: String, csvContent: String) {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val outFile = File(dir, fileName)
    outFile.writeText(csvContent, Charsets.UTF_8)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, fileName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
