package com.example.model

enum class TacticalViewMode(val label: String) {
    DRILL_DECK("01 // DRILL ENGINE"),
    PROFICIENCY_DASHBOARD("02 // PROFICIENCY HUD")
}

enum class DrillDifficulty(
    val id: String,
    val shortLabel: String,
    val tacticalTitle: String,
    val basePoints: Int,
    val streakBonus: Int,
    val description: String
) {
    ALL(
        id = "ALL",
        shortLabel = "ALL LEVELS",
        tacticalTitle = "ADAPTIVE SPECTRUM (100–350 PTS)",
        basePoints = 200,
        streakBonus = 25,
        description = "All SY0-701 difficulty tiers active; points scale per vector complexity."
    ),
    EASY(
        id = "EASY",
        shortLabel = "EASY // 100P",
        tacticalTitle = "RECON BASELINE (100 PTS)",
        basePoints = 100,
        streakBonus = 15,
        description = "Foundational SY0-701 controls, cryptography basics, and core security concepts."
    ),
    MEDIUM(
        id = "MEDIUM",
        shortLabel = "MEDIUM // 200P",
        tacticalTitle = "HARDENED OPS (200 PTS)",
        basePoints = 200,
        streakBonus = 30,
        description = "Enterprise architecture, PKI validation, quantitative risk, and incident response."
    ),
    HARD(
        id = "HARD",
        shortLabel = "HARD // 350P",
        tacticalTitle = "ADVERSARY SIM (350 PTS)",
        basePoints = 350,
        streakBonus = 50,
        description = "Deep packet/memory forensics, Zero Trust control plane, and nation-state mitigations."
    )
}

enum class SecurityDomain(
    val code: String,
    val shortTitle: String,
    val fullTitle: String,
    val chartLabel: String
) {
    ALL("ALL", "ALL DOMAINS", "FULL SPECTRUM SY0-701", "All Domains"),
    DOM_1("1.0", "1.0 CONCEPTS", "1.0 GENERAL SECURITY CONCEPTS", "Concepts & Crypto"),
    DOM_2("2.0", "2.0 THREATS", "2.0 THREATS, VULNERABILITIES & MITIGATIONS", "Threats & Mitigations"),
    DOM_3("3.0", "3.0 ARCH", "3.0 SECURITY ARCHITECTURE", "Architecture"),
    DOM_4("4.0", "4.0 OPS", "4.0 SECURITY OPERATIONS", "Implementation & Ops"),
    DOM_5("5.0", "5.0 GOV/RISK", "5.0 PROGRAM MANAGEMENT & OVERSIGHT", "Governance & Risk")
}

enum class TacticalTier(
    val id: String,
    val label: String,
    val subtitle: String,
    val requiresClearance: Boolean,
    val pointMultiplier: Int
) {
    TIER_1(
        id = "L1-STD",
        label = "TIER-1 // FIELD OPS",
        subtitle = "SY0-701 Core Baseline Scenarios",
        requiresClearance = false,
        pointMultiplier = 1
    ),
    TIER_2(
        id = "L3-ZDAY",
        label = "TIER-2 // ZERO-DAY",
        subtitle = "Advanced Exploit Chain & Kernel Telemetry",
        requiresClearance = true,
        pointMultiplier = 2
    ),
    TIER_3(
        id = "L5-APT",
        label = "TIER-3 // APT RED-TEAM",
        subtitle = "Nation-State Adversary Architecture Drills",
        requiresClearance = true,
        pointMultiplier = 3
    )
}

enum class ThreatSeverity(val label: String) {
    CRITICAL("CRITICAL"),
    HIGH("HIGH"),
    ELEVATED("ELEVATED")
}

data class DrillQuestion(
    val vectorId: String,
    val domainCode: String,
    val domainTitle: String,
    val severity: ThreatSeverity,
    val difficulty: DrillDifficulty = DrillDifficulty.MEDIUM,
    val tier: TacticalTier = TacticalTier.TIER_1,
    val isAiSynthesized: Boolean = false,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val rationale: String,
    val commandReference: String = "sy0-701-audit --verify-controls"
)

enum class ConnectionState(val hudLabel: String) {
    SECURE_ONLINE("UPLINK: SECURE // READY"),
    SYNTHESIZING("UPLINK: GEMINI AI INFILTRATION ACTIVE..."),
    AI_SYNCED("UPLINK: AI VECTORS VERIFIED"),
    KEY_REQUIRED("UPLINK: API SECRET REQUIRED"),
    FAULT("UPLINK: TELEMETRY FAULT")
}

enum class PaywallTriggerReason(
    val code: String,
    val title: String,
    val detail: String
) {
    ADVANCED_TIER(
        code = "ERR_CLEARANCE_L3_REQUIRED",
        title = "CLASSIFIED DRILL TIER INTERCEPTED",
        detail = "Access to Tier-2 (Zero-Day) and Tier-3 (APT Red-Team) combat simulation decks requires active Level-5 Omega Clearance."
    ),
    AI_INFILTRATION(
        code = "ERR_NEURAL_UPLINK_RESTRICTED",
        title = "DYNAMIC AI INFILTRATION INTERCEPTED",
        detail = "On-demand synthesis of live CompTIA Security+ (SY0-701) examination scenarios via Gemini neural uplink requires Level-5 Omega Clearance."
    ),
    MANUAL_UPGRADE(
        code = "REQ_CLEARANCE_ELEVATION",
        title = "TACTICAL CLEARANCE ELEVATION PROTOCOL",
        detail = "Upgrade terminal credentials to unlock unlimited Gemini AI scenario synthesis, Zero-Day architecture drills, and full forensic telemetry."
    )
}

/**
 * Represents a single time-series telemetry snapshot across CompTIA Security+ domains.
 */
data class ProficiencyTimePoint(
    val timestampLabel: String,
    val sessionTag: String,
    val domainScores: Map<String, Int> // domainCode ("1.0".."5.0") -> proficiency percentage (0..100)
) {
    val compositeAverage: Int
        get() = if (domainScores.isEmpty()) 0 else domainScores.values.average().toInt()
}

/**
 * Summary telemetry for a single CompTIA Security+ domain in the Proficiency Dashboard.
 */
data class DomainProficiencySummary(
    val domain: SecurityDomain,
    val currentScore: Int,
    val previousScore: Int,
    val attemptsCount: Int,
    val correctCount: Int
) {
    val delta: Int
        get() = currentScore - previousScore

    val readinessStatus: String
        get() = when {
            currentScore >= 82 -> "COMBAT READY"
            currentScore >= 68 -> "CALIBRATING"
            else -> "VULNERABLE"
        }
}
