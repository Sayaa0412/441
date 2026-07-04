package cn.edu.sjtu.ega816.kotlinChatter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.databinding.ObservableArrayList
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ChattStore {
    const val serverUrl = "https://115.29.229.247/"
    val chatts = ObservableArrayList<Chatt>()
    private const val nFields = 5
    private val mainHandler = Handler(Looper.getMainLooper())

    private val client = run {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            OkHttpClient()
        }
    }

    fun getChatts() {
        val request = Request.Builder()
            .url(serverUrl + "getimages/")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("getChatts", "Failed GET request")
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val chattsReceived = try {
                        JSONObject(response.body?.string() ?: "").getJSONArray("chatts")
                    } catch (e: JSONException) {
                        Log.e("getChatts", "JSON parse error")
                        return
                    }
                    chatts.clear()
                    for (i in 0 until chattsReceived.length()) {
                        val chattEntry = chattsReceived[i] as JSONArray
                        if (chattEntry.length() == nFields) {
                            chatts.add(Chatt(username = chattEntry[0].toString(),
                                message = chattEntry[1].toString(),
                                timestamp = chattEntry[2].toString(),
                                imageUrl = chattEntry[3].toString(),
                                videoUrl = chattEntry[4].toString(),
                            ))
                        } else {
                            Log.e("getChatts", "Received unexpected number of fields " + chattEntry.length())
                        }
                    }
                }
            }
        })
    }

    fun postChatt(context: Context, chatt: Chatt, imageUri: android.net.Uri?, videoUri: android.net.Uri?,
                  completion: (String) -> Unit) {
        val mpFD = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("username", chatt.username ?: "")
                .addFormDataPart("message", chatt.message ?: "")
        imageUri?.run {
            this.toFile(context)?.let { file ->
                mpFD.addFormDataPart("image", "chattImage",
                    file.asRequestBody("image/jpeg".toMediaType()))
            } ?: context.toast("Unsupported image format")
        }
        videoUri?.run {
            this.toFile(context)?.let { file ->
                mpFD.addFormDataPart("video", "chattVideo",
                        file.asRequestBody("video/mp4".toMediaType()))
            } ?: context.toast("Unsupported video format")
        }
        val request = Request.Builder()
                .url(serverUrl + "postimages/")
                .post(mpFD.build())
                .build()
        context.toast("Posting . . . wait for 'Chatt posted!'")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    completion(e.localizedMessage ?: "Posting failed")
                }
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    getChatts()
                    mainHandler.post {
                        completion("Chatt posted!")
                    }
                }
            }
        })
    }
}