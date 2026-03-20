package com.ai.finrisk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.finrisk.domain.classifier.RiskClassifier
import com.ai.finrisk.domain.model.RiskResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Risk Assessment screen.
 *
 * Manages user input state and triggers ML inference when inputs change.
 *
 * ## Data Flow
 * 1. User adjusts slider → [updateIncome]/[updateDebtRatio]/[updateCreditHistory]
 * 2. Input values stored in [uiState]
 * 3. [performRiskAssessment] called automatically
 * 4. Features normalized and sent to [RiskClassifier]
 * 5. Result updates [uiState] → UI recomposes
 *
 * ## Preprocessing
 * Raw inputs are normalized to 0-1 range to match model training:
 * - Income: ($20K-$200K) → (0.0-1.0)
 * - Debt Ratio: already 0-1 (percentage)
 * - Credit History: (300-850) → (0.0-1.0)
 *
 * @param riskClassifier Injected ML classifier for inference
 */
@HiltViewModel
class RiskAssessmentViewModel @Inject constructor(
    private val riskClassifier: RiskClassifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiskAssessmentUiState())
    val uiState: StateFlow<RiskAssessmentUiState> = _uiState.asStateFlow()

    init {
        // Run initial assessment with default values
        performRiskAssessment()
    }

    /**
     * Updates annual income and triggers reassessment.
     * @param newIncome Income in dollars ($20,000 - $200,000)
     */
    fun updateIncome(newIncome: Float) {
        _uiState.value = _uiState.value.copy(income = newIncome)
        performRiskAssessment()
    }

    fun updateDebtRatio(newDebtRatio: Float) {
        _uiState.value = _uiState.value.copy(debtRatio = newDebtRatio)
        performRiskAssessment()
    }

    fun updateCreditHistory(newCreditHistory: Int) {
        _uiState.value = _uiState.value.copy(creditHistory = newCreditHistory)
        performRiskAssessment()
    }

    /**
     * Preprocesses inputs and runs ML inference.
     *
     * Normalization formula: (value - min) / (max - min)
     * This matches the preprocessing used during model training.
     */
    private fun performRiskAssessment() {
        viewModelScope.launch {
            val currentState = _uiState.value

            // Only show loading spinner on the first inference (no previous result).
            // Subsequent inferences complete in microseconds -- showing a spinner
            // causes visible flicker as the UI swaps between result and "Analyzing..."
            val showLoading = currentState.riskResult == null
            _uiState.value = currentState.copy(
                isLoading = showLoading,
                error = null
            )

            // Normalize inputs to 0-1 range (must match training preprocessing)
            val preprocessedFeatures = floatArrayOf(
                (currentState.income - INCOME_MIN) / (INCOME_MAX - INCOME_MIN),
                currentState.debtRatio,  // Already 0-1
                (currentState.creditHistory - CREDIT_HISTORY_MIN) / (CREDIT_HISTORY_MAX - CREDIT_HISTORY_MIN)
            )

            val result = riskClassifier.assess(preprocessedFeatures)

            result.fold(
                onSuccess = { riskResult ->
                    _uiState.value = _uiState.value.copy(
                        riskResult = riskResult,
                        preprocessedFeatures = preprocessedFeatures,
                        inferenceTimeMicros = riskResult.inferenceTimeMicros,
                        isLoading = false
                    )
                },
                onFailure = {
                    // Graceful degradation: show fallback instead of error
                    _uiState.value = _uiState.value.copy(
                        riskResult = RiskResult.fallback(),
                        isLoading = false,
                        error = null
                    )
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        riskClassifier.close()
    }

    companion object {
        // Normalization constants (must match training data range)
        private const val INCOME_MIN = 20000f
        private const val INCOME_MAX = 200000f
        private const val CREDIT_HISTORY_MIN = 300f
        private const val CREDIT_HISTORY_MAX = 850f
        // Debt ratio: 0.0-1.0, no constants needed (already normalized)
    }
}
