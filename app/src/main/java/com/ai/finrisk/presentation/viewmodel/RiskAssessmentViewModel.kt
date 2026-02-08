package com.ai.finrisk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.finrisk.domain.classifier.RiskClassifier
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
 * 1. User adjusts slider → [updateIncome]/[updateAge]/[updateAppEngagement]
 * 2. Input values stored in [uiState]
 * 3. [performRiskAssessment] called automatically
 * 4. Features normalized and sent to [RiskClassifier]
 * 5. Result updates [uiState] → UI recomposes
 *
 * ## Preprocessing
 * Raw inputs are normalized to 0-1 range to match model training:
 * - Income: ($20K-$200K) → (0.0-1.0)
 * - Age: (18-65) → (0.0-1.0)
 * - Engagement: already 0-1
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

    /**
     * Updates age and triggers reassessment.
     * @param newAge Age in years (18 - 65)
     */
    fun updateAge(newAge: Int) {
        _uiState.value = _uiState.value.copy(age = newAge)
        performRiskAssessment()
    }

    /**
     * Updates app engagement score and triggers reassessment.
     * @param newEngagement Engagement percentage (0.0 - 1.0)
     */
    fun updateAppEngagement(newEngagement: Float) {
        _uiState.value = _uiState.value.copy(appEngagement = newEngagement)
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val currentState = _uiState.value

            // Normalize inputs to 0-1 range (must match training preprocessing)
            val preprocessedFeatures = floatArrayOf(
                (currentState.income - INCOME_MIN) / (INCOME_MAX - INCOME_MIN),
                (currentState.age - AGE_MIN) / (AGE_MAX - AGE_MIN),
                currentState.appEngagement // Already 0-1
            )

            val result = riskClassifier.assess(preprocessedFeatures)

            result.fold(
                onSuccess = { riskResult ->
                    _uiState.value = _uiState.value.copy(
                        riskResult = riskResult,
                        preprocessedFeatures = preprocessedFeatures,
                        inferenceTimeMs = riskResult.inferenceTimeMs,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error"
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
        private const val AGE_MIN = 18f
        private const val AGE_MAX = 65f
    }
}
