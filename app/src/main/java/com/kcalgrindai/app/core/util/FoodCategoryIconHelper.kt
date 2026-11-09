package com.kcalgrindai.app.core.util

import androidx.annotation.DrawableRes
import com.kcalgrindai.app.R

/**
 * Single-responsibility helper providing bundled vector category icons for food items.
 * Strictly offline-first: no external URL dependencies or scraped web images.
 * Uses lightweight local vector drawables (avoiding multi-megabyte icon library overhead).
 *
 * Mappings:
 * - Legumes, Pulses & Dal -> ic_cat_dal (soup bowl with steam)
 * - Grains, Breads & Rice -> ic_cat_grains (bakery bread & grains)
 * - Meat, Poultry & Fish -> ic_cat_meat (kebab dining skewer)
 * - Eggs -> ic_cat_eggs (egg silhouette)
 * - Dairy & Beverages -> ic_cat_dairy (cup / beverage container)
 * - Vegetables & Fruits -> ic_cat_veg (fresh produce eco leaf)
 * - Generic -> ic_cat_other (restaurant cutlery)
 */
object FoodCategoryIconHelper {

    enum class Category(val displayName: String, @DrawableRes val iconRes: Int) {
        DAL_PULSES("Dal & Legumes", R.drawable.ic_cat_dal),
        GRAINS_BREADS("Grains & Breads", R.drawable.ic_cat_grains),
        MEAT_POULTRY("Meat & Poultry", R.drawable.ic_cat_meat),
        EGGS("Eggs", R.drawable.ic_cat_eggs),
        DAIRY("Dairy & Drinks", R.drawable.ic_cat_dairy),
        VEGETABLES_FRUITS("Vegetables & Fruits", R.drawable.ic_cat_veg),
        OTHER("Other Meals", R.drawable.ic_cat_other)
    }

    fun getCategory(foodName: String?): Category {
        if (foodName.isNullOrBlank()) return Category.OTHER
        val lower = foodName.lowercase()

        return when {
            // Legumes & Pulses (Dal, Lentils, Chana, Rajma, Sambar, etc.)
            lower.contains("dal") || lower.contains("lentil") || lower.contains("chana") ||
            lower.contains("rajma") || lower.contains("sambar") || lower.contains("bean") ||
            lower.contains("chickpea") || lower.contains("moong") || lower.contains("urad") ->
                Category.DAL_PULSES

            // Grains, Breads & Rice
            lower.contains("roti") || lower.contains("chapati") || lower.contains("rice") ||
            lower.contains("bread") || lower.contains("paratha") || lower.contains("oat") ||
            lower.contains("poha") || lower.contains("idli") || lower.contains("dosa") ||
            lower.contains("khichdi") || lower.contains("biryani") || lower.contains("naan") ||
            lower.contains("puri") || lower.contains("pasta") || lower.contains("wheat") ->
                Category.GRAINS_BREADS

            // Dedicated Eggs
            lower.contains("egg") || lower.contains("omelet") || lower.contains("bhurji") && !lower.contains("paneer") ->
                Category.EGGS

            // Dairy & Beverages
            lower.contains("milk") || lower.contains("curd") || lower.contains("dahi") ||
            lower.contains("yogurt") || lower.contains("paneer") || lower.contains("cheese") ||
            lower.contains("lassi") || lower.contains("chaas") || lower.contains("buttermilk") ||
            lower.contains("shake") || lower.contains("smoothie") || lower.contains("tea") ||
            lower.contains("coffee") || lower.contains("whey") ->
                Category.DAIRY

            // Vegetables & Fruits
            lower.contains("sabzi") || lower.contains("salad") || lower.contains("palak") ||
            lower.contains("bhindi") || lower.contains("gobi") || lower.contains("aloo") ||
            lower.contains("apple") || lower.contains("banana") || lower.contains("orange") ||
            lower.contains("mango") || lower.contains("fruit") || lower.contains("spinach") ||
            lower.contains("tomato") || lower.contains("onion") || lower.contains("methi") ->
                Category.VEGETABLES_FRUITS

            // Meat, Poultry & Fish
            lower.contains("chicken") || lower.contains("mutton") || lower.contains("fish") ||
            lower.contains("prawn") || lower.contains("meat") || lower.contains("kebab") ||
            lower.contains("tikka") && (lower.contains("chicken") || lower.contains("meat")) ||
            lower.contains("salmon") || lower.contains("pork") || lower.contains("beef") ->
                Category.MEAT_POULTRY

            else -> Category.OTHER
        }
    }

    @DrawableRes
    fun getCategoryIconRes(foodName: String?): Int {
        return getCategory(foodName).iconRes
    }
}
