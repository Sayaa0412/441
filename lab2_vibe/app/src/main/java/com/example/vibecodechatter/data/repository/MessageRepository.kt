package com.example.vibecodechatter.data.repository

import com.example.vibecodechatter.data.model.ChattMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MessageRepository {

    suspend fun getMessages(): List<ChattMessage> = withContext(Dispatchers.IO) {
        val connection = createConnection(GET_MESSAGES_PATH, "GET")
        try {
            val responseCode = connection.responseCode
            val responseBody = readResponseBody(connection, responseCode)
            if (responseCode !in 200..299) {
                throw IOException("Unable to load messages. HTTP $responseCode")
            }

            parseMessages(responseBody)
        } finally {
            connection.disconnect()
        }
    }

    suspend fun postMessage(username: String, message: String) = withContext(Dispatchers.IO) {
        val connection = createConnection(POST_MESSAGE_PATH, "POST")
        try {
            val requestBody = JSONObject()
                .put("username", username.trim())
                .put("message", message.trim())
                .toString()

            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(requestBody)
            }

            val responseCode = connection.responseCode
            readResponseBody(connection, responseCode)
            if (responseCode !in 200..299) {
                throw IOException("Unable to send message. HTTP $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseMessages(responseBody: String): List<ChattMessage> {
        val messages = mutableListOf<ChattMessage>()
        val root = JSONObject(responseBody)
        val chatts = root.optJSONArray("chatts") ?: return emptyList()

        for (index in 0 until chatts.length()) {
            val row = chatts.optJSONArray(index) ?: continue
            messages += ChattMessage(
                username = row.optString(0).trim(),
                message = row.optString(1).trim(),
                rawTimestamp = row.optString(2).trim()
            )
        }

        return messages
    }

    private fun createConnection(path: String, method: String): HttpsURLConnection {
        return (URL(BASE_URL + path).openConnection() as HttpsURLConnection).apply {
            requestMethod = method
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            doInput = true
            useCaches = false
            setRequestProperty("Accept", "application/json")
        }
    }

    private fun readResponseBody(
        connection: HttpURLConnection,
        responseCode: Int
    ): String {
        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        } ?: return ""

        return stream.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
    }

    private companion object {
        const val BASE_URL = "https://8.160.114.186"
        const val GET_MESSAGES_PATH = "/getchatts/"
        const val POST_MESSAGE_PATH = "/postchatt/"
        const val TIMEOUT_MS = 15_000
    }
}

