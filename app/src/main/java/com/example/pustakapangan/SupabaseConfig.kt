package com.example.pustakapangan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object SupabaseConfig {
    private const val BASE_URL = "https://jteteeubteryqfgrnjks.supabase.co/rest/v1"
    private const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imp0ZXRlZXVidGVyeXFmZ3JuamtzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAxMjkzNjgsImV4cCI6MjEwNTcwNTM2OH0.DvfxObIzo-HcewLlVDmqHcF71e66dflaz7yukJPw0MQ"

    suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL/$path")
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("apikey", ANON_KEY)
        connection.setRequestProperty("Authorization", "Bearer $ANON_KEY")
        connection.requestMethod = "GET"

        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        val hasil = stream.bufferedReader().use { it.readText() }
        connection.disconnect()

        if (responseCode !in 200..299) {
            throw Exception("Supabase error ($responseCode): $hasil")
        }
        hasil
    }
}