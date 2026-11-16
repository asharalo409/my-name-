package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiActionDraft
import com.example.data.ai.LedgerOcrEntry
import com.example.data.ai.LegacyMemberImportEntry
import com.example.data.ai.ManobsebaAiRepository
import com.example.data.model.AiChatMessage
import com.example.data.model.AiConversation
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Fund
import com.example.data.model.Member
import com.example.data.model.MonthlyFee
import com.example.data.model.Notice
import com.example.data.repository.FoundationRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface AiStreamState {
    data object Idle : AiStreamState
    data object Loading : AiStreamState
    data class Streaming(val text: String) : AiStreamState
    data class Success(val text: String) : AiStreamState
    data class Error(val message: String) : AiStreamState
}

class AiViewModel(
    private val foundationRepo: FoundationRepository,
    private val aiRepo: ManobsebaAiRepository,
    private val currentUserId: String
) : ViewModel() {

    val conversations: StateFlow<List<AiConversation>> =
        foundationRepo.observeAiConversations(currentUserId)
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing AI conversations", fsErr ?: error)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<AiChatMessage>> =
        _activeConversationId
            .flatMapLatest { convId ->
                if (convId.isNullOrBlank()) flowOf(emptyList())
                else foundationRepo.observeAiMessages(currentUserId, convId)
            }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing AI messages", fsErr ?: error)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _chatStreamState = MutableStateFlow<AiStreamState>(AiStreamState.Idle)
    val chatStreamState: StateFlow<AiStreamState> = _chatStreamState.asStateFlow()

    private val _pendingAction = MutableStateFlow<AiActionDraft?>(null)
    val pendingAction: StateFlow<AiActionDraft?> = _pendingAction.asStateFlow()

    private val _auditState = MutableStateFlow<AiStreamState>(AiStreamState.Idle)
    val auditState: StateFlow<AiStreamState> = _auditState.asStateFlow()

    private val _ocrBusy = MutableStateFlow(false)
    val ocrBusy: StateFlow<Boolean> = _ocrBusy.asStateFlow()

    private val _ocrSummary = MutableStateFlow("")
    val ocrSummary: StateFlow<String> = _ocrSummary.asStateFlow()

    private val _ocrEntries = MutableStateFlow<List<LedgerOcrEntry>>(emptyList())
    val ocrEntries: StateFlow<List<LedgerOcrEntry>> = _ocrEntries.asStateFlow()

    private val _ocrError = MutableStateFlow<String?>(null)
    val ocrError: StateFlow<String?> = _ocrError.asStateFlow()

    private val _legacyBusy = MutableStateFlow(false)
    val legacyBusy: StateFlow<Boolean> = _legacyBusy.asStateFlow()

    private val _legacyEntries = MutableStateFlow<List<LegacyMemberImportEntry>>(emptyList())
    val legacyEntries: StateFlow<List<LegacyMemberImportEntry>> = _legacyEntries.asStateFlow()

    private val _legacyError = MutableStateFlow<String?>(null)
    val legacyError: StateFlow<String?> = _legacyError.asStateFlow()

    fun selectConversation(convId: String?) {
        _activeConversationId.value = convId
        _chatStreamState.value = AiStreamState.Idle
    }

    fun startNewConversation() {
        _activeConversationId.value = null
        _pendingAction.value = null
        _chatStreamState.value = AiStreamState.Idle
    }

    fun sendChatPrompt(
        userMessage: String,
        orgContextSummary: String,
        fundsList: List<Fund>,
        language: String
    ) {
        val trimmed = userMessage.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _chatStreamState.value = AiStreamState.Loading
            _pendingAction.value = null

            val existingConvId = _activeConversationId.value
            val convId = if (existingConvId.isNullOrBlank()) {
                val created = foundationRepo.saveAiConversation(
                    AiConversation(
                        title = trimmed.take(48),
                        language = language,
                        scope = "general"
                    )
                )
                created.getOrNull()?.also { _activeConversationId.value = it } ?: return@launch
            } else existingConvId

            val history = activeMessages.value.map { it.role to it.content }
            foundationRepo.saveAiChatMessage(
                convId,
                AiChatMessage(role = "user", content = trimmed)
            )

            try {
                val draft = aiRepo.detectActionDraft(trimmed, fundsList.joinToString { it.name })
                if (draft != null) _pendingAction.value = draft

                var finalReply = ""
                aiRepo.streamAssistantReply(orgContextSummary, history, trimmed, language).collect { partial ->
                    finalReply = partial
                    _chatStreamState.value = AiStreamState.Streaming(partial)
                }

                _chatStreamState.value = AiStreamState.Success(finalReply)
                foundationRepo.saveAiChatMessage(
                    convId,
                    AiChatMessage(
                        role = "assistant",
                        content = finalReply,
                        actionStatus = if (draft != null) "pending" else "none",
                        actionSummary = draft?.summary
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chat error", e)
                _chatStreamState.value = AiStreamState.Error(e.localizedMessage ?: "AI উত্তর তৈরি করতে পারেনি।")
            }
        }
    }

    fun executeActionDraft(draft: AiActionDraft, funds: List<Fund>, members: List<Member>) {
        viewModelScope.launch {
            val matchedFund = funds.find { it.name.contains(draft.fundName, ignoreCase = true) } ?: funds.firstOrNull()
            val today = LocalDate.now().toString()
            when (draft.actionType) {
                "record_donation" -> {
                    foundationRepo.saveDonation(
                        Donation(
                            donorName = draft.personOrTitle.ifBlank { "শুভাকাঙ্ক্ষী" },
                            fundId = matchedFund?.id ?: "",
                            fundName = matchedFund?.name ?: draft.fundName,
                            amount = draft.amount.coerceAtLeast(1.0),
                            category = draft.category.ifBlank { "সাধারণ অনুদান" },
                            method = draft.method.ifBlank { "বিকাশ" },
                            donatedOn = today
                        )
                    )
                }
                "record_expense" -> {
                    foundationRepo.saveExpense(
                        Expense(
                            title = draft.personOrTitle.ifBlank { "মানবসেবা খরচ" },
                            fundId = matchedFund?.id ?: "",
                            fundName = matchedFund?.name ?: draft.fundName,
                            amount = draft.amount.coerceAtLeast(1.0),
                            category = draft.category.ifBlank { "ত্রাণ ও সহযোগিতা" },
                            spentOn = today
                        )
                    )
                }
                "record_monthly_fee" -> {
                    val member = members.find { it.fullName.contains(draft.personOrTitle, ignoreCase = true) } ?: members.firstOrNull()
                    val now = LocalDate.now()
                    foundationRepo.saveMonthlyFee(
                        MonthlyFee(
                            memberId = member?.id ?: "manual",
                            memberName = member?.fullName ?: draft.personOrTitle.ifBlank { "সদস্য" },
                            year = now.year,
                            month = now.monthValue,
                            amount = draft.amount.coerceAtLeast(100.0),
                            paidDate = today,
                            method = draft.method.ifBlank { "বিকাশ" },
                            status = "paid"
                        )
                    )
                }
                "draft_notice" -> {
                    foundationRepo.saveNotice(
                        Notice(
                            title = draft.personOrTitle.ifBlank { draft.title },
                            body = draft.summary.ifBlank { draft.title },
                            isUrgent = false,
                            authorName = "মানবসেবা AI"
                        )
                    )
                }
            }
            _pendingAction.value = null
            _chatStreamState.value = AiStreamState.Success("কাজটি সফলভাবে ডাটাবেসে সংরক্ষণ করা হয়েছে: ${draft.summary}")
        }
    }

    fun dismissActionDraft() {
        _pendingAction.value = null
    }

    fun runFinancialAudit(auditContext: String) {
        viewModelScope.launch {
            _auditState.value = AiStreamState.Loading
            try {
                var finalText = ""
                aiRepo.streamFinancialAudit(auditContext).collect { partial ->
                    finalText = partial
                    _auditState.value = AiStreamState.Streaming(partial)
                }
                _auditState.value = AiStreamState.Success(finalText)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Audit error", e)
                _auditState.value = AiStreamState.Error(e.localizedMessage ?: "অডিট রিপোর্ট তৈরি ব্যর্থ হয়েছে।")
            }
        }
    }

    fun analyzeLedgerPhoto(bitmap: Bitmap, funds: List<Fund>, members: List<Member>) {
        viewModelScope.launch {
            _ocrBusy.value = true
            _ocrError.value = null
            try {
                val (summary, entries) = aiRepo.analyzeHandwrittenLedger(
                    bitmap = bitmap,
                    availableFunds = funds.map { it.name },
                    memberNames = members.map { it.fullName }
                )
                _ocrSummary.value = summary
                _ocrEntries.value = entries
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ocrError.value = e.localizedMessage ?: "ছবি থেকে লেখা পড়া যায়নি।"
            } finally {
                _ocrBusy.value = false
            }
        }
    }

    fun importOcrEntries(entries: List<LedgerOcrEntry>, funds: List<Fund>) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            entries.forEach { entry ->
                val fund = funds.find { it.name.contains(entry.fundName, ignoreCase = true) } ?: funds.firstOrNull()
                if (entry.entryType == "expense") {
                    foundationRepo.saveExpense(
                        Expense(
                            title = entry.personOrTitle,
                            fundId = fund?.id ?: "",
                            fundName = fund?.name ?: entry.fundName,
                            amount = entry.amount.coerceAtLeast(1.0),
                            category = entry.category.ifBlank { "অন্যান্য ব্যয়" },
                            spentOn = entry.date.ifBlank { today },
                            notes = "AI খাতা রিডার: ${entry.notes}".trim()
                        )
                    )
                } else {
                    foundationRepo.saveDonation(
                        Donation(
                            donorName = entry.personOrTitle,
                            fundId = fund?.id ?: "",
                            fundName = fund?.name ?: entry.fundName,
                            amount = entry.amount.coerceAtLeast(1.0),
                            category = if (entry.entryType == "monthly_fee") "সদস্যদের মাসিক চাঁদা" else entry.category.ifBlank { "সাধারণ অনুদান" },
                            method = entry.method.ifBlank { "নগদ অর্থ" },
                            donatedOn = entry.date.ifBlank { today },
                            notes = "AI খাতা রিডার: ${entry.notes}".trim()
                        )
                    )
                }
            }
            _ocrEntries.value = emptyList()
            _ocrSummary.value = "${entries.size}টি এন্ট্রি সফলভাবে হিসাবের খাতায় যুক্ত হয়েছে!"
        }
    }

    fun parseLegacyText(rawText: String) {
        if (rawText.isBlank()) return
        viewModelScope.launch {
            _legacyBusy.value = true
            _legacyError.value = null
            try {
                _legacyEntries.value = aiRepo.parseLegacyMembers(rawText)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _legacyError.value = e.localizedMessage ?: "লিগ্যাসি তালিকা পার্স করা যায়নি।"
            } finally {
                _legacyBusy.value = false
            }
        }
    }

    fun importLegacyMembers(items: List<LegacyMemberImportEntry>) {
        viewModelScope.launch {
            items.forEach { item ->
                foundationRepo.saveMember(
                    Member(
                        fullName = item.fullName,
                        memberCode = item.memberCode,
                        phone = item.phone.ifBlank { null },
                        role = item.role,
                        openingBalance = item.openingBalance.takeIf { it > 0 },
                        notes = item.notes.ifBlank { null }
                    )
                )
            }
            _legacyEntries.value = emptyList()
        }
    }

    private companion object {
        const val TAG = "AiVM"
    }
}
