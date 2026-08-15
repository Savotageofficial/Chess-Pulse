package com.example.chesspulse.data
// ChessBoardState.kt
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.chesspulse.R
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveList


class ChessBoardState(initialFen: String? = null) {
    private val board = Board().apply { if (initialFen != null) loadFromFen(initialFen) }

    var selectedSquare by mutableStateOf<Square?>(null)
        private set

    var legalTargets by mutableStateOf<List<Square>>(emptyList())
        private set

    // Bump this to force recomposition whenever the board mutates
    var boardVersion by mutableStateOf(0)
        private set

    fun pieceAt(square: Square) = board.getPiece(square)

    fun onSquareTapped(square: Square , context: Context ,onMove: (Move) -> Boolean) {
        val currentSelection = selectedSquare

        if (currentSelection == null) {
            // First tap: select if there's a piece of the side to move
            val piece = board.getPiece(square)
            if (piece != com.github.bhlangonijr.chesslib.Piece.NONE &&
                piece.pieceSide == board.sideToMove) {
                selectedSquare = square
                legalTargets = legalMovesFrom(square)
            }
        } else {
            // Second tap: try to move
            if (square == currentSelection) {
                clearSelection() // tapped same square, deselect
                return
            }

            val move = Move(currentSelection, square)

            if (board.legalMoves().any {
                    it.from == currentSelection &&
                            it.to == square
                }
            ) {

                // Calculate SAN WITHOUT changing the real board
                val moveList = MoveList(board.fen)
                moveList.add(move)

                val san = moveList.toSanArray()[0]

                Log.d("move", "SAN = $san")

                // Put the SAN onto the Move object
                move.setSan(san)

                // Ask caller whether this move is allowed
                val accepted = onMove(move)

                if (accepted) {
                    board.doMove(move)
                    boardVersion++
                    val targetPiece = board.getPiece(move.getTo())
                    val movingPiece = board.getPiece(move.getFrom())

                    val isCapture =
                        (targetPiece != Piece.NONE && targetPiece.getPieceSide() != movingPiece.getPieceSide())
                                || (movingPiece.getPieceType() == PieceType.PAWN && move.getTo()
                            .equals(board.getEnPassantTarget()))
                    if (isCapture){
                        takeSound(context)
                    }else {
                        playSound(context)
                    }
                }
            }

            clearSelection()
        }
    }

    fun onSquareTapped(square: Square , nextMove: String , context : Context ,onMove: (Move) -> Boolean) {
        val currentSelection = selectedSquare

        if (currentSelection == null) {
            // First tap: select if there's a piece of the side to move
            val piece = board.getPiece(square)
            if (piece != com.github.bhlangonijr.chesslib.Piece.NONE &&
                piece.pieceSide == board.sideToMove) {
                selectedSquare = square
                legalTargets = legalMovesFrom(square)
            }
        } else {
            // Second tap: try to move
            if (square == currentSelection) {
                clearSelection() // tapped same square, deselect
                return
            }

            val move = Move(currentSelection, square)

            if (board.legalMoves().any {
                    it.from == currentSelection &&
                            it.to == square
                }
            ) {

                // Calculate SAN WITHOUT changing the real board
                val moveList = MoveList(board.fen)
                moveList.add(move)

                val san = moveList.toSanArray()[0]

                Log.d("move", "SAN = $san")

                // Put the SAN onto the Move object
                move.setSan(san)

                // Ask caller whether this move is allowed
                val accepted = onMove(move)

                if (accepted) {
                    board.doMove(move)
                    boardVersion++
                    val targetPiece = board.getPiece(move.getTo())
                    val movingPiece = board.getPiece(move.getFrom())

                    val isCapture =
                        (targetPiece != Piece.NONE && targetPiece.getPieceSide() != movingPiece.getPieceSide())
                                || (movingPiece.getPieceType() == PieceType.PAWN && move.getTo()
                            .equals(board.getEnPassantTarget()))
                    if (isCapture){
                        takeSound(context)
                    }else {
                        playSound(context)
                    }
                    this.makeMove(nextMove , context = context)
                    boardVersion++

                }
            }

            clearSelection()
        }
    }

    fun playSound(context: Context) {
        var mediaPlayer = MediaPlayer.create(context, R.raw.move)
        if (board.isMated) {
            mediaPlayer = MediaPlayer.create(context, R.raw.checkmate)
        }
        mediaPlayer?.start()

        // Optional: release memory when done playing
        mediaPlayer?.setOnCompletionListener { mp ->
            mp.release()
        }
    }
    fun takeSound(context: Context) {
        var mediaPlayer = MediaPlayer.create(context, R.raw.capture)
        if (board.isMated) {
            mediaPlayer = MediaPlayer.create(context, R.raw.checkmate)
        }
        mediaPlayer?.start()

        // Optional: release memory when done playing
        mediaPlayer?.setOnCompletionListener { mp ->
            mp.release()
        }
    }

    fun makeMove(move : String , context: Context){
        val success = board.doMove(move)
        if (move.contains("x")){
            takeSound(context)
        }
        else {
            playSound(context)
        }




        boardVersion++
        clearSelection()
    }
    fun showBoard(): Board{
        return board
    }
    private fun legalMovesFrom(square: Square): List<Square> =
        board.legalMoves().filter { it.from == square }.map { it.to }

    private fun clearSelection() {
        selectedSquare = null
        legalTargets = emptyList()
    }

    fun undo(){
        board.undoMove()
        boardVersion++
        clearSelection()
    }
    fun fen(): String = board.fen
    fun isGameOver(): Boolean = board.isMated || board.isDraw || board.isStaleMate
    fun isChecked(): Boolean = board.isKingAttacked
    fun sidetomove(): Any = board.sideToMove

    fun checkedKingSquare(): Square? {
        return if (board.isKingAttacked) board.getKingSquare(board.sideToMove) else null
    }
}