package com.example.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Achievement
import com.example.data.model.BellSchedule
import com.example.data.model.SchoolConfig
import com.example.data.model.SchoolVideo
import com.example.data.repository.SchoolRepository
import com.example.util.SchoolBellPlayer
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppMode {
    AUTO,
    TV_SIGNAGE,
    ADMIN_CONTROLLER,
    TV_PREVIEW
}

data class BellAlert(
    val title: String,
    val subtitle: String,
    val bellType: String,
    val timestamp: Long = System.currentTimeMillis()
)

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SchoolRepository(application.applicationContext)
    val bellPlayer = SchoolBellPlayer(application.applicationContext)
    private val prefs: SharedPreferences = application.getSharedPreferences("edubell_school_prefs", Context.MODE_PRIVATE)

    val defaultSchedulesList = listOf(
        BellSchedule(id = "1", schoolId = "admin_school", periodName = "Bel Masuk Sekolah", startTime = "07:00", endTime = "07:15", bellType = "MASUK", order = 1L),
        BellSchedule(id = "2", schoolId = "admin_school", periodName = "Jam Pelajaran Ke-1", startTime = "07:15", endTime = "08:00", bellType = "PERGANTIAN", order = 2L),
        BellSchedule(id = "3", schoolId = "admin_school", periodName = "Jam Pelajaran Ke-2", startTime = "08:00", endTime = "08:45", bellType = "PERGANTIAN", order = 3L),
        BellSchedule(id = "4", schoolId = "admin_school", periodName = "Jam Pelajaran Ke-3", startTime = "08:45", endTime = "09:30", bellType = "PERGANTIAN", order = 4L),
        BellSchedule(id = "5", schoolId = "admin_school", periodName = "Istirahat Pertama", startTime = "09:30", endTime = "10:00", bellType = "ISTIRAHAT", order = 5L),
        BellSchedule(id = "6", schoolId = "admin_school", periodName = "Jam Pelajaran Ke-4", startTime = "10:00", endTime = "10:45", bellType = "PERGANTIAN", order = 6L),
        BellSchedule(id = "7", schoolId = "admin_school", periodName = "Jam Pelajaran Ke-5", startTime = "10:45", endTime = "11:30", bellType = "PERGANTIAN", order = 7L),
        BellSchedule(id = "8", schoolId = "admin_school", periodName = "Istirahat Siang & Sholat", startTime = "11:30", endTime = "12:30", bellType = "ISTIRAHAT", order = 8L),
        BellSchedule(id = "9", schoolId = "admin_school", periodName = "Jam Pelajaran Ke-6", startTime = "12:30", endTime = "13:15", bellType = "PERGANTIAN", order = 9L),
        BellSchedule(id = "10", schoolId = "admin_school", periodName = "Jam Pelajaran Ke-7", startTime = "13:15", endTime = "14:00", bellType = "PERGANTIAN", order = 10L),
        BellSchedule(id = "11", schoolId = "admin_school", periodName = "Bel Pulang Sekolah", startTime = "14:00", endTime = "14:15", bellType = "PULANG", order = 11L)
    )

    val defaultAchievementsList = listOf(
        Achievement(
            id = "1",
            schoolId = "admin_school",
            studentName = "Siti Nurhayati & Tim Robotika",
            title = "Juara 1 Lomba Inovasi Teknologi & Robotika Nasional 2026",
            category = "Sains & Teknologi",
            photoUrl = ""
        ),
        Achievement(
            id = "2",
            schoolId = "admin_school",
            studentName = "Ahmad Zaki Pratama",
            title = "Medali Emas Olimpiade Sains Nasional (OSN) Bidang Fisika",
            category = "Akademik",
            photoUrl = ""
        )
    )

    val defaultVideosList = listOf(
        SchoolVideo(
            id = "1",
            schoolId = "admin_school",
            title = "Dokumentasi Inovasi Robotika Siswa 2026",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            description = "Karya riset dan robotika siswa berprestasi tingkat nasional",
            order = 1L
        ),
        SchoolVideo(
            id = "2",
            schoolId = "admin_school",
            title = "Profil Kegiatan Ekstrakurikuler Seni & Olahraga",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            description = "Pengembangan bakat, kreativitas, dan sportivitas murid",
            order = 2L
        ),
        SchoolVideo(
            id = "3",
            schoolId = "admin_school",
            title = "Peringatan Hari Guru & Apresiasi Sekolah",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            description = "Dokumentasi upacara, persembahan siswa, dan penghargaan guru",
            order = 3L
        )
    )

    private val _schoolConfig = MutableStateFlow<SchoolConfig?>(loadSavedConfig())
    val schoolConfig: StateFlow<SchoolConfig?> = _schoolConfig.asStateFlow()

    private val _videos = MutableStateFlow<List<SchoolVideo>>(loadSavedVideos())
    val videos: StateFlow<List<SchoolVideo>> = _videos.asStateFlow()

    private val _currentVideoIndex = MutableStateFlow(0)
    val currentVideoIndex: StateFlow<Int> = _currentVideoIndex.asStateFlow()

    private val _schedules = MutableStateFlow<List<BellSchedule>>(loadSavedSchedules())
    val schedules: StateFlow<List<BellSchedule>> = _schedules.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(loadSavedAchievements())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _appMode = MutableStateFlow(AppMode.AUTO)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    private val _currentTimeStr = MutableStateFlow("00:00:00")
    val currentTimeStr: StateFlow<String> = _currentTimeStr.asStateFlow()

    private val _currentDateStr = MutableStateFlow("")
    val currentDateStr: StateFlow<String> = _currentDateStr.asStateFlow()

    private val _currentPeriod = MutableStateFlow<BellSchedule?>(null)
    val currentPeriod: StateFlow<BellSchedule?> = _currentPeriod.asStateFlow()

    private val _nextPeriod = MutableStateFlow<BellSchedule?>(null)
    val nextPeriod: StateFlow<BellSchedule?> = _nextPeriod.asStateFlow()

    private val _activeBellAlert = MutableStateFlow<BellAlert?>(null)
    val activeBellAlert: StateFlow<BellAlert?> = _activeBellAlert.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private var lastObservedManualBell: Long? = null
    private var lastRungTimeKey: String = ""

    init {
        // Initialize clock & scheduler ticker
        startClockAndScheduleChecker()

        // Observe cloud data if user is signed into Firebase Auth
        if (Firebase.auth.currentUser != null) {
            observeCloudData()
        }

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                observeCloudData()
            }
        }

        // Realtime manual bell trigger observer
        viewModelScope.launch {
            schoolConfig.collect { config ->
                if (config != null) {
                    val trigger = config.manualBellTrigger
                    if (lastObservedManualBell == null) {
                        lastObservedManualBell = trigger
                    } else if (trigger > (lastObservedManualBell ?: 0L)) {
                        lastObservedManualBell = trigger
                        val message = config.manualBellMessage.ifBlank { "Bel Sekolah Dering Manual" }
                        triggerBellLocally("MANUAL", "BEL MANUAL BERBUNYI", message)
                    }
                }
            }
        }
    }

    private fun observeCloudData() {
        viewModelScope.launch {
            try {
                repository.seedDefaultDataIfEmpty()
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            repository.observeSchoolConfig()
                .catch { /* fallback to local memory */ }
                .collect { cloudConfig ->
                    if (cloudConfig != null) {
                        _schoolConfig.value = cloudConfig
                        persistConfig(cloudConfig)
                    }
                }
        }
        viewModelScope.launch {
            repository.observeSchedules()
                .catch { /* fallback to local memory */ }
                .collect { cloudSchedules ->
                    if (cloudSchedules.isNotEmpty()) {
                        _schedules.value = cloudSchedules
                        persistSchedules(cloudSchedules)
                    }
                }
        }
        viewModelScope.launch {
            repository.observeAchievements()
                .catch { /* fallback to local memory */ }
                .collect { cloudAchievements ->
                    if (cloudAchievements.isNotEmpty()) {
                        _achievements.value = cloudAchievements
                        persistAchievements(cloudAchievements)
                    }
                }
        }
        viewModelScope.launch {
            repository.observeVideos()
                .catch { /* fallback to local memory */ }
                .collect { cloudVideos ->
                    if (cloudVideos.isNotEmpty()) {
                        _videos.value = cloudVideos
                        persistVideos(cloudVideos)
                    }
                }
        }
    }

    private fun loadSavedConfig(): SchoolConfig {
        val name = prefs.getString("school_name", "SMA Unggulan Nusantara") ?: "SMA Unggulan Nusantara"
        val motto = prefs.getString("school_motto", "Berkarakter, Berprestasi, dan Berwawasan Global") ?: "Berkarakter, Berprestasi, dan Berwawasan Global"
        val logo = prefs.getString("logo_url", "") ?: ""
        val video = prefs.getString("video_url", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        val vTitle = prefs.getString("video_title", "Dokumentasi Prestasi & Inovasi Siswa") ?: "Dokumentasi Prestasi & Inovasi Siswa"
        val running = prefs.getString("running_text", "Selamat datang di SMA Unggulan Nusantara • Ujian Tengah Semester dimulai Senin pekan depan • Tingkatkan kedisiplinan dan raih prestasi gemilang!") ?: "Selamat datang di SMA Unggulan Nusantara • Ujian Tengah Semester dimulai Senin pekan depan • Tingkatkan kedisiplinan dan raih prestasi gemilang!"
        val manualTrigger = prefs.getLong("manual_trigger", 0L)
        val manualMsg = prefs.getString("manual_msg", "") ?: ""

        return SchoolConfig(
            userId = "admin_school",
            schoolName = name,
            schoolMotto = motto,
            logoUrl = logo,
            videoUrl = video,
            videoTitle = vTitle,
            runningText = running,
            manualBellTrigger = manualTrigger,
            manualBellMessage = manualMsg
        )
    }

    private fun persistConfig(config: SchoolConfig) {
        prefs.edit()
            .putString("school_name", config.schoolName)
            .putString("school_motto", config.schoolMotto)
            .putString("logo_url", config.logoUrl)
            .putString("video_url", config.videoUrl)
            .putString("video_title", config.videoTitle)
            .putString("running_text", config.runningText)
            .putLong("manual_trigger", config.manualBellTrigger)
            .putString("manual_msg", config.manualBellMessage)
            .apply()
    }

    private fun loadSavedSchedules(): List<BellSchedule> {
        val raw = prefs.getString("saved_schedules", null)
        if (raw.isNullOrBlank()) {
            return defaultSchedulesList
        }
        return try {
            raw.split("###").mapNotNull { itemStr ->
                val parts = itemStr.split(";;")
                if (parts.size >= 6) {
                    BellSchedule(
                        id = parts[0],
                        schoolId = "admin_school",
                        periodName = parts[1],
                        startTime = parts[2],
                        endTime = parts[3],
                        bellType = parts[4],
                        order = parts[5].toLongOrNull() ?: 1L
                    )
                } else null
            }.sortedBy { it.order }.ifEmpty { defaultSchedulesList }
        } catch (_: Exception) {
            defaultSchedulesList
        }
    }

    private fun persistSchedules(list: List<BellSchedule>) {
        val serialized = list.joinToString("###") { s ->
            "${s.id};;${s.periodName};;${s.startTime};;${s.endTime};;${s.bellType};;${s.order}"
        }
        prefs.edit().putString("saved_schedules", serialized).apply()
    }

    private fun loadSavedAchievements(): List<Achievement> {
        val raw = prefs.getString("saved_achievements", null)
        if (raw.isNullOrBlank()) {
            return defaultAchievementsList
        }
        return try {
            raw.split("###").mapNotNull { itemStr ->
                val parts = itemStr.split(";;")
                if (parts.size >= 5) {
                    Achievement(
                        id = parts[0],
                        schoolId = "admin_school",
                        studentName = parts[1],
                        title = parts[2],
                        category = parts[3],
                        photoUrl = parts[4]
                    )
                } else null
            }.ifEmpty { defaultAchievementsList }
        } catch (_: Exception) {
            defaultAchievementsList
        }
    }

    private fun persistAchievements(list: List<Achievement>) {
        val serialized = list.joinToString("###") { a ->
            "${a.id};;${a.studentName};;${a.title};;${a.category};;${a.photoUrl}"
        }
        prefs.edit().putString("saved_achievements", serialized).apply()
    }

    private fun loadSavedVideos(): List<SchoolVideo> {
        val raw = prefs.getString("saved_videos", null)
        if (raw.isNullOrBlank()) {
            return defaultVideosList
        }
        return try {
            raw.split("###").mapNotNull { itemStr ->
                val parts = itemStr.split(";;")
                if (parts.size >= 5) {
                    SchoolVideo(
                        id = parts[0],
                        schoolId = "admin_school",
                        title = parts[1],
                        videoUrl = parts[2],
                        description = parts[3],
                        order = parts[4].toLongOrNull() ?: 1L
                    )
                } else null
            }.sortedBy { it.order }.ifEmpty { defaultVideosList }
        } catch (_: Exception) {
            defaultVideosList
        }
    }

    private fun persistVideos(list: List<SchoolVideo>) {
        val serialized = list.joinToString("###") { v ->
            "${v.id};;${v.title};;${v.videoUrl};;${v.description};;${v.order}"
        }
        prefs.edit().putString("saved_videos", serialized).apply()
    }

    private fun startClockAndScheduleChecker() {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val comparisonTimeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))

        viewModelScope.launch {
            while (true) {
                val now = Date()
                _currentTimeStr.value = timeFormat.format(now)
                _currentDateStr.value = dateFormat.format(now)
                val currentHHmm = comparisonTimeFormat.format(now)

                val schedList = schedules.value
                evaluateCurrentAndNextPeriod(currentHHmm, schedList)

                // Check automatic bell schedule matches
                val currentSecond = SimpleDateFormat("ss", Locale.US).format(now)
                if (currentSecond == "00" && currentHHmm != lastRungTimeKey) {
                    val matchingSchedule = schedList.firstOrNull { it.startTime == currentHHmm }
                    if (matchingSchedule != null) {
                        lastRungTimeKey = currentHHmm
                        triggerBellLocally(
                            matchingSchedule.bellType,
                            matchingSchedule.periodName,
                            "Waktunya: ${matchingSchedule.periodName} (${matchingSchedule.startTime} - ${matchingSchedule.endTime})"
                        )
                    }
                }

                // Auto rotate playlist video every 45s if multiple videos exist
                val currentSecInt = currentSecond.toIntOrNull() ?: 0
                if (currentSecInt % 45 == 0 && _videos.value.size > 1) {
                    _currentVideoIndex.value = (_currentVideoIndex.value + 1) % _videos.value.size
                }

                delay(1000L)
            }
        }
    }

    private fun evaluateCurrentAndNextPeriod(currentHHmm: String, list: List<BellSchedule>) {
        if (list.isEmpty()) {
            _currentPeriod.value = null
            _nextPeriod.value = null
            return
        }

        val active = list.firstOrNull { sched ->
            currentHHmm >= sched.startTime && currentHHmm < sched.endTime
        }
        _currentPeriod.value = active

        val next = list.firstOrNull { sched ->
            sched.startTime > currentHHmm
        } ?: list.firstOrNull()
        _nextPeriod.value = next
    }

    fun triggerBellLocally(bellType: String, title: String, subtitle: String) {
        val alert = BellAlert(title = title, subtitle = subtitle, bellType = bellType)
        _activeBellAlert.value = alert
        bellPlayer.ringBell(
            bellType = bellType,
            announcementMessage = subtitle,
            enableVoice = true,
            scope = viewModelScope
        )

        // Auto dismiss alert modal after 15 seconds
        viewModelScope.launch {
            delay(15000L)
            if (_activeBellAlert.value == alert) {
                _activeBellAlert.value = null
            }
        }
    }

    fun dismissBellAlert() {
        _activeBellAlert.value = null
    }

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
    }

    fun triggerManualBellCloud(customMessage: String) {
        val msg = customMessage.ifBlank { "Bel Sekolah Dering Manual" }
        triggerBellLocally("MANUAL", "BEL MANUAL BERBUNYI", msg)

        viewModelScope.launch {
            if (Firebase.auth.currentUser != null) {
                _statusMessage.value = "Mengirim sinyal bel ke cloud..."
                val result = repository.triggerManualBell(msg)
                if (result.isSuccess) {
                    _statusMessage.value = "Bel dibunyikan & sinyal cloud terkirim ke TV!"
                } else {
                    _statusMessage.value = "Bel dibunyikan lokal (Cloud: ${result.exceptionOrNull()?.message})"
                }
            } else {
                _statusMessage.value = "Bel manual berhasil dibunyikan!"
            }
        }
    }

    fun updateSchoolProfile(
        schoolName: String,
        schoolMotto: String,
        logoUrl: String,
        videoUrl: String,
        videoTitle: String,
        runningText: String,
        onComplete: (Boolean) -> Unit
    ) {
        val current = _schoolConfig.value ?: loadSavedConfig()
        val updated = current.copy(
            schoolName = schoolName,
            schoolMotto = schoolMotto,
            logoUrl = logoUrl,
            videoUrl = videoUrl,
            videoTitle = videoTitle,
            runningText = runningText
        )
        _schoolConfig.value = updated
        persistConfig(updated)

        viewModelScope.launch {
            if (Firebase.auth.currentUser != null) {
                val result = repository.saveSchoolConfig(updated)
                if (result.isSuccess) {
                    _statusMessage.value = "Profil sekolah & TV berhasil diperbarui (Tersinkron Cloud)!"
                    onComplete(true)
                } else {
                    _statusMessage.value = "Disimpan lokal (Cloud: ${result.exceptionOrNull()?.message})"
                    onComplete(true)
                }
            } else {
                _statusMessage.value = "Profil sekolah & TV berhasil diperbarui!"
                onComplete(true)
            }
        }
    }

    fun saveSchedule(schedule: BellSchedule, onComplete: (Boolean) -> Unit) {
        val currentList = _schedules.value.toMutableList()
        val id = if (schedule.id.isNotBlank()) schedule.id else UUID.randomUUID().toString().take(8)
        val itemToSave = schedule.copy(id = id)

        val existingIndex = currentList.indexOfFirst { it.id == id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = itemToSave
        } else {
            currentList.add(itemToSave)
        }
        val sorted = currentList.sortedBy { it.order }
        _schedules.value = sorted
        persistSchedules(sorted)

        viewModelScope.launch {
            if (Firebase.auth.currentUser != null) {
                val result = repository.saveSchedule(itemToSave)
                if (result.isSuccess) {
                    _statusMessage.value = "Jadwal bel tersimpan & tersinkron cloud!"
                    onComplete(true)
                } else {
                    _statusMessage.value = "Jadwal bel berhasil disimpan!"
                    onComplete(true)
                }
            } else {
                _statusMessage.value = "Jadwal bel berhasil disimpan!"
                onComplete(true)
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        val updated = _schedules.value.filterNot { it.id == scheduleId }
        _schedules.value = updated
        persistSchedules(updated)

        viewModelScope.launch {
            if (Firebase.auth.currentUser != null) {
                repository.deleteSchedule(scheduleId)
            }
            _statusMessage.value = "Jadwal berhasil dihapus."
        }
    }

    fun saveAchievement(achievement: Achievement, onComplete: (Boolean) -> Unit) {
        val currentList = _achievements.value.toMutableList()
        val id = if (achievement.id.isNotBlank()) achievement.id else UUID.randomUUID().toString().take(8)
        val itemToSave = achievement.copy(id = id)

        val existingIndex = currentList.indexOfFirst { it.id == id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = itemToSave
        } else {
            currentList.add(itemToSave)
        }
        _achievements.value = currentList
        persistAchievements(currentList)

        viewModelScope.launch {
            if (Firebase.auth.currentUser != null) {
                val result = repository.saveAchievement(itemToSave)
                if (result.isSuccess) {
                    _statusMessage.value = "Prestasi siswa tersimpan & tersinkron cloud!"
                    onComplete(true)
                } else {
                    _statusMessage.value = "Prestasi siswa berhasil disimpan!"
                    onComplete(true)
                }
            } else {
                _statusMessage.value = "Prestasi siswa berhasil disimpan!"
                onComplete(true)
            }
        }
    }

    fun deleteAchievement(achievementId: String) {
        val updated = _achievements.value.filterNot { it.id == achievementId }
        _achievements.value = updated
        persistAchievements(updated)

        viewModelScope.launch {
            if (Firebase.auth.currentUser != null) {
                repository.deleteAchievement(achievementId)
            }
            _statusMessage.value = "Prestasi siswa berhasil dihapus."
        }
    }

    fun nextVideo() {
        val list = _videos.value
        if (list.isNotEmpty()) {
            _currentVideoIndex.value = (_currentVideoIndex.value + 1) % list.size
        }
    }

    fun previousVideo() {
        val list = _videos.value
        if (list.isNotEmpty()) {
            val prev = _currentVideoIndex.value - 1
            _currentVideoIndex.value = if (prev < 0) list.size - 1 else prev
        }
    }

    fun selectVideo(index: Int) {
        val list = _videos.value
        if (index in list.indices) {
            _currentVideoIndex.value = index
        }
    }

    fun saveVideo(video: SchoolVideo, onComplete: (Boolean) -> Unit) {
        val currentList = _videos.value.toMutableList()
        val id = if (video.id.isNotBlank()) video.id else UUID.randomUUID().toString().take(8)
        val itemToSave = video.copy(id = id)

        val existingIndex = currentList.indexOfFirst { it.id == id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = itemToSave
        } else {
            currentList.add(itemToSave)
        }
        val sorted = currentList.sortedBy { it.order }
        _videos.value = sorted
        persistVideos(sorted)

        // Sync first video with config videoUrl if appropriate
        if (sorted.isNotEmpty()) {
            val first = sorted.first()
            val currentCfg = _schoolConfig.value
            if (currentCfg != null) {
                val updatedCfg = currentCfg.copy(videoUrl = first.videoUrl, videoTitle = first.title)
                _schoolConfig.value = updatedCfg
                persistConfig(updatedCfg)
            }
        }

        viewModelScope.launch {
            if (Firebase.auth.currentUser != null) {
                val result = repository.saveVideo(itemToSave)
                if (result.isSuccess) {
                    _statusMessage.value = "Video TV berhasil disimpan & tersinkron cloud!"
                    onComplete(true)
                } else {
                    _statusMessage.value = "Video TV berhasil disimpan!"
                    onComplete(true)
                }
            } else {
                _statusMessage.value = "Video TV berhasil disimpan!"
                onComplete(true)
            }
        }
    }

    fun deleteVideo(videoId: String) {
        val updated = _videos.value.filterNot { it.id == videoId }
        _videos.value = updated
        persistVideos(updated)
        if (_currentVideoIndex.value >= updated.size) {
            _currentVideoIndex.value = (updated.size - 1).coerceAtLeast(0)
        }

        viewModelScope.launch {
            if (Firebase.auth.currentUser != null) {
                repository.deleteVideo(videoId)
            }
            _statusMessage.value = "Video TV berhasil dihapus."
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        bellPlayer.release()
    }
}
