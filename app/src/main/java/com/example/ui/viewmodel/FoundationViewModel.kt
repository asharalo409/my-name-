package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BloodDonor
import com.example.data.model.BloodRequest
import com.example.data.model.ChatMessage
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.FieldReport
import com.example.data.model.FoundationEvent
import com.example.data.model.Fund
import com.example.data.model.IslamicJournalEntry
import com.example.data.model.Member
import com.example.data.model.MonthlyFee
import com.example.data.model.Notice
import com.example.data.model.OrgSettings
import com.example.data.model.Poll
import com.example.data.model.Project
import com.example.data.repository.FoundationRepository
import com.example.data.repository.FoundationSeed
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoundationViewModel(
    private val repository: FoundationRepository,
    private val db: FirebaseFirestore,
    private val currentUserId: String
) : ViewModel() {

    init {
        val user = Firebase.auth.currentUser
        FoundationSeed.seedInitialWorkspaceIfEmpty(
            db = db,
            uid = currentUserId,
            userDisplayName = user?.displayName,
            userEmail = user?.email
        )
    }

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    private fun <T> Flow<T>.safeStateIn(initial: T, tag: String): StateFlow<T> =
        this.catch { error ->
            val fsErr = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            val root = fsErr ?: error
            Log.w(TAG, "Stream error in $tag", root)
            _statusBanner.value = root.localizedMessage ?: "ডেটা সিঙ্ক করতে সমস্যা হয়েছে।"
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = initial
        )

    val settings: StateFlow<OrgSettings> =
        repository.observeSettings(currentUserId).safeStateIn(OrgSettings(), "settings")

    val funds: StateFlow<List<Fund>> =
        repository.observeFunds(currentUserId).safeStateIn(emptyList(), "funds")

    val members: StateFlow<List<Member>> =
        repository.observeMembers(currentUserId).safeStateIn(emptyList(), "members")

    val donations: StateFlow<List<Donation>> =
        repository.observeDonations(currentUserId).safeStateIn(emptyList(), "donations")

    val expenses: StateFlow<List<Expense>> =
        repository.observeExpenses(currentUserId).safeStateIn(emptyList(), "expenses")

    val monthlyFees: StateFlow<List<MonthlyFee>> =
        repository.observeMonthlyFees(currentUserId).safeStateIn(emptyList(), "monthlyFees")

    val projects: StateFlow<List<Project>> =
        repository.observeProjects(currentUserId).safeStateIn(emptyList(), "projects")

    val fieldReports: StateFlow<List<FieldReport>> =
        repository.observeFieldReports(currentUserId).safeStateIn(emptyList(), "fieldReports")

    val bloodDonors: StateFlow<List<BloodDonor>> =
        repository.observeBloodDonors(currentUserId).safeStateIn(emptyList(), "bloodDonors")

    val bloodRequests: StateFlow<List<BloodRequest>> =
        repository.observeBloodRequests(currentUserId).safeStateIn(emptyList(), "bloodRequests")

    val events: StateFlow<List<FoundationEvent>> =
        repository.observeEvents(currentUserId).safeStateIn(emptyList(), "events")

    val polls: StateFlow<List<Poll>> =
        repository.observePolls(currentUserId).safeStateIn(emptyList(), "polls")

    val notices: StateFlow<List<Notice>> =
        repository.observeNotices(currentUserId).safeStateIn(emptyList(), "notices")

    val messages: StateFlow<List<ChatMessage>> =
        repository.observeMessages(currentUserId).safeStateIn(emptyList(), "messages")

    val islamicJournal: StateFlow<List<IslamicJournalEntry>> =
        repository.observeIslamicJournal(currentUserId).safeStateIn(emptyList(), "islamicJournal")

    private fun runMutation(successMessage: String? = null, block: suspend () -> Result<*>) {
        viewModelScope.launch {
            val res = block()
            res.onSuccess {
                if (!successMessage.isNullOrBlank()) _statusBanner.value = successMessage
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "অপারেশন ব্যর্থ হয়েছে।"
            }
        }
    }

    fun updateSettings(updated: OrgSettings) = runMutation("সেটিংস সংরক্ষিত হয়েছে") {
        repository.saveSettings(updated)
    }

    fun saveFund(fund: Fund) = runMutation("তহবিল সংরক্ষিত হয়েছে") {
        repository.saveFund(fund)
    }

    fun saveMember(member: Member) = runMutation("সদস্য তথ্য সংরক্ষিত হয়েছে") {
        repository.saveMember(member)
    }

    fun deleteMember(memberId: String) = runMutation("সদস্য অপসারিত হয়েছে") {
        repository.deleteMember(memberId)
    }

    fun saveDonation(donation: Donation) = runMutation("অনুদান সংরক্ষিত হয়েছে") {
        repository.saveDonation(donation)
    }

    fun deleteDonation(donationId: String) = runMutation("অনুদান মুছে ফেলা হয়েছে") {
        repository.deleteDonation(donationId)
    }

    fun saveExpense(expense: Expense) = runMutation("খরচ সংরক্ষিত হয়েছে") {
        repository.saveExpense(expense)
    }

    fun deleteExpense(expenseId: String) = runMutation("খরচ মুছে ফেলা হয়েছে") {
        repository.deleteExpense(expenseId)
    }

    fun saveMonthlyFee(fee: MonthlyFee) = runMutation("মাসিক চাঁদা আপডেট হয়েছে") {
        repository.saveMonthlyFee(fee)
    }

    fun saveProject(project: Project) = runMutation("প্রকল্প সংরক্ষিত হয়েছে") {
        repository.saveProject(project)
    }

    fun saveFieldReport(report: FieldReport) = runMutation("কাজের প্রতিবেদন যুক্ত হয়েছে") {
        repository.saveFieldReport(report)
    }

    fun saveBloodDonor(donor: BloodDonor) = runMutation("রক্তদাতা তথ্য সংরক্ষিত হয়েছে") {
        repository.saveBloodDonor(donor)
    }

    fun saveBloodRequest(request: BloodRequest) = runMutation("রক্তের আবেদন প্রকাশিত হয়েছে") {
        repository.saveBloodRequest(request)
    }

    fun toggleBloodManaged(request: BloodRequest) = runMutation {
        repository.saveBloodRequest(request.copy(isManaged = !request.isManaged))
    }

    fun deleteBloodRequest(requestId: String) = runMutation("রক্তের আবেদন মুছে ফেলা হয়েছে") {
        repository.deleteBloodRequest(requestId)
    }

    fun saveEvent(event: FoundationEvent) = runMutation("ইভেন্ট সংরক্ষিত হয়েছে") {
        repository.saveEvent(event)
    }

    fun rsvpEvent(event: FoundationEvent) = runMutation("অংশগ্রহণ নিশ্চিত হয়েছে") {
        repository.saveEvent(event.copy(rsvpCount = event.rsvpCount + 1))
    }

    fun savePoll(poll: Poll) = runMutation("ভোট জরিপ প্রকাশিত হয়েছে") {
        repository.savePoll(poll)
    }

    fun castVote(poll: Poll, optionIdx: Int) = runMutation("আপনার ভোট গৃহীত হয়েছে") {
        val nextVotes = poll.votes.toMutableList()
        while (nextVotes.size < poll.options.size) nextVotes.add(0L)
        if (poll.selectedOption in nextVotes.indices && nextVotes[poll.selectedOption] > 0) {
            nextVotes[poll.selectedOption] = nextVotes[poll.selectedOption] - 1L
        }
        if (optionIdx in nextVotes.indices) {
            nextVotes[optionIdx] = nextVotes[optionIdx] + 1L
        }
        repository.savePoll(poll.copy(votes = nextVotes, selectedOption = optionIdx))
    }

    fun saveNotice(notice: Notice) = runMutation("নোটিশ প্রকাশিত হয়েছে") {
        repository.saveNotice(notice)
    }

    fun reactNotice(notice: Notice) = runMutation {
        repository.saveNotice(notice.copy(reactions = notice.reactions + 1))
    }

    fun deleteNotice(noticeId: String) = runMutation("নোটিশ মুছে ফেলা হয়েছে") {
        repository.deleteNotice(noticeId)
    }

    fun sendChatMessage(channel: String, body: String, receiverName: String? = null) = runMutation {
        val sender = Firebase.auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "সদস্য"
        repository.sendChatMessage(
            ChatMessage(
                channel = channel,
                senderName = sender,
                receiverName = receiverName,
                body = body
            )
        )
    }

    fun deleteChatMessage(messageId: String) = runMutation {
        repository.deleteChatMessage(messageId)
    }

    fun saveIslamicJournal(entry: IslamicJournalEntry) = runMutation("আজকের আমল সংরক্ষিত হয়েছে") {
        repository.saveIslamicJournal(entry)
    }

    private companion object {
        const val TAG = "FoundationVM"
    }
}
