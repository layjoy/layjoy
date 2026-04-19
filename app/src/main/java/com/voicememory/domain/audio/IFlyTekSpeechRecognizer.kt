package com.voicememory.domain.audio

import android.util.Base64
import com.voicememory.data.remote.IFlyTekConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IFlyTekSpeechRecognizer @Inject constructor() {
    
    private val client = OkHttpClient()
    
    suspend fun recognizeAudio(audioFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val authUrl = getAuthUrl()
            val result = StringBuilder()
            var error: String? = null
            
            val request = Request.Builder()
                .url(authUrl)
                .build()
            
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    // 发送音频数据
                    val audioBytes = audioFile.readBytes()
                    val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
                    
                    val params = JSONObject().apply {
                        put("common", JSONObject().apply {
                            put("app_id", IFlyTekConfig.APP_ID)
                        })
                        put("business", JSONObject().apply {
                            put("language", "zh_cn")
                            put("domain", "iat")
                            put("accent", "mandarin")
                            put("vad_eos", 10000)
                            put("dwa", "wpgs")
                        })
                        put("data", JSONObject().apply {
                            put("status", 2) // 2表示最后一帧
                            put("format", "audio/L16;rate=16000")
                            put("encoding", "raw")
                            put("audio", base64Audio)
                        })
                    }
                    
                    webSocket.send(params.toString())
                }
                
                override fun onMessage(webSocket: WebSocket, text: String) {
                    val json = JSONObject(text)
                    val code = json.getInt("code")
                    
                    if (code != 0) {
                        error = json.optString("message", "识别失败")
                        webSocket.close(1000, "Error")
                        return
                    }
                    
                    val data = json.optJSONObject("data")
                    if (data != null) {
                        val resultObj = data.optJSONObject("result")
                        if (resultObj != null) {
                            val ws = resultObj.optJSONArray("ws")
                            if (ws != null) {
                                for (i in 0 until ws.length()) {
                                    val wsItem = ws.getJSONObject(i)
                                    val cw = wsItem.optJSONArray("cw")
                                    if (cw != null && cw.length() > 0) {
                                        val word = cw.getJSONObject(0).getString("w")
                                        result.append(word)
                                    }
                                }
                            }
                        }
                    }
                    
                    // 检查是否结束
                    if (json.optInt("code") == 0 && json.optJSONObject("data")?.optInt("status") == 2) {
                        webSocket.close(1000, "Finished")
                    }
                }
                
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    error = t.message ?: "连接失败"
                }
            }
            
            client.newWebSocket(request, listener)
            
            // 等待识别完成（最多30秒）
            var waitTime = 0
            while (error == null && result.isEmpty() && waitTime < 30000) {
                Thread.sleep(100)
                waitTime += 100
            }
            
            if (error != null) {
                Result.failure(Exception(error))
            } else if (result.isEmpty()) {
                Result.failure(Exception("识别超时"))
            } else {
                Result.success(result.toString())
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getAuthUrl(): String {
        val url = URL(IFlyTekConfig.ASR_URL)
        val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }.format(Date())
        
        val builder = StringBuilder("host: ").append(url.host).append("\n")
            .append("date: ").append(date).append("\n")
            .append("GET ").append(url.path).append(" HTTP/1.1")
        
        val sha = hmacSHA256(builder.toString(), IFlyTekConfig.API_SECRET)
        val authorization = Base64.encodeToString(sha, Base64.NO_WRAP)
        
        val authBase = "api_key=\"${IFlyTekConfig.API_KEY}\", " +
                "algorithm=\"hmac-sha256\", " +
                "headers=\"host date request-line\", " +
                "signature=\"$authorization\""
        
        val authorizationBase64 = Base64.encodeToString(
            authBase.toByteArray(Charset.forName("UTF-8")),
            Base64.NO_WRAP
        )
        
        return "${IFlyTekConfig.ASR_URL}?authorization=$authorizationBase64&date=$date&host=${url.host}"
    }
    
    private fun hmacSHA256(data: String, key: String): ByteArray {
        val secretKey = SecretKeySpec(key.toByteArray(Charset.forName("UTF-8")), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)
        return mac.doFinal(data.toByteArray(Charset.forName("UTF-8")))
    }
}
