package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Achievement
import com.example.data.model.BellSchedule
import com.example.data.model.SchoolConfig
import com.example.data.model.SchoolVideo
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SchoolRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeSchoolConfig(): Flow<SchoolConfig?> = flow {
        val uid = requireUserId()
        val docRef = db.collection("schools").document(uid)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(SchoolConfig::class.java)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                    throw error
                }
        )
    }

    suspend fun getSchoolConfig(): Result<SchoolConfig?> = try {
        val uid = requireUserId()
        val docRef = db.collection("schools").document(uid)
        val snap = docRef.get().await()
        Result.success(if (snap.exists()) snap.toObject(SchoolConfig::class.java) else null)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.GET, "schools/${auth.currentUser?.uid}")
        Result.failure(e)
    }

    suspend fun saveSchoolConfig(config: SchoolConfig): Result<Unit> = try {
        val uid = requireUserId()
        val docRef = db.collection("schools").document(uid)
        val existingSnap = docRef.get().await()

        if (existingSnap.exists()) {
            val payload = mapOf(
                "schoolName" to config.schoolName,
                "schoolMotto" to config.schoolMotto,
                "logoUrl" to config.logoUrl,
                "videoUrl" to config.videoUrl,
                "videoTitle" to config.videoTitle,
                "runningText" to config.runningText,
                "manualBellTrigger" to config.manualBellTrigger,
                "manualBellMessage" to config.manualBellMessage,
                "themeKey" to config.themeKey,
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            docRef.update(payload).await()
        } else {
            val payload = mapOf(
                "userId" to uid,
                "schoolName" to config.schoolName,
                "schoolMotto" to config.schoolMotto,
                "logoUrl" to config.logoUrl,
                "videoUrl" to config.videoUrl,
                "videoTitle" to config.videoTitle,
                "runningText" to config.runningText,
                "manualBellTrigger" to config.manualBellTrigger,
                "manualBellMessage" to config.manualBellMessage,
                "themeKey" to config.themeKey,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            docRef.set(payload).await()
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "schools/${auth.currentUser?.uid}")
        Result.failure(e)
    }

    suspend fun triggerManualBell(message: String): Result<Unit> = try {
        val uid = requireUserId()
        val docRef = db.collection("schools").document(uid)
        val existing = docRef.get().await()
        val newTrigger = (existing.getLong("manualBellTrigger") ?: 0L) + 1L

        if (existing.exists()) {
            docRef.update(
                mapOf(
                    "manualBellTrigger" to newTrigger,
                    "manualBellMessage" to message,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } else {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "schoolName" to "SMA Unggulan Nusantara",
                    "manualBellTrigger" to newTrigger,
                    "manualBellMessage" to message,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, "schools/${auth.currentUser?.uid}")
        Result.failure(e)
    }

    fun observeSchedules(): Flow<List<BellSchedule>> = flow {
        val uid = requireUserId()
        val path = "schools/$uid/schedules"
        emitAll(
            db.collection("schools").document(uid).collection("schedules")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(BellSchedule::class.java).sortedBy { it.order }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveSchedule(schedule: BellSchedule): Result<String> = try {
        val uid = requireUserId()
        val colRef = db.collection("schools").document(uid).collection("schedules")

        val docId = if (schedule.id.isNotBlank()) schedule.id else colRef.document().id
        val docRef = colRef.document(docId)
        val existing = docRef.get().await()

        if (existing.exists()) {
            val payload = mapOf(
                "periodName" to schedule.periodName,
                "startTime" to schedule.startTime,
                "endTime" to schedule.endTime,
                "dayOfWeek" to schedule.dayOfWeek,
                "bellType" to schedule.bellType,
                "order" to schedule.order,
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.update(payload).await()
        } else {
            val payload = mapOf(
                "schoolId" to uid,
                "periodName" to schedule.periodName,
                "startTime" to schedule.startTime,
                "endTime" to schedule.endTime,
                "dayOfWeek" to schedule.dayOfWeek,
                "bellType" to schedule.bellType,
                "order" to schedule.order,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
        }
        Result.success(docId)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "schools/${auth.currentUser?.uid}/schedules")
        Result.failure(e)
    }

    suspend fun deleteSchedule(scheduleId: String): Result<Unit> = try {
        val uid = requireUserId()
        val docRef = db.collection("schools").document(uid).collection("schedules").document(scheduleId)
        docRef.delete().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, "schools/${auth.currentUser?.uid}/schedules/$scheduleId")
        Result.failure(e)
    }

    fun observeAchievements(): Flow<List<Achievement>> = flow {
        val uid = requireUserId()
        val path = "schools/$uid/achievements"
        emitAll(
            db.collection("schools").document(uid).collection("achievements")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Achievement::class.java)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveAchievement(achievement: Achievement): Result<String> = try {
        val uid = requireUserId()
        val colRef = db.collection("schools").document(uid).collection("achievements")

        val docId = if (achievement.id.isNotBlank()) achievement.id else colRef.document().id
        val docRef = colRef.document(docId)
        val existing = docRef.get().await()

        if (existing.exists()) {
            val payload = mapOf(
                "studentName" to achievement.studentName,
                "title" to achievement.title,
                "category" to achievement.category,
                "photoUrl" to achievement.photoUrl,
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.update(payload).await()
        } else {
            val payload = mapOf(
                "schoolId" to uid,
                "studentName" to achievement.studentName,
                "title" to achievement.title,
                "category" to achievement.category,
                "photoUrl" to achievement.photoUrl,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
        }
        Result.success(docId)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "schools/${auth.currentUser?.uid}/achievements")
        Result.failure(e)
    }

    suspend fun deleteAchievement(achievementId: String): Result<Unit> = try {
        val uid = requireUserId()
        val docRef = db.collection("schools").document(uid).collection("achievements").document(achievementId)
        docRef.delete().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, "schools/${auth.currentUser?.uid}/achievements/$achievementId")
        Result.failure(e)
    }

    fun observeVideos(): Flow<List<SchoolVideo>> = flow {
        val uid = requireUserId()
        val path = "schools/$uid/videos"
        emitAll(
            db.collection("schools").document(uid).collection("videos")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(SchoolVideo::class.java).sortedBy { it.order }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveVideo(video: SchoolVideo): Result<String> = try {
        val uid = requireUserId()
        val colRef = db.collection("schools").document(uid).collection("videos")

        val docId = if (video.id.isNotBlank()) video.id else colRef.document().id
        val docRef = colRef.document(docId)
        val existing = docRef.get().await()

        if (existing.exists()) {
            val payload = mapOf(
                "title" to video.title,
                "videoUrl" to video.videoUrl,
                "description" to video.description,
                "order" to video.order,
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.update(payload).await()
        } else {
            val payload = mapOf(
                "schoolId" to uid,
                "title" to video.title,
                "videoUrl" to video.videoUrl,
                "description" to video.description,
                "order" to video.order,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
        }
        Result.success(docId)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "schools/${auth.currentUser?.uid}/videos")
        Result.failure(e)
    }

    suspend fun deleteVideo(videoId: String): Result<Unit> = try {
        val uid = requireUserId()
        val docRef = db.collection("schools").document(uid).collection("videos").document(videoId)
        docRef.delete().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, "schools/${auth.currentUser?.uid}/videos/$videoId")
        Result.failure(e)
    }

    suspend fun seedDefaultDataIfEmpty(): Result<Unit> = try {
        val uid = requireUserId()
        val docRef = db.collection("schools").document(uid)
        val snap = docRef.get().await()
        if (!snap.exists()) {
            saveSchoolConfig(
                SchoolConfig(
                    userId = uid,
                    schoolName = "SMA Unggulan Nusantara",
                    schoolMotto = "Berkarakter, Berprestasi, dan Berwawasan Global",
                    logoUrl = "", // Empty indicates default local emblem asset
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    videoTitle = "Dokumentasi Prestasi & Inovasi Siswa",
                    runningText = "Selamat datang di SMA Unggulan Nusantara • Ujian Tengah Semester dimulai Senin pekan depan • Tingkatkan kedisiplinan dan raih prestasi gemilang!"
                )
            )

            // Seed default videos
            val defaultVideos = listOf(
                SchoolVideo(
                    title = "Dokumentasi Inovasi Robotika Siswa 2026",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    description = "Karya riset dan robotika siswa berprestasi tingkat nasional",
                    order = 1L
                ),
                SchoolVideo(
                    title = "Profil Kegiatan Ekstrakurikuler Seni & Olahraga",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    description = "Pengembangan bakat, kreativitas, dan sportivitas murid",
                    order = 2L
                ),
                SchoolVideo(
                    title = "Peringatan Hari Guru & Apresiasi Sekolah",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    description = "Dokumentasi upacara, persembahan siswa, dan penghargaan guru",
                    order = 3L
                )
            )
            for (vid in defaultVideos) {
                saveVideo(vid)
            }

            // Seed default schedules
            val defaultSchedules = listOf(
                BellSchedule(periodName = "Bel Masuk Sekolah", startTime = "07:00", endTime = "07:15", bellType = "MASUK", order = 1),
                BellSchedule(periodName = "Jam Pelajaran Ke-1", startTime = "07:15", endTime = "08:00", bellType = "PERGANTIAN", order = 2),
                BellSchedule(periodName = "Jam Pelajaran Ke-2", startTime = "08:00", endTime = "08:45", bellType = "PERGANTIAN", order = 3),
                BellSchedule(periodName = "Jam Pelajaran Ke-3", startTime = "08:45", endTime = "09:30", bellType = "PERGANTIAN", order = 4),
                BellSchedule(periodName = "Istirahat Pertama", startTime = "09:30", endTime = "10:00", bellType = "ISTIRAHAT", order = 5),
                BellSchedule(periodName = "Jam Pelajaran Ke-4", startTime = "10:00", endTime = "10:45", bellType = "PERGANTIAN", order = 6),
                BellSchedule(periodName = "Jam Pelajaran Ke-5", startTime = "10:45", endTime = "11:30", bellType = "PERGANTIAN", order = 7),
                BellSchedule(periodName = "Istirahat Siang & Sholat", startTime = "11:30", endTime = "12:30", bellType = "ISTIRAHAT", order = 8),
                BellSchedule(periodName = "Jam Pelajaran Ke-6", startTime = "12:30", endTime = "13:15", bellType = "PERGANTIAN", order = 9),
                BellSchedule(periodName = "Jam Pelajaran Ke-7", startTime = "13:15", endTime = "14:00", bellType = "PERGANTIAN", order = 10),
                BellSchedule(periodName = "Bel Pulang Sekolah", startTime = "14:00", endTime = "14:15", bellType = "PULANG", order = 11)
            )

            for (sched in defaultSchedules) {
                saveSchedule(sched)
            }

            // Seed default achievement
            saveAchievement(
                Achievement(
                    studentName = "Siti Nurhayati & Tim Robotika",
                    title = "Juara 1 Lomba Inovasi Teknologi & Robotika Nasional 2026",
                    category = "Sains & Teknologi",
                    photoUrl = ""
                )
            )
            saveAchievement(
                Achievement(
                    studentName = "Ahmad Zaki Pratama",
                    title = "Medali Emas Olimpiade Sains Nasional (OSN) Bidang Fisika",
                    category = "Akademik",
                    photoUrl = ""
                )
            )
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "schools/${auth.currentUser?.uid}")
        Result.failure(e)
    }
}
