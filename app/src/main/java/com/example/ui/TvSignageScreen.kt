package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Achievement
import com.example.data.model.BellSchedule
import com.example.data.model.SchoolConfig
import com.example.data.model.SchoolVideo
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Public TV preview shown when app first opens without requiring immediate login (like YouTube).
 */
@Composable
fun PublicTvPreviewScreen(
    viewModel: SchoolViewModel,
    onOpenLogin: () -> Unit
) {
    val schoolConfig by viewModel.schoolConfig.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val currentVideoIndex by viewModel.currentVideoIndex.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTimeStr.collectAsStateWithLifecycle()
    val currentDate by viewModel.currentDateStr.collectAsStateWithLifecycle()
    val currentPeriod by viewModel.currentPeriod.collectAsStateWithLifecycle()
    val activeBellAlert by viewModel.activeBellAlert.collectAsStateWithLifecycle()

    val config = schoolConfig ?: SchoolConfig(
        schoolName = "SMA Unggulan Nusantara",
        schoolMotto = "Berkarakter, Berprestasi, dan Berwawasan Global",
        runningText = "Selamat datang di TV Informasi & Bel Sekolah • Tekan tombol 'Setting TV / Login' di kanan atas untuk menyesuaikan nama sekolah, logo, video, jadwal pelajaran, dan pengumuman."
    )

    TvSignageContent(
        config = config,
        videos = if (videos.isNotEmpty()) videos else viewModel.defaultVideosList,
        currentVideoIndex = currentVideoIndex,
        onNextVideo = { viewModel.nextVideo() },
        onPrevVideo = { viewModel.previousVideo() },
        schedules = if (schedules.isNotEmpty()) schedules else viewModel.defaultSchedulesList,
        achievements = if (achievements.isNotEmpty()) achievements else viewModel.defaultAchievementsList,
        currentTime = currentTime,
        currentDate = currentDate,
        currentPeriod = currentPeriod,
        activeBellAlert = activeBellAlert,
        onSettingsOrLoginClick = onOpenLogin,
        settingsButtonLabel = "Setting TV / Login",
        onDismissAlert = { viewModel.dismissBellAlert() },
        showBackButton = false
    )
}


/**
 * Authenticated TV screen reflecting real-time Firestore database synchronization.
 */
@Composable
fun TvSignageScreen(
    viewModel: SchoolViewModel,
    onBackToPhoneMode: () -> Unit,
    isPreviewMode: Boolean = false
) {
    val schoolConfig by viewModel.schoolConfig.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val currentVideoIndex by viewModel.currentVideoIndex.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTimeStr.collectAsStateWithLifecycle()
    val currentDate by viewModel.currentDateStr.collectAsStateWithLifecycle()
    val currentPeriod by viewModel.currentPeriod.collectAsStateWithLifecycle()
    val activeBellAlert by viewModel.activeBellAlert.collectAsStateWithLifecycle()

    val config = schoolConfig ?: SchoolConfig(
        schoolName = "SMA Unggulan Nusantara",
        schoolMotto = "Berkarakter, Berprestasi, dan Berwawasan Global",
        runningText = "Selamat datang di TV Informasi & Bel Sekolah • Pantau jadwal pelajaran dan pengumuman kegiatan siswa terkini."
    )

    TvSignageContent(
        config = config,
        videos = if (videos.isNotEmpty()) videos else viewModel.defaultVideosList,
        currentVideoIndex = currentVideoIndex,
        onNextVideo = { viewModel.nextVideo() },
        onPrevVideo = { viewModel.previousVideo() },
        schedules = schedules,
        achievements = achievements,
        currentTime = currentTime,
        currentDate = currentDate,
        currentPeriod = currentPeriod,
        activeBellAlert = activeBellAlert,
        onSettingsOrLoginClick = onBackToPhoneMode,
        settingsButtonLabel = if (isPreviewMode) "Kembali ke HP" else "Pengaturan TV",
        onDismissAlert = { viewModel.dismissBellAlert() },
        showBackButton = isPreviewMode,
        onBackClick = onBackToPhoneMode
    )
}

@Composable
fun TvSignageContent(
    config: SchoolConfig,
    videos: List<SchoolVideo> = emptyList(),
    currentVideoIndex: Int = 0,
    onNextVideo: () -> Unit = {},
    onPrevVideo: () -> Unit = {},
    schedules: List<BellSchedule>,
    achievements: List<Achievement>,
    currentTime: String,
    currentDate: String,
    currentPeriod: BellSchedule?,
    activeBellAlert: BellAlert?,
    onSettingsOrLoginClick: () -> Unit,
    settingsButtonLabel: String,
    onDismissAlert: () -> Unit,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tv_signage_screen"),
        color = Color(0xFFF1F5F9) // Light theme background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {

                // 1. TOP HEADER BAR
                TvHeaderBar(
                    config = config,
                    currentTime = currentTime,
                    currentDate = currentDate,
                    currentPeriod = currentPeriod,
                    showBackButton = showBackButton,
                    onBackClick = onBackClick,
                    onSettingsOrLoginClick = onSettingsOrLoginClick,
                    settingsButtonLabel = settingsButtonLabel
                )

                // 2. MAIN SPLIT CONTENT AREA
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Pane: Video Documentation & Today's Schedule Timeline
                    TvLeftPane(
                        modifier = Modifier.weight(1.1f),
                        config = config,
                        videos = videos,
                        currentVideoIndex = currentVideoIndex,
                        onNextVideo = onNextVideo,
                        onPrevVideo = onPrevVideo,
                        schedules = schedules,
                        currentPeriod = currentPeriod
                    )

                    // Right Pane: Student Achievements Slide Carousel
                    TvRightPane(
                        modifier = Modifier.weight(0.9f),
                        achievements = achievements
                    )
                }

                // 3. BOTTOM MARQUEE RUNNING TEXT TICKER
                TvRunningTextTicker(runningText = config.runningText.ifBlank {
                    "Selamat datang di Sistem Digital Signage Sekolah • Tetap semangat dan capai prestasi terbaik hari ini!"
                })
            }

            // 4. FULLSCREEN OVERLAY MODAL FOR BELL ALERTS
            AnimatedVisibility(
                visible = activeBellAlert != null,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(targetScale = 0.85f)
            ) {
                activeBellAlert?.let { alert ->
                    TvBellAlertOverlay(
                        alert = alert,
                        onDismiss = onDismissAlert
                    )
                }
            }
        }
    }
}

@Composable
private fun TvHeaderBar(
    config: SchoolConfig,
    currentTime: String,
    currentDate: String,
    currentPeriod: BellSchedule?,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onSettingsOrLoginClick: () -> Unit,
    settingsButtonLabel: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 16.dp, end = 16.dp, bottom = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: School Logo & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF1D4ED8)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // School Logo
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF))
                        .border(2.dp, Color(0xFFD97706), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (config.logoUrl.isNotBlank()) {
                        AsyncImage(
                            model = config.logoUrl,
                            contentDescription = "Logo Sekolah",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.school_emblem_logo_1792993367263),
                            contentDescription = "Logo Sekolah Default",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = config.schoolName.ifBlank { "SMA Unggulan Nusantara" },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                letterSpacing = 0.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF047857))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE TV",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = config.schoolMotto.ifBlank { "Berkarakter, Berprestasi, dan Berwawasan Global" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Center: Current Period Status
            if (currentPeriod != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1D4ED8), Color(0xFF2563EB))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "SEDANG BERLANGSUNG",
                                color = Color(0xFFDBEAFE),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${currentPeriod.periodName} (${currentPeriod.startTime} - ${currentPeriod.endTime})",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Right: Date & Clock
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color(0xFF1D4ED8),
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                )
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Settings / Login Button (Requested by user)
            Button(
                onClick = onSettingsOrLoginClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1D4ED8),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("settings_or_login_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = settingsButtonLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TvLeftPane(
    modifier: Modifier = Modifier,
    config: SchoolConfig,
    videos: List<SchoolVideo> = emptyList(),
    currentVideoIndex: Int = 0,
    onNextVideo: () -> Unit = {},
    onPrevVideo: () -> Unit = {},
    schedules: List<BellSchedule>,
    currentPeriod: BellSchedule?
) {
    val effectiveVideos = if (videos.isNotEmpty()) videos else listOf(
        SchoolVideo(
            title = config.videoTitle.ifBlank { "Kegiatan & Prestasi Laboratorium Sains Siswa 2026" },
            videoUrl = config.videoUrl,
            description = "Tayangan berkala aktivitas belajar, karya inovasi, dan profil sekolah"
        )
    )
    val safeIndex = if (effectiveVideos.isNotEmpty()) currentVideoIndex % effectiveVideos.size else 0
    val activeVideo = effectiveVideos.getOrNull(safeIndex) ?: effectiveVideos.first()

    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Video Documentation Section (Playlist player)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.school_video_docs_1792993380792),
                    contentDescription = "Video Dokumentasi Sekolah",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Top Bar: Video Dokumentasi Tag & Playlist Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDC2626))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DOKUMENTASI SEKOLAH",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Playlist pill & switcher
                    if (effectiveVideos.size > 1) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = onPrevVideo,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Sebelumnya",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "VIDEO ${safeIndex + 1}/${effectiveVideos.size}",
                                color = Color(0xFFFDE047),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = onNextVideo,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Berikutnya",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Center Play Icon Indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Play",
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.size(52.dp)
                    )
                }

                // Bottom Video Title & Info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = activeVideo.title.ifBlank { "Kegiatan & Prestasi Laboratorium Sains Siswa 2026" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activeVideo.description.ifBlank { "Tayangan berkala aktivitas belajar, karya inovasi, dan profil sekolah" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE2E8F0)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Playlist dots indicator
                    if (effectiveVideos.size > 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            effectiveVideos.forEachIndexed { dotIndex, _ ->
                                val isSelected = dotIndex == safeIndex
                                Box(
                                    modifier = Modifier
                                        .height(4.dp)
                                        .width(if (isSelected) 16.dp else 6.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isSelected) Color(0xFFFDE047) else Color.White.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today's Lesson Schedule Timeline (Light Theme)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF1D4ED8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jadwal Pelajaran & Bel Hari Ini",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        )
                    }
                    Text(
                        text = "${schedules.size} Jam Terjadwal",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (schedules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada jadwal. Silakan atur jadwal dari HP.",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(schedules) { sched ->
                            val isCurrent = currentPeriod?.id == sched.id
                            ScheduleItemRow(schedule = sched, isCurrent = isCurrent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleItemRow(schedule: BellSchedule, isCurrent: Boolean) {
    val bgColor = if (isCurrent) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
    val borderColor = if (isCurrent) Color(0xFF2563EB) else Color(0xFFE2E8F0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(if (isCurrent) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when (schedule.bellType) {
                            "MASUK" -> Color(0xFF059669)
                            "ISTIRAHAT" -> Color(0xFFD97706)
                            "PULANG" -> Color(0xFFDC2626)
                            else -> Color(0xFF1D4ED8)
                        }
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = schedule.bellType,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = schedule.periodName,
                color = if (isCurrent) Color(0xFF1D4ED8) else Color(0xFF1E293B),
                fontSize = 12.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
            )
        }

        Text(
            text = "${schedule.startTime} - ${schedule.endTime}",
            color = if (isCurrent) Color(0xFF1D4ED8) else Color(0xFF64748B),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TvRightPane(
    modifier: Modifier = Modifier,
    achievements: List<Achievement>
) {
    var currentIndex by remember { mutableIntStateOf(0) }

    // Auto-advance carousel every 7 seconds
    LaunchedEffect(achievements.size) {
        if (achievements.isNotEmpty()) {
            while (true) {
                delay(7000L)
                currentIndex = (currentIndex + 1) % achievements.size
            }
        }
    }

    val activeAchievement = if (achievements.isNotEmpty()) {
        achievements.getOrNull(currentIndex % achievements.size)
    } else {
        Achievement(
            studentName = "Siti Nurhayati & Tim Robotika",
            title = "Juara 1 Lomba Inovasi Teknologi & Robotika Nasional 2026",
            category = "Sains & Teknologi"
        )
    }

    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Prestasi Siswa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Prestasi Siswa & Kebanggaan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Slide indicators
                if (achievements.size > 1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        achievements.forEachIndexed { idx, _ ->
                            Box(
                                modifier = Modifier
                                    .size(if (idx == currentIndex % achievements.size) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (idx == currentIndex % achievements.size) Color(0xFF1D4ED8) else Color(0xFFCBD5E1)
                                    )
                            )
                        }
                    }
                }
            }

            // Photo Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                if (activeAchievement != null && activeAchievement.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = activeAchievement.photoUrl,
                        contentDescription = activeAchievement.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.student_award_photo_1792993395217),
                        contentDescription = "Foto Prestasi Siswa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Category tag badge overlay
                activeAchievement?.let { ach ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = ach.category.ifBlank { "Prestasi" },
                            color = Color(0xFFD97706),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Achievement Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = activeAchievement?.title ?: "Juara 1 Lomba Siswa Nasional",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color(0xFF1D4ED8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activeAchievement?.studentName ?: "Siswa Berprestasi",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TvRunningTextTicker(runningText: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D4ED8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Megaphone icon with highlight
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFBBF24))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Pengumuman",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "INFO",
                        color = Color(0xFF0F172A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Marquee Animated Text Banner
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val boxWidth = maxWidth
                val density = LocalDensity.current
                val textLength = runningText.length
                val animDuration = (textLength * 180).coerceAtLeast(10000)

                val infiniteTransition = rememberInfiniteTransition(label = "marquee")
                val offsetFraction by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = -1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = animDuration, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "marquee_offset"
                )

                Box(
                    modifier = Modifier.offset {
                        IntOffset(
                            x = (with(density) { boxWidth.toPx() } * offsetFraction).toInt(),
                            y = 0
                        )
                    }
                ) {
                    Text(
                        text = runningText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun TvBellAlertOverlay(
    alert: BellAlert,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bell_swing")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ringing Bell Icon with swing animation
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Bel Berbunyi",
                        tint = Color.White,
                        modifier = Modifier
                            .size(60.dp)
                            .rotate(rotation)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "🔔 BEL SEKOLAH BERBUNYI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.ExtraBold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = alert.subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF1D4ED8),
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tutup Pemberitahuan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
