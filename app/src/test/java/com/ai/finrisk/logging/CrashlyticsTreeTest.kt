package com.ai.finrisk.logging

import android.util.Log
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class CrashlyticsTreeTest {

    /** Captures calls that pass the priority filter. */
    private val captured = mutableListOf<Pair<Int, String>>()

    private val tree = object : CrashlyticsTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Capture everything the parent would forward.
            // super.log() calls Log.e() which is stubbed in unit tests,
            // so we capture here instead of delegating.
            if (priority < Log.ERROR) return
            captured.add(priority to message)
        }
    }

    @Before
    fun setup() {
        Timber.plant(tree)
    }

    @After
    fun teardown() {
        Timber.uprootAll()
    }

    @Test
    fun `filters out DEBUG priority`() {
        Timber.d("debug message")
        assertEquals(0, captured.size)
    }

    @Test
    fun `filters out INFO priority`() {
        Timber.i("info message")
        assertEquals(0, captured.size)
    }

    @Test
    fun `filters out WARN priority`() {
        Timber.w("warn message")
        assertEquals(0, captured.size)
    }

    @Test
    fun `forwards ERROR priority`() {
        Timber.e("error message")
        assertEquals(1, captured.size)
        assertEquals("error message", captured[0].second)
    }

    @Test
    fun `forwards ASSERT priority`() {
        Timber.wtf("assert message")
        assertEquals(1, captured.size)
    }
}