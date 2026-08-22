package com.example.chesspulse

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chesspulse.data.StudyRepository
import com.example.chesspulse.remote.PgnParser
import com.example.chesspulse.ui.theme.ChessPulseTheme

class ChapterSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra("title")
        val course_id = intent.getStringExtra("courseID")
        val repo = StudyRepository()
        enableEdgeToEdge()
        setContent {
            ChessPulseTheme {
                ChapterSelectionScreen(repo = repo , course_id , title , onChapterClick = {chapter , index , oldChapters ->
                    val intent = Intent(this, LearnActivity::class.java).apply {
                        putExtra("title" , chapter.name)
                        putExtra("startingFEN" , chapter.startFen)
                        putExtra("PGN" , chapter.pgn)
                        putExtra("chaptersList" , ArrayList(oldChapters))
                        putExtra("chapterindx" , index)
                        putExtra("chapterID" , chapter.id)
                        putExtra("courseID" , course_id)
                    }
                    startActivity(intent)
                    finish()
                })
            }
        }
    }
}

@Composable
fun ChapterSelectionScreen(repo : StudyRepository, courseid: String?, title: String?, onChapterClick:( PgnParser.Chapter , Int , List<PgnParser.Chapter>) -> Unit , modifier: Modifier = Modifier) {
    var chapters by remember { mutableStateOf<List<PgnParser.Chapter>>(emptyList()) }
    var oldChapters by remember { mutableStateOf<List<PgnParser.Chapter>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val auth = AuthRepository()


    LaunchedEffect(Unit) {
        try {
            if (courseid == null || title == null) {
                chapters = emptyList()
            } else {
                oldChapters = repo.fetchChapters(courseid, title)
                val userCourses = auth.getCurrentUserChapters(courseId = courseid)

                val filtered = oldChapters.filter { chapter -> userCourses.contains(chapter.id) }

                chapters = when {
                    oldChapters.isEmpty() -> emptyList()
                    filtered.isEmpty() -> listOf(oldChapters.first())
                    else -> {
                        val lastUnlockedIndex = oldChapters.indexOfLast { it in filtered }
                        val nextIndex = lastUnlockedIndex + 1
                        if (nextIndex < oldChapters.size) {
                            filtered + oldChapters[nextIndex]
                        } else {
                            filtered // user already has the last chapter, nothing to add
                        }
                    }
                }
            }
        } catch (e: Exception) {
            error = "Failed to load chapters"
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
                text = "$title",
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
                LazyColumn(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
                ) {
                    itemsIndexed(chapters, key = { _, chapter -> chapter.name }) { index, chapter ->
                        CourseCardChapters(
                            course = chapter,
                            displayTitle = "Chapter ${index + 1}",
                            modifier = Modifier.clickable{
                                onChapterClick(chapter , index , oldChapters)
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
fun CourseCardChapters(
    displayTitle: String,
    course: PgnParser.Chapter,
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
                text = displayTitle ,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF361F1A) // text-primary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    ChessPulseTheme {
    }
}