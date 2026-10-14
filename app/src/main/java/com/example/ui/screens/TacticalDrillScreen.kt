package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.ConnectionState
import com.example.model.DrillDifficulty
import com.example.model.DrillQuestion
import com.example.model.SecurityDomain
import com.example.model.TacticalTier
import com.example.model.TacticalViewMode
import com.example.model.ThreatSeverity
import com.example.ui.components.BrutalistSectionDivider
import com.example.ui.components.ChamferedTacticalBox
import com.example.ui.components.PulsingStatusLed
import com.example.ui.components.SegmentedHudBar
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedDim
import com.example.ui.theme.GeminiAqua
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusCyanDim
import com.example.ui.theme.TacticalBackground
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalBorderHighlight
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TerminalTextMuted
import com.example.ui.theme.TerminalTextPrimary
import com.example.ui.theme.TerminalTextSecondary
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenDim
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberDim
import com.example.viewmodel.TacticalDrillUiState
import com.example.viewmodel.TacticalDrillViewModel
import java.util.Locale

fun difficultyAccentColor(difficulty: DrillDifficulty): Color = when (difficulty) {
    DrillDifficulty.EASY -> VerifiedGreen
    DrillDifficulty.MEDIUM -> WarningAmber
    DrillDifficulty.HARD -> AlertRed
    DrillDifficulty.ALL -> StatusCyan
}

fun difficultyDimColor(difficulty: DrillDifficulty): Color = when (difficulty) {
    DrillDifficulty.EASY -> VerifiedGreenDim
    DrillDifficulty.MEDIUM -> WarningAmberDim
    DrillDifficulty.HARD -> AlertRedDim
    DrillDifficulty.ALL -> StatusCyanDim
}

@Composable
fun TacticalDrillScreen(
    viewModel: TacticalDrillViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Paywall Intercept Protocol Modal
    uiState.activePaywallReason?.let { reason ->
        PaywallInterceptModal(
            reason = reason,
            onAuthorizeClearance = viewModel::authorizeClearanceAndProceed,
            onDismiss = viewModel::dismissPaywall
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TacticalBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 860.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Smooth Gemini/iOS Dynamic-Island Inspired Header & Points System
                item {
                    TacticalHudHeader(
                        uiState = uiState,
                        onOpenClearanceModal = viewModel::openClearanceModal,
                        onToggleAudio = viewModel::toggleAudioFeedback,
                        onResetDrill = viewModel::resetTacticalDrill
                    )
                }

                // 2. Smooth iOS Segmented Pill Mode Switcher
                item {
                    TacticalConsoleModeTabBar(
                        activeMode = uiState.viewMode,
                        overallProficiencyIndex = uiState.overallProficiencyIndex,
                        onSelectMode = viewModel::setViewMode
                    )
                }

                if (uiState.viewMode == TacticalViewMode.PROFICIENCY_DASHBOARD) {
                    item {
                        ProficiencyDashboard(
                            uiState = uiState,
                            onSelectTimePoint = viewModel::selectTimePointIndex,
                            onToggleDomainSeries = viewModel::toggleChartDomainSeries,
                            onEnableAllDomains = viewModel::enableAllChartDomains,
                            onLogSnapshot = viewModel::logCalibrationSnapshot,
                            onDrillDomain = viewModel::drillSpecificDomain,
                            onBackToDrillDeck = { viewModel.setViewMode(TacticalViewMode.DRILL_DECK) }
                        )
                    }
                } else {
                    // 3. Dynamic AI Infiltration & Tier Selector Bar
                    item {
                        TacticalInfiltrationAndTierBar(
                            uiState = uiState,
                            onSelectTier = viewModel::attemptSelectTier,
                            onTriggerAiInfiltration = viewModel::attemptAiInfiltration,
                            onToggleJsonPanel = viewModel::toggleJsonTelemetryPanel,
                            onDismissBanner = viewModel::dismissAiBanner
                        )
                    }

                    // 4. Difficulty Level Calibration Selector (ALL, EASY, MEDIUM, HARD)
                    item {
                        DifficultySelectorPanel(
                            selectedDifficulty = uiState.selectedDifficulty,
                            availableVectorCount = uiState.filteredDeck.size,
                            tierMultiplier = uiState.selectedTier.pointMultiplier,
                            onSelectDifficulty = viewModel::selectDifficulty
                        )
                    }

                    // 5. Smooth Pill Domain Filter Strip (CompTIA SY0-701 1.0 - 5.0)
                    item {
                        DomainSelectorStrip(
                            selectedDomain = uiState.selectedDomain,
                            onSelectDomain = viewModel::selectDomain
                        )
                    }

                    // 6. Smooth Squircle Interactive Quiz Deck Card
                    item {
                        InteractiveQuizDeckCard(
                            uiState = uiState,
                            onSelectOption = viewModel::selectOption,
                            onExecuteEvaluation = viewModel::executeEvaluation,
                            onNextVector = viewModel::advanceNextVector,
                            onPrevVector = viewModel::selectPreviousVector
                        )
                    }

                    // 7. Diagnostic Analysis (Green/Red Result Banner + Technical Rationale Terminal Box)
                    item {
                        AnimatedVisibility(
                            visible = uiState.isEvaluated,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            DiagnosticAnalysisTerminal(
                                question = uiState.currentQuestion,
                                selectedOptionIndex = uiState.selectedOptionIndex ?: 0,
                                pointsDelta = uiState.lastPointsDelta
                            )
                        }
                    }

                    // 8. Compact Live Proficiency Telemetry Strip on Drill Deck
                    item {
                        CompactProficiencyPreviewCard(
                            uiState = uiState,
                            onOpenProficiencyDashboard = {
                                viewModel.setViewMode(TacticalViewMode.PROFICIENCY_DASHBOARD)
                            }
                        )
                    }
                }

                // 9. Live Terminal Audit Log & Prototype Security Notice Footer
                item {
                    TacticalTelemetryFooter(
                        logs = uiState.terminalLogs,
                        lastSoundEventCode = uiState.lastSoundEventCode,
                        isAudioEnabled = uiState.isAudioEnabled
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultySelectorPanel(
    selectedDifficulty: DrillDifficulty,
    availableVectorCount: Int,
    tierMultiplier: Int,
    onSelectDifficulty: (DrillDifficulty) -> Unit
) {
    val activeAccent = difficultyAccentColor(selectedDifficulty)

    ChamferedTacticalBox(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("difficulty_selector_panel"),
        containerColor = TacticalSurface,
        borderColor = TacticalBorderHighlight,
        accentTickColor = activeAccent,
        cutTopRight = 20.dp,
        cutBottomLeft = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Drill Difficulty Calibration",
                        style = MaterialTheme.typography.titleMedium,
                        color = TerminalTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(activeAccent.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$availableVectorCount Vectors • ${tierMultiplier}x Mult",
                        style = MaterialTheme.typography.labelSmall,
                        color = activeAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Smooth iOS Pill Difficulty Selector Chips: ALL, EASY, MEDIUM, HARD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DrillDifficulty.entries.forEach { diff ->
                    val isSelected = diff == selectedDifficulty
                    val chipAccent = difficultyAccentColor(diff)
                    val chipBg = if (isSelected) difficultyDimColor(diff) else TacticalBackground

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(chipBg)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) chipAccent else TacticalBorder,
                                shape = CircleShape
                            )
                            .clickable { onSelectDifficulty(diff) }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                            .testTag("difficulty_chip_${diff.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) chipAccent else TerminalTextMuted)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = diff.shortLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) chipAccent else TerminalTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Active Difficulty Scoring & Description Readout Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TacticalBackground)
                    .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDifficulty.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = TerminalTextSecondary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedDifficulty == DrillDifficulty.ALL) {
                        "100–350 PTS"
                    } else {
                        "Base ${selectedDifficulty.basePoints * tierMultiplier} PTS (+${selectedDifficulty.streakBonus}/Strk)"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = activeAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TacticalConsoleModeTabBar(
    activeMode: TacticalViewMode,
    overallProficiencyIndex: Int,
    onSelectMode: (TacticalViewMode) -> Unit
) {
    // Smooth iOS-style segmented pill control container
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(TacticalSurface)
            .border(1.dp, TacticalBorderHighlight, CircleShape)
            .padding(5.dp)
            .testTag("console_mode_tab_bar"),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TacticalViewMode.entries.forEach { mode ->
            val isSelected = mode == activeMode
            val accent = if (mode == TacticalViewMode.PROFICIENCY_DASHBOARD) VerifiedGreen else StatusCyan

            Box(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    StatusCyanDim,
                                    GeminiViolet.copy(alpha = 0.42f)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        }
                    )
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) accent.copy(alpha = 0.8f) else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSelectMode(mode) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("tab_${mode.name}"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (mode == TacticalViewMode.DRILL_DECK) {
                            Icons.Default.Security
                        } else {
                            Icons.AutoMirrored.Filled.ShowChart
                        },
                        contentDescription = null,
                        tint = if (isSelected) accent else TerminalTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (mode == TacticalViewMode.PROFICIENCY_DASHBOARD) {
                            "${mode.label} [$overallProficiencyIndex%]"
                        } else {
                            mode.label
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) TerminalTextPrimary else TerminalTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactProficiencyPreviewCard(
    uiState: TacticalDrillUiState,
    onOpenProficiencyDashboard: () -> Unit
) {
    ChamferedTacticalBox(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenProficiencyDashboard)
            .testTag("open_proficiency_dashboard_card"),
        containerColor = TacticalSurface,
        borderColor = TacticalBorderHighlight,
        accentTickColor = VerifiedGreen,
        cutTopRight = 20.dp,
        cutBottomLeft = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(VerifiedGreenDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Live Domain Proficiency Telemetry",
                            style = MaterialTheme.typography.titleMedium,
                            color = TerminalTextPrimary
                        )
                        Text(
                            text = "Tap to open interactive time-series dashboard",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusCyan
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(VerifiedGreenDim)
                        .border(1.dp, VerifiedGreen.copy(alpha = 0.7f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${uiState.overallProficiencyIndex}% Avg",
                        style = MaterialTheme.typography.labelMedium,
                        color = VerifiedGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.domainSummaries.forEach { summary ->
                    val color = domainAccentColor(summary.domain.code)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(TacticalBackground)
                            .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = summary.domain.code,
                            style = MaterialTheme.typography.labelSmall,
                            color = TerminalTextSecondary
                        )
                        Text(
                            text = "${summary.currentScore}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TacticalHudHeader(
    uiState: TacticalDrillUiState,
    onOpenClearanceModal: () -> Unit,
    onToggleAudio: () -> Unit,
    onResetDrill: () -> Unit
) {
    val ledColor = when (uiState.connectionState) {
        ConnectionState.SECURE_ONLINE, ConnectionState.AI_SYNCED -> VerifiedGreen
        ConnectionState.SYNTHESIZING -> StatusCyan
        ConnectionState.KEY_REQUIRED -> WarningAmber
        ConnectionState.FAULT -> AlertRed
    }

    ChamferedTacticalBox(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hud_header_card"),
        containerColor = TacticalSurface,
        borderColor = TacticalBorderHighlight,
        accentTickColor = GeminiViolet,
        cutTopRight = 24.dp,
        cutBottomLeft = 24.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Soft ambient hero visual + Gemini aurora radial glow
            Image(
                painter = painterResource(id = R.drawable.img_tactical_hud_banner_1791120029119),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                alpha = 0.16f
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                StatusCyan.copy(alpha = 0.10f),
                                GeminiViolet.copy(alpha = 0.08f),
                                TacticalSurface.copy(alpha = 0.96f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Row: Dynamic Island Style Capsules (Connection + Audio + Clearance)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dynamic Island Uplink Capsule
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(TacticalBackground.copy(alpha = 0.88f))
                                .border(1.dp, TacticalBorder, CircleShape)
                                .padding(horizontal = 11.dp, vertical = 6.dp)
                                .testTag("connection_status_badge")
                        ) {
                            PulsingStatusLed(color = ledColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.connectionState.hudLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = ledColor
                            )
                        }

                        // Smooth Pill Audio Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isAudioEnabled) StatusCyanDim else TacticalBackground.copy(alpha = 0.88f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (uiState.isAudioEnabled) StatusCyan.copy(alpha = 0.7f) else TacticalBorder,
                                    shape = CircleShape
                                )
                                .clickable(onClick = onToggleAudio)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("audio_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isAudioEnabled) {
                                    Icons.AutoMirrored.Filled.VolumeUp
                                } else {
                                    Icons.AutoMirrored.Filled.VolumeOff
                                },
                                contentDescription = if (uiState.isAudioEnabled) "Mute Tactical Audio" else "Enable Tactical Audio",
                                tint = if (uiState.isAudioEnabled) StatusCyan else TerminalTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isAudioEnabled) "SFX" else "Mute",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isAudioEnabled) StatusCyan else TerminalTextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Smooth Pill Clearance Status Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(
                                if (uiState.hasOmegaClearance) VerifiedGreenDim else WarningAmberDim
                            )
                            .border(
                                width = 1.dp,
                                color = if (uiState.hasOmegaClearance) VerifiedGreen.copy(alpha = 0.75f) else WarningAmber.copy(alpha = 0.75f),
                                shape = CircleShape
                            )
                            .clickable(onClick = onOpenClearanceModal)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("clearance_status_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.hasOmegaClearance) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Clearance Level",
                            tint = if (uiState.hasOmegaClearance) VerifiedGreen else WarningAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (uiState.hasOmegaClearance) {
                                "Omega Pro"
                            } else {
                                "Pro $9.99"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.hasOmegaClearance) VerifiedGreen else WarningAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Title & Subtitle with smooth Inter typography
                Column {
                    Text(
                        text = stringResource(R.string.hud_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TerminalTextPrimary
                    )
                    Text(
                        text = stringResource(R.string.hud_subtitle),
                        style = MaterialTheme.typography.labelMedium,
                        color = StatusCyan
                    )
                }

                // Smooth Rounded Glass Metric Readout Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HudMetricReadout(
                        label = "Score",
                        value = String.format(Locale.US, "%04d PTS", uiState.scorePoints),
                        accentColor = StatusCyan,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("score_counter_badge")
                    )
                    HudMetricReadout(
                        label = "Streak",
                        value = "${uiState.streakCount}x Mult",
                        accentColor = GeminiViolet,
                        modifier = Modifier.weight(0.9f)
                    )
                    HudMetricReadout(
                        label = "Accuracy",
                        value = "${uiState.accuracyPercent}%",
                        accentColor = if (uiState.accuracyPercent >= 75) VerifiedGreen else WarningAmber,
                        modifier = Modifier.weight(0.9f)
                    )
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(TacticalSurfaceElevated)
                            .border(1.dp, TacticalBorder, CircleShape)
                            .clickable(onClick = onResetDrill)
                            .padding(12.dp)
                            .testTag("reset_drill_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Drill Telemetry",
                            tint = TerminalTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HudMetricReadout(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(TacticalBackground.copy(alpha = 0.85f))
            .border(1.dp, TacticalBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TerminalTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = accentColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun TacticalInfiltrationAndTierBar(
    uiState: TacticalDrillUiState,
    onSelectTier: (TacticalTier) -> Unit,
    onTriggerAiInfiltration: () -> Unit,
    onToggleJsonPanel: () -> Unit,
    onDismissBanner: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Smooth Pill Tier Selection Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TacticalTier.entries.forEach { tier ->
                val isSelected = uiState.selectedTier == tier
                val isLocked = tier.requiresClearance && !uiState.hasOmegaClearance

                val borderColor = when {
                    isSelected -> StatusCyan
                    isLocked -> WarningAmber.copy(alpha = 0.55f)
                    else -> TacticalBorder
                }
                val bgColor = when {
                    isSelected -> StatusCyanDim
                    else -> TacticalSurface
                }

                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(bgColor)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = borderColor,
                            shape = CircleShape
                        )
                        .clickable { onSelectTier(tier) }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                        .testTag("tier_chip_${tier.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Tier",
                                tint = WarningAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = tier.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) TerminalTextPrimary else if (isLocked) WarningAmber else TerminalTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Smooth Gemini Aurora Card for Dynamic AI Infiltration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChamferedTacticalBox(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clickable(
                        enabled = !uiState.isSynthesizingAi,
                        onClick = onTriggerAiInfiltration
                    )
                    .testTag("ai_infiltrate_button"),
                containerColor = TacticalSurfaceElevated,
                borderColor = GeminiViolet.copy(alpha = 0.8f),
                accentTickColor = GeminiAqua,
                strokeWidth = 1.5.dp,
                cutTopRight = 20.dp,
                cutBottomLeft = 20.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    StatusCyan.copy(alpha = 0.12f),
                                    GeminiViolet.copy(alpha = 0.14f),
                                    GeminiAqua.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(StatusCyan, GeminiViolet)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isSynthesizingAi) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = TacticalBackground
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TacticalBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.infiltrate_ai_vectors),
                                style = MaterialTheme.typography.titleMedium,
                                color = TerminalTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (uiState.hasOmegaClearance) {
                                    "Gemini 3.5 Flash • Synthesize ${uiState.selectedDifficulty.id} JSON Vectors"
                                } else {
                                    "Requires Omega Clearance • Tap to upgrade"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusCyan
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (uiState.hasOmegaClearance) VerifiedGreenDim else WarningAmberDim)
                            .border(
                                1.dp,
                                if (uiState.hasOmegaClearance) VerifiedGreen.copy(alpha = 0.7f) else WarningAmber.copy(alpha = 0.7f),
                                CircleShape
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (uiState.hasOmegaClearance) "Ready" else "Pro",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.hasOmegaClearance) VerifiedGreen else WarningAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (uiState.latestAiJsonPayload != null) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(if (uiState.showJsonTelemetryPanel) StatusCyanDim else TacticalSurface)
                        .border(1.dp, StatusCyan, CircleShape)
                        .clickable(onClick = onToggleJsonPanel)
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                        .testTag("toggle_json_telemetry_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Inspect JSON Array",
                            tint = StatusCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "JSON",
                            style = MaterialTheme.typography.labelMedium,
                            color = StatusCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Smooth Rounded AI Status Banner
        uiState.aiStatusBanner?.let { bannerText ->
            val isError = uiState.connectionState == ConnectionState.FAULT ||
                uiState.connectionState == ConnectionState.KEY_REQUIRED
            val bannerColor = if (isError) WarningAmber else VerifiedGreen
            val bannerBg = if (isError) WarningAmberDim else VerifiedGreenDim

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bannerBg)
                    .border(1.dp, bannerColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onDismissBanner)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("ai_status_banner"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = bannerText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = bannerColor,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dismiss",
                    style = MaterialTheme.typography.labelSmall,
                    color = TerminalTextPrimary
                )
            }
        }

        // Expandable Raw Strict JSON Array Telemetry Inspector
        AnimatedVisibility(visible = uiState.showJsonTelemetryPanel && uiState.latestAiJsonPayload != null) {
            ChamferedTacticalBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("json_telemetry_panel"),
                containerColor = TacticalBackground,
                borderColor = StatusCyan,
                cutTopRight = 16.dp,
                cutBottomLeft = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Gemini 3.5 Flash • Strict JSON Array Payload",
                        style = MaterialTheme.typography.labelLarge,
                        color = StatusCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.latestAiJsonPayload.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = VerifiedGreen,
                        maxLines = 18,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DomainSelectorStrip(
    selectedDomain: SecurityDomain,
    onSelectDomain: (SecurityDomain) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BrutalistSectionDivider(
            leftLabel = "SY0-701 Domain Filter",
            rightLabel = selectedDomain.fullTitle
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecurityDomain.entries.forEach { domain ->
                val active = domain == selectedDomain
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(if (active) StatusCyanDim else TacticalSurface)
                        .border(
                            width = if (active) 1.5.dp else 1.dp,
                            color = if (active) StatusCyan else TacticalBorder,
                            shape = CircleShape
                        )
                        .clickable { onSelectDomain(domain) }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                        .testTag("domain_chip_${domain.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = domain.shortTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (active) StatusCyan else TerminalTextSecondary,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveQuizDeckCard(
    uiState: TacticalDrillUiState,
    onSelectOption: (Int) -> Unit,
    onExecuteEvaluation: () -> Unit,
    onNextVector: () -> Unit,
    onPrevVector: () -> Unit
) {
    val question = uiState.currentQuestion
    val deckSize = uiState.filteredDeck.size.coerceAtLeast(1)
    val safeIndex = uiState.currentQuestionIndex.coerceIn(0, deckSize - 1)

    val severityColor = when (question.severity) {
        ThreatSeverity.CRITICAL -> AlertRed
        ThreatSeverity.HIGH -> WarningAmber
        ThreatSeverity.ELEVATED -> StatusCyan
    }
    val diffColor = difficultyAccentColor(question.difficulty)
    val diffBg = difficultyDimColor(question.difficulty)

    ChamferedTacticalBox(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quiz_deck_card"),
        containerColor = TacticalSurface,
        borderColor = TacticalBorderHighlight,
        accentTickColor = StatusCyan,
        cutTopRight = 24.dp,
        cutBottomLeft = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Smooth Gemini Gradient Progress Bar
            SegmentedHudBar(
                totalSegments = deckSize,
                activeIndex = safeIndex
            )

            // Vector Metadata Row: Vector ID Pill + Difficulty Pill + Severity Pill + Index
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vector ID Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(TacticalBackground)
                            .border(1.dp, StatusCyan.copy(alpha = 0.6f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("vector_id_badge")
                    ) {
                        Text(
                            text = question.vectorId,
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Question Difficulty & Potential Points Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(diffBg)
                            .border(1.dp, diffColor.copy(alpha = 0.7f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("question_difficulty_badge")
                    ) {
                        Text(
                            text = "${question.difficulty.id} • +${uiState.currentQuestionPotentialPoints}P",
                            style = MaterialTheme.typography.labelSmall,
                            color = diffColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (question.isAiSynthesized) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(VerifiedGreenDim)
                                .border(1.dp, VerifiedGreen, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Gemini AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = VerifiedGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Threat Severity Pill & Index Counter
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(severityColor.copy(alpha = 0.14f))
                            .border(1.dp, severityColor.copy(alpha = 0.6f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = question.severity.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = severityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = String.format(Locale.US, "%02d/%02d", safeIndex + 1, deckSize),
                        style = MaterialTheme.typography.labelSmall,
                        color = TerminalTextSecondary
                    )
                }
            }

            // Smooth Rounded Domain Category Pill Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TacticalBackground)
                    .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("domain_category_badge"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = question.domainTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = VerifiedGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = question.tier.id,
                    style = MaterialTheme.typography.labelSmall,
                    color = TerminalTextSecondary
                )
            }

            // Scenario Question Stem (Smooth Inter Typography)
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyLarge,
                color = TerminalTextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("question_stem_text")
            )

            // 4 Smooth iOS/Gemini Squircle Selectable Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                question.options.forEachIndexed { index, optionText ->
                    val optionPrefix = ('A' + index).toString()
                    val isSelected = uiState.selectedOptionIndex == index
                    val isCorrectOption = index == question.correctIndex

                    val containerColor = when {
                        uiState.isEvaluated && isCorrectOption -> VerifiedGreenDim
                        uiState.isEvaluated && isSelected && !isCorrectOption -> AlertRedDim
                        isSelected -> StatusCyanDim
                        else -> TacticalBackground
                    }

                    val borderColor = when {
                        uiState.isEvaluated && isCorrectOption -> VerifiedGreen
                        uiState.isEvaluated && isSelected && !isCorrectOption -> AlertRed
                        isSelected -> StatusCyan
                        else -> TacticalBorder
                    }

                    val prefixColor = when {
                        uiState.isEvaluated && isCorrectOption -> VerifiedGreen
                        uiState.isEvaluated && isSelected && !isCorrectOption -> AlertRed
                        isSelected -> StatusCyan
                        else -> TerminalTextSecondary
                    }

                    ChamferedTacticalBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clickable(enabled = !uiState.isEvaluated) {
                                onSelectOption(index)
                            }
                            .testTag("option_card_$index"),
                        containerColor = containerColor,
                        borderColor = borderColor,
                        accentTickColor = prefixColor,
                        strokeWidth = if (isSelected || (uiState.isEvaluated && isCorrectOption)) 1.5.dp else 1.dp,
                        cutTopRight = 14.dp,
                        cutBottomLeft = 14.dp,
                        showCornerReticles = isSelected || (uiState.isEvaluated && isCorrectOption)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Smooth Circular Option Badge (A, B, C, D)
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected || (uiState.isEvaluated && isCorrectOption)) {
                                            prefixColor.copy(alpha = 0.22f)
                                        } else {
                                            TacticalSurface
                                        }
                                    )
                                    .border(1.dp, prefixColor.copy(alpha = 0.75f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionPrefix,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = prefixColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TerminalTextPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            if (uiState.isEvaluated && isCorrectOption) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Answer",
                                    tint = VerifiedGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (uiState.isEvaluated && isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Breach Option",
                                    tint = AlertRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Smooth Pill Action Deck Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Vector Pill Button
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(TacticalSurfaceElevated)
                        .border(1.dp, TacticalBorder, CircleShape)
                        .clickable(onClick = onPrevVector)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("prev_vector_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Vector",
                        tint = TerminalTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (!uiState.isEvaluated) {
                    val canExecute = uiState.selectedOptionIndex != null
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(
                                if (canExecute) {
                                    Brush.horizontalGradient(
                                        colors = listOf(StatusCyan, GeminiViolet, GeminiAqua)
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(TacticalSurfaceElevated, TacticalSurfaceElevated)
                                    )
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (canExecute) StatusCyan else TacticalBorder,
                                shape = CircleShape
                            )
                            .clickable(
                                enabled = canExecute,
                                onClick = onExecuteEvaluation
                            )
                            .testTag("execute_evaluation_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 15.dp, horizontal = 18.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (canExecute) TacticalBackground else TerminalTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.execute_evaluation),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (canExecute) TacticalBackground else TerminalTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(VerifiedGreen, GeminiAqua)
                                )
                            )
                            .clickable(onClick = onNextVector)
                            .testTag("next_vector_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 15.dp, horizontal = 18.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.advance_next_vector),
                                style = MaterialTheme.typography.labelLarge,
                                color = TacticalBackground,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = TacticalBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticAnalysisTerminal(
    question: DrillQuestion,
    selectedOptionIndex: Int,
    pointsDelta: Int
) {
    val isCorrect = selectedOptionIndex == question.correctIndex
    val bannerBorderColor = if (isCorrect) VerifiedGreen else AlertRed
    val bannerBgColor = if (isCorrect) VerifiedGreenDim else AlertRedDim

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Smooth Squircle Green/Red Diagnostic Result Banner
        ChamferedTacticalBox(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("diagnostic_result_banner"),
            containerColor = bannerBgColor,
            borderColor = bannerBorderColor,
            accentTickColor = bannerBorderColor,
            strokeWidth = 1.5.dp,
            cutTopRight = 18.dp,
            cutBottomLeft = 18.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.GppGood else Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = bannerBorderColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isCorrect) {
                                stringResource(R.string.diagnostic_verified)
                            } else {
                                stringResource(R.string.diagnostic_alert)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = bannerBorderColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isCorrect) {
                                "Selected Option [${'A' + selectedOptionIndex}] verified (${question.difficulty.id} • +$pointsDelta PTS)"
                            } else {
                                "Selected [${'A' + selectedOptionIndex}] rejected • Optimal control: [${'A' + question.correctIndex}]"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = TerminalTextPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(TacticalBackground)
                        .border(1.dp, bannerBorderColor, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isCorrect) "+$pointsDelta PTS" else "0 PTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = bannerBorderColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Smooth Squircle Technical Rationale Box
        ChamferedTacticalBox(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("technical_rationale_box"),
            containerColor = TacticalSurface,
            borderColor = StatusCyan.copy(alpha = 0.7f),
            accentTickColor = GeminiViolet,
            cutTopRight = 20.dp,
            cutBottomLeft = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = StatusCyan,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.rationale_header),
                            style = MaterialTheme.typography.labelLarge,
                            color = StatusCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "TTY-01",
                        style = MaterialTheme.typography.labelSmall,
                        color = TerminalTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TacticalBackground)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "root@secplus-tac:~# ${question.commandReference}",
                        style = MaterialTheme.typography.labelSmall,
                        color = VerifiedGreen
                    )
                }

                Text(
                    text = question.rationale,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TerminalTextPrimary
                )
            }
        }
    }
}

@Composable
private fun TacticalTelemetryFooter(
    logs: List<String>,
    lastSoundEventCode: String,
    isAudioEnabled: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrutalistSectionDivider(
                leftLabel = "Live Telemetry Stream",
                rightLabel = "Audio DSP: ${if (isAudioEnabled) lastSoundEventCode else "Muted"}",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = if (isAudioEnabled) StatusCyan else TerminalTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }

        ChamferedTacticalBox(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("telemetry_log_box"),
            containerColor = TacticalSurface,
            borderColor = TacticalBorder,
            accentTickColor = TerminalTextSecondary,
            cutTopRight = 18.dp,
            cutBottomLeft = 18.dp,
            showCornerReticles = false
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                logs.takeLast(5).forEach { entry ->
                    val lineColor = when {
                        entry.contains("PASS") || entry.contains("GRANTED") || entry.contains("200 OK") || entry.contains("SNAPSHOT") -> VerifiedGreen
                        entry.contains("FAIL") || entry.contains("INTERCEPT") || entry.contains("FAULT") -> AlertRed
                        entry.contains("UPLINK") || entry.contains("HALT") || entry.contains("AUDIO DSP") || entry.contains("DIFFICULTY") -> WarningAmber
                        else -> TerminalTextSecondary
                    }
                    Text(
                        text = entry,
                        style = MaterialTheme.typography.labelSmall,
                        color = lineColor
                    )
                }
            }
        }

        // Prototype API Key Caution Notice per gemini-api skill
        Text(
            text = stringResource(R.string.prototype_security_notice),
            style = MaterialTheme.typography.labelSmall,
            color = TerminalTextMuted,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
