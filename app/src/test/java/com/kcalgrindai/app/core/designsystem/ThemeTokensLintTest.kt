package com.kcalgrindai.app.core.designsystem

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ThemeTokensLintTest {

    @Test
    fun `no hardcoded Color(0x) hex literals outside theme designsystem package`() {
        val rootDir = File("").canonicalFile
        val srcDir = if (File(rootDir, "src/main/java").exists()) {
            File(rootDir, "src/main/java")
        } else if (File(rootDir, "app/src/main/java").exists()) {
            File(rootDir, "app/src/main/java")
        } else {
            File(rootDir, "LuminaAndroid/app/src/main/java")
        }

        assertTrue("Source directory not found: ${srcDir.absolutePath}", srcDir.exists())

        val hexColorRegex = Regex("""Color\(0x[0-9a-fA-F]+""")
        val violations = mutableListOf<String>()

        srcDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { file ->
                val normalizedPath = file.invariantSeparatorsPath
                !normalizedPath.contains("/core/designsystem/")
            }
            .forEach { file ->
                file.useLines { lines ->
                    lines.forEachIndexed { index, line ->
                        if (hexColorRegex.containsMatchIn(line)) {
                            val relPath = file.relativeTo(srcDir).invariantSeparatorsPath
                            violations.add("$relPath:${index + 1}: ${line.trim()}")
                        }
                    }
                }
            }

        assertTrue(
            "Found ${violations.size} hardcoded Color(0x...) hex literal(s) outside theme package:\n" +
                violations.joinToString("\n"),
            violations.isEmpty()
        )
    }
}
