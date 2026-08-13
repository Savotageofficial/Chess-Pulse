package com.example.chesspulse.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface LichessApiService {
    @Streaming // important: avoids loading the whole ndjson blob into memory at once
    @GET("api/study/by/{username}")
    suspend fun getStudiesByUser(
        @Path("username") username: String
    ): Response<ResponseBody>


    @GET("api/study/{studyId}.pgn")
    suspend fun getStudyChapters(
        @Path("studyId") studyId: String,
        @Query("clocks") clocks: Boolean = true,
        @Query("comments") comments: Boolean = true,
        @Query("variations") variations: Boolean = true,
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}

