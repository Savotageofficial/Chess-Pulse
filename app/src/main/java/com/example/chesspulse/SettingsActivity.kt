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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import com.example.chesspulse.data.StudyRepository
import com.example.chesspulse.CourseGroup
import com.example.chesspulse.groupStudiesByBaseName
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

        Spacer(modifier = Modifier.height(16.dp))

        UserCourseProgressSection()

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


@Composable
fun UserCourseProgressSection(modifier: Modifier = Modifier) {
    val authRepo = remember { AuthRepository() }
    val studyRepo = remember { StudyRepository() }
    var courseGroups by remember { mutableStateOf<List<CourseGroup>>(emptyList()) }
    var chapterTotals by remember { mutableStateOf<Map<String, Int>>(emptyMap()) } // partId -> total chapters
    var completedChapters by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) } // partId -> done chapter ids
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val studies = studyRepo.fetchStudiesMetadata("Mrbullet5")
            courseGroups = groupStudiesByBaseName(studies)
            completedChapters = authRepo.getCurrentUserCourses()
            // Total chapter counts per part (cached network calls)
            val totals = mutableMapOf<String, Int>()
            for (group in courseGroups) {
                for (part in group.parts) {
                    if (totals.containsKey(part.id)) continue
                    totals[part.id] = try {
                        studyRepo.fetchChapters(part.id, part.name).size
                    } catch (e: Exception) {
                        0
                    }
                }
            }
            chapterTotals = totals
        } catch (e: Exception) {
            // keep defaults; UI shows an empty state
        } finally {
            isLoading = false
        }
    }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(
            text = "My Courses",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF361F1A)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (courseGroups.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                Text(text = "No courses yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.height(240.dp)) {
                items(courseGroups, key = { it.baseName }) { group ->
                    CourseProgressCard(
                        group = group,
                        completedChapters = completedChapters,
                        chapterTotals = chapterTotals
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun CourseProgressCard(
    group: CourseGroup,
    completedChapters: Map<String, List<String>>,
    chapterTotals: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    // Progress = completed chapters across all parts / total chapters across all parts
    val done = group.parts.sumOf { completedChapters[it.id]?.size ?: 0 }
    val total = maxOf(group.parts.sumOf { chapterTotals[it.id] ?: 0 }, 1)
    val progress = (done.toFloat() / total).coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Course logo
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF4E342E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.chess_knight_24px),
                contentDescription = null,
                tint = Color(0xFFC19C94),
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = group.baseName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF361F1A),
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    text = "$percent%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E342E)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4E342E),
                trackColor = Color(0xFFE5D5CC)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$done of $total chapters",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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