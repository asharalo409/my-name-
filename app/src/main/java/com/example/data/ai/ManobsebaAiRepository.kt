package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class LedgerOcrEntry(
    val rowNumber: Int,
    val entryType: String,
    val personOrTitle: String,
    val amount: Double,
    val date: String,
    val fundName: String,
    val category: String,
    val method: String,
    val notes: String,
    val confidence: String,
    val uncertain: Boolean
)

data class LegacyMemberImportEntry(
    val fullName: String,
    val memberCode: String,
    val phone: String,
    val role: String,
    val openingBalance: Double,
    val notes: String,
    val uncertain: Boolean
)

data class AiActionDraft(
    val hasAction: Boolean,
    val actionType: String,
    val title: String,
    val summary: String,
    val personOrTitle: String,
    val amount: Double,
    val fundName: String,
    val category: String,
    val method: String
)

class ManobsebaAiRepository {

    private val chatModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val ledgerOcrModel by lazy {
        val entrySchema = Schema.obj(
            properties = mapOf(
                "rowNumber" to Schema.integer("Row index starting at 1"),
                "entryType" to Schema.enumeration(listOf("donation", "monthly_fee", "expense")),
                "personOrTitle" to Schema.string("Donor, member name, or expense title"),
                "amount" to Schema.double("Amount in BDT"),
                "date" to Schema.string("YYYY-MM-DD date string or empty"),
                "fundName" to Schema.string("Matching fund name"),
                "category" to Schema.string("Category"),
                "method" to Schema.string("Payment method like বিকাশ, নগদ, ব্যাংক, নগদ অর্থ"),
                "notes" to Schema.string("Additional remarks or raw text"),
                "confidence" to Schema.enumeration(listOf("high", "medium", "low")),
                "uncertain" to Schema.boolean("True if handwriting is blurry or ambiguous")
            ),
            optionalProperties = listOf("date", "notes")
        )
        val rootSchema = Schema.obj(
            properties = mapOf(
                "entries" to Schema.array(entrySchema, "Extracted ledger entries"),
                "pageSummary" to Schema.string("Short Bengali summary of the scanned ledger page")
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = rootSchema
            }
        )
    }

    private val legacyImportModel by lazy {
        val memberSchema = Schema.obj(
            properties = mapOf(
                "fullName" to Schema.string("Member full name"),
                "memberCode" to Schema.string("Member code like MSF-101 or generated"),
                "phone" to Schema.string("Phone number or empty"),
                "role" to Schema.enumeration(listOf("admin", "president", "general_secretary", "cashier", "health", "sports", "publicity", "member")),
                "openingBalance" to Schema.double("Historical paid balance in BDT"),
                "notes" to Schema.string("Notes or raw line"),
                "uncertain" to Schema.boolean("True if name/phone/amount unclear")
            ),
            optionalProperties = listOf("phone", "notes")
        )
        val rootSchema = Schema.obj(
            properties = mapOf(
                "members" to Schema.array(memberSchema, "Parsed legacy members")
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = rootSchema
            }
        )
    }

    private val actionDetectorModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "hasAction" to Schema.boolean("True only if user explicitly asks to record a donation, expense, monthly fee, or post a notice"),
                "actionType" to Schema.enumeration(listOf("none", "record_donation", "record_expense", "record_monthly_fee", "draft_notice")),
                "title" to Schema.string("Short action title in Bengali"),
                "summary" to Schema.string("One-line confirmation summary"),
                "personOrTitle" to Schema.string("Donor/member name or expense/notice title"),
                "amount" to Schema.double("Amount in BDT (0 if notice or none)"),
                "fundName" to Schema.string("Fund name or সাধারণ তহবিল"),
                "category" to Schema.string("Category"),
                "method" to Schema.string("Payment method like বিকাশ or নগদ অর্থ")
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    fun streamAssistantReply(
        orgContextSummary: String,
        historyPairs: List<Pair<String, String>>,
        userMessage: String,
        language: String
    ): Flow<String> = flow {
        val systemInstruction = if (language == "en") {
            "You are Manobseba AI, the official transparency & humanitarian operations assistant for Manobseba Foundation (Bangladesh). Be concise, accurate, warm, and strictly rely on the provided live foundation metrics when citing numbers. Context:\n$orgContextSummary"
        } else {
            "আপনি 'মানবসেবা AI' — মানবসেবা ফাউন্ডেশনের অফিসিয়াল স্বচ্ছতা, হিসাব ও সমাজকল্যাণ সহকারী। বাংলায় পরিষ্কার, বিনয়ী ও নির্ভুল উত্তর দিন। সংগঠনের প্রদত্ত লাইভ হিসাবের ভিত্তিতেই কেবল সংখ্যা বলুন। প্রেক্ষাপট:\n$orgContextSummary"
        }
        val fullPrompt = buildString {
            appendLine(systemInstruction)
            if (historyPairs.isNotEmpty()) {
                appendLine("\nসাম্প্রতিক কথোপকথন:")
                historyPairs.takeLast(6).forEach { (r, msg) ->
                    appendLine("$r: $msg")
                }
            }
            appendLine("\nব্যবহারকারীর প্রশ্ন: $userMessage")
        }

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                chatModel.generateContentStream(fullPrompt).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    if (text.isNotEmpty()) {
                        accumulated.append(text)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("AI মডেল কোনো উত্তর পাঠায়নি।")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI chat timed out", e)
            throw IllegalStateException("AI উত্তর দিতে ৬০ সেকেন্ডের বেশি সময় নিয়েছে। আবার চেষ্টা করুন।", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI chat failed", e)
            throw e
        }
    }

    suspend fun detectActionDraft(userMessage: String, fundsList: String): AiActionDraft? {
        return try {
            val response = withTimeout(30_000L) {
                actionDetectorModel.generateContent(
                    "Analyze whether the user is requesting to record a foundation transaction or post a notice. Available funds: $fundsList. User message: \"$userMessage\""
                )
            }
            val raw = response.text ?: return null
            val obj = JSONObject(raw)
            if (!obj.optBoolean("hasAction", false)) return null
            val actionType = obj.optString("actionType", "none")
            if (actionType == "none") return null
            AiActionDraft(
                hasAction = true,
                actionType = actionType,
                title = obj.optString("title"),
                summary = obj.optString("summary"),
                personOrTitle = obj.optString("personOrTitle"),
                amount = obj.optDouble("amount", 0.0),
                fundName = obj.optString("fundName", "সাধারণ তহবিল"),
                category = obj.optString("category", "সাধারণ অনুদান"),
                method = obj.optString("method", "বিকাশ")
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Action detection skipped", e)
            null
        }
    }

    fun streamFinancialAudit(auditContext: String): Flow<String> = flow {
        val prompt = """
            আপনি মানবসেবা ফাউন্ডেশনের সিনিয়র ফাইন্যান্স অডিটর। নিচের লাইভ আর্থিক তথ্যের ওপর ভিত্তি করে একটি স্পষ্ট, স্বচ্ছ ও actionable অডিট সারসংক্ষেপ তৈরি করুন:
            ১) তহবিলের বর্তমান স্বাস্থ্য ও ভারসাম্য
            ২) আয়-ব্যয়ের প্রবণতা ও লক্ষ্যমাত্রা অগ্রগতি
            ৩) সদস্যদের মাসিক চাঁদার অবস্থান
            ৪) কার্যকর ২টি সুপারিশ

            লাইভ তথ্য:
            $auditContext
        """.trimIndent()
        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                chatModel.generateContentStream(prompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) throw IllegalStateException("অডিট রিপোর্ট খালি এসেছে।")
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Audit timed out", e)
            throw IllegalStateException("অডিট বিশ্লেষণ সময়সীমা অতিক্রম করেছে।", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Audit failed", e)
            throw e
        }
    }

    suspend fun analyzeHandwrittenLedger(
        bitmap: Bitmap,
        availableFunds: List<String>,
        memberNames: List<String>
    ): Pair<String, List<LedgerOcrEntry>> {
        val scaled = downscaleForGemini(bitmap, maxDim = 1600)
        val prompt = """
            Read every handwritten or printed accounting line item in this Bangladeshi NGO cashbook / ledger photo carefully.
            Convert Bengali or English digits to standard numbers.
            Match fund names against: ${availableFunds.joinToString(", ")}.
            Known members: ${memberNames.take(30).joinToString(", ")}.
            Mark uncertain=true if any number or name is smeared or ambiguous.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                ledgerOcrModel.generateContent(
                    content {
                        image(scaled)
                        text(prompt)
                    }
                )
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("খাতা রিডার খালি উত্তর দিয়েছে।"))
            val summary = json.optString("pageSummary", "হাতে লেখা খাতা বিশ্লেষণ সম্পন্ন হয়েছে।")
            val arr = json.optJSONArray("entries")
            val list = mutableListOf<LedgerOcrEntry>()
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    list += LedgerOcrEntry(
                        rowNumber = item.optInt("rowNumber", i + 1),
                        entryType = item.optString("entryType", "donation"),
                        personOrTitle = item.optString("personOrTitle", "অজানা এন্ট্রি"),
                        amount = item.optDouble("amount", 0.0),
                        date = item.optString("date", ""),
                        fundName = item.optString("fundName", availableFunds.firstOrNull() ?: "সাধারণ তহবিল"),
                        category = item.optString("category", "সাধারণ অনুদান"),
                        method = item.optString("method", "নগদ অর্থ"),
                        notes = item.optString("notes", ""),
                        confidence = item.optString("confidence", "medium"),
                        uncertain = item.optBoolean("uncertain", false)
                    )
                }
            }
            summary to list
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Ledger OCR timed out", e)
            throw IllegalStateException("হাতে লেখা খাতা বিশ্লেষণে ৬০ সেকেন্ডের বেশি সময় লেগেছে।", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ledger OCR failed", e)
            throw e
        }
    }

    suspend fun parseLegacyMembers(rawText: String): List<LegacyMemberImportEntry> {
        val prompt = """
            Parse these legacy member records (Bengali or English CSV / WhatsApp / Excel rows) into structured member records.
            Raw text:
            $rawText
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                legacyImportModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("লিগ্যাসি পার্সার খালি উত্তর দিয়েছে।"))
            val arr = json.optJSONArray("members") ?: return emptyList()
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                LegacyMemberImportEntry(
                    fullName = obj.optString("fullName", "সদস্য ${i + 1}"),
                    memberCode = obj.optString("memberCode", "MSF-${200 + i}"),
                    phone = obj.optString("phone", ""),
                    role = obj.optString("role", "member"),
                    openingBalance = obj.optDouble("openingBalance", 0.0),
                    notes = obj.optString("notes", ""),
                    uncertain = obj.optBoolean("uncertain", false)
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Legacy parse timed out", e)
            throw IllegalStateException("তালিকা বিশ্লেষণ সময়সীমা অতিক্রম করেছে।", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Legacy parse failed", e)
            throw e
        }
    }

    private fun downscaleForGemini(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / maxOf(w, h).toFloat()
        return Bitmap.createScaledBitmap(bitmap, (w * ratio).toInt().coerceAtLeast(1), (h * ratio).toInt().coerceAtLeast(1), true)
    }

    private companion object {
        const val TAG = "ManobsebaAI"
    }
}
