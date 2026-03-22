package com.ai.finrisk.data.ml

import com.ai.finrisk.domain.model.RiskDecision
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackRiskClassifierTest {

    private val classifier = FallbackRiskClassifier()

    @Test
    fun `assess returns success`() = runTest {
        val result = classifier.assess(floatArrayOf(0.5f, 0.3f, 0.8f))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `assess always returns REVIEW decision`() = runTest {
        val result = classifier.assess(floatArrayOf(0.5f, 0.3f, 0.8f))
        assertEquals(RiskDecision.REVIEW, result.getOrThrow().decision)
    }

    @Test
    fun `assess returns same result regardless of input`() = runTest {
        val result1 = classifier.assess(floatArrayOf(0.0f, 0.0f, 0.0f))
        val result2 = classifier.assess(floatArrayOf(1.0f, 1.0f, 1.0f))
        assertEquals(result1.getOrThrow().decision, result2.getOrThrow().decision)
        assertEquals(result1.getOrThrow().probability, result2.getOrThrow().probability, 0.001f)
    }

    @Test
    fun `assess with empty input returns success`() = runTest {
        val result = classifier.assess(floatArrayOf())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `close does not throw`() {
        // Should complete without exception
        classifier.close()
    }
}