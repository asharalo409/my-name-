package com.example.data.repository

import com.example.data.model.BloodDonor
import com.example.data.model.BloodRequest
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.FieldReport
import com.example.data.model.FoundationEvent
import com.example.data.model.Fund
import com.example.data.model.Member
import com.example.data.model.MonthlyFee
import com.example.data.model.Notice
import com.example.data.model.OrgSettings
import com.example.data.model.Poll
import com.example.data.model.Project
import com.google.firebase.firestore.FirebaseFirestore

object FoundationSeed {
    fun seedInitialWorkspaceIfEmpty(db: FirebaseFirestore, uid: String, userDisplayName: String?, userEmail: String?) {
        val userRef = db.collection("users").document(uid)
        val settingsDoc = userRef.collection("settings").document("org")
        settingsDoc.get().addOnSuccessListener { snapshot ->
            if (snapshot != null && snapshot.exists()) return@addOnSuccessListener

            val batch = db.batch()
            batch.set(settingsDoc, OrgSettings().toWriteMap(isCreate = true))

            val funds = listOf(
                "fund_general" to Fund(name = "সাধারণ তহবিল", description = "সংগঠনের নিয়মিত মানবিক ও পরিচালন কার্যক্রম", targetAmount = 250000.0),
                "fund_relief" to Fund(name = "জরুরি ত্রাণ তহবিল", description = "বন্যা, ঘূর্ণিঝড় ও দুর্যোগে তাৎক্ষণিক খাদ্য ও চিকিৎসা সহায়তা", targetAmount = 180000.0),
                "fund_health" to Fund(name = "চিকিৎসা ও রক্তদান তহবিল", description = "অসহায় রোগীদের অস্ত্রোপচার, ওষুধ ও রক্তদান সমন্বয়", targetAmount = 120000.0),
                "fund_edu" to Fund(name = "শিক্ষাবৃত্তি তহবিল", description = "দরিদ্র মেধাবী শিক্ষার্থীদের বই, খাতা ও বৃত্তি প্রদান", targetAmount = 100000.0),
            )
            funds.forEach { (id, f) ->
                batch.set(userRef.collection("funds").document(id), f.toWriteMap(isCreate = true))
            }

            val adminName = userDisplayName?.takeIf { it.isNotBlank() } ?: "আরিফুল ইসলাম"
            val members = listOf(
                "mem_1" to Member(fullName = adminName, memberCode = "MSF-101", phone = "01711223301", email = userEmail, bloodGroup = "O+", district = "ঢাকা", upazila = "ধানমন্ডি", role = "admin", occupation = "সমাজকর্মী", isBloodDonor = true, joinedAt = "2024-01-10"),
                "mem_2" to Member(fullName = "মাহমুদুল হাসান", memberCode = "MSF-102", phone = "01812334402", bloodGroup = "A+", district = "সিলেট", upazila = "গোয়াইনঘাট", role = "president", occupation = "শিক্ষক", isBloodDonor = true, joinedAt = "2024-02-01"),
                "mem_3" to Member(fullName = "ফারহানা আক্তার", memberCode = "MSF-103", phone = "01913445503", bloodGroup = "B+", district = "ঢাকা", upazila = "মিরপুর", role = "cashier", occupation = "হিসাবরক্ষক", isBloodDonor = false, joinedAt = "2024-02-15"),
                "mem_4" to Member(fullName = "তানভীর আহমেদ", memberCode = "MSF-104", phone = "01614556604", bloodGroup = "AB+", district = "কুড়িগ্রাম", upazila = "চিলমারী", role = "health", occupation = "মেডিকেল টেকনোলজিস্ট", isBloodDonor = true, joinedAt = "2024-03-05"),
                "mem_5" to Member(fullName = "সাদিয়া রহমান", memberCode = "MSF-105", phone = "01715667705", bloodGroup = "O-", district = "কুমিল্লা", upazila = "লাকসাম", role = "general_secretary", occupation = "প্রকৌশলী", isBloodDonor = true, joinedAt = "2024-03-20"),
            )
            members.forEach { (id, m) ->
                batch.set(userRef.collection("members").document(id), m.toWriteMap(isCreate = true))
            }

            val donations = listOf(
                "don_1" to Donation(donorName = adminName, memberId = "mem_1", fundId = "fund_relief", fundName = "জরুরি ত্রাণ তহবিল", amount = 25000.0, category = "বিশেষ অনুদান", method = "ব্যাংক", trxId = "TRX-9901", donatedOn = "2026-09-12"),
                "don_2" to Donation(donorName = "মাহমুদুল হাসান", memberId = "mem_2", fundId = "fund_general", fundName = "সাধারণ তহবিল", amount = 18000.0, category = "সাধারণ অনুদান", method = "বিকাশ", trxId = "TRX-9902", donatedOn = "2026-10-04"),
                "don_3" to Donation(donorName = "প্রবাসী কল্যাণ শাখা", fundId = "fund_edu", fundName = "শিক্ষাবৃত্তি তহবিল", amount = 32000.0, category = "প্রকল্পভিত্তিক অনুদান", method = "ব্যাংক", trxId = "TRX-9903", donatedOn = "2026-10-19"),
                "don_4" to Donation(donorName = "ফারহানা আক্তার", memberId = "mem_3", fundId = "fund_health", fundName = "চিকিৎসা ও রক্তদান তহবিল", amount = 12500.0, category = "ব্যক্তিগত অনুদান", method = "নগদ", trxId = "TRX-9904", donatedOn = "2026-11-05"),
            )
            donations.forEach { (id, d) ->
                batch.set(userRef.collection("donations").document(id), d.toWriteMap(isCreate = true))
            }

            val expenses = listOf(
                "exp_1" to Expense(title = "কুড়িগ্রাম বন্যা শুকনো খাবার ও স্যালাইন বিতরণ", fundId = "fund_relief", fundName = "জরুরি ত্রাণ তহবিল", amount = 16500.0, category = "ত্রাণ ও সহযোগিতা", spentOn = "2026-09-18", recipient = "৮৫টি পরিবার"),
                "exp_2" to Expense(title = "অসহায় রোগীর জরুরি অপারেশন ও ওষুধ সহায়তা", fundId = "fund_health", fundName = "চিকিৎসা ও রক্তদান তহবিল", amount = 9500.0, category = "চিকিৎসা ও স্বাস্থ্যসেবা", spentOn = "2026-10-14", recipient = "ঢাকা মেডিকেল ইউনিট"),
                "exp_3" to Expense(title = "গ্রামীণ পাঠশালা বই ও খাতা বিতরণ", fundId = "fund_edu", fundName = "শিক্ষাবৃত্তি তহবিল", amount = 14000.0, category = "শিক্ষা ও প্রশিক্ষণ", spentOn = "2026-11-02", recipient = "৪২ জন শিক্ষার্থী"),
            )
            expenses.forEach { (id, e) ->
                batch.set(userRef.collection("expenses").document(id), e.toWriteMap(isCreate = true))
            }

            val fees = listOf(
                "fee_mem1_11" to MonthlyFee(memberId = "mem_1", memberName = adminName, year = 2026, month = 11, amount = 500.0, paidDate = "2026-11-03", method = "বিকাশ", status = "paid"),
                "fee_mem2_11" to MonthlyFee(memberId = "mem_2", memberName = "মাহমুদুল হাসান", year = 2026, month = 11, amount = 500.0, paidDate = "2026-11-05", method = "নগদ", status = "paid"),
                "fee_mem3_11" to MonthlyFee(memberId = "mem_3", memberName = "ফারহানা আক্তার", year = 2026, month = 11, amount = 500.0, paidDate = "2026-11-07", method = "বিকাশ", status = "paid"),
            )
            fees.forEach { (id, f) ->
                batch.set(userRef.collection("monthlyFees").document(id), f.toWriteMap(isCreate = true))
            }

            val projects = listOf(
                "proj_1" to Project(title = "উত্তরাঞ্চল বন্যা ও শীতবস্ত্র পুনর্বাসন", area = "কুড়িগ্রাম ও সুনামগঞ্জ", fundId = "fund_relief", fundName = "জরুরি ত্রাণ তহবিল", goalAmount = 150000.0, spentAmount = 82500.0, beneficiaries = 320, status = "active", description = "নদীভাঙন ও বন্যাকবলিত পরিবারগুলোর মাঝে বিশুদ্ধ পানি, শুকনো খাবার ও কম্বল বিতরণ।", bannerKey = "flood"),
                "proj_2" to Project(title = "আলোর পাঠশালা ও মেধাবৃত্তি", area = "কুমিল্লা ও সিলেট", fundId = "fund_edu", fundName = "শিক্ষাবৃত্তি তহবিল", goalAmount = 100000.0, spentAmount = 54000.0, beneficiaries = 110, status = "active", description = "সুবিধাবঞ্চিত শিশুদের শিক্ষা উপকরণ, স্কুল ব্যাগ ও মাসিক বৃত্তি প্রদান।", bannerKey = "edu"),
            )
            projects.forEach { (id, p) ->
                batch.set(userRef.collection("projects").document(id), p.toWriteMap(isCreate = true))
            }

            val reports = listOf(
                "rep_1" to FieldReport(title = "চিলমারী চরে ৮৫ পরিবারে ত্রাণ পৌঁছে দেওয়া হয়েছে", projectId = "proj_1", projectTitle = "উত্তরাঞ্চল বন্যা ও শীতবস্ত্র পুনর্বাসন", location = "চিলমারী, কুড়িগ্রাম", summary = "স্বেচ্ছাসেবক দল নৌকাযোগে খাদ্য ও বিশুদ্ধ পানি স্যালাইন পৌঁছে দিয়েছে।", beneficiaryCount = 85, amountSpent = 16500.0, reportDate = "2026-09-18", verified = true),
                "rep_2" to FieldReport(title = "লাকসাম স্কুলে ৪২ শিক্ষার্থীকে বই-খাতা বিতরণ", projectId = "proj_2", projectTitle = "আলোর পাঠশালা ও মেধাবৃত্তি", location = "লাকসাম, কুমিল্লা", summary = "বার্ষিক শিক্ষা উপকরণ ও পরীক্ষার ফরম ফি সহায়তা প্রদান সম্পন্ন।", beneficiaryCount = 42, amountSpent = 14000.0, reportDate = "2026-11-02", verified = true),
            )
            reports.forEach { (id, r) ->
                batch.set(userRef.collection("fieldReports").document(id), r.toWriteMap(isCreate = true))
            }

            val donors = listOf(
                "donr_1" to BloodDonor(name = adminName, phone = "01711223301", bloodGroup = "O+", district = "ঢাকা", upazila = "ধানমন্ডি", lastDonatedDate = "2026-07-10", available = true),
                "donr_2" to BloodDonor(name = "মাহমুদুল হাসান", phone = "01812334402", bloodGroup = "A+", district = "সিলেট", upazila = "সদর", lastDonatedDate = "2026-08-18", available = true),
                "donr_3" to BloodDonor(name = "সাদিয়া রহমান", phone = "01715667705", bloodGroup = "O-", district = "কুমিল্লা", upazila = "লাকসাম", lastDonatedDate = "2026-06-05", available = true),
            )
            donors.forEach { (id, d) ->
                batch.set(userRef.collection("bloodDonors").document(id), d.toWriteMap(isCreate = true))
            }

            val reqs = listOf(
                "breq_1" to BloodRequest(bloodGroup = "O+", hospital = "ঢাকা মেডিকেল কলেজ হাসপাতাল", district = "ঢাকা", upazila = "শাহবাগ", contact = "01719001122", patientProblem = "জরুরি সার্জারি (২ ব্যাগ)", applicantName = "নাসরিন সুলতানা", neededBy = "আজ সন্ধ্যা ৬টা", urgent = true, isManaged = false),
            )
            reqs.forEach { (id, br) ->
                batch.set(userRef.collection("bloodRequests").document(id), br.toWriteMap(isCreate = true))
            }

            val events = listOf(
                "ev_1" to FoundationEvent(title = "শীতবস্ত্র সংগ্রহ ও প্যাকেজিং ক্যাম্প", eventDate = "2026-11-28", eventTime = "বিকাল ৩:৩০", location = "ধানমন্ডি কেন্দ্রীয় কার্যালয়, ঢাকা", category = "ত্রাণ ক্যাম্প", description = "উত্তরাঞ্চলে কম্বল ও শিশুদের গরম কাপড় পাঠানোর প্রস্তুতি সভা।", rsvpCount = 14),
                "ev_2" to FoundationEvent(title = "স্বেচ্ছাসেবক রক্তদান ও স্বাস্থ্য ক্যাম্প", eventDate = "2026-12-05", eventTime = "সকাল ৯:০০", location = "সিলেট কেন্দ্রীয় শহীদ মিনার প্রাঙ্গণ", category = "স্বাস্থ্য ও রক্তদান", description = "বিনামূল্যে রক্তের গ্রুপ নির্ণয় ও ডায়াবেটিস পরীক্ষা।", rsvpCount = 22),
            )
            events.forEach { (id, ev) ->
                batch.set(userRef.collection("events").document(id), ev.toWriteMap(isCreate = true))
            }

            val poll = Poll(
                question = "আসন্ন শীতবস্ত্র বিতরণ কর্মসূচি কোন জেলায় অগ্রাধিকার পাবে?",
                options = listOf("কুড়িগ্রাম ও গাইবান্ধা", "সুনামগঞ্জ হাওরাঞ্চল", "পঞ্চগড় ও ঠাকুরগাঁও"),
                votes = listOf(12L, 8L, 10L),
                selectedOption = -1,
                isOpen = true
            )
            batch.set(userRef.collection("polls").document("poll_1"), poll.toWriteMap(isCreate = true))

            val notices = listOf(
                "not_1" to Notice(title = "নভেম্বর মাসের চাঁদা ও শীতবস্ত্র তহবিল সংগ্রহ চলছে", body = "সকল সদস্যদের চলতি মাসের চাঁদা ও শীতবস্ত্র তহবিলের অনুদান স্বচ্ছতার সাথে জমা দেওয়ার অনুরোধ করা হচ্ছে।", isUrgent = true, authorName = "কেন্দ্রীয় কোষাধ্যক্ষ", reactions = 18),
                "not_2" to Notice(title = "রক্তদান জরুরি হেল্পলাইন ২৪ ঘণ্টা চালু রয়েছে", body = "যেকোনো জরুরি রক্তের প্রয়োজনে রক্তদান পাতায় আবেদন প্রকাশ করুন অথবা হটলাইনে কল করুন।", isUrgent = false, authorName = "স্বাস্থ্য সমন্বয়ক", reactions = 11),
            )
            notices.forEach { (id, n) ->
                batch.set(userRef.collection("notices").document(id), n.toWriteMap(isCreate = true))
            }

            batch.commit()
        }
    }
}
