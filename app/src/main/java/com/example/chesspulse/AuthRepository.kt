package com.example.chesspulse

import android.util.Log
import com.example.chesspulse.data.ProfileRepository
import com.example.chesspulse.data.UserProfile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()




    fun getCurrentUserID() : String?{
        return auth.currentUser?.uid
    }
    fun createAccount(
        email: String,
        password: String,
        name: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        courses : Map<String, List<String>> = emptyMap()
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userData = hashMapOf<String, Any?>(
                            "name" to name,
                            "email" to email,
                            "courses" to courses
                        )

                        db.collection("users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                // Create profile document based on user type

                                createUserProfile(user.uid, name, email)
                                sendEmailVerification(onSuccess, onFailure)
                            }
                            .addOnFailureListener { exception ->
                                onFailure(exception.message ?: "Failed to save user data")
                            }
                    } else {
                        onFailure("Failed to create user")
                    }
                } else {
                    onFailure(task.exception?.message ?: "Sign up failed")
                }
            }
    }



    private fun createUserProfile(uid: String, name: String, email: String) {
        val User = UserProfile(
            id = uid,
            name = name,
            email = email,
        )

        ProfileRepository.getInstance().createUser(User) { success ->
            if (success) {
                Log.d("AuthRepo", "User profile created successfully for: $uid")
            } else {
                Log.e("AuthRepo", "Failed to create patient profile for: $uid")
            }
        }
    }
    suspend fun getCurrentUserChapters(courseId: String): List<String> {
        val currentUser = auth.currentUser ?: return emptyList()
        return try {
            val document = db.collection("users").document(currentUser.uid).get().await()

            @Suppress("UNCHECKED_CAST")
            val coursesMap = document.get("courses") as? Map<String, List<String>> ?: emptyMap()

            // Return only chapters for the requested courseId
            coursesMap[courseId] ?: emptyList()
        } catch (e: Exception) {
            Log.e("GetChapters", "Error fetching chapters for $courseId", e)
            emptyList()
        }
    }


    suspend fun addChapterToCurrentUser(courseId: String, chapter: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            val userDocRef = db.collection("users").document(currentUser.uid)

            // "courses.$courseId" targets the specific key in the user's courses map field
            userDocRef.update("courses.$courseId", FieldValue.arrayUnion(chapter)).await()
            true
        } catch (e: Exception) {
            Log.e("AddChapter", "Failed to add chapter $chapter to course $courseId", e)
            false
        }
    }

    // Email verification
    private fun sendEmailVerification(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("Failed to send verification email")
                }
            } ?: onFailure("User not available")
    }

    // Sign in
    fun signIn(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser?.isEmailVerified == true) {
                        db.collection("users").document(currentUser.uid).get()
                            .addOnSuccessListener { document ->
                                val userType = document.getString("userType") ?: "Patient"
                                onSuccess(userType)
                            }
                            .addOnFailureListener {
                                onFailure("Couldn't get your info")
                            }
                    } else {
                        onFailure("Please verify your email")
                    }
                } else {
                    onFailure(task.exception?.message ?: "Sign in failed")
                }
            }
    }

    // Reset password
    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.message ?: "Failed to send reset password email")
                }
            }
    }

    // Check if user signed in
    fun isUserSignedIn(): Boolean {
        val currentUser = auth.currentUser
        return currentUser != null && currentUser.isEmailVerified
    }

    // Get current user type
    fun getCurrentUserType(onResult: (String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    onResult(document.getString("userType"))
                }
                .addOnFailureListener {
                    onResult(null)
                }
        } else {
            onResult(null)
        }
    }

    // Logout
    fun logout() {
        auth.signOut()
    }
}