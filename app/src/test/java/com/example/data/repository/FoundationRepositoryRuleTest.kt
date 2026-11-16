package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.BloodRequest
import com.example.data.model.Donation
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import java.util.UUID
import kotlin.test.assertFailsWith

class FoundationRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveDonation_validPayload_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = FoundationRepository(firestore)
        val id = UUID.randomUUID().toString()

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveDonation(
                Donation(
                    id = id,
                    donorName = "আবদুল করিম",
                    fundId = "general",
                    fundName = "সাধারণ তহবিল",
                    amount = 1500.0,
                    category = "সাধারণ অনুদান",
                    method = "বিকাশ",
                    donatedOn = "2026-11-16",
                    status = "verified"
                )
            )
        }
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEqualTo(id)
    }

    @Test
    fun observeDonations_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = FoundationRepository(firestore)
        val id = UUID.randomUUID().toString()

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveDonation(
                Donation(
                    id = id,
                    donorName = "ফাতেমা বেগম",
                    fundId = "relief",
                    fundName = "জরুরি ত্রাণ তহবিল",
                    amount = 2000.0,
                    donatedOn = "2026-11-16"
                )
            ).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeDonations(aliceUid).first { list -> list.any { it.id == id } }
        }
        assertThat(emitted.map { it.id }).contains(id)
    }

    @Test
    fun observeDonations_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FoundationRepository(firestore)
        val id = UUID.randomUUID().toString()
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveDonation(
                Donation(
                    id = id,
                    donorName = "সাদিকুর রহমান",
                    fundId = "general",
                    fundName = "সাধারণ তহবিল",
                    amount = 500.0,
                    donatedOn = "2026-11-16"
                )
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = FoundationRepository(firestore)
        val exception = assertFailsWith<Exception> {
            withTimeout(FLOW_TIMEOUT_MS) { bobRepo.observeDonations(aliceUid).first() }
        }
        val firestoreException = checkNotNull(exception.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $exception"
        }
        assertThat(firestoreException.code).isEqualTo(FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }

    @Test
    fun observeBloodRequests_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = FoundationRepository(firestore)

        val exception = assertFailsWith<Exception> {
            withTimeout(FLOW_TIMEOUT_MS) { repository.observeBloodRequests("alice_uid_placeholder").first() }
        }
        val firestoreException = checkNotNull(exception.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $exception"
        }
        assertThat(firestoreException.code).isEqualTo(FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }

    @Test
    fun saveBloodRequest_validPayload_persistsDocument() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = FoundationRepository(firestore)
        val reqId = UUID.randomUUID().toString()

        val saveRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveBloodRequest(
                BloodRequest(
                    id = reqId,
                    bloodGroup = "O+",
                    hospital = "ঢাকা মেডিকেল কলেজ হাসপাতাল",
                    district = "ঢাকা",
                    contact = "01711000000",
                    patientProblem = "জরুরি অস্ত্রোপচার",
                    applicantName = "রহিম উদ্দিন",
                    urgent = true
                )
            )
        }
        assertThat(saveRes.isSuccess).isTrue()

        val list = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeBloodRequests(uid).first { items -> items.any { it.id == reqId } }
        }
        assertThat(list.first { it.id == reqId }.bloodGroup).isEqualTo("O+")
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
