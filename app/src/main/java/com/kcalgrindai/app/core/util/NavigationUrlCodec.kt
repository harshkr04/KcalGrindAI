package com.kcalgrindai.app.core.util

import com.kcalgrindai.app.core.navigation.KcalGrindRoute
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Single-responsibility URL encoder and decoder for Compose navigation arguments and quick suggestions.
 * Ensures parameters are encoded exactly once when constructing navigation URIs,
 * and decoded cleanly upon arrival without double-encoding artifacts (e.g. "snack%20suggestio").
 */
object NavigationUrlCodec {

    private val UTF8 = StandardCharsets.UTF_8.name()

    /**
     * URL-encodes a string parameter once using UTF-8.
     */
    fun encode(value: String): String {
        return try {
            URLEncoder.encode(value, UTF8)
        } catch (_: Exception) {
            value
        }
    }

    /**
     * URL-decodes a string parameter using UTF-8.
     * Safely recovers if text was inadvertently double-encoded (e.g. "%2520" -> "%20" -> " ")
     * or contains raw percent-encoded spaces ("%20").
     */
    fun decode(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return try {
            var decoded = URLDecoder.decode(value, UTF8)
            // Defensive recovery: if input was double-encoded or contains un-decoded %20
            if (decoded.contains("%20") || decoded.contains("%25")) {
                decoded = URLDecoder.decode(decoded, UTF8)
            }
            decoded
        } catch (_: Exception) {
            value
        }
    }

    /**
     * Constructs a navigation route to the AI Coach screen with an encoded initial prompt.
     */
    fun buildAiChatRoute(prompt: String): String {
        val encodedPrompt = encode(prompt.trim())
        return "${KcalGrindRoute.AiChat.route}?prompt=$encodedPrompt"
    }
}
