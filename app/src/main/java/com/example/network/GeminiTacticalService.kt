package com.example.network

import com.example.BuildConfig
import com.example.model.DrillDifficulty
import com.example.model.DrillQuestion
import com.example.model.TacticalTier
import com.example.model.ThreatSeverity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiRawApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body requestBody: RequestBody
    ): ResponseBody
}

sealed class AiInfiltrationResult {
    data class Success(
        val questions: List<DrillQuestion>,
        val rawJsonArray: String
    ) : AiInfiltrationResult()

    data class ApiKeyMissing(val message: String) : AiInfiltrationResult()
    data class Error(val message: String) : AiInfiltrationResult()
}

object GeminiTacticalClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val apiService: GeminiRawApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(GeminiRawApiService::class.java)
    }

    suspend fun synthesizeTacticalScenarios(
        domainFilter: String,
        difficultyFilter: DrillDifficulty,
        tier: TacticalTier,
        count: Int = 3
    ): AiInfiltrationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext AiInfiltrationResult.ApiKeyMissing(
                "GEMINI_API_KEY is not configured. Configure your key in the AI Studio Secrets panel to enable live Gemini API synthesis."
            )
        }

        val targetDomainPrompt = if (domainFilter == "ALL") {
            "across diverse CompTIA Security+ (SY0-701) domains (1.0 General Security Concepts, 2.0 Threats/Vulnerabilities/Mitigations, 3.0 Security Architecture, 4.0 Security Operations, 5.0 Program Management)"
        } else {
            "strictly focused on CompTIA Security+ (SY0-701) Domain $domainFilter"
        }

        val targetDifficultyPrompt = if (difficultyFilter == DrillDifficulty.ALL) {
            "spanning EASY, MEDIUM, and HARD difficulty levels"
        } else {
            "strictly calibrated to ${difficultyFilter.id} difficulty (${difficultyFilter.tacticalTitle}: ${difficultyFilter.description})"
        }

        val prompt = """
            Synthesize exactly $count realistic CompTIA Security+ (SY0-701) tactical examination scenarios $targetDomainPrompt and $targetDifficultyPrompt at tier ${tier.label}.
            Each scenario must read like an enterprise cyber-defense incident or architecture audit with concrete technical telemetry.
            Ensure:
            - vectorId follows the format "VEC-AI-701-XXX" where XXX is a 3-digit hex/tactical code.
            - domainCode is one of "1.0", "2.0", "3.0", "4.0", "5.0".
            - domainTitle is the full CompTIA SY0-701 domain title (e.g., "2.0 THREATS, VULNERABILITIES & MITIGATIONS").
            - difficulty is one of "EASY", "MEDIUM", "HARD".
            - severity is one of "CRITICAL", "HIGH", "ELEVATED".
            - options is an array of exactly 4 plausible, technical options (no "A)" prefixes inside the text; the HUD renders prefixes).
            - correctIndex is the 0-based integer index (0, 1, 2, or 3) of the best answer.
            - rationale is a thorough forensic diagnostic explanation detailing why the correct option is optimal and why the other options fail.
            - commandReference is a realistic CLI diagnostic command relevant to the scenario.
        """.trimIndent()

        val payloadJson = buildStructuredRequestJson(prompt)
        val requestBody = payloadJson.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        try {
            val responseBody = apiService.generateContent(apiKey, requestBody)
            val rawResponse = responseBody.string()
            val root = JSONObject(rawResponse)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text", "")?.trim() ?: ""

            if (rawText.isEmpty()) {
                return@withContext AiInfiltrationResult.Error("Empty response payload from Gemini neural uplink.")
            }

            val cleanJsonText = rawText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonArray = JSONArray(cleanJsonText)
            val synthesizedList = mutableListOf<DrillQuestion>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val optionsJson = item.getJSONArray("options")
                val optionsList = mutableListOf<String>()
                for (j in 0 until optionsJson.length()) {
                    optionsList.add(optionsJson.getString(j))
                }
                while (optionsList.size < 4) {
                    optionsList.add("Implement compensatory network segmentation and continuous SIEM monitoring")
                }

                val severityStr = item.optString("severity", "CRITICAL").uppercase()
                val severity = when (severityStr) {
                    "HIGH" -> ThreatSeverity.HIGH
                    "ELEVATED" -> ThreatSeverity.ELEVATED
                    else -> ThreatSeverity.CRITICAL
                }

                val difficultyStr = item.optString("difficulty", difficultyFilter.id).uppercase()
                val parsedDifficulty = when (difficultyStr) {
                    "EASY" -> DrillDifficulty.EASY
                    "HARD" -> DrillDifficulty.HARD
                    else -> DrillDifficulty.MEDIUM
                }

                val correctIdx = item.optInt("correctIndex", 0).coerceIn(0, 3)

                synthesizedList.add(
                    DrillQuestion(
                        vectorId = item.optString("vectorId", "VEC-AI-701-${100 + i}"),
                        domainCode = item.optString("domainCode", "2.0"),
                        domainTitle = item.optString("domainTitle", "2.0 THREATS, VULNERABILITIES & MITIGATIONS"),
                        severity = severity,
                        difficulty = parsedDifficulty,
                        tier = tier,
                        isAiSynthesized = true,
                        question = item.getString("question"),
                        options = optionsList.take(4),
                        correctIndex = correctIdx,
                        rationale = item.getString("rationale"),
                        commandReference = item.optString(
                            "commandReference",
                            "journalctl -u secplus-edr.service --since '5 min ago'"
                        )
                    )
                )
            }

            if (synthesizedList.isEmpty()) {
                AiInfiltrationResult.Error("JSON array parsed 0 scenarios from uplink.")
            } else {
                AiInfiltrationResult.Success(
                    questions = synthesizedList,
                    rawJsonArray = jsonArray.toString(2)
                )
            }
        } catch (e: Exception) {
            AiInfiltrationResult.Error(
                "UPLINK EXCEPTION: ${e.localizedMessage ?: "Failed to reach Gemini endpoint"}"
            )
        }
    }

    private fun buildStructuredRequestJson(prompt: String): JSONObject {
        val schemaProperties = JSONObject().apply {
            put("vectorId", JSONObject().put("type", "STRING").put("description", "Tactical vector ID such as VEC-AI-701-804"))
            put("domainCode", JSONObject().put("type", "STRING").put("description", "Domain number: 1.0, 2.0, 3.0, 4.0, or 5.0"))
            put("domainTitle", JSONObject().put("type", "STRING").put("description", "Full CompTIA SY0-701 domain name"))
            put("difficulty", JSONObject().put("type", "STRING").put("description", "Difficulty level: EASY, MEDIUM, or HARD"))
            put("severity", JSONObject().put("type", "STRING").put("description", "CRITICAL, HIGH, or ELEVATED"))
            put("question", JSONObject().put("type", "STRING").put("description", "Detailed CompTIA Security+ scenario stem"))
            put(
                "options",
                JSONObject()
                    .put("type", "ARRAY")
                    .put("items", JSONObject().put("type", "STRING"))
                    .put("description", "Exactly 4 multiple-choice options")
            )
            put("correctIndex", JSONObject().put("type", "INTEGER").put("description", "0-based index (0 to 3) of the correct option"))
            put("rationale", JSONObject().put("type", "STRING").put("description", "Technical rationale explaining correct and incorrect choices"))
            put("commandReference", JSONObject().put("type", "STRING").put("description", "Relevant security CLI command"))
        }

        val requiredFields = JSONArray().apply {
            put("vectorId")
            put("domainCode")
            put("domainTitle")
            put("difficulty")
            put("severity")
            put("question")
            put("options")
            put("correctIndex")
            put("rationale")
            put("commandReference")
        }

        val itemSchema = JSONObject().apply {
            put("type", "OBJECT")
            put("properties", schemaProperties)
            put("required", requiredFields)
        }

        val arraySchema = JSONObject().apply {
            put("type", "ARRAY")
            put("items", itemSchema)
        }

        val generationConfig = JSONObject().apply {
            put("responseMimeType", "application/json")
            put("responseSchema", arraySchema)
            put("temperature", 0.65)
        }

        val systemInstruction = JSONObject().apply {
            put(
                "parts",
                JSONArray().put(
                    JSONObject().put(
                        "text",
                        "You are the CompTIA Security+ (SY0-701) Tactical Drill Engine AI. Output strictly valid JSON arrays conforming to the schema."
                    )
                )
            )
        }

        val contents = JSONArray().put(
            JSONObject().put(
                "parts",
                JSONArray().put(JSONObject().put("text", prompt))
            )
        )

        return JSONObject().apply {
            put("systemInstruction", systemInstruction)
            put("contents", contents)
            put("generationConfig", generationConfig)
        }
    }
}
