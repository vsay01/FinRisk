package com.ai.finrisk.analytics

import com.ai.finrisk.domain.model.RiskResult
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class MlAnalyticsTest {

    private val analytics = MlAnalytics()

    @Before
    fun setUp() {
        // Plant a no-op tree so Timber.d() / Timber.e() don't crash
        // in unit tests (no Android runtime)
        if (Timber.treeCount == 0) {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    // no-op for tests
                }
            })
        }
    }

    @Test
    fun `trackInference does not throw with valid input`() {
        val result = RiskResult.fromProbability(0.75f, 1500L)
        analytics.trackInference(result, floatArrayOf(0.5f, 0.3f, 0.8f))
    }

    @Test
    fun `trackInference does not throw with empty features`() {
        val result = RiskResult.fromProbability(0.5f, 0L)
        analytics.trackInference(result, floatArrayOf())
    }

    @Test
    fun `trackInference does not throw with fallback result`() {
        val result = RiskResult.fallback()
        analytics.trackInference(result, floatArrayOf(0.0f, 0.0f, 0.0f))
    }

    @Test
    fun `trackModelLoadFailure does not throw with exception`() {
        analytics.trackModelLoadFailure(RuntimeException("model file missing"))
    }

    @Test
    fun `trackModelLoadFailure does not throw with null message`() {
        analytics.trackModelLoadFailure(RuntimeException())
    }
}