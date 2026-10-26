package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Achievement
import com.example.data.model.BellSchedule
import com.example.data.model.SchoolConfig
import com.example.data.model.SchoolVideo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun saveAndObserveSchoolConfig_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = SchoolRepository(firestore)

    val saveResult: Result<Unit> = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.saveSchoolConfig(
        SchoolConfig(
          userId = uid,
          schoolName = "SMA Bintang Prestasi",
          schoolMotto = "Maju Bersama Hebat Semua",
          runningText = "Ujian Tengah Semester 2026",
          manualBellTrigger = 1L
        )
      )
    }
    assertTrue(saveResult.isSuccess)

    val emittedConfig: SchoolConfig? = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeSchoolConfig().first { it != null && it.schoolName == "SMA Bintang Prestasi" }
    }
    assertNotNull(emittedConfig)
    assertEquals("SMA Bintang Prestasi", emittedConfig?.schoolName)
    assertEquals("Ujian Tengah Semester 2026", emittedConfig?.runningText)
  }

  @Test
  fun saveScheduleAndAchievement_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = SchoolRepository(firestore)

    val schedResult: Result<String> = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.saveSchedule(
        BellSchedule(
          schoolId = uid,
          periodName = "Jam Ke-1",
          startTime = "07:00",
          endTime = "07:45",
          bellType = "MASUK",
          order = 1
        )
      )
    }
    assertTrue(schedResult.isSuccess)

    val achResult: Result<String> = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.saveAchievement(
        Achievement(
          schoolId = uid,
          studentName = "Rina Wijaya",
          title = "Juara Pidato Bahasa Inggris",
          category = "Bahasa"
        )
      )
    }
    assertTrue(achResult.isSuccess)
  }

  @Test
  fun saveAndObserveVideo_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = SchoolRepository(firestore)

    val vidResult: Result<String> = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.saveVideo(
        SchoolVideo(
          schoolId = uid,
          title = "Video Profil 2026",
          videoUrl = "https://example.com/video.mp4",
          description = "Profil sekolah unggulan",
          order = 1L
        )
      )
    }
    assertTrue("Save video failed: ${vidResult.exceptionOrNull()?.message}", vidResult.isSuccess)

    val videos = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeVideos().first { list -> list.any { it.title == "Video Profil 2026" } }
    }
    assertEquals(1, videos.size)
    assertEquals("Video Profil 2026", videos.first().title)
  }

  @Test
  fun observeSchoolConfig_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = SchoolRepository(firestore)

    val exception = assertThrows(Exception::class.java) {
      runBlocking {
        withTimeout(FLOW_TIMEOUT_MS) {
          repo.observeSchoolConfig().first()
        }
      }
    }
    assertNotNull(exception)
  }

  private companion object {
    const val ALICE_EMAIL = "alice_school@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
