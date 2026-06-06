package com.example.data

import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getAiResponse(
        projectTitle: String,
        projectDesc: String,
        conversationContext: List<Pair<String, Boolean>>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "¡Hola! Para activar mi chat flotante interactivo y proveer soporte en vivo, ingresa tu API Key de Gemini en el panel de Secrets de AI Studio. Mientras tanto, dime: ¿en qué puedo ayudarte hoy?"
        }

        val url = "$BASE_URL/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val contentsArray = JSONArray()

        // Construct conversational history
        conversationContext.takeLast(12).forEach { (message, isUser) ->
            val messageObj = JSONObject()
            messageObj.put("role", if (isUser) "user" else "model")
            
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", message)
            partsArray.put(partObj)
            
            messageObj.put("parts", partsArray)
            contentsArray.put(messageObj)
        }

        val systemInstructionText = """
            Eres la Inteligencia del Laboratorio EcoReEngine (Asistente Técnico Móvil).
            Ayudas al usuario a construir inventos tecnológicos sustentables con deshechos, aclarando dudas sobre: '$projectTitle' ($projectDesc).
            PAUTAS ESENCIALES:
            1. Sé amigable, optimista y conciso. Evita tecnicismos aburridos; usa explicaciones didácticas con analogías sencillas del mundo real.
            2. SEGURIDAD ANTE TODO: Alerta de manera explícita y activa sobre los riesgos de los proyectos (ej. cuidado de quemaduras con soldador de estaño, verificar cortocircuitos con multímetro casero, y una advertencia gigante de NUNCA soldar con calor directo sobre celdas de litio 18650, usar soportes magnéticos o cinta, o no perforarlas porque pueden incendiarse).
            3. Identificación: Indica si los materiales que consulta se pueden extraer de chatarra común (como reproductores de CD, impresoras o juguetes viejos).
            4. Responde siempre en español. No extiendas tus explicaciones a más de 3 párrafos cortos.
        """.trimIndent()

        val root = JSONObject()
        root.put("contents", contentsArray)

        // Inject configuration
        val systemInstructionObj = JSONObject()
        val sysPartsArray = JSONArray()
        val sysPartObj = JSONObject()
        sysPartObj.put("text", systemInstructionText)
        sysPartsArray.put(sysPartObj)
        systemInstructionObj.put("parts", sysPartsArray)
        root.put("systemInstruction", systemInstructionObj)

        val requestBodyText = root.toString()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestBodyText.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Oh, mi satélite de servicio científico reportó un error de enlace (Código ${response.code}). ¡Revisa los datos de conexión!"
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val contentObj = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No tengo una respuesta en este momento.")
                    }
                }
                return@withContext "Recibí un flujo de señal incomprensible del laboratorio sustentable."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Parece que estamos fuera de red: ${e.localizedMessage ?: "Verifica tu conexión a internet"}"
        }
    }
}
