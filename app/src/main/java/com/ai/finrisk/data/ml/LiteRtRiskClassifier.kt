package com.ai.finrisk.data.ml

import android.content.Context
import com.ai.finrisk.domain.classifier.RiskClassifier
import com.ai.finrisk.domain.model.RiskResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TensorFlow Lite (LiteRT) implementation of [RiskClassifier].
 *
 * Loads and runs the `finrisk_classifier.tflite` model from assets.
 *
 * ## Model Details
 * - Input: [1, 3] float tensor (income, age, engagement - all normalized 0-1)
 * - Output: [1, 1] float tensor (approval probability 0-1)
 * - Size: ~2KB (simple logistic regression)
 *
 * ## Thread Safety
 * Inference runs on [Dispatchers.Default] to avoid blocking the main thread.
 * The TFLite Interpreter is thread-safe for concurrent calls.
 *
 * ## Lifecycle
 * - Model loads eagerly in constructor
 * - Call [close] when done to release native resources
 *
 * @param context Application context for accessing assets
 */
@Singleton
class LiteRtRiskClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) : RiskClassifier {

    private var interpreter: Interpreter? = null

    init {
        loadModel()
    }

    /**
     * Loads the TFLite model from assets into memory.
     *
     * @throws IllegalStateException if model file is missing or corrupted
     */
    private fun loadModel() {
        try {
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options().apply {
                setNumThreads(2)
            }
            interpreter = Interpreter(modelBuffer, options)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load ML model: ${e.message}", e)
        }
    }

    /**
     * Memory-maps the model file for efficient loading.
     */
    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_FILE)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Runs inference on the TFLite model.
     *
     * @param features Normalized float array of size 3
     * @return [Result.success] with [RiskResult], or [Result.failure] on error
     */
    override suspend fun assess(features: FloatArray): Result<RiskResult> = withContext(Dispatchers.Default) {
        try {
            val currentInterpreter = interpreter
                ?: return@withContext Result.failure(IllegalStateException("Model not loaded"))

            if (features.size != INPUT_SIZE) {
                return@withContext Result.failure(
                    IllegalArgumentException("Expected $INPUT_SIZE features, got ${features.size}")
                )
            }

            // Prepare input buffer [1, 3]
            val inputBuffer = ByteBuffer.allocateDirect(INPUT_SIZE * FLOAT_SIZE).apply {
                order(ByteOrder.nativeOrder())
                features.forEach { putFloat(it) }
                rewind()
            }

            // Prepare output buffer [1, 1]
            val outputBuffer = ByteBuffer.allocateDirect(OUTPUT_SIZE * FLOAT_SIZE).apply {
                order(ByteOrder.nativeOrder())
            }

            // Run inference and measure time
            val startTime = System.currentTimeMillis()
            currentInterpreter.run(inputBuffer, outputBuffer)
            val inferenceTime = System.currentTimeMillis() - startTime

            // Extract probability from output
            outputBuffer.rewind()
            val probability = outputBuffer.float.coerceIn(0f, 1f)

            Result.success(RiskResult.fromProbability(probability, inferenceTime))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Releases the TFLite interpreter and associated resources.
     */
    override fun close() {
        interpreter?.close()
        interpreter = null
    }

    companion object {
        /** Model filename in assets folder */
        private const val MODEL_FILE = "finrisk_classifier.tflite"

        /** Number of input features (income, age, engagement) */
        private const val INPUT_SIZE = 3

        /** Number of output values (probability) */
        private const val OUTPUT_SIZE = 1

        /** Size of float in bytes */
        private const val FLOAT_SIZE = 4
    }
}
