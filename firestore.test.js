const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const { serverTimestamp } = require("firebase/firestore");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read donations or settings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("donations").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("settings").doc("org").get());
});

test("Owner isolation: Bob cannot read or write Alice's financial ledger", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(ALICE_UID).collection("donations").doc("don_1").set({
      donorName: "রহিম",
      fundId: "f1",
      fundName: "সাধারণ তহবিল",
      amount: 1000,
      category: "সাধারণ অনুদান",
      method: "বিকাশ",
      donatedOn: "2026-11-16",
      status: "verified",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("donations").doc("don_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("donations").get());
});

test("Owner can create and update valid donation with server timestamps; rejects negative amount or ghost field", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("donations");

  await assertSucceeds(
    col.doc("don_valid").set({
      donorName: "করিম",
      fundId: "f1",
      fundName: "সাধারণ তহবিল",
      amount: 1500,
      category: "সাধারণ অনুদান",
      method: "বিকাশ",
      donatedOn: "2026-11-16",
      status: "verified",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Negative amount must fail
  await assertFails(
    col.doc("don_bad_amt").set({
      donorName: "করিম",
      fundId: "f1",
      fundName: "সাধারণ তহবিল",
      amount: -50,
      category: "সাধারণ অনুদান",
      method: "বিকাশ",
      donatedOn: "2026-11-16",
      status: "verified",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Ghost field injection must fail
  await assertFails(
    col.doc("don_ghost").set({
      donorName: "করিম",
      fundId: "f1",
      fundName: "সাধারণ তহবিল",
      amount: 500,
      category: "সাধারণ অনুদান",
      method: "বিকাশ",
      donatedOn: "2026-11-16",
      status: "verified",
      isAdminOverride: true,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
});

test("Owner can create OrgSettings, BloodRequest, Poll, IslamicJournalEntry", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const base = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(
    base.collection("settings").doc("org").set({
      orgName: "মানবসেবা ফাউন্ডেশন",
      slogan: "মানুষের পাশে, মানবতার পাশে",
      hotline: "01700000000",
      bankDetails: "বিকাশ: 01700000000",
      defaultMonthlyFee: 200,
      goalAmount: 500000,
      themePalette: "green",
      darkMode: false,
      language: "bn",
      district: "ঢাকা",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    base.collection("bloodRequests").doc("br_1").set({
      bloodGroup: "O+",
      hospital: "ঢাকা মেডিকেল",
      contact: "01711223344",
      patientProblem: "অপারেশন",
      applicantName: "সালমা",
      urgent: true,
      isManaged: false,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    base.collection("polls").doc("poll_1").set({
      question: "শীতবস্ত্র বিতরণ কোথায় হবে?",
      options: ["কুড়িগ্রাম", "সুনামগঞ্জ"],
      votes: [5, 3],
      selectedOption: 0,
      isOpen: true,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    base.collection("islamicJournal").doc("2026-11-16").set({
      dateKey: "2026-11-16",
      prayersCompleted: 5,
      quranMinutes: 20,
      zikrCount: 100,
      charityDone: true,
      amolItems: ["ফজর", "যোহর"],
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
});
