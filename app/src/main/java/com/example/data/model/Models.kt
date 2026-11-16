package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class OrgSettings(
    @DocumentId val id: String = "org",
    val orgName: String = "মানবসেবা ফাউন্ডেশন",
    val slogan: String = "মানুষের পাশে, মানবতার পাশে",
    val hotline: String? = "+880 1700-000000",
    val bankDetails: String? = "বিকাশ (মার্চেন্ট): ০১৭০০-০০০০০০\nনগদ: ০১৭০০-০০০০০০\nসোনালী ব্যাংক: ০০১২৩৪৫৬৭৮৯ (প্রধান শাখা)",
    val defaultMonthlyFee: Double = 200.0,
    val goalAmount: Double = 500000.0,
    val themePalette: String = "green",
    val darkMode: Boolean = false,
    val language: String = "bn",
    val district: String = "ঢাকা",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("orgName", orgName.ifBlank { "মানবসেবা ফাউন্ডেশন" }.take(120))
        put("slogan", slogan.take(200))
        hotline?.takeIf { it.isNotBlank() }?.let { put("hotline", it.take(40)) }
        bankDetails?.takeIf { it.isNotBlank() }?.let { put("bankDetails", it.take(1000)) }
        put("defaultMonthlyFee", defaultMonthlyFee.coerceAtLeast(0.0))
        put("goalAmount", goalAmount.coerceAtLeast(0.0))
        put("themePalette", themePalette.ifBlank { "green" })
        put("darkMode", darkMode)
        put("language", if (language == "en") "en" else "bn")
        put("district", district.ifBlank { "ঢাকা" }.take(80))
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class Fund(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String? = null,
    val targetAmount: Double = 0.0,
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("name", name.trim().take(120))
        description?.takeIf { it.isNotBlank() }?.let { put("description", it.take(500)) }
        put("targetAmount", targetAmount.coerceAtLeast(0.0))
        put("isActive", isActive)
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class Member(
    @DocumentId val id: String = "",
    val fullName: String = "",
    val memberCode: String = "",
    val phone: String? = null,
    val email: String? = null,
    val bloodGroup: String? = null,
    val district: String? = null,
    val upazila: String? = null,
    val role: String = "member",
    val occupation: String? = null,
    val monthlyFeeOverride: Double? = null,
    val openingBalance: Double? = null,
    val isActive: Boolean = true,
    val isBloodDonor: Boolean = false,
    val lastDonatedDate: String? = null,
    val joinedAt: String? = null,
    val notes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("fullName", fullName.trim().take(120))
        put("memberCode", memberCode.ifBlank { "MSF-000" }.take(40))
        phone?.takeIf { it.isNotBlank() }?.let { put("phone", it.take(40)) }
        email?.takeIf { it.isNotBlank() }?.let { put("email", it.take(160)) }
        bloodGroup?.takeIf { it.isNotBlank() }?.let { put("bloodGroup", it.take(10)) }
        district?.takeIf { it.isNotBlank() }?.let { put("district", it.take(80)) }
        upazila?.takeIf { it.isNotBlank() }?.let { put("upazila", it.take(80)) }
        put("role", role.ifBlank { "member" }.take(60))
        occupation?.takeIf { it.isNotBlank() }?.let { put("occupation", it.take(120)) }
        monthlyFeeOverride?.takeIf { it >= 0 }?.let { put("monthlyFeeOverride", it) }
        openingBalance?.takeIf { it >= 0 }?.let { put("openingBalance", it) }
        put("isActive", isActive)
        put("isBloodDonor", isBloodDonor)
        lastDonatedDate?.takeIf { it.isNotBlank() }?.let { put("lastDonatedDate", it.take(40)) }
        joinedAt?.takeIf { it.isNotBlank() }?.let { put("joinedAt", it.take(40)) }
        notes?.takeIf { it.isNotBlank() }?.let { put("notes", it.take(1000)) }
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class Donation(
    @DocumentId val id: String = "",
    val donorName: String = "",
    val memberId: String? = null,
    val fundId: String = "",
    val fundName: String = "সাধারণ তহবিল",
    val amount: Double = 0.0,
    val category: String = "সাধারণ অনুদান",
    val method: String = "বিকাশ",
    val trxId: String? = null,
    val donatedOn: String = "",
    val notes: String? = null,
    val status: String = "verified",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("donorName", donorName.trim().take(120))
        memberId?.takeIf { it.isNotBlank() }?.let { put("memberId", it.take(128)) }
        put("fundId", fundId.take(128))
        put("fundName", fundName.ifBlank { "সাধারণ তহবিল" }.take(120))
        put("amount", amount.coerceAtLeast(1.0))
        put("category", category.ifBlank { "সাধারণ অনুদান" }.take(80))
        put("method", method.ifBlank { "বিকাশ" }.take(60))
        trxId?.takeIf { it.isNotBlank() }?.let { put("trxId", it.take(80)) }
        put("donatedOn", donatedOn.ifBlank { "2026-11-16" }.take(40))
        notes?.takeIf { it.isNotBlank() }?.let { put("notes", it.take(500)) }
        put("status", if (status in listOf("verified", "pending", "rejected")) status else "verified")
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class Expense(
    @DocumentId val id: String = "",
    val title: String = "",
    val fundId: String = "",
    val fundName: String = "সাধারণ তহবিল",
    val amount: Double = 0.0,
    val category: String = "ত্রাণ ও সহযোগিতা",
    val spentOn: String = "",
    val recipient: String? = null,
    val notes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("title", title.trim().take(160))
        put("fundId", fundId.take(128))
        put("fundName", fundName.ifBlank { "সাধারণ তহবিল" }.take(120))
        put("amount", amount.coerceAtLeast(1.0))
        put("category", category.ifBlank { "ত্রাণ ও সহযোগিতা" }.take(80))
        put("spentOn", spentOn.ifBlank { "2026-11-16" }.take(40))
        recipient?.takeIf { it.isNotBlank() }?.let { put("recipient", it.take(120)) }
        notes?.takeIf { it.isNotBlank() }?.let { put("notes", it.take(500)) }
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class MonthlyFee(
    @DocumentId val id: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val year: Int = 2026,
    val month: Int = 11,
    val amount: Double = 200.0,
    val paidDate: String? = null,
    val method: String? = null,
    val status: String = "paid",
    val notes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("memberId", memberId.take(128))
        put("memberName", memberName.trim().take(120))
        put("year", year.coerceIn(2000, 2100))
        put("month", month.coerceIn(1, 12))
        put("amount", amount.coerceAtLeast(0.0))
        paidDate?.takeIf { it.isNotBlank() }?.let { put("paidDate", it.take(40)) }
        method?.takeIf { it.isNotBlank() }?.let { put("method", it.take(60)) }
        put("status", if (status == "due") "due" else "paid")
        notes?.takeIf { it.isNotBlank() }?.let { put("notes", it.take(500)) }
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class Project(
    @DocumentId val id: String = "",
    val title: String = "",
    val area: String = "",
    val fundId: String? = null,
    val fundName: String? = null,
    val goalAmount: Double = 0.0,
    val spentAmount: Double = 0.0,
    val beneficiaries: Int = 0,
    val status: String = "active",
    val description: String? = null,
    val bannerKey: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("title", title.trim().take(160))
        put("area", area.trim().ifBlank { "সারা বাংলাদেশ" }.take(120))
        fundId?.takeIf { it.isNotBlank() }?.let { put("fundId", it.take(128)) }
        fundName?.takeIf { it.isNotBlank() }?.let { put("fundName", it.take(120)) }
        put("goalAmount", goalAmount.coerceAtLeast(0.0))
        put("spentAmount", spentAmount.coerceAtLeast(0.0))
        put("beneficiaries", beneficiaries.coerceAtLeast(0))
        put("status", if (status in listOf("active", "completed", "planned")) status else "active")
        description?.takeIf { it.isNotBlank() }?.let { put("description", it.take(2000)) }
        bannerKey?.takeIf { it.isNotBlank() }?.let { put("bannerKey", it.take(60)) }
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class FieldReport(
    @DocumentId val id: String = "",
    val title: String = "",
    val projectId: String? = null,
    val projectTitle: String? = null,
    val location: String = "",
    val summary: String = "",
    val beneficiaryCount: Int = 0,
    val amountSpent: Double = 0.0,
    val reportDate: String = "",
    val verified: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("title", title.trim().take(160))
        projectId?.takeIf { it.isNotBlank() }?.let { put("projectId", it.take(128)) }
        projectTitle?.takeIf { it.isNotBlank() }?.let { put("projectTitle", it.take(160)) }
        put("location", location.trim().ifBlank { "ঢাকা" }.take(120))
        put("summary", summary.trim().ifBlank { "মাঠ পর্যায়ের প্রতিবেদন" }.take(2000))
        put("beneficiaryCount", beneficiaryCount.coerceAtLeast(0))
        put("amountSpent", amountSpent.coerceAtLeast(0.0))
        put("reportDate", reportDate.ifBlank { "2026-11-16" }.take(40))
        put("verified", verified)
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class BloodDonor(
    @DocumentId val id: String = "",
    val name: String = "",
    val phone: String = "",
    val bloodGroup: String = "O+",
    val district: String = "ঢাকা",
    val upazila: String? = null,
    val lastDonatedDate: String? = null,
    val available: Boolean = true,
    val notes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("name", name.trim().take(120))
        put("phone", phone.trim().take(40))
        put("bloodGroup", bloodGroup)
        put("district", district.trim().ifBlank { "ঢাকা" }.take(80))
        upazila?.takeIf { it.isNotBlank() }?.let { put("upazila", it.take(80)) }
        lastDonatedDate?.takeIf { it.isNotBlank() }?.let { put("lastDonatedDate", it.take(40)) }
        put("available", available)
        notes?.takeIf { it.isNotBlank() }?.let { put("notes", it.take(500)) }
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class BloodRequest(
    @DocumentId val id: String = "",
    val bloodGroup: String = "O+",
    val hospital: String = "",
    val district: String? = null,
    val upazila: String? = null,
    val contact: String = "",
    val patientProblem: String = "",
    val applicantName: String = "",
    val neededBy: String? = null,
    val urgent: Boolean = true,
    val isManaged: Boolean = false,
    val notes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("bloodGroup", bloodGroup)
        put("hospital", hospital.trim().take(160))
        district?.takeIf { it.isNotBlank() }?.let { put("district", it.take(80)) }
        upazila?.takeIf { it.isNotBlank() }?.let { put("upazila", it.take(80)) }
        put("contact", contact.trim().take(40))
        put("patientProblem", patientProblem.trim().take(200))
        put("applicantName", applicantName.trim().take(120))
        neededBy?.takeIf { it.isNotBlank() }?.let { put("neededBy", it.take(60)) }
        put("urgent", urgent)
        put("isManaged", isManaged)
        notes?.takeIf { it.isNotBlank() }?.let { put("notes", it.take(500)) }
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class FoundationEvent(
    @DocumentId val id: String = "",
    val title: String = "",
    val eventDate: String = "",
    val eventTime: String? = null,
    val location: String = "",
    val category: String = "ত্রাণ ক্যাম্প",
    val description: String? = null,
    val rsvpCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("title", title.trim().take(160))
        put("eventDate", eventDate.ifBlank { "2026-11-25" }.take(40))
        eventTime?.takeIf { it.isNotBlank() }?.let { put("eventTime", it.take(40)) }
        put("location", location.trim().take(160))
        put("category", category.ifBlank { "কার্যক্রম" }.take(80))
        description?.takeIf { it.isNotBlank() }?.let { put("description", it.take(1000)) }
        put("rsvpCount", rsvpCount.coerceAtLeast(0))
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class Poll(
    @DocumentId val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val votes: List<Long> = emptyList(),
    val selectedOption: Int = -1,
    val isOpen: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        val cleanOpts = options.take(10).ifEmpty { listOf("হ্যাঁ", "না") }
        val cleanVotes = cleanOpts.indices.map { idx -> votes.getOrElse(idx) { 0L }.coerceAtLeast(0L) }
        put("question", question.trim().take(240))
        put("options", cleanOpts)
        put("votes", cleanVotes)
        put("selectedOption", selectedOption.coerceIn(-1, cleanOpts.lastIndex))
        put("isOpen", isOpen)
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class Notice(
    @DocumentId val id: String = "",
    val title: String = "",
    val body: String = "",
    val isUrgent: Boolean = false,
    val authorName: String = "কেন্দ্রীয় কমিটি",
    val reactions: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("title", title.trim().take(200))
        put("body", body.trim().take(2000))
        put("isUrgent", isUrgent)
        put("authorName", authorName.ifBlank { "কেন্দ্রীয় কমিটি" }.take(120))
        put("reactions", reactions.coerceAtLeast(0))
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val channel: String = "room",
    val senderName: String = "",
    val receiverName: String? = null,
    val body: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("channel", channel.ifBlank { "room" }.take(128))
        put("senderName", senderName.ifBlank { "সদস্য" }.take(120))
        receiverName?.takeIf { it.isNotBlank() }?.let { put("receiverName", it.take(120)) }
        put("body", body.trim().take(2000))
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class AiConversation(
    @DocumentId val id: String = "",
    val title: String = "নতুন কথোপকথন",
    val language: String = "bn",
    val scope: String? = "general",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("title", title.trim().ifBlank { "নতুন কথোপকথন" }.take(200))
        put("language", if (language == "en") "en" else "bn")
        scope?.takeIf { it.isNotBlank() }?.let { put("scope", it.take(80)) }
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class AiChatMessage(
    @DocumentId val id: String = "",
    val role: String = "user",
    val content: String = "",
    val actionStatus: String = "none",
    val actionSummary: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("role", if (role in listOf("user", "assistant", "system")) role else "user")
        put("content", content.trim().ifBlank { " " }.take(16000))
        put("actionStatus", if (actionStatus in listOf("none", "pending", "executed", "cancelled")) actionStatus else "none")
        actionSummary?.takeIf { it.isNotBlank() }?.let { put("actionSummary", it.take(1000)) }
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class IslamicJournalEntry(
    @DocumentId val id: String = "",
    val dateKey: String = "",
    val prayersCompleted: Int = 0,
    val quranMinutes: Int = 0,
    val zikrCount: Int = 0,
    val charityDone: Boolean = false,
    val amolItems: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toWriteMap(isCreate: Boolean): Map<String, Any> = buildMap {
        put("dateKey", dateKey.take(20))
        put("prayersCompleted", prayersCompleted.coerceIn(0, 5))
        put("quranMinutes", quranMinutes.coerceIn(0, 1440))
        put("zikrCount", zikrCount.coerceIn(0, 1000000))
        put("charityDone", charityDone)
        put("amolItems", amolItems.take(12))
        put("createdAt", if (isCreate || createdAt == null) FieldValue.serverTimestamp() else createdAt)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

data class RoleDefinition(
    val key: String,
    val bnLabel: String,
    val enLabel: String,
    val rank: Int,
    val permissions: Set<String>
)

object FoundationRoles {
    val ALL = listOf(
        RoleDefinition("admin", "অ্যাডমিন", "Admin", 1, setOf("*")),
        RoleDefinition("president", "সভাপতি", "President", 2, setOf("manage_members", "post_notices", "verify_reports")),
        RoleDefinition("general_secretary", "সাধারণ সম্পাদক", "General Secretary", 3, setOf("manage_members", "post_notices", "manage_blood")),
        RoleDefinition("cashier", "কোষাধ্যক্ষ", "Treasurer", 4, setOf("edit_finance", "verify_reports")),
        RoleDefinition("health", "স্বাস্থ্য ও রক্তদান সমন্বয়ক", "Health & Blood Coordinator", 5, setOf("manage_blood")),
        RoleDefinition("sports", "ক্রীড়া ও সাংস্কৃতিক সম্পাদক", "Sports & Cultural Sec.", 6, setOf("post_notices")),
        RoleDefinition("publicity", "প্রচার ও প্রকাশনা সম্পাদক", "Publicity Secretary", 7, setOf("post_notices")),
        RoleDefinition("member", "সদস্য", "Member", 100, emptySet()),
    )

    fun labelOf(key: String, lang: String = "bn"): String {
        val found = ALL.find { it.key == key }
        return if (found != null) {
            if (lang == "en") found.enLabel else found.bnLabel
        } else key
    }
}

object FinanceCategories {
    val INCOME = listOf(
        "মাসিক অনুদান",
        "সাধারণ অনুদান",
        "সদস্যদের মাসিক চাঁদা",
        "বিশেষ অনুদান",
        "প্রকল্পভিত্তিক অনুদান",
        "ব্যক্তিগত অনুদান",
        "অন্যান্য আয়"
    )
    val EXPENSE = listOf(
        "মানবসেবা কার্যক্রম",
        "চিকিৎসা ও স্বাস্থ্যসেবা",
        "শিক্ষা ও প্রশিক্ষণ",
        "ত্রাণ ও সহযোগিতা",
        "যাতায়াত",
        "আপ্যায়ন",
        "অফিস পরিচালনা",
        "প্রকল্পের ব্যয়",
        "অন্যান্য ব্যয়"
    )
    val METHODS = listOf("বিকাশ", "নগদ", "রকেট", "ব্যাংক", "নগদ অর্থ")
    val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
}
