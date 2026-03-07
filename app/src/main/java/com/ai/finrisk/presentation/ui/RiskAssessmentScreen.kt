package com.ai.finrisk.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ai.finrisk.domain.model.RiskDecision
import com.ai.finrisk.domain.model.RiskResult
import com.ai.finrisk.presentation.ui.components.CreditHistorySlider
import com.ai.finrisk.presentation.ui.components.DebtRatioSlider
import com.ai.finrisk.presentation.ui.components.IncomeSlider
import com.ai.finrisk.presentation.ui.components.RiskResultCard
import com.ai.finrisk.presentation.ui.components.TechnicalDetailsCard
import com.ai.finrisk.presentation.viewmodel.RiskAssessmentUiState
import com.ai.finrisk.presentation.viewmodel.RiskAssessmentViewModel
import com.ai.finrisk.ui.theme.FinRiskTheme

/**
 * Main Risk Assessment screen with ViewModel integration.
 *
 * @param modifier Modifier for the root layout
 * @param viewModel Hilt-injected ViewModel
 */
@Composable
fun RiskAssessmentScreen(
    modifier: Modifier = Modifier,
    viewModel: RiskAssessmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    RiskAssessmentContent(
        modifier = modifier,
        uiState = uiState,
        onIncomeChange = viewModel::updateIncome,
        onDebtRatioChange = viewModel::updateDebtRatio,
        onCreditHistoryChange = viewModel::updateCreditHistory
    )
}

/**
 * Stateless content composable for the Risk Assessment screen.
 *
 * Extracted for preview support and testing.
 */
@Composable
internal fun RiskAssessmentContent(
    modifier: Modifier = Modifier,
    uiState: RiskAssessmentUiState,
    onIncomeChange: (Float) -> Unit,
    onDebtRatioChange: (Float) -> Unit,
    onCreditHistoryChange: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Text(
            text = "FinRisk Credit Assessment",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        // Input Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Adjust Your Profile",
                    style = MaterialTheme.typography.titleMedium
                )

                IncomeSlider(
                    currentIncome = uiState.income,
                    onIncomeChange = onIncomeChange
                )

                DebtRatioSlider(
                    currentDebtRatio = uiState.debtRatio,
                    onDebtRatioChange = onDebtRatioChange
                )

                CreditHistorySlider(
                    currentCreditHistory = uiState.creditHistory,
                    onCreditHistoryChange = onCreditHistoryChange
                )
            }
        }

        // Results Section
        RiskResultCard(
            riskResult = uiState.riskResult,
            isLoading = uiState.isLoading
        )

        // Error display
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Error: $error",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Technical Details
        TechnicalDetailsCard(
            preprocessedFeatures = uiState.preprocessedFeatures,
            inferenceTimeMicros = uiState.inferenceTimeMicros
        )
    }
}

// region Previews
// Approved preview: high income, low DTI, excellent credit
@Preview(showBackground = true, showSystemUi = true, name = "Approved State")
@Composable
private fun RiskAssessmentScreenApprovedPreview() {
    FinRiskTheme {
        RiskAssessmentContent(
            uiState = RiskAssessmentUiState(
                income = 120000f,
                debtRatio = 0.20f,
                creditHistory = 780,
                riskResult = RiskResult(
                    probability = 0.85f,
                    decision = RiskDecision.APPROVED,
                    inferenceTimeMicros = 2
                ),
                preprocessedFeatures = floatArrayOf(0.5556f, 0.3617f, 0.8000f),
                inferenceTimeMicros = 2,
                isLoading = false
            ),
            onIncomeChange = {},
            onDebtRatioChange = {},
            onCreditHistoryChange = {}
        )
    }
}

// Review preview: moderate income, moderate DTI, fair credit
@Preview(showBackground = true, showSystemUi = true, name = "Review State")
@Composable
private fun RiskAssessmentScreenReviewPreview() {
    FinRiskTheme {
        RiskAssessmentContent(
            uiState = RiskAssessmentUiState(
                income = 60000f,
                debtRatio = 0.38f,
                creditHistory = 660,
                riskResult = RiskResult(
                    probability = 0.55f,
                    decision = RiskDecision.REVIEW,
                    inferenceTimeMicros = 3
                ),
                preprocessedFeatures = floatArrayOf(0.2222f, 0.2128f, 0.5000f),
                inferenceTimeMicros = 3,
                isLoading = false
            ),
            onIncomeChange = {},
            onDebtRatioChange = {},
            onCreditHistoryChange = {}
        )
    }
}

// Rejected preview: low income, high DTI, poor credit
@Preview(showBackground = true, showSystemUi = true, name = "Rejected State")
@Composable
private fun RiskAssessmentScreenRejectedPreview() {
    FinRiskTheme {
        RiskAssessmentContent(
            uiState = RiskAssessmentUiState(
                income = 30000f,
                debtRatio = 0.55f,
                creditHistory = 520,
                riskResult = RiskResult(
                    probability = 0.18f,
                    decision = RiskDecision.REJECTED,
                    inferenceTimeMicros = 2
                ),
                preprocessedFeatures = floatArrayOf(0.0556f, 0.0851f, 0.2000f),
                inferenceTimeMicros = 2,
                isLoading = false
            ),
            onIncomeChange = {},
            onDebtRatioChange = {},
            onCreditHistoryChange = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Loading State")
@Composable
private fun RiskAssessmentScreenLoadingPreview() {
    FinRiskTheme {
        RiskAssessmentContent(
            uiState = RiskAssessmentUiState(
                income = 60000f,
                debtRatio = 0.55f,
                creditHistory = 520,
                isLoading = true
            ),
            onIncomeChange = {},
            onDebtRatioChange = {},
            onCreditHistoryChange = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Error State")
@Composable
private fun RiskAssessmentScreenErrorPreview() {
    FinRiskTheme {
        RiskAssessmentContent(
            uiState = RiskAssessmentUiState(
                income = 60000f,
                debtRatio = 0.55f,
                creditHistory = 520,
                isLoading = false,
                error = "Failed to load ML model"
            ),
            onIncomeChange = {},
            onDebtRatioChange = {},
            onCreditHistoryChange = {}
        )
    }
}

// endregion
