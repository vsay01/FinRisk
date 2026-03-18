package com.ai.finrisk.data.ml

import org.tensorflow.lite.Interpreter

/**
 * Represents the lifecycle state of the ML model.
 *
 * Using a sealed class instead of a nullable Interpreter makes
 * the model's state explicit — you can distinguish "not loaded yet"
 * from "tried to load and failed" instead of just seeing null.
 */
sealed class ModelState {
    /** Model is being loaded from assets. */
    object Loading : ModelState()

    /** Model loaded successfully and is ready for inference. */
    data class Ready(val interpreter: Interpreter) : ModelState()

    /** Model failed to load. Contains the cause for crash reporting. */
    data class Failed(val error: Throwable) : ModelState()
}
