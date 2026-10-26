const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_school_1";
const BOB_UID = "bob_school_2";

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

test("Unauthenticated user: cannot read school config", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("schools").doc(ALICE_UID).get());
});

test("Authenticated user: can create, get, and update own school config", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const schoolRef = aliceDb.collection("schools").doc(ALICE_UID);

  await assertSucceeds(
    schoolRef.set({
      userId: ALICE_UID,
      schoolName: "SMA Negeri 1 Prestasi",
      schoolMotto: "Unggul dalam Prestasi dan Karakter",
      runningText: "Selamat datang di semester baru",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(schoolRef.get());

  await assertSucceeds(
    schoolRef.update({
      runningText: "Pengumuman: Ujian dimulai jam 08.00",
      updatedAt: new Date(),
    })
  );
});

test("Cross-user isolation: Bob cannot read or modify Alice's school config or schedules", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("schools").doc(ALICE_UID).set({
      userId: ALICE_UID,
      schoolName: "SMA Negeri 1 Prestasi",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("schools").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("schools").doc(ALICE_UID).update({
      schoolName: "Hacked School",
    })
  );
});

test("Authenticated owner: can create bell schedule and achievement", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const schedRef = aliceDb.collection("schools").doc(ALICE_UID).collection("schedules").doc("sched_1");
  await assertSucceeds(
    schedRef.set({
      schoolId: ALICE_UID,
      periodName: "Jam Ke-1",
      startTime: "07:00",
      endTime: "07:45",
      bellType: "MASUK",
      order: 1,
      createdAt: new Date(),
    })
  );

  const achRef = aliceDb.collection("schools").doc(ALICE_UID).collection("achievements").doc("ach_1");
  await assertSucceeds(
    achRef.set({
      schoolId: ALICE_UID,
      studentName: "Budi Santoso",
      title: "Juara 1 Olimpiade Sains Nasional",
      category: "Akademik",
      createdAt: new Date(),
    })
  );
});
