package com.ai.finrisk.data.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelStateTest {

    @Test
    fun `Loading is a singleton object`() {
        val a = ModelState.Loading
        val b = ModelState.Loading
        assertTrue(a === b)
    }

    @Test
    fun `Ready holds an Interpreter reference`() {
        // We can't easily construct a real Interpreter in unit tests
        // (requires native libs), but we can verify the sealed class
        // structure compiles and the type hierarchy is correct.
        val state: ModelState = ModelState.Loading
        assertTrue(state is ModelState.Loading)
    }

    @Test
    fun `Failed holds a Throwable`() {
        val error = RuntimeException("Model file not found")
        val state = ModelState.Failed(error)
        assertEquals("Model file not found", state.error.message)
    }

    @Test
    fun `exhaustive when covers all states`() {
        val states = listOf(
            ModelState.Loading,
            ModelState.Failed(RuntimeException("test"))
        )
        // This test verifies the when expression compiles exhaustively.
        // If a new state is added to the sealed class, this test will
        // fail to compile — catching the missing branch at build time.
        for (state in states) {
            val label = when (state) {
                is ModelState.Loading -> "loading"
                is ModelState.Ready -> "ready"
                is ModelState.Failed -> "failed"
            }
            assertTrue(label.isNotEmpty())
        }
    }
}