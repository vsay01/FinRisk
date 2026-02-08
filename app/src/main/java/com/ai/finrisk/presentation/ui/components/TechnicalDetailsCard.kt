package com.ai.finrisk.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.finrisk.ui.theme.FinRiskTheme

@Composable
fun TechnicalDetailsCard(
    preprocessedFeatures: FloatArray?,
    inferenceTime: Long?
) {
    if (preprocessedFeatures != null && inferenceTime != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Technical Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Preprocessed Features (normalized 0-1):",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "  Income:     ${String.format("%.4f", preprocessedFeatures[0])}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "  Age:        ${String.format("%.4f", preprocessedFeatures[1])}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "  Engagement: ${String.format("%.4f", preprocessedFeatures[2])}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "Inference Time: ${inferenceTime}ms",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun TechnicalDetailsCardPreview() {
    FinRiskTheme {
        TechnicalDetailsCard(
            preprocessedFeatures = floatArrayOf(0.2222f, 0.2553f, 0.6500f),
            inferenceTime = 2L
        )
    }
}

@Preview(showBackground = true, name = "High Values")
@Composable
private fun TechnicalDetailsCardHighValuesPreview() {
    FinRiskTheme {
        TechnicalDetailsCard(
            preprocessedFeatures = floatArrayOf(0.8889f, 0.7872f, 0.9500f),
            inferenceTime = 1L
        )
    }
}

// endregion
