package com.example.chesspulse.remote

import android.os.Parcelable
import android.util.Log
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveList
import kotlinx.parcelize.Parcelize

class PgnParser {
    @Parcelize
    data class Chapter(
        val id: String?,        // from ChapterURL, e.g. "7erFiCmZ" — use this as the unique key, not name
        val name: String,       // from ChapterName tag directly
        val startFen: String?,  // null means standard starting position
        val pgn: String
    ): Parcelable

    private val eventTagRegex = Regex("""\[Event\s+"(.*?)"]""")
    private val chapterNameRegex = Regex("""\[ChapterName\s+"(.*?)"]""")
    private val chapterUrlRegex = Regex("""\[ChapterURL\s+"https://lichess\.org/study/[^/]+/(.*?)"]""")
    private val fenRegex = Regex("""\[FEN\s+"(.*?)"]""")

    fun extractMainlineMoves(pgn: String): List<List<String>> {

        // Find the end of the PGN headers
        val headerEnd = pgn.indexOf("\n\n")
        if (headerEnd == -1) return emptyList()

        val movetext = pgn.substring(headerEnd + 2)

        val result = mutableListOf<MutableList<String>>()
        var currentMove: MutableList<String>? = null

        var i = 0
        var commentDepth = 0
        var variationDepth = 0

        while (i < movetext.length) {

            val c = movetext[i]

            // Skip comments
            if (commentDepth > 0) {
                if (c == '}') commentDepth--
                i++
                continue
            }

            // Skip variations
            if (variationDepth > 0) {
                if (c == '(') variationDepth++
                else if (c == ')') variationDepth--
                i++
                continue
            }

            when (c) {
                '{' -> {
                    commentDepth++
                    i++
                    continue
                }

                '(' -> {
                    variationDepth++
                    i++
                    continue
                }
            }

            if (c.isWhitespace()) {
                i++
                continue
            }

            // Read one token
            val start = i
            while (
                i < movetext.length &&
                !movetext[i].isWhitespace() &&
                movetext[i] != '{' &&
                movetext[i] != '('
            ) {
                i++
            }

            var token = movetext.substring(start, i)

            // Ignore game results
            if (
                token == "*" ||
                token == "1-0" ||
                token == "0-1" ||
                token == "1/2-1/2"
            ) {
                continue
            }

            // Ignore NAGs
            if (token.startsWith("$"))
                continue

            // White move number: 1. 2. 3.
            if (Regex("""\d+\.""").matches(token)) {
                currentMove = mutableListOf()
                result.add(currentMove)
                continue
            }

            // Black move number: 2...
            if (Regex("""\d+\.\.\.""").matches(token))
                continue

            // Remove annotations (!! ?! etc.)
            token = token
                .replace("!!", "")
                .replace("!?", "")
                .replace("?!", "")
                .replace("??", "")
                .replace("!", "")
                .replace("?", "?") // keep check '? no-op
                .trim()

            currentMove?.add(token)
        }

        return result
    }

    fun IsBlackOrientation(pgn: String): Boolean {
        val regex = Regex("""\[Orientation\s+"(\w+)"\]""")
        val orientation = regex.find(pgn)?.groupValues?.get(1)?.lowercase() ?: "white"
        return orientation == "black"
    }
    fun extractMoveComments(pgn: String): List<String?> {

        // Remove PGN headers
        val movetext = pgn.substringAfter("\n\n")

        val comments = mutableListOf<String?>()

        var moveIndex = -1
        var variationDepth = 0

        // Tokenize the PGN
        val tokenRegex = Regex(
            """\(|\)|\{[^}]*\}|\d+\.(?:\.\.)?|[^\s(){}]+"""
        )

        for (match in tokenRegex.findAll(movetext)) {

            val token = match.value

            when {

                // Enter variation
                token == "(" -> variationDepth++

                // Exit variation
                token == ")" -> variationDepth--

                // Ignore move numbers
                token.matches(Regex("""\d+\.(?:\.\.)?""")) -> {}

                // Comment
                token.startsWith("{") -> {

                    if (variationDepth == 0 && moveIndex >= 0) {

                        val comment = token
                            .removePrefix("{")
                            .removeSuffix("}")
                            .trim()

                        // Ignore Lichess arrows/highlights
                        if (!comment.startsWith("[%")) {
                            comments[moveIndex] = comment
                        }
                    }
                }

                // Ignore game result
                token == "*" ||
                        token == "1-0" ||
                        token == "0-1" ||
                        token == "1/2-1/2" -> {}

                // Mainline move
                variationDepth == 0 -> {
                    moveIndex++
                    comments.add(null)
                }
            }
        }

        return comments
    }

    fun extractArrows(pgn: String): List<List<List<String>>> {

        val result = mutableListOf<MutableList<List<String>>>()
        // Remove headers
        val movesText = pgn
            .replace(Regex("""(?s)^\s*(?:\[.*?]\s*)+"""), "")
            .trim()

        val arrowRegex = Regex("""[GRYB]([a-h][1-8])([a-h][1-8])""")
        val calRegex = Regex("""\[%cal\s+([^\]]+)]""")

        // Split into moves and comments
        val tokens = Regex("""(\d+\.\.\.|\d+\.)|(\{.*?\})|([^\s{}]+)""", RegexOption.DOT_MATCHES_ALL)
            .findAll(movesText)
            .map { it.value }
            .toList()

        var lastWasMove = false

        for (token in tokens) {

            // A SAN move
            if (!token.startsWith("{") &&
                !token.matches(Regex("""\d+\.*""")) &&
                token != "*") {

                result.add(mutableListOf())
                lastWasMove = true
            }

            // A comment after a move
            else if (token.startsWith("{") && lastWasMove) {

                val cal = calRegex.find(token)

                if (cal != null) {

                    val arrows = result.last()

                    for (arrow in arrowRegex.findAll(cal.groupValues[1])) {
                        arrows.add(
                            listOf(
                                arrow.groupValues[1],
                                arrow.groupValues[2]
                            )
                        )
                    }
                }
            }
        }

        return result
    }
    fun extractMainlineMoves2(pgn: String): List<String> {

        // Find the end of the PGN headers
        val headerEnd = pgn.indexOf("\n\n")
        if (headerEnd == -1) return emptyList()

        val movetext = pgn.substring(headerEnd + 2)

        val result = mutableListOf<String>()

        var i = 0
        var commentDepth = 0
        var variationDepth = 0

        while (i < movetext.length) {

            val c = movetext[i]

            // Skip comments
            if (commentDepth > 0) {
                if (c == '}') commentDepth--
                i++
                continue
            }

            // Skip variations
            if (variationDepth > 0) {
                if (c == '(') variationDepth++
                else if (c == ')') variationDepth--
                i++
                continue
            }

            when (c) {
                '{' -> {
                    commentDepth++
                    i++
                    continue
                }
                '(' -> {
                    variationDepth++
                    i++
                    continue
                }
            }

            if (c.isWhitespace()) {
                i++
                continue
            }

            // Read one token
            val start = i
            while (
                i < movetext.length &&
                !movetext[i].isWhitespace() &&
                movetext[i] != '{' &&
                movetext[i] != '('
            ) {
                i++
            }

            var token = movetext.substring(start, i)

            // Ignore game results
            if (
                token == "*" ||
                token == "1-0" ||
                token == "0-1" ||
                token == "1/2-1/2"
            ) {
                continue
            }

            // Ignore NAGs
            if (token.startsWith("$"))
                continue

            // Ignore move numbers
            if (Regex("""\d+\.""").matches(token) ||
                Regex("""\d+\.\.\.""").matches(token)
            ) {
                continue
            }

            // Remove annotations
            token = token
                .replace("!!", "")
                .replace("!?", "")
                .replace("?!", "")
                .replace("??", "")
                .replace("!", "")
                .replace("?", "?") // keep '?' if you really want
                .trim()

            result.add(token)
        }

        return result
    }

    fun extractMainlineComment(pgn: String): String? {

        // Skip the PGN headers
        val movetext = pgn.substringAfter("\n\n", "")

        // Find the very first {...} block
        val match = Regex(
            """\{([\s\S]*?)\}"""
        ).find(movetext) ?: return null

        val comment = match.groupValues[1].trim()

        // Ignore graphical annotations if they happen to be first
        return if (comment.startsWith("[%")) null else comment
    }
    fun parseChaptersFromPgn(rawPgn: String, studyName: String): List<Chapter> {
        val gameBlocks = rawPgn.split(Regex("(?=\\[Event )")).filter { it.isNotBlank() }

        return gameBlocks.map { block ->
            val chapterId = chapterUrlRegex.find(block)?.groupValues?.get(1)
            val startFen = fenRegex.find(block)?.groupValues?.get(1)

            // Prefer the dedicated ChapterName tag; fall back to parsing Event only if it's ever missing
            val chapterName = chapterNameRegex.find(block)?.groupValues?.get(1)?.trim()
                ?: eventTagRegex.find(block)?.groupValues?.get(1)
                    ?.trim()
                    ?.removePrefix("$studyName:")
                    ?.trim()
                ?: "Untitled Chapter"

            Chapter(
                id = chapterId,
                name = chapterName,
                startFen = startFen,
                pgn = block.trim()
            )
        }
    }
}