package com.ai.finrisk.domain.model

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class RiskResultTest {

    @Test
    fun `fallback returns REVIEW decision`() {
        val result = RiskResult.fallback()
        assertEquals(RiskDecision.REVIEW, result.decision)
    }

    @Test
    fun `fallback returns 0_5 probability`() {
        val result = RiskResult.fallback()
        Assert.assertEquals(0.5f, result.probability, 0.001f)
    }

    @Test
    fun `fallback returns zero inference time`() {
        val result = RiskResult.fallback()
        Assert.assertEquals(0L, result.inferenceTimeMicros)
    }

    @Test
    fun `fromProbability at approval threshold returns APPROVED`() {
        val result = RiskResult.fromProbability(0.6f, 100L)
        assertEquals(RiskDecision.APPROVED, result.decision)
    }

    @Test
    fun `fromProbability at review threshold returns REVIEW`() {
        val result = RiskResult.fromProbability(0.4f, 100L)
        assertEquals(RiskDecision.REVIEW, result.decision)
    }

    @Test
    fun `fromProbability below review threshold returns REJECTED`() {
        val result = RiskResult.fromProbability(0.39f, 100L)
        assertEquals(RiskDecision.REJECTED, result.decision)
    }

    @Test
    fun `fromProbability clamps above 1`() {
        val result = RiskResult.fromProbability(1.5f, 100L)
        Assert.assertEquals(1.0f, result.probability, 0.001f)
    }

    @Test
    fun `fromProbability clamps below 0`() {
        val result = RiskResult.fromProbability(-0.5f, 100L)
        Assert.assertEquals(0.0f, result.probability, 0.001f)
    }
}