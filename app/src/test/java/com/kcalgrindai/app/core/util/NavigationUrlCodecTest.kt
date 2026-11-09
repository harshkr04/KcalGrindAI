package com.kcalgrindai.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NavigationUrlCodecTest {

    @Test
    fun `encode once and decode once correctly restores original prompt`() {
        val originalPrompt = "snack suggestion"
        val encoded = NavigationUrlCodec.encode(originalPrompt)

        // Verify it was encoded once
        assertEquals("snack+suggestion", encoded)

        // Verify decoding once restores original string with spaces
        val decoded = NavigationUrlCodec.decode(encoded)
        assertEquals(originalPrompt, decoded)
        assertFalse(decoded.contains("%20"))
    }

    @Test
    fun `decode handles raw percent-twenty encoded string without leaving escapes`() {
        // Simulates receiving "snack%20suggestio" directly from malformed or un-decoded navigation argument
        val malformedPrompt = "snack%20suggestio"
        val decoded = NavigationUrlCodec.decode(malformedPrompt)

        assertEquals("snack suggestio", decoded)
        assertFalse("Decoded text must not contain raw %20", decoded.contains("%20"))
    }

    @Test
    fun `decode recovers double-encoded percent sequences cleanly`() {
        // Simulates text that was URL-encoded twice: "snack%2520suggestion"
        val doubleEncoded = "snack%2520suggestion"
        val decoded = NavigationUrlCodec.decode(doubleEncoded)

        assertEquals("snack suggestion", decoded)
        assertFalse(decoded.contains("%25"))
        assertFalse(decoded.contains("%20"))
    }

    @Test
    fun `complex coach query with punctuation and macros survives round trip intact`() {
        val query = "What should I eat for dinner with 40g protein? & under 500 kcal!"
        val encoded = NavigationUrlCodec.encode(query)
        val decoded = NavigationUrlCodec.decode(encoded)

        assertEquals(query, decoded)
    }

    @Test
    fun `buildAiChatRoute produces valid route with encoded prompt`() {
        val prompt = "quick protein snack"
        val route = NavigationUrlCodec.buildAiChatRoute(prompt)

        assertEquals("ai_chat?prompt=quick+protein+snack", route)
        val extractedArg = route.substringAfter("prompt=")
        assertEquals(prompt, NavigationUrlCodec.decode(extractedArg))
    }

    @Test
    fun `null and blank handling is safe and non-crashing`() {
        assertEquals("", NavigationUrlCodec.decode(null))
        assertEquals("", NavigationUrlCodec.decode(""))
        assertEquals("", NavigationUrlCodec.decode("   "))
    }
}
