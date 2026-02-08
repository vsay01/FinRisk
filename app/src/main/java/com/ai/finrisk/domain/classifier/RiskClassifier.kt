package com.ai.finrisk.domain.classifier

import com.ai.finrisk.domain.model.RiskResult

/**
 * Contract for ML-based credit risk assessment.
 *
 * This interface abstracts the ML inference implementation, allowing:
 * - Easy swapping of ML frameworks (TFLite, ONNX, remote API)
 * - Unit testing with mock implementations
 * - Domain layer independence from data layer details
 *
 * ## Implementation Notes
 * Implementations must handle:
 * - Model loading and lifecycle
 * - Input validation (expected 3 normalized features)
 * - Thread safety for concurrent calls
 *
 * @see com.ai.finrisk.data.ml.LiteRtRiskClassifier for TensorFlow Lite implementation
 */
interface RiskClassifier {

    /**
     * Performs credit risk assessment using ML inference.
     *
     * @param features Normalized input features array of size 3:
     *   - [0] Income: normalized to 0-1 range (from $20K-$200K)
     *   - [1] Age: normalized to 0-1 range (from 18-65 years)
     *   - [2] App Engagement: already 0-1 (percentage)
     *
     * @return [Result.success] with [RiskResult] containing probability and decision,
     *         or [Result.failure] if inference fails
     *
     * @throws IllegalArgumentException if features array size != 3
     */
    suspend fun assess(features: FloatArray): Result<RiskResult>

    /**
     * Releases ML model resources.
     *
     * Should be called when the classifier is no longer needed,
     * typically in ViewModel's onCleared() or Application's onTerminate().
     */
    fun close()
}
