package com.example.chesspulse

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.format.DateUtils
import android.util.Log
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chesspulse.data.StudyMetadata
import com.example.chesspulse.data.StudyRepository
import com.example.chesspulse.ui.theme.ChessPulseTheme
import com.example.chesspulse.ui.theme.appColors



inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayListExtra(key)
    }
class PartSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val group: ArrayList<StudyMetadata>? = intent.parcelableArrayList("group")
        val title = intent.getStringExtra("title")
        val repo = StudyRepository()
        enableEdgeToEdge()
        setContent {
            ChessPulseTheme {
                PartSelectionScreen(repo , group , title)
            }
        }
    }
}

@Composable
fun PartSelectionScreen(repo : StudyRepository, courseGroup : ArrayList<StudyMetadata>?, title: String? , modifier: Modifier = Modifier) {
    var studies by remember { mutableStateOf<List<StudyMetadata>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = appColors().screenBg)

    ) {

        Spacer(modifier= Modifier
            .height(20.dp)
            .fillMaxWidth()
            .background(color = appColors().surface)
        )

        header()

        Spacer(modifier= Modifier.height(20.dp))


        Column(
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Text(
                text = "$title",
                fontWeight = FontWeight.SemiBold,
                color = appColors().accent
            )
        }




        Spacer(modifier= Modifier.height(20.dp))


        if(courseGroup != null){
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
            ) {
                items(items = courseGroup, key = { it.id }) { course ->
                    CourseCardSingle(course = course , modifier = Modifier
                        .clickable{
                            val intent = Intent(context, ChapterSelectionActivity::class.java).apply {
                                putExtra("title" , course.name)
                                putExtra("courseID" , course.id)
                            }
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }


    }

}


@Composable
fun CourseCardSingle(
    course: StudyMetadata,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(width = 2.dp , color = appColors().accent ,  shape = RoundedCornerShape(20.dp))
            .background(color = appColors().surface)
            .padding(16.dp)

    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(appColors().accent), // bg-primary-container
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.chess_knight_24px),
                contentDescription = null,
                tint = appColors().accentSoft, // text-on-primary-container
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.name ,
                style = MaterialTheme.typography.titleMedium,
                color = appColors().textPrimary // text-primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = relativeTime(course.updatedAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}




