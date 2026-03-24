package com.ai.finrisk.analytics

import com.ai.finrisk.domain.model.RiskResult
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks ML-specific analytics events.
 *
 * In production, these events would go to Firebase Analytics:
 *
 * ```kotlin
 * firebaseAnalytics.logEvent("ml_inference") {
 *     param("decision", result.decision.name)
 *     param("probability", result.probability.toDouble())
 *     param("latency_micros", result.inferenceTimeMicros)
 *     param("model_version", BuildConfig.MODEL_VERSION)
 * }
 * ```
 *
 * This implementation logs via Timber as a placeholder.
 * Replace with Firebase Analytics after adding Firebase to your project.
 */
@Singleton
class MlAnalytics @Inject constructor() {

    /**
     * Track an ML inference event.
     * Call after every successful prediction.
     */
    fun trackInference(result: RiskResult, features: FloatArray) {
        Timber.d(
            "ML inference: decision=%s probability=%.4f latency=%dµs features=%s",
            result.decision.name,
            result.probability,
            result.inferenceTimeMicros,
            features.contentToString()
        )
        // TODO: Replace with Firebase Analytics when configured
        // firebaseAnalytics.logEvent("ml_inference") { ... }
    }

    /**
     * Track a model load failure.
     * Call when LiteRtRiskClassifier fails to initialize.
     */
    fun trackModelLoadFailure(error: Throwable) {
        Timber.e(error, "ML model load failed: %s", error.message)
        // TODO: Replace with Firebase Analytics when configured
        // firebaseAnalytics.logEvent("ml_model_load_failed") { ... }
    }
}