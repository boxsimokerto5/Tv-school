package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class SchoolConfig(
    @DocumentId val id: String = "",
    val userId: String = "",
    val schoolName: String = "",
    val schoolMotto: String = "",
    val logoUrl: String = "",
    val videoUrl: String = "",
    val videoTitle: String = "",
    val runningText: String = "",
    val manualBellTrigger: Long = 0L,
    val manualBellMessage: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BellSchedule(
    @DocumentId val id: String = "",
    val schoolId: String = "",
    val periodName: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val dayOfWeek: String = "ALL",
    val bellType: String = "MASUK", // MASUK, PERGANTIAN, ISTIRAHAT, PULANG
    val order: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Achievement(
    @DocumentId val id: String = "",
    val schoolId: String = "",
    val studentName: String = "",
    val title: String = "",
    val category: String = "Akademik",
    val photoUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SchoolVideo(
    @DocumentId val id: String = "",
    val schoolId: String = "",
    val title: String = "",
    val videoUrl: String = "",
    val description: String = "",
    val order: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

