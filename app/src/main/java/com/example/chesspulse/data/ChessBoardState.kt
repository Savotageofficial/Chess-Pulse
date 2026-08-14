package com.example.chesspulse.data
// ChessBoardState.kt
import android.util.Log
import androidx.compose.runtime.*
import com.github.bhlangonijr.chesslib.Board
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

    fun onSquareTapped(square: Square ,onMove: (Move) -> Boolean) {
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
                }
            }

            clearSelection()
        }
    }

    fun onSquareTapped(square: Square , nextMove: String ,onMove: (Move) -> Boolean) {
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
                    this.makeMove(nextMove)
                    boardVersion++
                }
            }

            clearSelection()
        }
    }

    fun makeMove(move : String){
        val success = board.doMove(move)



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