package com.example.chesspulse

import android.R.attr.bitmap
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import androidx.compose.material3.Icon
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.example.chesspulse.data.ProfileRepository
import com.example.chesspulse.data.StudyMetadata
import com.example.chesspulse.data.StudyRepository
import com.example.chesspulse.ui.theme.ChessPulseTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class CourseGroup(
    val baseName: String,
    val parts: List<StudyMetadata>
) {
    val mostRecentUpdate: Long get() = parts.maxOf { it.updatedAt }
}

fun groupStudiesByBaseName(studies: List<StudyMetadata>): List<CourseGroup> {
    return studies
        .groupBy { extractBaseName(it.name) }
        .map { (baseName, parts) ->
            CourseGroup(baseName = baseName, parts = parts.sortedBy { it.name })
        }
        .sortedByDescending { it.mostRecentUpdate }
}
class MainActivity : ComponentActivity() {




    private val repo = StudyRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessPulseTheme {
                HomeScreen(repo = repo)
            }
        }
    }
}


fun extractBaseName(name: String): String {
    return name.trim()
        .replace(Regex("""[\s]*[0-9٠-٩]+$"""), "") // strips trailing digits, Arabic or Latin
        .trim()
}
@Composable
fun HomeScreen(repo : StudyRepository, modifier: Modifier = Modifier) {
    var studies by remember { mutableStateOf<List<StudyMetadata>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        try {
            studies = repo.fetchStudiesMetadata("Mrbullet5")
        } catch (e: Exception) {
            error = "Failed to load studies"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color= Color(0xFFcfdce4))

    ) {

        Spacer(modifier= Modifier
                    .height(20.dp)
                    .fillMaxWidth()
                    .background(color = Color(0xFFFFFFFF))
        )

        header()

        Spacer(modifier= Modifier.height(20.dp))


        Column(
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Text(
                text = "Available Courses",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4E342E)
            )
        }




        Spacer(modifier= Modifier.height(20.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(error!!)
                }
            }

            else -> {
                val courseGroups = remember(studies) { groupStudiesByBaseName(studies) }
                LazyColumn(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
                ) {
                    items(courseGroups, key = { it.baseName }) { group ->
                        CourseCard(group = group,
                            modifier = Modifier.clickable{
                                val intent = Intent(context, PartSelectionActivity::class.java).apply {
                                    putExtra("group", ArrayList(group.parts))
                                    putExtra("title" , group.baseName)
                                }

                                var Myintent = context.startActivity(intent)

                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

}

@Composable
fun CourseCard(
    group: CourseGroup,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(width = 2.dp , color = Color(0xFF4E342E) ,  shape = RoundedCornerShape(20.dp))
            .background(color = Color(0xFFFFFFFF))
            .padding(16.dp)

    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF4E342E)), // bg-primary-container
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.chess_knight_24px),
                contentDescription = null,
                tint = Color(0xFFC19C94), // text-on-primary-container
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.baseName,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF361F1A) // text-primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = relativeTime(group.mostRecentUpdate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


fun relativeTime(millis: Long): String =
    DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
@Composable
private fun CourseThumbnail() {
    // simple checkerboard placeholder until you have real study images
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF5B8DB8)))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White))
            }
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF5B8DB8)))
            }
        }
    }
}
fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Composable
fun header(){

    val authRepo = remember { AuthRepository() }

    val profileRepo = remember { ProfileRepository() }
    var profileImageBase64 by remember { mutableStateOf<String?>(null) }

    val userId = authRepo.getCurrentUserID()

    profileRepo.getUserProfileImage(userId ?: "" , { image ->
        profileImageBase64 = image
    }
    )

    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(color = Color(0xFFFFFFFF))
            .padding(horizontal = 15.dp , vertical = 0.dp),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
//        Icon(
//            imageVector = Icons.Filled.Menu,
//            contentDescription = "Menu",
//            modifier = Modifier
//                .size(30.dp)
//        )
        Text(
            text = "Chess Pulse",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )
//        Box(modifier = Modifier
//
//            .background(color = Color(0xFFcfdce4))
//            ,
//            contentAlignment = Alignment.Center
//        ) {

        val bitmap = profileImageBase64?.let { profileRepo.base64ToBitmap(it) }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .clip(RoundedCornerShape(45.dp))
                    .size(50.dp)
                    .clickable {
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    }

            )
        } else{
            Image(
                painter = painterResource(R.drawable.blank_profile_picture),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .clip(RoundedCornerShape(45.dp))
                    .size(50.dp)
                    .clickable {
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    }

            )
        }
        //}

    }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(color = Color(0xFFC19C94))
    )
}

@Preview(showSystemUi = false)
@Composable
fun GreetingPreview() {
    ChessPulseTheme {
        header()
    }
}