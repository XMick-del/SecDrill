package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DomainProficiencySummary
import com.example.model.ProficiencyTimePoint
import com.example.model.SecurityDomain
import com.example.ui.components.BrutalistSectionDivider
import com.example.ui.components.ChamferedTacticalBox
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
import com.example.viewmodel.TacticalDrillUiState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

fun domainAccentColor(domainCode: String): Color = when (domainCode) {
    "2.0" -> AlertRed       // Threats, Vulnerabilities & Mitigations
    "3.0" -> StatusCyan     // Security Architecture
    "4.0" -> VerifiedGreen  // Security Operations / Implementation
    "1.0" -> WarningAmber   // General Security Concepts
    "5.0" -> GeminiViolet   // Program Management & Oversight
    else -> StatusCyan
}

fun domainShortLegend(domainCode: String): String = when (domainCode) {
    "2.0" -> "2.0 Threats"
    "3.0" -> "3.0 Architecture"
    "4.0" -> "4.0 Implementation"
    "1.0" -> "1.0 Concepts"
    "5.0" -> "5.0 Gov & Risk"
    else -> domainCode
}

@Composable
fun ProficiencyDashboard(
    uiState: TacticalDrillUiState,
    onSelectTimePoint: (Int) -> Unit,
    onToggleDomainSeries: (String) -> Unit,
    onEnableAllDomains: () -> Unit,
    onLogSnapshot: () -> Unit,
    onDrillDomain: (SecurityDomain) -> Unit,
    onBackToDrillDeck: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToDrillDeck)

    val history = uiState.proficiencyHistory
    val selectedIdx = uiState.selectedTimePointIndex.coerceIn(0, (history.size - 1).coerceAtLeast(0))
    val activePoint = history.getOrElse(selectedIdx) {
        ProficiencyTimePoint("LIVE", "ACTIVE", emptyMap())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("proficiency_dashboard_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Proficiency Overview Banner & Readiness KPI Cards
        ProficiencyOverviewBanner(
            uiState = uiState,
            onLogSnapshot = onLogSnapshot
        )

        // 2. Smooth Recharts-Style Interactive Multi-Series Time-Series Telemetry Card
        ChamferedTacticalBox(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("proficiency_timeseries_card"),
            containerColor = TacticalSurface,
            borderColor = TacticalBorderHighlight,
            accentTickColor = StatusCyan,
            cutTopRight = 22.dp,
            cutBottomLeft = 22.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StatusCyanDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = null,
                                tint = StatusCyan,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.proficiency_dashboard_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = TerminalTextPrimary
                            )
                            Text(
                                text = stringResource(R.string.proficiency_chart_subtitle),
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
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${uiState.overallProficiencyIndex}% Index",
                            style = MaterialTheme.typography.labelMedium,
                            color = VerifiedGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Smooth Pill Series Filter Legend Chips
                SeriesLegendFilterBar(
                    enabledDomains = uiState.enabledChartDomains,
                    onToggleDomain = onToggleDomainSeries,
                    onEnableAll = onEnableAllDomains
                )

                // Multi-Series Canvas Line + Area Chart with Y-Axis & Interactive Crosshair Scrubbing
                ProficiencyTimeSeriesCanvas(
                    history = history,
                    selectedIndex = selectedIdx,
                    enabledDomains = uiState.enabledChartDomains,
                    onSelectIndex = onSelectTimePoint
                )

                // Smooth iOS Segmented Pill Time Checkpoint Selector Strip (T-5 .. LIVE)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(TacticalBackground)
                        .border(1.dp, TacticalBorder, CircleShape)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    history.forEachIndexed { idx, point ->
                        val isSelected = idx == selectedIdx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            colors = listOf(StatusCyanDim, GeminiViolet.copy(alpha = 0.45f))
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            colors = listOf(Color.Transparent, Color.Transparent)
                                        )
                                    }
                                )
                                .clickable { onSelectTimePoint(idx) }
                                .padding(vertical = 8.dp)
                                .testTag("timepoint_chip_${point.timestampLabel}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = point.timestampLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) TerminalTextPrimary else TerminalTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Inspected Checkpoint Telemetry Tooltip Card
                InspectedTimePointTooltipCard(
                    timePoint = activePoint,
                    enabledDomains = uiState.enabledChartDomains
                )
            }
        }

        // 3. Domain Radar Topology & Comparative Vector Distribution
        DomainRadarAndDistributionCard(
            summaries = uiState.domainSummaries
        )

        // 4. Domain Readiness Matrix Cards with Direct Drill Launchers
        BrutalistSectionDivider(
            leftLabel = "Domain Readiness Matrix",
            rightLabel = "Tap to focus drill"
        )

        uiState.domainSummaries.forEach { summary ->
            DomainReadinessCard(
                summary = summary,
                onDrillDomain = { onDrillDomain(summary.domain) }
            )
        }
    }
}

@Composable
private fun ProficiencyOverviewBanner(
    uiState: TacticalDrillUiState,
    onLogSnapshot: () -> Unit
) {
    val readyCount = uiState.domainSummaries.count { it.currentScore >= 80 }
    val strongest = uiState.domainSummaries.maxByOrNull { it.currentScore }
    val weakest = uiState.domainSummaries.minByOrNull { it.currentScore }

    ChamferedTacticalBox(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("proficiency_overview_banner"),
        containerColor = TacticalSurfaceElevated,
        borderColor = TacticalBorderHighlight,
        accentTickColor = GeminiViolet,
        cutTopRight = 20.dp,
        cutBottomLeft = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OverviewKpiTile(
                    label = "Composite Readiness",
                    value = "${uiState.overallProficiencyIndex}%",
                    subValue = "Target: 85% SY0-701",
                    accentColor = if (uiState.overallProficiencyIndex >= 80) VerifiedGreen else StatusCyan,
                    modifier = Modifier.weight(1f)
                )
                OverviewKpiTile(
                    label = "Domains Verified",
                    value = "$readyCount / 5",
                    subValue = "Peak: ${strongest?.domain?.shortTitle ?: "1.0"}",
                    accentColor = VerifiedGreen,
                    modifier = Modifier.weight(1f)
                )
                OverviewKpiTile(
                    label = "Priority Focus",
                    value = weakest?.domain?.code ?: "4.0",
                    subValue = "${weakest?.currentScore ?: 75}% Proficiency",
                    accentColor = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            // Smooth Pill Button to Commit New Telemetry Snapshot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(StatusCyanDim, GeminiViolet.copy(alpha = 0.35f))
                        )
                    )
                    .border(1.dp, StatusCyan.copy(alpha = 0.65f), CircleShape)
                    .clickable(onClick = onLogSnapshot)
                    .testTag("log_snapshot_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = StatusCyan,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.log_calibration_snapshot),
                        style = MaterialTheme.typography.labelLarge,
                        color = TerminalTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewKpiTile(
    label: String,
    value: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(TacticalBackground)
            .border(1.dp, TacticalBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TerminalTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = accentColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = subValue,
            style = MaterialTheme.typography.labelSmall,
            color = TerminalTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SeriesLegendFilterBar(
    enabledDomains: Set<String>,
    onToggleDomain: (String) -> Unit,
    onEnableAll: () -> Unit
) {
    val orderedCodes = listOf("2.0", "3.0", "4.0", "1.0", "5.0")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val allActive = enabledDomains.size == orderedCodes.size
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .background(if (allActive) StatusCyanDim else TacticalBackground)
                .border(1.dp, if (allActive) StatusCyan else TacticalBorder, CircleShape)
                .clickable(onClick = onEnableAll)
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("series_toggle_ALL"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "All Series",
                style = MaterialTheme.typography.labelMedium,
                color = if (allActive) StatusCyan else TerminalTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        orderedCodes.forEach { code ->
            val active = enabledDomains.contains(code)
            val color = domainAccentColor(code)
            Row(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .background(if (active) color.copy(alpha = 0.16f) else TacticalBackground)
                    .border(1.dp, if (active) color else TacticalBorder, CircleShape)
                    .clickable { onToggleDomain(code) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("series_toggle_$code"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (active) color else TerminalTextMuted)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = domainShortLegend(code),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) TerminalTextPrimary else TerminalTextMuted,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ProficiencyTimeSeriesCanvas(
    history: List<ProficiencyTimePoint>,
    selectedIndex: Int,
    enabledDomains: Set<String>,
    onSelectIndex: (Int) -> Unit
) {
    val orderedCodes = listOf("2.0", "3.0", "4.0", "1.0", "5.0")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(225.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(TacticalBackground)
            .border(1.dp, TacticalBorder, RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Y-Axis Scale Labels
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            listOf("100%", "85%", "75%", "50%", "25%").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (label == "85%") VerifiedGreen else TerminalTextMuted
                )
            }
        }

        // Main Interactive Multi-Series Chart Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("proficiency_chart_canvas")
                    .pointerInput(history.size) {
                        detectTapGestures { offset ->
                            if (history.size > 1 && size.width > 0) {
                                val stepX = size.width.toFloat() / (history.size - 1).coerceAtLeast(1)
                                val tappedIdx = (offset.x / stepX).roundToInt().coerceIn(0, history.lastIndex)
                                onSelectIndex(tappedIdx)
                            }
                        }
                    }
                    .pointerInput(history.size) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            if (history.size > 1 && size.width > 0) {
                                val stepX = size.width.toFloat() / (history.size - 1).coerceAtLeast(1)
                                val draggedIdx = (change.position.x / stepX).roundToInt().coerceIn(0, history.lastIndex)
                                onSelectIndex(draggedIdx)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val minVal = 25f
                val maxVal = 100f
                val range = maxVal - minVal

                fun valueToY(score: Float): Float {
                    val normalized = ((score.coerceIn(minVal, maxVal) - minVal) / range)
                    return h - (normalized * h)
                }

                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                listOf(25f, 50f, 75f, 100f).forEach { gridVal ->
                    val y = valueToY(gridVal)
                    drawLine(
                        color = TacticalBorder.copy(alpha = 0.7f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 85% CompTIA Exam Readiness Threshold Line
                val thresholdY = valueToY(85f)
                drawLine(
                    color = VerifiedGreen.copy(alpha = 0.65f),
                    start = Offset(0f, thresholdY),
                    end = Offset(w, thresholdY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = dashEffect
                )

                if (history.isEmpty()) return@Canvas

                val stepX = if (history.size > 1) w / (history.size - 1).toFloat() else w / 2f

                // Vertical Grid Lines + Selected Crosshair
                for (i in history.indices) {
                    val x = i * stepX
                    val isSelected = i == selectedIndex
                    drawLine(
                        color = if (isSelected) StatusCyan else TacticalBorder.copy(alpha = 0.45f),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = if (isSelected) 2.dp.toPx() else 1.dp.toPx(),
                        pathEffect = if (isSelected) null else dashEffect
                    )
                }

                // Draw each enabled CompTIA Security+ domain series
                orderedCodes.filter { enabledDomains.contains(it) }.forEach { domainCode ->
                    val seriesColor = domainAccentColor(domainCode)
                    val points = history.mapIndexed { idx, timePoint ->
                        val score = (timePoint.domainScores[domainCode] ?: 70).toFloat()
                        Offset(idx * stepX, valueToY(score))
                    }

                    if (points.size >= 2) {
                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val controlX1 = (prev.x + curr.x) / 2f
                                val controlX2 = (prev.x + curr.x) / 2f
                                cubicTo(controlX1, prev.y, controlX2, curr.y, curr.x, curr.y)
                            }
                        }

                        val areaPath = Path().apply {
                            addPath(linePath)
                            lineTo(points.last().x, h)
                            lineTo(points.first().x, h)
                            close()
                        }

                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    seriesColor.copy(alpha = 0.22f),
                                    seriesColor.copy(alpha = 0.0f)
                                ),
                                startY = 0f,
                                endY = h
                            )
                        )

                        drawPath(
                            path = linePath,
                            color = seriesColor,
                            style = Stroke(
                                width = 2.8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // Draw Smooth Glowing Data Nodes
                    points.forEachIndexed { idx, pt ->
                        val isFocused = idx == selectedIndex
                        if (isFocused) {
                            drawCircle(
                                color = seriesColor.copy(alpha = 0.28f),
                                radius = 9.dp.toPx(),
                                center = pt
                            )
                        }
                        drawCircle(
                            color = TacticalBackground,
                            radius = if (isFocused) 5.5.dp.toPx() else 3.8.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = seriesColor,
                            radius = if (isFocused) 4.dp.toPx() else 2.6.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectedTimePointTooltipCard(
    timePoint: ProficiencyTimePoint,
    enabledDomains: Set<String>
) {
    val orderedCodes = listOf("2.0", "3.0", "4.0", "1.0", "5.0")

    ChamferedTacticalBox(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chart_tooltip_readout"),
        containerColor = TacticalBackground,
        borderColor = StatusCyan.copy(alpha = 0.7f),
        accentTickColor = GeminiAqua,
        cutTopRight = 14.dp,
        cutBottomLeft = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = StatusCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${timePoint.timestampLabel} • ${timePoint.sessionTag}",
                        style = MaterialTheme.typography.labelLarge,
                        color = StatusCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Avg ${timePoint.compositeAverage}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = VerifiedGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                orderedCodes.filter { enabledDomains.contains(it) }.forEach { code ->
                    val score = timePoint.domainScores[code] ?: 75
                    val color = domainAccentColor(code)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TacticalSurface)
                            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DOM $code",
                            style = MaterialTheme.typography.labelSmall,
                            color = TerminalTextSecondary,
                            maxLines = 1
                        )
                        Text(
                            text = "$score%",
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
private fun DomainRadarAndDistributionCard(
    summaries: List<DomainProficiencySummary>
) {
    ChamferedTacticalBox(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("domain_radar_card"),
        containerColor = TacticalSurface,
        borderColor = TacticalBorderHighlight,
        accentTickColor = GeminiViolet,
        cutTopRight = 20.dp,
        cutBottomLeft = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    tint = GeminiViolet,
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = "5-Axis Domain Topology & Balance",
                    style = MaterialTheme.typography.titleLarge,
                    color = TerminalTextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 5-Axis Pentagonal Radar Canvas
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(TacticalBackground)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(18.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val maxRadius = size.minDimension / 2.25f
                        val axes = summaries.size.coerceAtLeast(3)

                        listOf(0.35f, 0.68f, 1.0f).forEach { ringRatio ->
                            val ringPath = Path()
                            for (i in 0 until axes) {
                                val angle = (-PI / 2.0) + (2.0 * PI * i / axes)
                                val rx = cx + (maxRadius * ringRatio * cos(angle)).toFloat()
                                val ry = cy + (maxRadius * ringRatio * sin(angle)).toFloat()
                                if (i == 0) ringPath.moveTo(rx, ry) else ringPath.lineTo(rx, ry)
                            }
                            ringPath.close()
                            drawPath(
                                path = ringPath,
                                color = TacticalBorderHighlight.copy(alpha = 0.65f),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }

                        for (i in 0 until axes) {
                            val angle = (-PI / 2.0) + (2.0 * PI * i / axes)
                            val ex = cx + (maxRadius * cos(angle)).toFloat()
                            val ey = cy + (maxRadius * sin(angle)).toFloat()
                            drawLine(
                                color = TacticalBorder,
                                start = Offset(cx, cy),
                                end = Offset(ex, ey),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val dataPath = Path()
                        summaries.forEachIndexed { i, item ->
                            val ratio = (item.currentScore / 100f).coerceIn(0.1f, 1.0f)
                            val angle = (-PI / 2.0) + (2.0 * PI * i / axes)
                            val px = cx + (maxRadius * ratio * cos(angle)).toFloat()
                            val py = cy + (maxRadius * ratio * sin(angle)).toFloat()
                            if (i == 0) dataPath.moveTo(px, py) else dataPath.lineTo(px, py)
                        }
                        dataPath.close()

                        drawPath(
                            path = dataPath,
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    StatusCyan.copy(alpha = 0.35f),
                                    GeminiViolet.copy(alpha = 0.18f)
                                ),
                                center = Offset(cx, cy),
                                radius = maxRadius
                            )
                        )
                        drawPath(
                            path = dataPath,
                            color = StatusCyan,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        summaries.forEachIndexed { i, item ->
                            val ratio = (item.currentScore / 100f).coerceIn(0.1f, 1.0f)
                            val angle = (-PI / 2.0) + (2.0 * PI * i / axes)
                            val px = cx + (maxRadius * ratio * cos(angle)).toFloat()
                            val py = cy + (maxRadius * ratio * sin(angle)).toFloat()
                            drawCircle(
                                color = domainAccentColor(item.domain.code),
                                radius = 4.dp.toPx(),
                                center = Offset(px, py)
                            )
                        }
                    }
                }

                // Smooth Rounded Horizontal Domain Bars
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    summaries.forEach { summary ->
                        val color = domainAccentColor(summary.domain.code)
                        val deltaSign = if (summary.delta >= 0) "+${summary.delta}%" else "${summary.delta}%"
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${summary.domain.code} ${summary.domain.chartLabel}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TerminalTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${summary.currentScore}% ($deltaSign)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(CircleShape)
                                    .background(TacticalBackground)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((summary.currentScore / 100f).coerceIn(0.05f, 1f))
                                        .height(7.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DomainReadinessCard(
    summary: DomainProficiencySummary,
    onDrillDomain: () -> Unit
) {
    val accent = domainAccentColor(summary.domain.code)
    val statusColor = when {
        summary.currentScore >= 82 -> VerifiedGreen
        summary.currentScore >= 68 -> StatusCyan
        else -> AlertRed
    }
    val statusBg = when {
        summary.currentScore >= 82 -> VerifiedGreenDim
        summary.currentScore >= 68 -> StatusCyanDim
        else -> AlertRedDim
    }

    ChamferedTacticalBox(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("domain_readiness_card_${summary.domain.code}"),
        containerColor = TacticalSurface,
        borderColor = TacticalBorder,
        accentTickColor = accent,
        cutTopRight = 18.dp,
        cutBottomLeft = 18.dp
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.domain.fullTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = TerminalTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Vectors Cleared: ${summary.correctCount}/${summary.attemptsCount} • Delta: ${if (summary.delta >= 0) "+${summary.delta}%" else "${summary.delta}%"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TerminalTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusBg)
                        .border(1.dp, statusColor.copy(alpha = 0.7f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${summary.readinessStatus} • ${summary.currentScore}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            SegmentedHudBar(
                totalSegments = 10,
                activeIndex = ((summary.currentScore / 10) - 1).coerceIn(0, 9),
                activeColor = accent,
                completedColor = accent.copy(alpha = 0.7f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SY0-701 Weighted Telemetry",
                        style = MaterialTheme.typography.labelSmall,
                        color = TerminalTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f))
                        .border(1.dp, accent, CircleShape)
                        .clickable(onClick = onDrillDomain)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("drill_domain_button_${summary.domain.code}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Target Drill",
                            style = MaterialTheme.typography.labelMedium,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
