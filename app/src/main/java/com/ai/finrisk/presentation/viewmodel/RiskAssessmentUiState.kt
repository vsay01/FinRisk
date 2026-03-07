package com.ai.finrisk.presentation.viewmodel

import com.ai.finrisk.domain.model.RiskResult

data class RiskAssessmentUiState(
    val income: Float = 60000f,
    val debtRatio: Float = 0.35f,      // 35% DTI (typical)
    val creditHistory: Int = 680,       // US median credit score
    val riskResult: RiskResult? = null,
    val preprocessedFeatures: FloatArray? = null,
    val inferenceTimeMicros: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RiskAssessmentUiState

        if (income != other.income) return false
        if (debtRatio != other.debtRatio) return false
        if (creditHistory != other.creditHistory) return false
        if (riskResult != other.riskResult) return false
        if (preprocessedFeatures != null) {
            if (other.preprocessedFeatures == null) return false
            if (!preprocessedFeatures.contentEquals(other.preprocessedFeatures)) return false
        } else if (other.preprocessedFeatures != null) return false
        if (inferenceTimeMicros != other.inferenceTimeMicros) return false
        if (isLoading != other.isLoading) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = income.hashCode()
        result = 31 * result + debtRatio.toInt()
        result = 31 * result + creditHistory.hashCode()
        result = 31 * result + (riskResult?.hashCode() ?: 0)
        result = 31 * result + (preprocessedFeatures?.contentHashCode() ?: 0)
        result = 31 * result + (inferenceTimeMicros?.hashCode() ?: 0)
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}
