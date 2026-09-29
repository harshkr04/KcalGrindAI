package com.kcalgrindai.app.feature.aicoach.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Structural blocks extracted from AI Coach markdown responses.
 */
sealed interface MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class NumberedItem(val number: String, val text: String, val level: Int = 0) : MarkdownBlock
    data class BulletItem(val text: String, val level: Int = 0) : MarkdownBlock
    data class CodeBlock(val code: String, val language: String? = null) : MarkdownBlock
    data class Blockquote(val text: String) : MarkdownBlock
    object Divider : MarkdownBlock
}

/**
 * Parses raw markdown text into a sequence of structured [MarkdownBlock] items.
 */
fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    if (rawText.isBlank()) return emptyList()

    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var currentParagraph = StringBuilder()
    var inCodeBlock = false
    var codeLanguage: String? = null
    val currentCode = StringBuilder()

    fun flushParagraph() {
        if (currentParagraph.isNotBlank()) {
            blocks.add(MarkdownBlock.Paragraph(currentParagraph.toString().trim()))
            currentParagraph = StringBuilder()
        }
    }

    val headingRegex = Regex("""^(#{1,6})\s+(.+)$""")
    val numberedListRegex = Regex("""^(\s*)(\d+)[\.\)]\s+(.+)$""")
    val bulletListRegex = Regex("""^(\s*)[*\-•+]\s+(.+)$""")
    val dividerRegex = Regex("""^\s*([-*_]\s*){3,}$""")
    val blockquoteRegex = Regex("""^>\s*(.*)$""")

    for (line in lines) {
        val trimmedLine = line.trim()

        if (trimmedLine.startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(MarkdownBlock.CodeBlock(currentCode.toString().trimEnd(), codeLanguage))
                currentCode.clear()
                codeLanguage = null
                inCodeBlock = false
            } else {
                flushParagraph()
                codeLanguage = trimmedLine.removePrefix("```").trim().takeIf { it.isNotBlank() }
                inCodeBlock = true
            }
            continue
        }

        if (inCodeBlock) {
            if (currentCode.isNotEmpty()) currentCode.append("\n")
            currentCode.append(line)
            continue
        }

        if (trimmedLine.isEmpty()) {
            flushParagraph()
            continue
        }

        // Heading
        val headingMatch = headingRegex.matchEntire(trimmedLine)
        if (headingMatch != null) {
            flushParagraph()
            val level = headingMatch.groupValues[1].length
            val text = headingMatch.groupValues[2].trim()
            blocks.add(MarkdownBlock.Heading(level, text))
            continue
        }

        // Horizontal Divider
        if (dividerRegex.matches(trimmedLine)) {
            flushParagraph()
            blocks.add(MarkdownBlock.Divider)
            continue
        }

        // Blockquote
        val quoteMatch = blockquoteRegex.matchEntire(trimmedLine)
        if (quoteMatch != null) {
            flushParagraph()
            blocks.add(MarkdownBlock.Blockquote(quoteMatch.groupValues[1].trim()))
            continue
        }

        // Numbered List: e.g. "1. **Stick to a Sleep Schedule:** ..."
        val numberedMatch = numberedListRegex.matchEntire(line)
        if (numberedMatch != null) {
            flushParagraph()
            val indentSpaces = numberedMatch.groupValues[1].length
            val number = numberedMatch.groupValues[2]
            val content = numberedMatch.groupValues[3].trim()
            val level = (indentSpaces / 2).coerceIn(0, 3)
            blocks.add(MarkdownBlock.NumberedItem(number = number, text = content, level = level))
            continue
        }

        // Bullet List: e.g. "* **Oatmeal with berries...**" or indented "  * Avoid caffeine..."
        val bulletMatch = bulletListRegex.matchEntire(line)
        if (bulletMatch != null) {
            flushParagraph()
            val indentSpaces = bulletMatch.groupValues[1].length
            val content = bulletMatch.groupValues[2].trim()
            val level = (indentSpaces / 2).coerceIn(0, 3)
            blocks.add(MarkdownBlock.BulletItem(text = content, level = level))
            continue
        }

        // If line is indented and follows a list item without blank line, join to that item
        val lastBlock = blocks.lastOrNull()
        if (line.startsWith(" ") && (lastBlock is MarkdownBlock.NumberedItem || lastBlock is MarkdownBlock.BulletItem)) {
            if (lastBlock is MarkdownBlock.NumberedItem) {
                blocks[blocks.lastIndex] = lastBlock.copy(text = "${lastBlock.text} $trimmedLine")
            } else if (lastBlock is MarkdownBlock.BulletItem) {
                blocks[blocks.lastIndex] = lastBlock.copy(text = "${lastBlock.text} $trimmedLine")
            }
            continue
        }

        // Regular paragraph line
        if (currentParagraph.isNotEmpty()) {
            currentParagraph.append("\n").append(trimmedLine)
        } else {
            currentParagraph.append(trimmedLine)
        }
    }

    flushParagraph()
    if (inCodeBlock && currentCode.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(currentCode.toString().trimEnd(), codeLanguage))
    }

    return blocks
}

/**
 * Parses inline formatting (bold, italic, code, links) and converts them into an [AnnotatedString]
 * with actual typographic styles, stripping all raw Markdown syntax characters.
 */
fun parseInlineMarkdown(
    text: String,
    primaryColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified
): AnnotatedString = buildAnnotatedString {
    if (text.isEmpty()) return@buildAnnotatedString

    var currentIndex = 0

    // Token regex precedence:
    // 1 & 2: ***bold italic***
    // 3 & 4: **bold**
    // 5 & 6: __bold__
    // 7 & 8: `inline code`
    // 9 & 10: *italic*
    // 11 & 12: _italic_
    // 13, 14, 15: [link text](url)
    val tokenRegex = Regex(
        """(\*\*\*(.+?)\*\*\*)|(\*\*(.+?)\*\*)|(__(.+?)__)|(`(.+?)`)|(?<!\*)\*([^*\n]+?)\*(?!\*)|(?<!\w)_([^_\n]+?)_(?!\w)|(\[(.+?)\]\((https?://[^\s)]+)\))"""
    )

    val matches = tokenRegex.findAll(text)
    for (match in matches) {
        if (match.range.first > currentIndex) {
            val plainPart = text.substring(currentIndex, match.range.first)
                .replace("**", "")
                .replace("##", "")
            append(plainPart)
        }

        when {
            // Bold Italic: ***text***
            match.groups[2] != null -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                    append(match.groups[2]!!.value)
                }
            }
            // Bold: **text**
            match.groups[4] != null -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groups[4]!!.value)
                }
            }
            // Bold: __text__
            match.groups[6] != null -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groups[6]!!.value)
                }
            }
            // Code: `text`
            match.groups[8] != null -> {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = if (primaryColor != Color.Unspecified) primaryColor.copy(alpha = 0.12f) else Color.LightGray.copy(alpha = 0.25f)
                    )
                ) {
                    append(match.groups[8]!!.value)
                }
            }
            // Italic: *text*
            match.groups[10] != null -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(match.groups[10]!!.value)
                }
            }
            // Italic: _text_
            match.groups[12] != null -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(match.groups[12]!!.value)
                }
            }
            // Link: [text](url)
            match.groups[14] != null -> {
                withStyle(
                    SpanStyle(
                        color = if (primaryColor != Color.Unspecified) primaryColor else Color(0xFF10B981),
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(match.groups[14]!!.value)
                }
            }
        }
        currentIndex = match.range.last + 1
    }

    if (currentIndex < text.length) {
        val remaining = text.substring(currentIndex)
            .replace("**", "")
            .replace("##", "")
        append(remaining)
    }
}

/**
 * Main Composable to render AI Coach messages cleanly without raw Markdown artifacts,
 * crafted with Baseline UI standards (4pt/8pt grid, optical alignment, and Gestalt proximity).
 */
@Composable
fun AiCoachMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    isUser: Boolean = false
) {
    if (isUser) {
        // User messages are conversational text
        Text(
            text = remember(markdown) { parseInlineMarkdown(markdown, primaryColor, contentColor) },
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
                letterSpacing = 0.15.sp
            ),
            color = contentColor,
            modifier = modifier
        )
    } else {
        // Assistant responses contain structured lists, bold headers, paragraphs, etc.
        val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

        Column(modifier = modifier) {
            blocks.forEachIndexed { index, block ->
                val prevBlock = blocks.getOrNull(index - 1)

                // Gestalt proximity: deliberate spacing based on relationship between blocks
                if (index > 0) {
                    val topSpacing = when {
                        block is MarkdownBlock.Heading -> 14.dp
                        prevBlock is MarkdownBlock.Heading -> 4.dp
                        block is MarkdownBlock.NumberedItem && prevBlock is MarkdownBlock.NumberedItem -> 6.dp
                        block is MarkdownBlock.BulletItem && prevBlock is MarkdownBlock.NumberedItem -> 4.dp
                        block is MarkdownBlock.BulletItem && prevBlock is MarkdownBlock.BulletItem -> 5.dp
                        block is MarkdownBlock.Paragraph && (prevBlock is MarkdownBlock.NumberedItem || prevBlock is MarkdownBlock.BulletItem) -> 10.dp
                        prevBlock is MarkdownBlock.Paragraph && (block is MarkdownBlock.NumberedItem || block is MarkdownBlock.BulletItem) -> 8.dp
                        else -> 8.dp
                    }
                    Spacer(modifier = Modifier.height(topSpacing))
                }

                when (block) {
                    is MarkdownBlock.Heading -> {
                        val headingStyle = when (block.level) {
                            1 -> MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp,
                                letterSpacing = (-0.2).sp
                            )
                            2 -> MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp,
                                letterSpacing = (-0.1).sp
                            )
                            3 -> MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 20.sp
                            )
                            else -> MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 20.sp
                            )
                        }
                        Text(
                            text = remember(block.text) { parseInlineMarkdown(block.text, primaryColor, contentColor) },
                            style = headingStyle,
                            color = contentColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is MarkdownBlock.NumberedItem -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = (block.level * 16).dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Baseline optical alignment: number uses same lineHeight (22.sp) as text
                            val numWidth = if (block.number.length > 1) 28.dp else 22.dp
                            Text(
                                text = "${block.number}.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Start
                                ),
                                modifier = Modifier.width(numWidth)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = remember(block.text) { parseInlineMarkdown(block.text, primaryColor, contentColor) },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    letterSpacing = 0.15.sp
                                ),
                                color = contentColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    is MarkdownBlock.BulletItem -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = (if (block.level == 0) 2 else block.level * 16).dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Baseline geometric bullet: crisp circular indicator centered with first line cap-height
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp, start = 2.dp, end = 10.dp)
                                    .size(6.dp)
                                    .background(color = primaryColor, shape = CircleShape)
                            )
                            Text(
                                text = remember(block.text) { parseInlineMarkdown(block.text, primaryColor, contentColor) },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    letterSpacing = 0.15.sp
                                ),
                                color = contentColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    is MarkdownBlock.Paragraph -> {
                        Text(
                            text = remember(block.text) { parseInlineMarkdown(block.text, primaryColor, contentColor) },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp,
                                letterSpacing = 0.15.sp
                            ),
                            color = contentColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is MarkdownBlock.CodeBlock -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (block.language != null) {
                                    Text(
                                        text = block.language.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor
                                        ),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                Text(
                                    text = block.code,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 18.sp
                                    ),
                                    color = contentColor,
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                )
                            }
                        }
                    }

                    is MarkdownBlock.Blockquote -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .width(3.dp)
                                    .heightIn(min = 20.dp)
                                    .background(
                                        color = primaryColor,
                                        shape = RoundedCornerShape(1.5.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = remember(block.text) { parseInlineMarkdown(block.text, primaryColor, contentColor) },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 22.sp
                                ),
                                color = contentColor.copy(alpha = 0.92f)
                            )
                        }
                    }

                    is MarkdownBlock.Divider -> {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
