package com.example.pustakapangan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object SupabaseConfig {
    const val PROJECT_URL = "https://jteteeubteryqfgrnjks.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imp0ZXRlZXVidGVyeXFmZ3JuamtzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAxMjkzNjgsImV4cCI6MjEwNTcwNTM2OH0.DvfxObIzo-HcewLlVDmqHcF71e66dflaz7yukJPw0MQ"

    suspend fun get(path: String, accessToken: String? = null): String =
        request("$PROJECT_URL/rest/v1/$path", "GET", null, accessToken)

    suspend fun postRest(path: String, bodyJson: String, accessToken: String): String =
        request("$PROJECT_URL/rest/v1/$path", "POST", bodyJson, accessToken)

    suspend fun postRpc(fungsi: String, bodyJson: String, accessToken: String): String =
        request("$PROJECT_URL/rest/v1/rpc/$fungsi", "POST", bodyJson, accessToken)

    suspend fun postAuth(path: String, bodyJson: String): String =
        request("$PROJECT_URL/auth/v1/$path", "POST", bodyJson, null)

    private suspend fun request(urlString: String, method: String, bodyJson: String?, accessToken: String?): String =
        withContext(Dispatchers.IO) {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("apikey", ANON_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${accessToken ?: ANON_KEY}")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.requestMethod = method

            if (bodyJson != null) {
                connection.doOutput = true
                connection.setRequestProperty("Prefer", "return=representation")
                connection.outputStream.use { it.write(bodyJson.toByteArray()) }
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val hasil = stream?.bufferedReader()?.use { it.readText() } ?: ""
            connection.disconnect()

            if (responseCode !in 200..299) {
                val pesan = try {
                    val json = org.json.JSONObject(hasil)
                    json.optString("msg", null)
                        ?: json.optString("error_description", null)
                        ?: json.optString("message", null)
                        ?: hasil
                } catch (e: Exception) {
                    hasil
                }
                throw Exception(pesan)
            }
            hasil
        }
}