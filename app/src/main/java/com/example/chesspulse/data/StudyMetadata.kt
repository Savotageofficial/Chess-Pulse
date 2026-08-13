package com.example.chesspulse.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StudyMetadata(
    val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
): Parcelable