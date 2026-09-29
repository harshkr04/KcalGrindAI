package com.kcalgrindai.app.feature.aicoach

import androidx.compose.ui.text.font.FontWeight
import com.kcalgrindai.app.feature.aicoach.components.MarkdownBlock
import com.kcalgrindai.app.feature.aicoach.components.parseInlineMarkdown
import com.kcalgrindai.app.feature.aicoach.components.parseMarkdownBlocks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiCoachMarkdownParserTest {

    @Test
    fun `parse sleep quality response from screenshot with numbered list and nested bullets`() {
        val sleepResponse = """
Improving sleep quality is crucial for overall health and well-being! Here are some evidence-based tips that can help:

1. **Stick to a Sleep Schedule:** Go to bed and wake up at the same time every day, even on weekends. This helps regulate your body's natural sleep-wake cycle.
2. **Create a Relaxing Bedtime Routine:** Wind down before bed with activities like reading, taking a warm bath, or listening to calming music. Avoid screens (phones, tablets, computers, TVs) for at least an hour before sleep.
3. **Optimize Your Sleep Environment:** Make sure your bedroom is dark, quiet, and cool. Consider blackout curtains, earplugs, or a white noise machine if needed.
4. **Watch Your Diet and Drink Habits:**
   * Avoid caffeine and alcohol, especially in the late afternoon and evening.
   * Don't eat large meals close to bedtime.
   * Stay hydrated throughout the day, but try to limit fluids right before bed to avoid waking up for bathroom breaks.
5. **Get Regular Exercise:** Physical activity during the day can promote better sleep at night. However, try to avoid intense workouts too close to bedtime.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(sleepResponse)
        assertTrue("Blocks should not be empty", blocks.isNotEmpty())

        // 1. Initial paragraph
        assertTrue("First block should be Paragraph", blocks[0] is MarkdownBlock.Paragraph)
        val p1 = blocks[0] as MarkdownBlock.Paragraph
        assertTrue(p1.text.startsWith("Improving sleep quality is crucial"))

        // 2. Numbered items 1, 2, 3
        assertTrue(blocks[1] is MarkdownBlock.NumberedItem)
        val num1 = blocks[1] as MarkdownBlock.NumberedItem
        assertEquals("1", num1.number)
        assertTrue(num1.text.startsWith("**Stick to a Sleep Schedule:**"))

        assertTrue(blocks[2] is MarkdownBlock.NumberedItem)
        assertEquals("2", (blocks[2] as MarkdownBlock.NumberedItem).number)

        assertTrue(blocks[3] is MarkdownBlock.NumberedItem)
        assertEquals("3", (blocks[3] as MarkdownBlock.NumberedItem).number)

        // 3. Numbered item 4
        assertTrue(blocks[4] is MarkdownBlock.NumberedItem)
        val num4 = blocks[4] as MarkdownBlock.NumberedItem
        assertEquals("4", num4.number)
        assertTrue(num4.text.contains("Watch Your Diet and Drink Habits"))

        // 4. Nested bullet items under 4
        assertTrue(blocks[5] is MarkdownBlock.BulletItem)
        val bullet1 = blocks[5] as MarkdownBlock.BulletItem
        assertEquals(1, bullet1.level)
        assertTrue(bullet1.text.startsWith("Avoid caffeine and alcohol"))

        assertTrue(blocks[6] is MarkdownBlock.BulletItem)
        assertEquals(1, (blocks[6] as MarkdownBlock.BulletItem).level)

        assertTrue(blocks[7] is MarkdownBlock.BulletItem)
        assertEquals(1, (blocks[7] as MarkdownBlock.BulletItem).level)

        // 5. Numbered item 5
        assertTrue(blocks[8] is MarkdownBlock.NumberedItem)
        assertEquals("5", (blocks[8] as MarkdownBlock.NumberedItem).number)
        assertEquals(0, (blocks[8] as MarkdownBlock.NumberedItem).level)

        // Verify that inline parsing removes all raw ** and creates actual bold spans
        val annotatedNum1 = parseInlineMarkdown(num1.text)
        assertFalse("Rendered text must NOT contain raw **", annotatedNum1.text.contains("**"))
        assertTrue("Rendered text must retain bold title", annotatedNum1.text.startsWith("Stick to a Sleep Schedule:"))
        val boldSpan = annotatedNum1.spanStyles.find { it.item.fontWeight == FontWeight.Bold }
        assertNotNull("Bold span must be present for title", boldSpan)
        assertEquals(0, boldSpan!!.start)
        assertEquals("Stick to a Sleep Schedule:".length, boldSpan.end)
    }

    @Test
    fun `parse breakfast response from screenshot with bullet list and bold items`() {
        val breakfastResponse = """
A healthy breakfast typically includes a good balance of protein, complex carbohydrates, and healthy fats to keep you full and energized. Some great options include:

* **Oatmeal with berries and nuts:** Provides fiber, antioxidants, and healthy fats.
* **Scrambled eggs with whole-wheat toast and avocado:** A classic for protein, healthy fats, and complex carbs.
* **Greek yogurt with fruit and a sprinkle of seeds:** High in protein and probiotics.
* **Smoothie with protein powder, spinach, and fruit:** A quick and easy way to get a lot of nutrients.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(breakfastResponse)
        assertEquals(5, blocks.size) // 1 paragraph + 4 bullet items

        assertTrue(blocks[0] is MarkdownBlock.Paragraph)
        assertTrue(blocks[1] is MarkdownBlock.BulletItem)
        assertTrue(blocks[2] is MarkdownBlock.BulletItem)
        assertTrue(blocks[3] is MarkdownBlock.BulletItem)
        assertTrue(blocks[4] is MarkdownBlock.BulletItem)

        val bulletItem1 = blocks[1] as MarkdownBlock.BulletItem
        val annotated = parseInlineMarkdown(bulletItem1.text)
        assertFalse("Must not have raw **", annotated.text.contains("**"))
        assertFalse("Must not have raw bullet symbol in text", annotated.text.startsWith("*"))
        assertTrue(annotated.text.startsWith("Oatmeal with berries and nuts: Provides fiber"))
        val boldSpan = annotated.spanStyles.find { it.item.fontWeight == FontWeight.Bold }
        assertNotNull(boldSpan)
        assertEquals("Oatmeal with berries and nuts:".length, boldSpan!!.end)
    }

    @Test
    fun `parse water guidance plain paragraph from screenshot`() {
        val waterResponse = """
While individual needs can vary based on activity level, climate, and health conditions, a general guideline is to aim for around 8 glasses (about 2 liters or 2000ml) of water per day. Listening to your body and drinking when you feel thirsty is also important!
        """.trimIndent()

        val blocks = parseMarkdownBlocks(waterResponse)
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.Paragraph)
        assertEquals(waterResponse, (blocks[0] as MarkdownBlock.Paragraph).text)
    }

    @Test
    fun `parse headings and typography hierarchy`() {
        val headingsMd = """
# Main Title
## Section Subtitle
### Detailed Subsection
Regular text following section
        """.trimIndent()

        val blocks = parseMarkdownBlocks(headingsMd)
        assertEquals(4, blocks.size)

        assertTrue(blocks[0] is MarkdownBlock.Heading)
        assertEquals(1, (blocks[0] as MarkdownBlock.Heading).level)
        assertEquals("Main Title", (blocks[0] as MarkdownBlock.Heading).text)

        assertTrue(blocks[1] is MarkdownBlock.Heading)
        assertEquals(2, (blocks[1] as MarkdownBlock.Heading).level)
        assertEquals("Section Subtitle", (blocks[1] as MarkdownBlock.Heading).text)

        assertTrue(blocks[2] is MarkdownBlock.Heading)
        assertEquals(3, (blocks[2] as MarkdownBlock.Heading).level)
        assertEquals("Detailed Subsection", (blocks[2] as MarkdownBlock.Heading).text)

        assertTrue(blocks[3] is MarkdownBlock.Paragraph)
    }

    @Test
    fun `parse multiple inline bold elements in one sentence`() {
        val input = "You should eat **lean proteins**, **complex carbohydrates**, and **healthy fats**."
        val annotated = parseInlineMarkdown(input)

        assertFalse(annotated.text.contains("**"))
        assertEquals("You should eat lean proteins, complex carbohydrates, and healthy fats.", annotated.text)

        val boldSpans = annotated.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
        assertEquals(3, boldSpans.size)

        val firstBold = annotated.text.substring(boldSpans[0].start, boldSpans[0].end)
        assertEquals("lean proteins", firstBold)

        val secondBold = annotated.text.substring(boldSpans[1].start, boldSpans[1].end)
        assertEquals("complex carbohydrates", secondBold)

        val thirdBold = annotated.text.substring(boldSpans[2].start, boldSpans[2].end)
        assertEquals("healthy fats", thirdBold)
    }

    @Test
    fun `parse code blocks and blockquotes`() {
        val md = """
> Listening to your hunger cues is crucial for long-term health.

```kotlin
val dailyCalories = 2200
```
        """.trimIndent()

        val blocks = parseMarkdownBlocks(md)
        assertEquals(2, blocks.size)

        assertTrue(blocks[0] is MarkdownBlock.Blockquote)
        assertEquals("Listening to your hunger cues is crucial for long-term health.", (blocks[0] as MarkdownBlock.Blockquote).text)

        assertTrue(blocks[1] is MarkdownBlock.CodeBlock)
        assertEquals("val dailyCalories = 2200", (blocks[1] as MarkdownBlock.CodeBlock).code)
    }
}
