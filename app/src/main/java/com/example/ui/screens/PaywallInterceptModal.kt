package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.PaywallTriggerReason
import com.example.ui.components.ChamferedTacticalBox
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedDim
import com.example.ui.theme.GeminiAqua
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusCyanDim
import com.example.ui.theme.TacticalBackground
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TerminalTextPrimary
import com.example.ui.theme.TerminalTextSecondary
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.WarningAmber

@Composable
fun PaywallInterceptModal(
    reason: PaywallTriggerReason,
    onAuthorizeClearance: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ChamferedTacticalBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .widthIn(max = 520.dp)
                .testTag("paywall_intercept_modal"),
            containerColor = TacticalSurface,
            borderColor = GeminiViolet,
            accentTickColor = StatusCyan,
            strokeWidth = 1.5.dp,
            cutTopRight = 24.dp,
            cutBottomLeft = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Smooth Dynamic-Island Style Alert Pill Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AlertRedDim.copy(alpha = 0.85f))
                        .border(1.dp, AlertRed.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Intercept Warning",
                            tint = AlertRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.paywall_modal_title),
                            style = MaterialTheme.typography.labelLarge,
                            color = AlertRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(TacticalBackground.copy(alpha = 0.5f))
                            .testTag("paywall_close_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Intercept Modal",
                            tint = TerminalTextPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Code Pill & Intercept Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(WarningAmber.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = reason.code,
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningAmber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = reason.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TerminalTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = reason.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TerminalTextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // $9.99/month Gemini-Styled Clearance Card
                ChamferedTacticalBox(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = TacticalBackground,
                    borderColor = StatusCyan.copy(alpha = 0.7f),
                    accentTickColor = GeminiViolet,
                    cutTopRight = 16.dp,
                    cutBottomLeft = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = StatusCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LEVEL-5 OMEGA CLEARANCE",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = StatusCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(StatusCyanDim, GeminiViolet.copy(alpha = 0.35f))
                                        )
                                    )
                                    .border(1.dp, StatusCyan, CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "$9.99 / MONTH",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TerminalTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ClearanceFeatureRow(
                            text = "Dynamic Gemini AI Scenario Infiltration (Strict JSON Arrays)"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ClearanceFeatureRow(
                            text = "Tier-2 Zero-Day & Tier-3 APT Red-Team Combat Decks (2x/3x PTS)"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ClearanceFeatureRow(
                            text = "Full RFC 3227 & NIST SP 800-207 Forensic Rationale Terminal"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Smooth Gemini Aurora Gradient Primary Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(StatusCyan, GeminiViolet, GeminiAqua)
                            )
                        )
                        .clickable(onClick = onAuthorizeClearance)
                        .testTag("authorize_clearance_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp, horizontal = 18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TacticalBackground,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.authorize_clearance),
                            style = MaterialTheme.typography.labelLarge,
                            color = TacticalBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Smooth Pill Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(TacticalSurfaceElevated)
                        .border(1.dp, TacticalBorder, CircleShape)
                        .clickable(onClick = onDismiss)
                        .testTag("abort_intercept_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 13.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.abort_intercept),
                            style = MaterialTheme.typography.labelMedium,
                            color = TerminalTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClearanceFeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = VerifiedGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TerminalTextPrimary
        )
    }
}
