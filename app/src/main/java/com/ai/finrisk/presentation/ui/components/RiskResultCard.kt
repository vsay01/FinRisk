package com.ai.finrisk.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.ai.finrisk.domain.model.RiskDecision
import com.ai.finrisk.domain.model.RiskResult
import com.ai.finrisk.ui.theme.FinRiskTheme
import kotlin.math.roundToInt

@Composable
fun RiskResultCard(
    riskResult: RiskResult?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (riskResult?.decision) {
                RiskDecision.APPROVED -> Color(0xFFE8F5E9)
                RiskDecision.REVIEW -> Color(0xFFFFF3E0)
                RiskDecision.REJECTED -> Color(0xFFFFEBEE)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Risk Assessment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isLoading) {
                CircularProgressIndicator()
                Text("Analyzing...")
            } else if (riskResult != null) {
                RiskScoreGauge(score = riskResult.probability)

                DecisionBadge(decision = riskResult.decision)

                Text(
                    text = "${(riskResult.probability * 100).roundToInt()}% Approval Probability",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = when (riskResult.decision) {
                        RiskDecision.APPROVED -> "Pre-approved! You can proceed with the application."
                        RiskDecision.REVIEW -> "Additional review required. Please provide more documents."
                        RiskDecision.REJECTED -> "Consider our alternative products or improve your profile."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun RiskScoreGauge(score: Float) {
    val animatedScore by animateFloatAsState(
        targetValue = score,
        animationSpec = tween(1000),
        label = "score_animation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 10.dp.toPx()

            // Background arc
            drawArc(
                color = Color.Gray.copy(alpha = 0.3f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                topLeft = center - Offset(radius, radius),
                size = Size(radius * 2, radius * 2)
            )

            // Score arc
            val sweepAngle = animatedScore * 270f
            val arcColor = when {
                animatedScore >= 0.7f -> Color(0xFF4CAF50)
                animatedScore >= 0.4f -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }

            drawArc(
                color = arcColor,
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                topLeft = center - Offset(radius, radius),
                size = Size(radius * 2, radius * 2)
            )
        }

        Text(
            text = "${(animatedScore * 100).roundToInt()}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DecisionBadge(decision: RiskDecision) {
    val (backgroundColor, textColor, text) = when (decision) {
        RiskDecision.APPROVED -> Triple(
            Color(0xFF4CAF50),
            Color.White,
            "APPROVED"
        )
        RiskDecision.REVIEW -> Triple(
            Color(0xFFFF9800),
            Color.White,
            "NEEDS REVIEW"
        )
        RiskDecision.REJECTED -> Triple(
            Color(0xFFF44336),
            Color.White,
            "REJECTED"
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// region Previews

/**
 * Preview parameter provider for different risk scenarios.
 */
private class RiskResultPreviewProvider : PreviewParameterProvider<RiskResult?> {
    override val values = sequenceOf(
        RiskResult(probability = 0.85f, decision = RiskDecision.APPROVED, inferenceTimeMs = 2),
        RiskResult(probability = 0.55f, decision = RiskDecision.REVIEW, inferenceTimeMs = 3),
        RiskResult(probability = 0.25f, decision = RiskDecision.REJECTED, inferenceTimeMs = 2),
        null // Loading state
    )
}

@Preview(showBackground = true, name = "Approved")
@Composable
private fun RiskResultCardApprovedPreview() {
    FinRiskTheme {
        RiskResultCard(
            riskResult = RiskResult(
                probability = 0.85f,
                decision = RiskDecision.APPROVED,
                inferenceTimeMs = 2
            ),
            isLoading = false
        )
    }
}

@Preview(showBackground = true, name = "Review")
@Composable
private fun RiskResultCardReviewPreview() {
    FinRiskTheme {
        RiskResultCard(
            riskResult = RiskResult(
                probability = 0.55f,
                decision = RiskDecision.REVIEW,
                inferenceTimeMs = 3
            ),
            isLoading = false
        )
    }
}

@Preview(showBackground = true, name = "Rejected")
@Composable
private fun RiskResultCardRejectedPreview() {
    FinRiskTheme {
        RiskResultCard(
            riskResult = RiskResult(
                probability = 0.25f,
                decision = RiskDecision.REJECTED,
                inferenceTimeMs = 2
            ),
            isLoading = false
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun RiskResultCardLoadingPreview() {
    FinRiskTheme {
        RiskResultCard(
            riskResult = null,
            isLoading = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RiskScoreGaugePreview() {
    FinRiskTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RiskScoreGauge(score = 0.85f)
                RiskScoreGauge(score = 0.55f)
                RiskScoreGauge(score = 0.25f)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DecisionBadgePreview() {
    FinRiskTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DecisionBadge(decision = RiskDecision.APPROVED)
                DecisionBadge(decision = RiskDecision.REVIEW)
                DecisionBadge(decision = RiskDecision.REJECTED)
            }
        }
    }
}

// endregion
