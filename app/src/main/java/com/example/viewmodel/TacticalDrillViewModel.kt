package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.TacticalSoundEffect
import com.example.audio.TacticalSoundManager
import com.example.data.SecurityPlusRepository
import com.example.model.ConnectionState
import com.example.model.DomainProficiencySummary
import com.example.model.DrillDifficulty
import com.example.model.DrillQuestion
import com.example.model.PaywallTriggerReason
import com.example.model.ProficiencyTimePoint
import com.example.model.SecurityDomain
import com.example.model.TacticalTier
import com.example.model.TacticalViewMode
import com.example.network.AiInfiltrationResult
import com.example.network.GeminiTacticalClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DomainAttemptStats(
    val attempts: Int = 0,
    val correct: Int = 0
)

private val InitialProficiencyHistory = listOf(
    ProficiencyTimePoint(
        timestampLabel = "T-5",
        sessionTag = "CYCLE 01 // BASELINE",
        domainScores = mapOf(
            "1.0" to 62,
            "2.0" to 54,
            "3.0" to 58,
            "4.0" to 49,
            "5.0" to 66
        )
    ),
    ProficiencyTimePoint(
        timestampLabel = "T-4",
        sessionTag = "CYCLE 02 // RECON",
        domainScores = mapOf(
            "1.0" to 68,
            "2.0" to 61,
            "3.0" to 64,
            "4.0" to 56,
            "5.0" to 70
        )
    ),
    ProficiencyTimePoint(
        timestampLabel = "T-3",
        sessionTag = "CYCLE 03 // HARDENING",
        domainScores = mapOf(
            "1.0" to 72,
            "2.0" to 67,
            "3.0" to 71,
            "4.0" to 63,
            "5.0" to 74
        )
    ),
    ProficiencyTimePoint(
        timestampLabel = "T-2",
        sessionTag = "CYCLE 04 // RED-TEAM",
        domainScores = mapOf(
            "1.0" to 76,
            "2.0" to 73,
            "3.0" to 69,
            "4.0" to 71,
            "5.0" to 78
        )
    ),
    ProficiencyTimePoint(
        timestampLabel = "T-1",
        sessionTag = "CYCLE 05 // PRE-AUDIT",
        domainScores = mapOf(
            "1.0" to 81,
            "2.0" to 78,
            "3.0" to 75,
            "4.0" to 74,
            "5.0" to 82
        )
    ),
    ProficiencyTimePoint(
        timestampLabel = "LIVE",
        sessionTag = "ACTIVE SESSION",
        domainScores = mapOf(
            "1.0" to 84,
            "2.0" to 82,
            "3.0" to 79,
            "4.0" to 77,
            "5.0" to 85
        )
    )
)

data class TacticalDrillUiState(
    val viewMode: TacticalViewMode = TacticalViewMode.DRILL_DECK,
    val allQuestions: List<DrillQuestion> = SecurityPlusRepository.getInitialTacticalDeck(),
    val selectedDomain: SecurityDomain = SecurityDomain.ALL,
    val selectedDifficulty: DrillDifficulty = DrillDifficulty.ALL,
    val selectedTier: TacticalTier = TacticalTier.TIER_1,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isEvaluated: Boolean = false,
    val scorePoints: Int = 0,
    val streakCount: Int = 0,
    val evaluatedCount: Int = 0,
    val correctCount: Int = 0,
    val lastPointsDelta: Int = 0,
    val hasOmegaClearance: Boolean = false,
    val activePaywallReason: PaywallTriggerReason? = null,
    val pendingTierRequest: TacticalTier? = null,
    val pendingAiSynthesis: Boolean = false,
    val isSynthesizingAi: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.SECURE_ONLINE,
    val latestAiJsonPayload: String? = null,
    val showJsonTelemetryPanel: Boolean = false,
    val aiStatusBanner: String? = null,
    val isAudioEnabled: Boolean = TacticalSoundManager.isSoundEnabled,
    val lastSoundEventCode: String = "SFX_READY // 44.1KHZ",
    val proficiencyHistory: List<ProficiencyTimePoint> = InitialProficiencyHistory,
    val selectedTimePointIndex: Int = InitialProficiencyHistory.lastIndex,
    val enabledChartDomains: Set<String> = setOf("2.0", "3.0", "4.0", "1.0", "5.0"),
    val domainStats: Map<String, DomainAttemptStats> = mapOf(
        "1.0" to DomainAttemptStats(attempts = 12, correct = 10),
        "2.0" to DomainAttemptStats(attempts = 16, correct = 13),
        "3.0" to DomainAttemptStats(attempts = 14, correct = 11),
        "4.0" to DomainAttemptStats(attempts = 13, correct = 10),
        "5.0" to DomainAttemptStats(attempts = 10, correct = 8)
    ),
    val terminalLogs: List<String> = listOf(
        "[00:00.01] KERNEL BOOT: CompTIA Security+ (SY0-701) Tactical Drill Engine v7.5",
        "[00:00.04] AUDIO DSP: 16-bit PCM Tactical Acoustic Synthesizer ONLINE",
        "[00:00.09] DECK LOADED: 15 multi-difficulty (EASY/MEDIUM/HARD) vectors online",
        "[00:00.12] CLEARANCE: Level-1 Standard Field Ops [RESTRICTED TIER]"
    )
) {
    val filteredDeck: List<DrillQuestion>
        get() {
            val tierFiltered = allQuestions.filter { question ->
                if (hasOmegaClearance) {
                    question.tier == selectedTier || question.isAiSynthesized
                } else {
                    question.tier == TacticalTier.TIER_1
                }
            }.ifEmpty { allQuestions }

            val domainFiltered = if (selectedDomain == SecurityDomain.ALL) {
                tierFiltered
            } else {
                tierFiltered.filter { it.domainCode == selectedDomain.code }.ifEmpty { tierFiltered }
            }

            return if (selectedDifficulty == DrillDifficulty.ALL) {
                domainFiltered
            } else {
                domainFiltered.filter { it.difficulty == selectedDifficulty }.ifEmpty {
                    allQuestions.filter { it.difficulty == selectedDifficulty }.ifEmpty { domainFiltered }
                }
            }
        }

    val currentQuestion: DrillQuestion
        get() = filteredDeck.getOrElse(currentQuestionIndex.coerceIn(0, (filteredDeck.size - 1).coerceAtLeast(0))) {
            allQuestions.first()
        }

    val currentQuestionPotentialPoints: Int
        get() = (currentQuestion.difficulty.basePoints * selectedTier.pointMultiplier) +
            (streakCount * currentQuestion.difficulty.streakBonus)

    val accuracyPercent: Int
        get() = if (evaluatedCount == 0) 100 else ((correctCount * 100f) / evaluatedCount).toInt()

    val domainSummaries: List<DomainProficiencySummary>
        get() {
            val latest = proficiencyHistory.lastOrNull()?.domainScores.orEmpty()
            val prev = proficiencyHistory.getOrNull(proficiencyHistory.size - 2)?.domainScores ?: latest
            return listOf(
                SecurityDomain.DOM_2, // Threats
                SecurityDomain.DOM_3, // Architecture
                SecurityDomain.DOM_4, // Implementation & Ops
                SecurityDomain.DOM_1, // General Security Concepts
                SecurityDomain.DOM_5  // Program Management & Oversight
            ).map { domain ->
                val stats = domainStats[domain.code] ?: DomainAttemptStats()
                DomainProficiencySummary(
                    domain = domain,
                    currentScore = latest[domain.code] ?: 75,
                    previousScore = prev[domain.code] ?: 70,
                    attemptsCount = stats.attempts,
                    correctCount = stats.correct
                )
            }
        }

    val overallProficiencyIndex: Int
        get() = proficiencyHistory.lastOrNull()?.compositeAverage ?: 81
}

class TacticalDrillViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TacticalDrillUiState())
    val uiState: StateFlow<TacticalDrillUiState> = _uiState.asStateFlow()

    private fun emitTacticalSound(effect: TacticalSoundEffect) {
        TacticalSoundManager.play(effect)
    }

    fun toggleAudioFeedback() {
        val enabled = TacticalSoundManager.toggleSound()
        val statusLabel = if (enabled) "SFX_ONLINE // 44.1KHZ" else "SFX_MUTED // SILENT"
        val logEntry = if (enabled) {
            "[AUDIO DSP] Tactical acoustic feedback UNMUTED (44.1kHz PCM)"
        } else {
            "[AUDIO DSP] Tactical acoustic feedback MUTED (Silent Ops)"
        }
        _uiState.update { state ->
            state.copy(
                isAudioEnabled = enabled,
                lastSoundEventCode = statusLabel,
                terminalLogs = (state.terminalLogs + logEntry).takeLast(18)
            )
        }
    }

    fun selectDifficulty(difficulty: DrillDifficulty) {
        emitTacticalSound(TacticalSoundEffect.VECTOR_CYCLE)
        _uiState.update { state ->
            state.copy(
                selectedDifficulty = difficulty,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isEvaluated = false,
                lastPointsDelta = 0,
                lastSoundEventCode = TacticalSoundEffect.VECTOR_CYCLE.code,
                terminalLogs = (state.terminalLogs +
                    "[DIFFICULTY] Calibrated to ${difficulty.tacticalTitle}").takeLast(18)
            )
        }
    }

    fun setViewMode(mode: TacticalViewMode) {
        emitTacticalSound(TacticalSoundEffect.VECTOR_CYCLE)
        _uiState.update { state ->
            state.copy(
                viewMode = mode,
                lastSoundEventCode = TacticalSoundEffect.VECTOR_CYCLE.code,
                terminalLogs = (state.terminalLogs + "[HUD VIEW] Switched active console to ${mode.label}").takeLast(18)
            )
        }
    }

    fun selectTimePointIndex(index: Int) {
        emitTacticalSound(TacticalSoundEffect.TERMINAL_CLICK)
        _uiState.update { state ->
            val coerced = index.coerceIn(0, (state.proficiencyHistory.size - 1).coerceAtLeast(0))
            state.copy(
                selectedTimePointIndex = coerced,
                lastSoundEventCode = TacticalSoundEffect.TERMINAL_CLICK.code
            )
        }
    }

    fun toggleChartDomainSeries(domainCode: String) {
        emitTacticalSound(TacticalSoundEffect.TERMINAL_CLICK)
        _uiState.update { state ->
            val current = state.enabledChartDomains.toMutableSet()
            if (current.contains(domainCode) && current.size > 1) {
                current.remove(domainCode)
            } else {
                current.add(domainCode)
            }
            state.copy(
                enabledChartDomains = current,
                lastSoundEventCode = TacticalSoundEffect.TERMINAL_CLICK.code
            )
        }
    }

    fun enableAllChartDomains() {
        emitTacticalSound(TacticalSoundEffect.TERMINAL_CLICK)
        _uiState.update { state ->
            state.copy(
                enabledChartDomains = setOf("1.0", "2.0", "3.0", "4.0", "5.0"),
                lastSoundEventCode = TacticalSoundEffect.TERMINAL_CLICK.code
            )
        }
    }

    fun drillSpecificDomain(domain: SecurityDomain) {
        emitTacticalSound(TacticalSoundEffect.VECTOR_CYCLE)
        _uiState.update { state ->
            state.copy(
                selectedDomain = domain,
                viewMode = TacticalViewMode.DRILL_DECK,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isEvaluated = false,
                lastSoundEventCode = TacticalSoundEffect.VECTOR_CYCLE.code,
                terminalLogs = (state.terminalLogs + "[TARGET LOCK] Drilling ${domain.fullTitle}").takeLast(18)
            )
        }
    }

    fun logCalibrationSnapshot() {
        emitTacticalSound(TacticalSoundEffect.EVAL_VERIFIED)
        _uiState.update { state ->
            val currentHistory = state.proficiencyHistory
            val latest = currentHistory.lastOrNull()?.domainScores ?: emptyMap()
            val cycleNumber = currentHistory.size + 1
            val updatedScores = latest.mapValues { (code, score) ->
                val boost = when (code) {
                    "2.0" -> 3
                    "3.0" -> 4
                    "4.0" -> 3
                    else -> 2
                }
                (score + boost).coerceIn(35, 99)
            }
            val archivedPrevious = currentHistory.mapIndexed { idx, pt ->
                if (idx == currentHistory.lastIndex) {
                    pt.copy(
                        timestampLabel = "T-1",
                        sessionTag = "CYCLE 0${cycleNumber - 1} // VERIFIED"
                    )
                } else {
                    val stepsBack = currentHistory.size - idx
                    pt.copy(timestampLabel = "T-$stepsBack")
                }
            }.takeLast(5)

            val newLivePoint = ProficiencyTimePoint(
                timestampLabel = "LIVE",
                sessionTag = "CYCLE 0$cycleNumber // CALIBRATED",
                domainScores = updatedScores
            )
            val newList = archivedPrevious + newLivePoint
            state.copy(
                proficiencyHistory = newList,
                selectedTimePointIndex = newList.lastIndex,
                lastSoundEventCode = TacticalSoundEffect.EVAL_VERIFIED.code,
                terminalLogs = (state.terminalLogs + "[TELEMETRY SNAPSHOT] Cycle 0$cycleNumber proficiency vectors committed").takeLast(18)
            )
        }
    }

    fun selectOption(index: Int) {
        if (_uiState.value.isEvaluated) return
        emitTacticalSound(TacticalSoundEffect.TERMINAL_CLICK)
        _uiState.update { state ->
            state.copy(
                selectedOptionIndex = index,
                lastSoundEventCode = TacticalSoundEffect.TERMINAL_CLICK.code
            )
        }
    }

    fun executeEvaluation() {
        val state = _uiState.value
        val chosen = state.selectedOptionIndex ?: return
        if (state.isEvaluated) return

        val question = state.currentQuestion
        val isCorrect = chosen == question.correctIndex
        val soundEffect = if (isCorrect) {
            TacticalSoundEffect.EVAL_VERIFIED
        } else {
            TacticalSoundEffect.EVAL_BREACH_ALERT
        }
        emitTacticalSound(soundEffect)

        val multiplier = state.selectedTier.pointMultiplier
        val basePts = question.difficulty.basePoints
        val streakBonus = if (isCorrect) state.streakCount * question.difficulty.streakBonus else 0
        val pointsEarned = if (isCorrect) (basePts * multiplier) + streakBonus else 0
        val newStreak = if (isCorrect) state.streakCount + 1 else 0

        val logLine = if (isCorrect) {
            "[EVAL PASS] ${question.vectorId} [${question.difficulty.id}] mitigated (+$pointsEarned PTS | ${soundEffect.code})"
        } else {
            "[EVAL FAIL] ${question.vectorId} [${question.difficulty.id}] breach — Option [${'A' + chosen}] rejected (${soundEffect.code})"
        }

        // Update domain-specific stats and live proficiency curve scaled by difficulty
        val domainCode = question.domainCode
        val existingStats = state.domainStats[domainCode] ?: DomainAttemptStats(10, 8)
        val updatedDomainStats = state.domainStats.toMutableMap().apply {
            put(
                domainCode,
                existingStats.copy(
                    attempts = existingStats.attempts + 1,
                    correct = existingStats.correct + (if (isCorrect) 1 else 0)
                )
            )
        }

        val updatedHistory = state.proficiencyHistory.toMutableList()
        if (updatedHistory.isNotEmpty()) {
            val lastIdx = updatedHistory.lastIndex
            val lastPoint = updatedHistory[lastIdx]
            val currentDomainScore = lastPoint.domainScores[domainCode] ?: 78
            val proficiencyDelta = when {
                isCorrect && question.difficulty == DrillDifficulty.HARD -> 6
                isCorrect && question.difficulty == DrillDifficulty.MEDIUM -> 4
                isCorrect -> 3
                question.difficulty == DrillDifficulty.EASY -> -6
                else -> -4
            }
            val newDomainScore = (currentDomainScore + proficiencyDelta).coerceIn(25, 99)
            val newScores = lastPoint.domainScores.toMutableMap().apply {
                put(domainCode, newDomainScore)
            }
            updatedHistory[lastIdx] = lastPoint.copy(domainScores = newScores)
        }

        _uiState.update { current ->
            current.copy(
                isEvaluated = true,
                scorePoints = current.scorePoints + pointsEarned,
                streakCount = newStreak,
                evaluatedCount = current.evaluatedCount + 1,
                correctCount = current.correctCount + (if (isCorrect) 1 else 0),
                lastPointsDelta = pointsEarned,
                domainStats = updatedDomainStats,
                proficiencyHistory = updatedHistory,
                selectedTimePointIndex = updatedHistory.lastIndex,
                lastSoundEventCode = soundEffect.code,
                terminalLogs = (current.terminalLogs + logLine).takeLast(18)
            )
        }
    }

    fun advanceNextVector() {
        emitTacticalSound(TacticalSoundEffect.VECTOR_CYCLE)
        _uiState.update { state ->
            val deckSize = state.filteredDeck.size.coerceAtLeast(1)
            val nextIdx = (state.currentQuestionIndex + 1) % deckSize
            state.copy(
                currentQuestionIndex = nextIdx,
                selectedOptionIndex = null,
                isEvaluated = false,
                lastPointsDelta = 0,
                lastSoundEventCode = TacticalSoundEffect.VECTOR_CYCLE.code
            )
        }
    }

    fun selectPreviousVector() {
        emitTacticalSound(TacticalSoundEffect.VECTOR_CYCLE)
        _uiState.update { state ->
            val deckSize = state.filteredDeck.size.coerceAtLeast(1)
            val prevIdx = if (state.currentQuestionIndex - 1 < 0) deckSize - 1 else state.currentQuestionIndex - 1
            state.copy(
                currentQuestionIndex = prevIdx,
                selectedOptionIndex = null,
                isEvaluated = false,
                lastPointsDelta = 0,
                lastSoundEventCode = TacticalSoundEffect.VECTOR_CYCLE.code
            )
        }
    }

    fun selectDomain(domain: SecurityDomain) {
        emitTacticalSound(TacticalSoundEffect.TERMINAL_CLICK)
        _uiState.update { state ->
            state.copy(
                selectedDomain = domain,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isEvaluated = false,
                lastSoundEventCode = TacticalSoundEffect.TERMINAL_CLICK.code,
                terminalLogs = (state.terminalLogs + "[FILTER] Domain locked to ${domain.fullTitle}").takeLast(18)
            )
        }
    }

    fun attemptSelectTier(tier: TacticalTier) {
        val state = _uiState.value
        if (tier.requiresClearance && !state.hasOmegaClearance) {
            emitTacticalSound(TacticalSoundEffect.PAYWALL_INTERCEPT)
            _uiState.update {
                it.copy(
                    activePaywallReason = PaywallTriggerReason.ADVANCED_TIER,
                    pendingTierRequest = tier,
                    pendingAiSynthesis = false,
                    lastSoundEventCode = TacticalSoundEffect.PAYWALL_INTERCEPT.code,
                    terminalLogs = (it.terminalLogs + "[INTERCEPT] ${tier.label} blocked — $9.99/mo Clearance required").takeLast(18)
                )
            }
        } else {
            emitTacticalSound(TacticalSoundEffect.VECTOR_CYCLE)
            _uiState.update {
                it.copy(
                    selectedTier = tier,
                    currentQuestionIndex = 0,
                    selectedOptionIndex = null,
                    isEvaluated = false,
                    lastSoundEventCode = TacticalSoundEffect.VECTOR_CYCLE.code,
                    terminalLogs = (it.terminalLogs + "[TIER SHIFT] Engaged ${tier.label}").takeLast(18)
                )
            }
        }
    }

    fun attemptAiInfiltration() {
        val state = _uiState.value
        if (!state.hasOmegaClearance) {
            emitTacticalSound(TacticalSoundEffect.PAYWALL_INTERCEPT)
            _uiState.update {
                it.copy(
                    activePaywallReason = PaywallTriggerReason.AI_INFILTRATION,
                    pendingAiSynthesis = true,
                    pendingTierRequest = null,
                    lastSoundEventCode = TacticalSoundEffect.PAYWALL_INTERCEPT.code,
                    terminalLogs = (it.terminalLogs + "[INTERCEPT] Gemini AI Infiltration gated by Paywall Protocol").takeLast(18)
                )
            }
        } else {
            executeLiveGeminiSynthesis()
        }
    }

    fun openClearanceModal() {
        emitTacticalSound(TacticalSoundEffect.PAYWALL_INTERCEPT)
        _uiState.update {
            it.copy(
                activePaywallReason = PaywallTriggerReason.MANUAL_UPGRADE,
                pendingAiSynthesis = false,
                pendingTierRequest = null,
                lastSoundEventCode = TacticalSoundEffect.PAYWALL_INTERCEPT.code
            )
        }
    }

    fun dismissPaywall() {
        emitTacticalSound(TacticalSoundEffect.TERMINAL_CLICK)
        _uiState.update {
            it.copy(
                activePaywallReason = null,
                pendingTierRequest = null,
                pendingAiSynthesis = false,
                lastSoundEventCode = TacticalSoundEffect.TERMINAL_CLICK.code
            )
        }
    }

    fun authorizeClearanceAndProceed() {
        val state = _uiState.value
        val targetTier = state.pendingTierRequest ?: state.selectedTier
        val shouldSynthesize = state.pendingAiSynthesis
        emitTacticalSound(TacticalSoundEffect.CLEARANCE_GRANTED)

        _uiState.update {
            it.copy(
                hasOmegaClearance = true,
                selectedTier = targetTier,
                activePaywallReason = null,
                pendingTierRequest = null,
                pendingAiSynthesis = false,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isEvaluated = false,
                lastSoundEventCode = TacticalSoundEffect.CLEARANCE_GRANTED.code,
                terminalLogs = (it.terminalLogs + "[CLEARANCE GRANTED] Level-5 Omega Clearance ($9.99/mo) active").takeLast(18)
            )
        }

        if (shouldSynthesize) {
            executeLiveGeminiSynthesis()
        }
    }

    fun executeLiveGeminiSynthesis() {
        if (_uiState.value.isSynthesizingAi) return

        emitTacticalSound(TacticalSoundEffect.VECTOR_CYCLE)
        val domainFilter = _uiState.value.selectedDomain.code
        val difficultyFilter = _uiState.value.selectedDifficulty
        val activeTier = _uiState.value.selectedTier

        _uiState.update {
            it.copy(
                isSynthesizingAi = true,
                connectionState = ConnectionState.SYNTHESIZING,
                lastSoundEventCode = TacticalSoundEffect.VECTOR_CYCLE.code,
                aiStatusBanner = "TRANSMITTING SCHEMA TO GEMINI-3.5-FLASH // AWAITING STRICT JSON ARRAY...",
                terminalLogs = (it.terminalLogs + "[UPLINK] Requesting ${difficultyFilter.id} JSON scenarios via Gemini API...").takeLast(18)
            )
        }

        viewModelScope.launch {
            when (
                val result = GeminiTacticalClient.synthesizeTacticalScenarios(
                    domainFilter = domainFilter,
                    difficultyFilter = difficultyFilter,
                    tier = activeTier,
                    count = 3
                )
            ) {
                is AiInfiltrationResult.Success -> {
                    emitTacticalSound(TacticalSoundEffect.CLEARANCE_GRANTED)
                    _uiState.update { current ->
                        val updatedAll = result.questions + current.allQuestions
                        current.copy(
                            allQuestions = updatedAll,
                            currentQuestionIndex = 0,
                            selectedOptionIndex = null,
                            isEvaluated = false,
                            isSynthesizingAi = false,
                            connectionState = ConnectionState.AI_SYNCED,
                            latestAiJsonPayload = result.rawJsonArray,
                            showJsonTelemetryPanel = true,
                            lastSoundEventCode = TacticalSoundEffect.CLEARANCE_GRANTED.code,
                            aiStatusBanner = "INFILTRATION COMPLETE: ${result.questions.size} NEW AI SCENARIOS INJECTED INTO DECK",
                            terminalLogs = (current.terminalLogs +
                                "[GEMINI 200 OK] Parsed ${result.questions.size} strict JSON scenarios (${result.questions.first().vectorId})").takeLast(18)
                        )
                    }
                }

                is AiInfiltrationResult.ApiKeyMissing -> {
                    emitTacticalSound(TacticalSoundEffect.EVAL_BREACH_ALERT)
                    _uiState.update { current ->
                        current.copy(
                            isSynthesizingAi = false,
                            connectionState = ConnectionState.KEY_REQUIRED,
                            lastSoundEventCode = TacticalSoundEffect.EVAL_BREACH_ALERT.code,
                            aiStatusBanner = result.message,
                            terminalLogs = (current.terminalLogs + "[UPLINK HALT] ${result.message}").takeLast(18)
                        )
                    }
                }

                is AiInfiltrationResult.Error -> {
                    emitTacticalSound(TacticalSoundEffect.EVAL_BREACH_ALERT)
                    _uiState.update { current ->
                        current.copy(
                            isSynthesizingAi = false,
                            connectionState = ConnectionState.FAULT,
                            lastSoundEventCode = TacticalSoundEffect.EVAL_BREACH_ALERT.code,
                            aiStatusBanner = result.message,
                            terminalLogs = (current.terminalLogs + "[UPLINK FAULT] ${result.message}").takeLast(18)
                        )
                    }
                }
            }
        }
    }

    fun toggleJsonTelemetryPanel() {
        emitTacticalSound(TacticalSoundEffect.TERMINAL_CLICK)
        _uiState.update {
            it.copy(
                showJsonTelemetryPanel = !it.showJsonTelemetryPanel,
                lastSoundEventCode = TacticalSoundEffect.TERMINAL_CLICK.code
            )
        }
    }

    fun dismissAiBanner() {
        emitTacticalSound(TacticalSoundEffect.TERMINAL_CLICK)
        _uiState.update { it.copy(aiStatusBanner = null) }
    }

    fun resetTacticalDrill() {
        emitTacticalSound(TacticalSoundEffect.VECTOR_CYCLE)
        _uiState.update { state ->
            state.copy(
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isEvaluated = false,
                scorePoints = 0,
                streakCount = 0,
                evaluatedCount = 0,
                correctCount = 0,
                lastPointsDelta = 0,
                proficiencyHistory = InitialProficiencyHistory,
                selectedTimePointIndex = InitialProficiencyHistory.lastIndex,
                lastSoundEventCode = TacticalSoundEffect.VECTOR_CYCLE.code,
                terminalLogs = (state.terminalLogs + "[SYS RESET] Telemetry counters purged. Deck re-indexed.").takeLast(18)
            )
        }
    }
}
