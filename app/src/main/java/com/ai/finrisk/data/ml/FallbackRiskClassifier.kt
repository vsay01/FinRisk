package com.ai.finrisk.data.ml

import com.ai.finrisk.domain.classifier.RiskClassifier
import com.ai.finrisk.domain.model.RiskResult
import javax.inject.Inject

/**
 * Fallback classifier used when the ML model is unavailable.
 *
 * Returns REVIEW for all inputs — the safest default when ML
 * can't make a confident decision. This keeps the app functional
 * instead of crashing when the model fails to load or is disabled
 * via feature flag.
 */
class FallbackRiskClassifier @Inject constructor() : RiskClassifier {

    override suspend fun assess(features: FloatArray): Result<RiskResult> =
        Result.success(RiskResult.fallback())

    override fun close() { /* no resources to release */ }
}
