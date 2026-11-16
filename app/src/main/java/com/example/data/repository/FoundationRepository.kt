package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AiChatMessage
import com.example.data.model.AiConversation
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FoundationRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("Google Sign-In required before accessing Firestore.")

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    private fun <T> observeUserCollection(
        uid: String,
        collectionName: String,
        orderField: String? = null,
        descending: Boolean = true,
        mapper: (com.google.firebase.firestore.QuerySnapshot) -> List<T>
    ): Flow<List<T>> = flow {
        val baseCol = userDoc(uid).collection(collectionName)
        val query: Query = if (orderField != null) {
            baseCol.orderBy(
                orderField,
                if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING
            )
        } else {
            baseCol
        }
        val path = baseCol.path
        emitAll(
            query.snapshots()
                .map { snapshot -> mapper(snapshot) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeSettings(uid: String): Flow<OrgSettings> = flow {
        val docRef = userDoc(uid).collection("settings").document("org")
        emitAll(
            docRef.snapshots()
                .map { snap ->
                    snap.toObject(OrgSettings::class.java, ServerTimestampBehavior.ESTIMATE)
                        ?: OrgSettings()
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeFunds(uid: String): Flow<List<Fund>> =
        observeUserCollection(uid, "funds", "name", descending = false) {
            it.toObjects(Fund::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeMembers(uid: String): Flow<List<Member>> =
        observeUserCollection(uid, "members", "memberCode", descending = false) {
            it.toObjects(Member::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeDonations(uid: String): Flow<List<Donation>> =
        observeUserCollection(uid, "donations", "createdAt", descending = true) {
            it.toObjects(Donation::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeExpenses(uid: String): Flow<List<Expense>> =
        observeUserCollection(uid, "expenses", "createdAt", descending = true) {
            it.toObjects(Expense::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeMonthlyFees(uid: String): Flow<List<MonthlyFee>> =
        observeUserCollection(uid, "monthlyFees", "createdAt", descending = true) {
            it.toObjects(MonthlyFee::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeProjects(uid: String): Flow<List<Project>> =
        observeUserCollection(uid, "projects", "createdAt", descending = true) {
            it.toObjects(Project::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeFieldReports(uid: String): Flow<List<FieldReport>> =
        observeUserCollection(uid, "fieldReports", "createdAt", descending = true) {
            it.toObjects(FieldReport::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeBloodDonors(uid: String): Flow<List<BloodDonor>> =
        observeUserCollection(uid, "bloodDonors", "createdAt", descending = true) {
            it.toObjects(BloodDonor::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeBloodRequests(uid: String): Flow<List<BloodRequest>> =
        observeUserCollection(uid, "bloodRequests", "createdAt", descending = true) {
            it.toObjects(BloodRequest::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeEvents(uid: String): Flow<List<FoundationEvent>> =
        observeUserCollection(uid, "events", "eventDate", descending = false) {
            it.toObjects(FoundationEvent::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observePolls(uid: String): Flow<List<Poll>> =
        observeUserCollection(uid, "polls", "createdAt", descending = true) {
            it.toObjects(Poll::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeNotices(uid: String): Flow<List<Notice>> =
        observeUserCollection(uid, "notices", "createdAt", descending = true) {
            it.toObjects(Notice::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeMessages(uid: String): Flow<List<ChatMessage>> =
        observeUserCollection(uid, "messages", "createdAt", descending = false) {
            it.toObjects(ChatMessage::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeAiConversations(uid: String): Flow<List<AiConversation>> =
        observeUserCollection(uid, "aiConversations", "updatedAt", descending = true) {
            it.toObjects(AiConversation::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    fun observeAiMessages(uid: String, conversationId: String): Flow<List<AiChatMessage>> = flow {
        val col = userDoc(uid).collection("aiConversations").document(conversationId).collection("messages")
        emitAll(
            col.orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { it.toObjects(AiChatMessage::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, col.path)
                    }
                    throw error
                }
        )
    }

    fun observeIslamicJournal(uid: String): Flow<List<IslamicJournalEntry>> =
        observeUserCollection(uid, "islamicJournal", "dateKey", descending = true) {
            it.toObjects(IslamicJournalEntry::class.java, ServerTimestampBehavior.ESTIMATE)
        }

    private suspend fun writeDocument(
        collectionName: String,
        docId: String?,
        existingCreatedAt: com.google.firebase.Timestamp?,
        buildMap: (Boolean) -> Map<String, Any>
    ): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val col = userDoc(uid).collection(collectionName)
        val isCreate = docId.isNullOrBlank() || existingCreatedAt == null
        val targetId = docId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val ref = col.document(targetId)
        return try {
            val payload = buildMap(isCreate)
            ref.set(payload).await()
            Result.success(targetId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    private suspend fun deleteDocument(collectionName: String, docId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val ref = userDoc(uid).collection(collectionName).document(docId)
        return try {
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun saveSettings(settings: OrgSettings): Result<String> =
        writeDocument("settings", "org", settings.createdAt) { settings.toWriteMap(it) }

    suspend fun saveFund(fund: Fund): Result<String> =
        writeDocument("funds", fund.id.ifBlank { null }, fund.createdAt) { fund.toWriteMap(it) }

    suspend fun saveMember(member: Member): Result<String> =
        writeDocument("members", member.id.ifBlank { null }, member.createdAt) { member.toWriteMap(it) }

    suspend fun deleteMember(memberId: String): Result<Unit> =
        deleteDocument("members", memberId)

    suspend fun saveDonation(donation: Donation): Result<String> =
        writeDocument("donations", donation.id.ifBlank { null }, donation.createdAt) { donation.toWriteMap(it) }

    suspend fun deleteDonation(donationId: String): Result<Unit> =
        deleteDocument("donations", donationId)

    suspend fun saveExpense(expense: Expense): Result<String> =
        writeDocument("expenses", expense.id.ifBlank { null }, expense.createdAt) { expense.toWriteMap(it) }

    suspend fun deleteExpense(expenseId: String): Result<Unit> =
        deleteDocument("expenses", expenseId)

    suspend fun saveMonthlyFee(fee: MonthlyFee): Result<String> =
        writeDocument("monthlyFees", fee.id.ifBlank { null }, fee.createdAt) { fee.toWriteMap(it) }

    suspend fun saveProject(project: Project): Result<String> =
        writeDocument("projects", project.id.ifBlank { null }, project.createdAt) { project.toWriteMap(it) }

    suspend fun saveFieldReport(report: FieldReport): Result<String> =
        writeDocument("fieldReports", report.id.ifBlank { null }, report.createdAt) { report.toWriteMap(it) }

    suspend fun saveBloodDonor(donor: BloodDonor): Result<String> =
        writeDocument("bloodDonors", donor.id.ifBlank { null }, donor.createdAt) { donor.toWriteMap(it) }

    suspend fun saveBloodRequest(request: BloodRequest): Result<String> =
        writeDocument("bloodRequests", request.id.ifBlank { null }, request.createdAt) { request.toWriteMap(it) }

    suspend fun deleteBloodRequest(requestId: String): Result<Unit> =
        deleteDocument("bloodRequests", requestId)

    suspend fun saveEvent(event: FoundationEvent): Result<String> =
        writeDocument("events", event.id.ifBlank { null }, event.createdAt) { event.toWriteMap(it) }

    suspend fun savePoll(poll: Poll): Result<String> =
        writeDocument("polls", poll.id.ifBlank { null }, poll.createdAt) { poll.toWriteMap(it) }

    suspend fun saveNotice(notice: Notice): Result<String> =
        writeDocument("notices", notice.id.ifBlank { null }, notice.createdAt) { notice.toWriteMap(it) }

    suspend fun deleteNotice(noticeId: String): Result<Unit> =
        deleteDocument("notices", noticeId)

    suspend fun sendChatMessage(message: ChatMessage): Result<String> =
        writeDocument("messages", message.id.ifBlank { null }, message.createdAt) { message.toWriteMap(it) }

    suspend fun deleteChatMessage(messageId: String): Result<Unit> =
        deleteDocument("messages", messageId)

    suspend fun saveAiConversation(conversation: AiConversation): Result<String> =
        writeDocument("aiConversations", conversation.id.ifBlank { null }, conversation.createdAt) { conversation.toWriteMap(it) }

    suspend fun saveAiChatMessage(conversationId: String, message: AiChatMessage): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val col = userDoc(uid).collection("aiConversations").document(conversationId).collection("messages")
        val isCreate = message.id.isBlank() || message.createdAt == null
        val targetId = message.id.ifBlank { UUID.randomUUID().toString() }
        val ref = col.document(targetId)
        return try {
            ref.set(message.toWriteMap(isCreate)).await()
            Result.success(targetId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun saveIslamicJournal(entry: IslamicJournalEntry): Result<String> =
        writeDocument("islamicJournal", entry.dateKey, entry.createdAt) { entry.toWriteMap(it) }
}
