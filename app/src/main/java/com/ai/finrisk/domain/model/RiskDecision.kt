package com.ai.finrisk.domain.model

/**
 * Represents the outcome of a credit risk assessment.
 *
 * Business rules for decision thresholds:
 * - [APPROVED]: probability >= 0.7 (70%)
 * - [REVIEW]: probability >= 0.4 (40%) and < 0.7
 * - [REJECTED]: probability < 0.4 (40%)
 */
enum class RiskDecision {
    /** User is pre-approved for the loan */
    APPROVED,

    /** Additional manual review required */
    REVIEW,

    /** Application does not meet minimum criteria */
    REJECTED
}
