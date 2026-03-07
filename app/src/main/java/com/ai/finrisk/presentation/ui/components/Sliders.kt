package com.ai.finrisk.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.finrisk.ui.theme.FinRiskTheme
import kotlin.math.roundToInt

@Composable
fun IncomeSlider(
    currentIncome: Float,
    onIncomeChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Annual Income",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$${String.format("%,.0f", currentIncome)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = currentIncome,
            onValueChange = { onIncomeChange((it / 1000).toInt() * 1000f) }, // Round to nearest $1000
            valueRange = 20000f..200000f,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$20K",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$200K",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DebtRatioSlider(
    currentDebtRatio: Float,
    onDebtRatioChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Debt-to-Income Ratio",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${(currentDebtRatio * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = currentDebtRatio,
            onValueChange = { onDebtRatioChange((it * 100).toInt() / 100f) },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "0%", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "43% (CFPB limit)", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "100%", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CreditHistorySlider(
    currentCreditHistory: Int,
    onCreditHistoryChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Credit Score",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$currentCreditHistory",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = currentCreditHistory.toFloat(),
            onValueChange = { onCreditHistoryChange(it.roundToInt()) },
            valueRange = 300f..850f,
            steps = 54,  // 550 range / 10 = 55 positions, 54 intermediate steps
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "300 (Poor)", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "850 (Excellent)", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun IncomeSliderPreview() {
    FinRiskTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            var income by remember { mutableFloatStateOf(75000f) }
            IncomeSlider(
                currentIncome = income,
                onIncomeChange = { income = it }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DebtRatioSliderPreview() {
    FinRiskTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            var debtRatio by remember { mutableFloatStateOf(0.35f) }
            DebtRatioSlider(
                currentDebtRatio = debtRatio,
                onDebtRatioChange = { debtRatio = it }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreditHistorySliderPreview() {
    FinRiskTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            var creditScore by remember { mutableIntStateOf(720) }
            CreditHistorySlider(
                currentCreditHistory = creditScore,
                onCreditHistoryChange = { creditScore = it }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AllSlidersPreview() {
    FinRiskTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                var income by remember { mutableFloatStateOf(75000f) }
                IncomeSlider(
                    currentIncome = income,
                    onIncomeChange = { income = it }
                )

                var debtRatio by remember { mutableFloatStateOf(0.35f) }
                DebtRatioSlider(
                    currentDebtRatio = debtRatio,
                    onDebtRatioChange = { debtRatio = it }
                )

                var creditScore by remember { mutableIntStateOf(720) }
                CreditHistorySlider(
                    currentCreditHistory = creditScore,
                    onCreditHistoryChange = { creditScore = it }
                )
            }
        }
    }
}

// endregion
