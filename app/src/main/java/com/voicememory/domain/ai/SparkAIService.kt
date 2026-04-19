package com.voicememory.domain.ai

import android.util.Base64
import com.google.gson.Gson
import com.voicememory.data.model.Emotion
import com.voicememory.data.remote.SparkAIConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SparkAIService @Inject constructor() {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    
    /**
     * 生成智能摘要
     */
    suspend fun generateSummary(text: String): Result<String> = withContext(Dispatchers.IO) {
        if (text.length < 50) {
            return@withContext Result.success(text)
        }
        
        val prompt = """
            请为以下语音日记生成一个简洁的摘要（3-5句话）：
            
            $text
            
            要求：
            1. 提取核心内容
            2. 保留关键情绪
            3. 语言简洁流畅
        """.trimIndent()
        
        callSparkAPI(prompt)
    }
    
    /**
     * 深度情绪分析
     */
    suspend fun analyzeEmotion(text: String): Result<EmotionAnalysisResult> = withContext(Dispatchers.IO) {
        val prompt = """
            请分析以下语音日记的情绪，并以 JSON 格式返回：
            
            $text
            
            返回格式：
            {
              "emotion": "开心/平静/焦虑/悲伤/兴奋/中性",
              "intensity": 0.8,
              "reason": "情绪产生的原因",
              "suggestion": "给用户的建议"
            }
        """.trimIndent()
        
        try {
            val result = callSparkAPI(prompt)
            result.fold(
                onSuccess = { response ->
                    val json = JSONObject(response)
                    val emotionStr = json.getString("emotion")
                    val emotion = when {
                        emotionStr.contains("开心") -> Emotion.HAPPY
                        emotionStr.contains("平静") -> Emotion.CALM
                        emotionStr.contains("焦虑") -> Emotion.ANXIOUS
                        emotionStr.contains("悲伤") -> Emotion.SAD
                        emotionStr.contains("兴奋") -> Emotion.EXCITED
                        else -> Emotion.NEUTRAL
                    }
                    
                    Result.success(
                        EmotionAnalysisResult(
                            emotion = emotion,
                            intensity = json.getDouble("intensity").toFloat(),
                            reason = json.getString("reason"),
                            suggestion = json.getString("suggestion")
                        )
                    )
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 提取主题标签
     */
    suspend fun extractTags(text: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val prompt = """
            请为以下语音日记提取 3-5 个主题标签（用逗号分隔）：
            
            $text
            
            要求：
            1. 标签简洁（2-4个字）
            2. 反映核心主题
            3. 只返回标签，用逗号分隔
        """.trimIndent()
        
        try {
            val result = callSparkAPI(prompt)
            result.fold(
                onSuccess = { response ->
                    val tags = response.split("，", ",")
                        .map { it.trim().removePrefix("#") }
                        .filter { it.isNotEmpty() }
                        .take(5)
                    Result.success(tags)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * AI 对话
     */
    suspend fun chat(
        userMessage: String,
        context: List<String> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val contextText = if (context.isNotEmpty()) {
            "用户的历史录音摘要：\n${context.joinToString("\n")}\n\n"
        } else ""
        
        val prompt = """
            ${contextText}用户问题：$userMessage
            
            请作为一个温暖、专业的心理陪伴助手回答。
        """.trimIndent()
        
        callSparkAPI(prompt)
    }
    
    /**
     * 生成趋势分析报告
     */
    suspend fun generateTrendReport(
        entries: List<String>,
        period: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val allText = entries.joinToString("\n---\n")
        
        val prompt = """
            请分析以下${period}的语音日记，生成一份趋势分析报告：
            
            $allText
            
            报告内容：
            1. 整体情绪趋势
            2. 主要话题和关注点
            3. 情绪变化模式
            4. 给用户的建议
        """.trimIndent()
        
        callSparkAPI(prompt)
    }
    
    /**
     * 调用星火大模型 API
     */
    private suspend fun callSparkAPI(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val authUrl = getAuthUrl()
            val result = StringBuilder()
            var error: String? = null
            
            val request = Request.Builder()
                .url(authUrl)
                .build()
            
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    val params = JSONObject().apply {
                        put("header", JSONObject().apply {
                            put("app_id", SparkAIConfig.APP_ID)
                            put("uid", "user_${System.currentTimeMillis()}")
                        })
                        put("parameter", JSONObject().apply {
                            put("chat", JSONObject().apply {
                                put("domain", "generalv3.5")
                                put("temperature", 0.7)
                                put("max_tokens", 2048)
                            })
                        })
                        put("payload", JSONObject().apply {
                            put("message", JSONObject().apply {
                                put("text", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("role", "user")
                                        put("content", prompt)
                                    })
                                })
                            })
                        })
                    }
                    
                    webSocket.send(params.toString())
                }
                
                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val json = JSONObject(text)
                        val code = json.getJSONObject("header").getInt("code")
                        
                        if (code != 0) {
                            error = json.getJSONObject("header").optString("message", "API 调用失败")
                            webSocket.close(1000, "Error")
                            return
                        }
                        
                        val payload = json.optJSONObject("payload")
                        val choices = payload?.optJSONObject("choices")
                        val textArray = choices?.optJSONArray("text")
                        
                        if (textArray != null && textArray.length() > 0) {
                            val content = textArray.getJSONObject(0).getString("content")
                            result.append(content)
                        }
                        
                        // 检查是否结束
                        val status = json.getJSONObject("header").optInt("status", 0)
                        if (status == 2) {
                            webSocket.close(1000, "Finished")
                        }
                    } catch (e: Exception) {
                        error = e.message
                        webSocket.close(1000, "Error")
                    }
                }
                
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    error = t.message ?: "连接失败"
                }
            }
            
            client.newWebSocket(request, listener)
            
            // 等待响应（最多 30 秒）
            var waitTime = 0
            while (error == null && result.isEmpty() && waitTime < 30000) {
                Thread.sleep(100)
                waitTime += 100
            }
            
            if (error != null) {
                Result.failure(Exception(error))
            } else if (result.isEmpty()) {
                Result.failure(Exception("API 响应超时"))
            } else {
                Result.success(result.toString())
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getAuthUrl(): String {
        val url = URL(SparkAIConfig.SPARK_URL)
        val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }.format(Date())
        
        val builder = StringBuilder("host: ").append(url.host).append("\n")
            .append("date: ").append(date).append("\n")
            .append("GET ").append(url.path).append(" HTTP/1.1")
        
        val sha = hmacSHA256(builder.toString(), SparkAIConfig.API_SECRET)
        val authorization = Base64.encodeToString(sha, Base64.NO_WRAP)
        
        val authBase = "api_key=\"${SparkAIConfig.API_KEY}\", " +
                "algorithm=\"hmac-sha256\", " +
                "headers=\"host date request-line\", " +
                "signature=\"$authorization\""
        
        val authorizationBase64 = Base64.encodeToString(
            authBase.toByteArray(Charset.forName("UTF-8")),
            Base64.NO_WRAP
        )
        
        return "${SparkAIConfig.SPARK_URL}?authorization=$authorizationBase64&date=$date&host=${url.host}"
    }
    
    private fun hmacSHA256(data: String, key: String): ByteArray {
        val secretKey = SecretKeySpec(key.toByteArray(Charset.forName("UTF-8")), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)
        return mac.doFinal(data.toByteArray(Charset.forName("UTF-8")))
    }
}

data class EmotionAnalysisResult(
    val emotion: Emotion,
    val intensity: Float,
    val reason: String,
    val suggestion: String
)
