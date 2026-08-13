package com.example.chesspulse.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    open val id: String = "",
    open val name: String = "",
    open val email: String = "",
    open val msgHistory: List<String> = listOf<String>(),
    open val profileImageBase64: String? = null
): Parcelable