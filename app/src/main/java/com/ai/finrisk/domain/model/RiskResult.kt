package com.ai.finrisk.domain.model

/**
 * Result of ML-based credit risk assessment.
 *
 * Contains the raw probability from the model and the derived business decision.
 *
 * @property probability Raw model output (0.0 to 1.0), representing approval likelihood
 * @property decision Business decision derived from probability thresholds
 * @property inferenceTimeMs Time taken for ML inference in milliseconds
 */
data class RiskResult(
    val probability: Float,
    val decision: RiskDecision,
    val inferenceTimeMs: Long
) {
    companion object {
        private const val APPROVAL_THRESHOLD = 0.7f
        private const val REVIEW_THRESHOLD = 0.4f

        /**
         * Factory method to create [RiskResult] from raw model probability.
         *
         * Applies business rules to convert probability into [RiskDecision]:
         * - >= 70%: APPROVED
         * - >= 40%: REVIEW
         * - < 40%: REJECTED
         *
         * @param probability Raw model output (will be clamped to 0-1 range)
         * @param inferenceTimeMs Time taken for inference
         * @return [RiskResult] with appropriate decision
         */
        fun fromProbability(probability: Float, inferenceTimeMs: Long): RiskResult {
            val clampedProbability = probability.coerceIn(0f, 1f)
            val decision = when {
                clampedProbability >= APPROVAL_THRESHOLD -> RiskDecision.APPROVED
                clampedProbability >= REVIEW_THRESHOLD -> RiskDecision.REVIEW
                else -> RiskDecision.REJECTED
            }
            return RiskResult(
                probability = clampedProbability,
                decision = decision,
                inferenceTimeMs = inferenceTimeMs
            )
        }
    }
}
