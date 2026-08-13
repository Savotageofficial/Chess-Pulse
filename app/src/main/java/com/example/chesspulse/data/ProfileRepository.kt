package com.example.chesspulse.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ProfileRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()  // For UID


    // CREATE PROFILES with Firebase ID

    fun createUser(user: UserProfile, onDone: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Use Firebase Auth UID as document ID
            val patientWithId = user.copy(id = currentUser.uid)
            db.collection("patients")
                .document(currentUser.uid)
                .set(patientWithId)
                .addOnSuccessListener { onDone(true) }
                .addOnFailureListener { onDone(false) }
        } else {
            onDone(false)
        }
    }

    // GET PROFILES

    fun getCurrentUser(onResult: (UserProfile?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { snap ->
                    onResult(snap.toObject(UserProfile::class.java))
                }
                .addOnFailureListener { onResult(null) }
        } else {
            onResult(null)
        }
    }

    // Get by specific ID (for viewing other profiles)
    fun getUserById(id: String, onResult: (UserProfile?) -> Unit) {
        db.collection("patients")
            .document(id)
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.toObject(UserProfile::class.java))
            }
            .addOnFailureListener { onResult(null) }
    }

    //     UPDATE  PROFILES
    fun updateCurrentUser(data: Map<String, Any>, onDone: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .update(data)
                .addOnSuccessListener { onDone(true) }
                .addOnFailureListener { onDone(false) }
        } else {
            onDone(false)
        }
    }


    fun compressImageToBase64(
        context: Context,
        imageUri: Uri,
        maxDimension: Int = 300,
        quality: Int = 50
    ): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Scale down while preserving aspect ratio
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = maxDimension.toFloat() / maxOf(width, height)

            val scaledBitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (width * scale).toInt(),
                    (height * scale).toInt(),
                    true
                )
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()

            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }



    // Upload profile image
    fun uploadProfileImage(
        userId: String,
        base64Image: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {



        db.collection("users")
            .document(userId)
            .update("profileImageBase64", base64Image)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to upload image")
            }
    }

    // Delete profile image
    fun deleteProfileImage(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {

        db.collection("users")
            .document(userId)
            .update("profileImageBase64", null)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to delete image")
            }
    }


    fun getUserProfileImage(userId: String, onResult: (String?) -> Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(UserProfile::class.java)
                onResult(user?.profileImageBase64)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }


    fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        // Singleton instance
        private var instance: ProfileRepository? = null

        fun getInstance(): ProfileRepository {
            return instance ?: synchronized(this) {
                instance ?: ProfileRepository().also { instance = it } // create ProfileRepository
            }
        }
    }
}