package com.example.chesspulse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.chesspulse.data.ChessBoardState
import com.example.chesspulse.ui.theme.ChessPulseTheme
import com.example.chesspulse.ui.theme.appColors
import com.github.bhlangonijr.chesslib.Square
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chesspulse.remote.PgnParser
import com.github.bhlangonijr.chesslib.Piece
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalLayoutDirection
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LearnActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra("title")
        val startingFEN = intent.getStringExtra("startingFEN")
        val PGN = intent.getStringExtra("PGN")
        val chapterList: ArrayList<PgnParser.Chapter>? = intent.parcelableArrayList("chaptersList")
        val indx = intent.getIntExtra("chapterindx" , 0)
        val currentChapterID = intent.getStringExtra("chapterID")
        val CourseID = intent.getStringExtra("courseID")
        val parser = PgnParser()


        var nextChapter = chapterList?.get(indx)
        Log.d("trace" , "indx = ${indx} , size = ${chapterList?.size}")
        if (indx < chapterList?.size!! - 1) {
            nextChapter = chapterList[indx + 1]
        }



        val originComment = parser.extractMainlineComment(PGN ?: "")
        val interactive = originComment?.contains("<i>")

        val repo = AuthRepository()





        enableEdgeToEdge()
        setContent {
            ChessPulseTheme {
                LearnScreen(title = title , Pgn = PGN , startingFEN , parser , interactive = (interactive == true),  onNextChapterClick = {


                    lifecycleScope.launch {
                        repo.addChapterToCurrentUser(courseId = CourseID ?: "", chapter = currentChapterID ?: "")
                    }
                    val intent = Intent(this , LearnActivity::class.java).apply {
                        putExtra("title" , nextChapter?.name)
                        putExtra("startingFEN" , nextChapter?.startFen)
                        putExtra("PGN" , nextChapter?.pgn)
                        putExtra("chaptersList" , chapterList)
                        putExtra("chapterindx" , indx + 1)
                        putExtra("chapterID" , nextChapter?.id)
                        putExtra("courseID" , CourseID)
                    }
                    startActivity(intent)
                    finish()
                })
            }
        }
    }
}


private val LIGHT_SQUARE = Color(0xFFf0d9b5)
private val DARK_SQUARE = Color(0xFFb58863)
private val SELECTED = Color(0xFFF6F669)
private val LEGAL_DOT = Color(0xFF4CAF50)
private val CHECK_RED = Color(0xFFFF6B6B) // light red
@Composable
fun ChessBoard(
    state: ChessBoardState,
    arrows: List<List<String>> = emptyList(),
    interactive : Boolean,
    gameIndexer: Int,
    onGameIndexerChange: (Int) -> Unit,
    onCommentIndexerChange: (Int) -> Unit,
    game: List<String>,
    commentIndexer : Int,
    isFlipped: Boolean,
    modifier: Modifier = Modifier
) {

    var version = state.boardVersion
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
    Box(modifier = modifier) {
        Column {
            val rankRange = if (isFlipped) 0..7 else 7 downTo 0
            for (rank in rankRange) {
                Row {
                    val fileRange = if (isFlipped) 7 downTo 0 else 0..7
                    for (file in fileRange) {
                        val square = Square.encode(
                            com.github.bhlangonijr.chesslib.Rank.allRanks[rank],
                            com.github.bhlangonijr.chesslib.File.allFiles[file]
                        )
                        ChessSquare(
                            square = square,
                            state = state,
                            isLight = (rank + file) % 2 == 1,
                            interactive = interactive,
                            gameIndexer = gameIndexer,
                            game = game,
                            onGameIndexerChange = onGameIndexerChange,
                            onCommentIndexerChange = onCommentIndexerChange,
                            commentIndexer = commentIndexer
                        )
                    }
                }
            }
        }

        if (arrows.isNotEmpty()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val squareSize = size.width / 8f
                arrows.forEach { arrow ->
                    if (arrow.size == 2) {
                        drawChessArrow(
                            from = squareCenter(arrow[0], squareSize, isFlipped),
                            to = squareCenter(arrow[1], squareSize, isFlipped),
                            color = Color(0xCC00A000)
                        )
                    }
                }
            }
        }
    }
}
}
private fun squareCenter(square: String, squareSize: Float, isFlipped: Boolean): Offset {
    val file = square[0] - 'a'          // 0..7, a->0
    val rank = square[1] - '1'          // 0..7, rank1->0

    val col = if (isFlipped) 7 - file else file
    val row = if (isFlipped) rank else 7 - rank

    return Offset(col * squareSize + squareSize / 2f, row * squareSize + squareSize / 2f)
}

private fun DrawScope.drawChessArrow(
    from: Offset,
    to: Offset,
    color: Color,
    strokeWidth: Float = 15f
) {
    val angle = atan2((to.y - from.y).toDouble(), (to.x - from.x).toDouble())
    val headLen = strokeWidth * 3
    val shortenedTo = Offset(
        to.x - (headLen * 0.6f * cos(angle)).toFloat(),
        to.y - (headLen * 0.6f * sin(angle)).toFloat()
    )

    drawLine(color, start = from, end = shortenedTo, strokeWidth = strokeWidth, cap = StrokeCap.Round)

    val path = Path().apply {
        moveTo(to.x, to.y)
        lineTo(
            (to.x - headLen * cos(angle - Math.PI / 6)).toFloat(),
            (to.y - headLen * sin(angle - Math.PI / 6)).toFloat()
        )
        lineTo(
            (to.x - headLen * cos(angle + Math.PI / 6)).toFloat(),
            (to.y - headLen * sin(angle + Math.PI / 6)).toFloat()
        )
        close()
    }
    drawPath(path, color = color)
}

@Composable
private fun ChessSquare(
    square: Square,
    state: ChessBoardState,
    isLight: Boolean,
    interactive: Boolean,
    gameIndexer: Int,
    game : List<String>,
    onGameIndexerChange: (Int) -> Unit,
    commentIndexer : Int,
    onCommentIndexerChange: (Int) -> Unit
    ) {
    val version = state.boardVersion
    val isSelected = state.selectedSquare == square
    val isLegalTarget = square in state.legalTargets
    val piece = state.pieceAt(square)
    val isCheckedKing = square == state.checkedKingSquare()
    val context = LocalContext.current

    val bgColor = when {
        isCheckedKing -> CHECK_RED
        isSelected -> SELECTED
        else -> if (isLight) LIGHT_SQUARE else DARK_SQUARE
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(bgColor)
            .clickable {
                if (interactive && (gameIndexer < game.size)){
                    if (gameIndexer + 1 < game.size){
                        val nextMove = game[gameIndexer + 1]
                        state.onSquareTapped(square , nextMove = nextMove, context = context ,
                            { move ->
                                Log.d("move" , move.san)
                                if (move.san == game[gameIndexer] ){
                                    onGameIndexerChange(gameIndexer + 2)
                                    onCommentIndexerChange(commentIndexer + 2)
                                    true
                                }else{
                                    false
                                }

                            })
                    }else{
                        state.onSquareTapped(square , context = context,
                            { move ->
                                Log.d("move" , move.san)
                                if (move.san == game[gameIndexer] ){
                                    onGameIndexerChange(gameIndexer + 1)
                                    onCommentIndexerChange(commentIndexer + 1)
                                    true
                                }else{
                                    false
                                }

                            })

                    }

                }

            },
        contentAlignment = Alignment.Center
    ) {
        if (piece != Piece.NONE) {
            Icon(
                painter = painterResource(pieceGlyph(piece)),
                contentDescription = "chess piece",
                tint = Color.Unspecified
            )
        }
        if (isLegalTarget) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(LEGAL_DOT, shape = androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}




private fun pieceGlyph(piece: Piece): Int = when (piece) {



    Piece.WHITE_PAWN -> R.drawable.wp; Piece.WHITE_KNIGHT -> R.drawable.wn; Piece.WHITE_BISHOP -> R.drawable.wb
    Piece.WHITE_ROOK -> R.drawable.wr; Piece.WHITE_QUEEN -> R.drawable.wq; Piece.WHITE_KING -> R.drawable.wk
    Piece.BLACK_PAWN -> R.drawable.bp; Piece.BLACK_KNIGHT -> R.drawable.bn; Piece.BLACK_BISHOP -> R.drawable.bb
    Piece.BLACK_ROOK -> R.drawable.br; Piece.BLACK_QUEEN -> R.drawable.bq; Piece.BLACK_KING -> R.drawable.bk
    else -> R.drawable.question_mark
}


@Composable
fun LearnScreen(title : String? ,
                Pgn: String? ,
                startingFEN : String? ,
                parser : PgnParser ,
                modifier: Modifier = Modifier,
                interactive: Boolean,
                onNextChapterClick:() -> Unit
) {
    val boardState = remember { ChessBoardState(startingFEN) } // or pass a FEN for the lesson's position


    var arrows = parser.extractArrows(Pgn ?: "")
    arrows = arrows.toMutableList()
    arrows.add(0 , emptyList())


    val IsFlipped = parser.IsBlackOrientation(Pgn ?: "")

//    Log.d("trace" , arrows.toString())


    val game = remember(Pgn) {
        parser.extractMainlineMoves2(Pgn ?: "")
    }

    val comments = parser.extractMoveComments(Pgn ?: "").toMutableList()
    comments.add(0 , parser.extractMainlineComment(Pgn ?: "")?.replace("<i>" , ""))
    var gameIndexer by remember { mutableIntStateOf(0) }
    var currentturn by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    var commentIndexer by remember { mutableIntStateOf(0) }
    val currentArrows = arrows.getOrNull(gameIndexer) ?: emptyList()
    val context = LocalContext.current

    Log.d("trace" , gameIndexer.toString())

    val gradientBG = Brush.verticalGradient(
        colors = listOf(
            appColors().learnGradientTop,
            appColors().learnGradientBottom
        )
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column (
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = title ?: "",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            if (interactive){

                if(gameIndexer == (game.size) ) {
                    Text(
                        text = "Find The Best Move ✅",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }else{
                    Text(
                        text = "Find The Best Move",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ChessBoard(
                state = boardState,
                arrows = currentArrows,
                modifier = Modifier.fillMaxWidth(),
                interactive = interactive,
                gameIndexer = gameIndexer,
                game = game,
                commentIndexer = commentIndexer,
                onGameIndexerChange = { gameIndexer = it },
                isFlipped = IsFlipped,
                onCommentIndexerChange = {commentIndexer = it}
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp)
                .border(width = 2.dp , color = appColors().panelBorder , shape = RoundedCornerShape(topStart = 20.dp , topEnd = 20.dp))
                .clip(RoundedCornerShape(topStart = 20.dp , topEnd = 20.dp))
                .background(gradientBG)


        ) {


            Column(
                modifier = Modifier.weight(1f).padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
//            Spacer(modifier = Modifier.height(30.dp))
//
//            Text(
//                text = title ?: "",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold
//            )
//
//            Spacer(modifier = Modifier.height(20.dp))

//            ChessBoard(
//                state = boardState,
//                modifier = Modifier.fillMaxWidth()
//            )
                Spacer(Modifier.height(16.dp))

                Text(comments[commentIndexer] ?: "", modifier = Modifier.verticalScroll(scrollState))

            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {

//            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
//                 contentDescription = "arrow back",
//                modifier = Modifier
//                    .size(40.dp)
//                    .clickable{
//                    if (boardState.fen() != startingFEN){
//                        boardState.undo()
//                        gameIndexer -= 1
////                        if (gameIndexer == 0){
////                            gameIndexer = 1
////                            currentturn--
////                        }else{
////                            gameIndexer -= 1
////                        }
//
//                    }
//                }
//            )


                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        if (boardState.fen() != startingFEN) {
                            boardState.undo()
                            gameIndexer -= 1
                            commentIndexer -=1
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "arrow back",
                        modifier = Modifier.size(40.dp)
                    )

                    Text(
                        text = "Previous",
                        fontSize = 12.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        if (gameIndexer < game.size) {
                            boardState.makeMove(game[gameIndexer] , context = context)
                            gameIndexer += 1
                            commentIndexer +=1
                        }
                    }
                ) {

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "arrow Forward",
                        modifier = Modifier
                            .size(40.dp)
                    )

                    Text(
                        text = "Next",
                        fontSize = 12.sp
                    )
                }


                if(gameIndexer == (game.size) ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(onClick = onNextChapterClick)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "next chapter",
                            modifier = Modifier
                                .size(40.dp)

                        )
                        Text(
                            text = "Next Chapter",
                            fontSize = 12.sp
                        )
                    }
                }

            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    ChessPulseTheme {
    }
}