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
fun AgeSlider(
    currentAge: Int,
    onAgeChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Age",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$currentAge years",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = currentAge.toFloat(),
            onValueChange = { onAgeChange(it.roundToInt()) },
            valueRange = 18f..65f,
            steps = 46, // 48 values (18-65) needs 46 intermediate steps
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "18",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "65",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EngagementSlider(
    currentEngagement: Float,
    onEngagementChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "App Engagement",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${(currentEngagement * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = currentEngagement,
            onValueChange = { onEngagementChange((it * 100).toInt() / 100f) }, // Round to nearest 1%
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Low",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "High",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
private fun AgeSliderPreview() {
    FinRiskTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            var age by remember { mutableIntStateOf(35) }
            AgeSlider(
                currentAge = age,
                onAgeChange = { age = it }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EngagementSliderPreview() {
    FinRiskTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            var engagement by remember { mutableFloatStateOf(0.65f) }
            EngagementSlider(
                currentEngagement = engagement,
                onEngagementChange = { engagement = it }
            )
        }
    }
}

@Preview(showBackground = true, name = "All Sliders")
@Composable
private fun AllSlidersPreview() {
    FinRiskTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                IncomeSlider(currentIncome = 60000f, onIncomeChange = {})
                AgeSlider(currentAge = 30, onAgeChange = {})
                EngagementSlider(currentEngagement = 0.5f, onEngagementChange = {})
            }
        }
    }
}

// endregion
