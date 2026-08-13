package com.example.chesspulse

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chesspulse.data.ProfileRepository
import com.example.chesspulse.ui.theme.ChessPulseTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessPulseTheme {
                    SettingsScreen(
                        onLogout = {
                            // Navigate back to login
                            val intent = Intent(this, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        }

                    )
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier , onLogout: () -> Unit) {
    val authRepo = remember { AuthRepository() }
    val profileRepo = remember { ProfileRepository() }

    val userId = authRepo.getCurrentUserID()


    val context = LocalContext.current
    var profileImageBase64 by remember { mutableStateOf<String?>(null) }

    profileRepo.getUserProfileImage(userId ?: "" , { image ->
        profileImageBase64 = image
    }
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64Image = profileRepo.compressImageToBase64(context, uri)
            if (base64Image != null) {
                profileRepo.uploadProfileImage(
                    userId = userId ?: "",
                    base64Image = base64Image,
                    onSuccess = { profileImageBase64 = base64Image },
                    onFailure = { /* show a toast if you want */ }
                )
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        // Show current image, or a placeholder
        val bitmap = profileImageBase64?.let { profileRepo.base64ToBitmap(it) }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .clickable {
                        imagePickerLauncher.launch("image/*")
                    }
            )
        } else {
            Image(
                painter = painterResource(R.drawable.blank_profile_picture),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable {
                        imagePickerLauncher.launch("image/*")
                    }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        var name by remember { mutableStateOf("") }
        
        LaunchedEffect(Unit) {
            profileRepo.getCurrentUser { user ->
                name = user?.name ?: ""
            }
        }
        var hasFocus by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.onFocusChanged {
                if (hasFocus && !it.isFocused) {
                    profileRepo.updateCurrentUser(
                        mapOf("name" to name),
                        onDone = {}
                    )
                }
                hasFocus = it.isFocused
            }
        )

        Button(
            onClick = {
                authRepo.logout()
                onLogout()
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE5E5)),
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .height(55.dp)

        ) {
            Text(
                text = "Log Out",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }
    }


}


@Preview(showSystemUi = true)
@Composable
fun preview(){
    ChessPulseTheme {
        SettingsScreen(onLogout = {})
    }
}