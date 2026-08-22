package com.example.chesspulse.data

import android.util.Log
import com.example.chesspulse.remote.LichessApiService
import com.example.chesspulse.remote.PgnParser
import com.example.chesspulse.remote.RetrofitInstance
import com.example.chesspulse.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudyRepository(private val api: LichessApiService = RetrofitInstance.api) {

    private val pgnParser = PgnParser()
    private val gson = Gson()

    suspend fun fetchStudiesMetadata(username: String): List<StudyMetadata> = withContext(
        Dispatchers.IO) {
        val response = api.getStudiesByUser(username)

        if (!response.isSuccessful) {
            throw Exception("Lichess API error: ${response.code()}")
        }

        val body = response.body()?.string() ?: return@withContext emptyList()

        body.lineSequence()
            .filter { it.isNotBlank() }
            .map { line -> gson.fromJson(line, StudyMetadata::class.java) }
            .toList()
    }

    suspend fun fetchChapters(studyId: String, studyName: String): List<PgnParser.Chapter> =
        withContext(Dispatchers.IO) {
            val response = api.getStudyChapters(
                studyId = studyId,
                token = "Bearer ${BuildConfig.lichess_key}"
            )
            if (!response.isSuccessful) throw Exception("Failed to fetch chapters: ${response.code()}")
            val rawPgn = response.body()?.string() ?: return@withContext emptyList()
            pgnParser.parseChaptersFromPgn(rawPgn, studyName)
        }
}