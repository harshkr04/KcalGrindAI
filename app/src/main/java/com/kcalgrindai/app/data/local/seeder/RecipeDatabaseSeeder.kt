package com.kcalgrindai.app.data.local.seeder

import com.kcalgrindai.app.data.local.dao.RecipeDao
import com.kcalgrindai.app.data.local.entity.RecipeEntity
import com.kcalgrindai.app.domain.model.RecipeIngredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeDatabaseSeeder @Inject constructor(
    private val recipeDao: RecipeDao
) {
    suspend fun seedRecipesIfEmpty() = withContext(Dispatchers.IO) {
        val entities = getPredefinedRecipes()
        recipeDao.insertRecipes(entities)
    }

    fun getPredefinedRecipes(): List<RecipeEntity> {
        return listOf(
            RecipeEntity(
                id = "recipe_bf_01",
                name = "Classic Avocado Toast & Poached Egg",
                description = "Crispy whole-wheat sourdough topped with crushed avocado, a perfectly poached egg, and red pepper flakes.",
                emoji = "🥑",
                mealType = "breakfast",
                prepTimeMinutes = 10,
                totalCalories = 322.0,
                proteinG = 11.8,
                carbsG = 26.4,
                fatG = 19.6,
                fiberG = 8.0,
                dietTags = listOf("High-Protein", "Vegetarian", "Quick <15min", "Mediterranean"),
                ingredients = listOf(
                RecipeIngredient(name = "Whole Wheat Sourdough Bread", amount = 1.0, unit = "slice (45g)", calories = 110.0, proteinG = 4.0, carbsG = 20.0, fatG = 1.5, fiberG = 3.0),
                RecipeIngredient(name = "Hass Avocado", amount = 0.5, unit = "medium (75g)", calories = 120.0, proteinG = 1.5, carbsG = 6.0, fatG = 11.0, fiberG = 5.0),
                RecipeIngredient(name = "Large Egg", amount = 1.0, unit = "large (50g)", calories = 72.0, proteinG = 6.3, carbsG = 0.4, fatG = 4.8, fiberG = 0.0),
                RecipeIngredient(name = "Extra Virgin Olive Oil", amount = 0.5, unit = "tsp (2.5ml)", calories = 20.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.3, fiberG = 0.0),
                RecipeIngredient(name = "Red Chili Flakes & Sea Salt", amount = 1.0, unit = "pinch", calories = 0.0, proteinG = 0.0, carbsG = 0.0, fatG = 0.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Toast the sourdough slice until golden brown and crisp.", 
                "Mash avocado with a squeeze of lemon juice, sea salt, and black pepper.", 
                "Poach the egg in gently simmering water with a drop of vinegar for 3 minutes.", 
                "Spread mashed avocado over toast, crown with the warm poached egg, drizzle olive oil and sprinkle chili flakes."
                ),
                popularityScore = 96.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_02",
                name = "Greek Yogurt Berry Protein Bowl",
                description = "Creamy non-fat Greek yogurt layered with wild blueberries, chia seeds, raw almonds, and honey.",
                emoji = "🫐",
                mealType = "breakfast",
                prepTimeMinutes = 5,
                totalCalories = 330.0,
                proteinG = 27.5,
                carbsG = 31.0,
                fatG = 11.3,
                fiberG = 7.0,
                dietTags = listOf("High-Protein", "Vegetarian", "Quick <15min", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Plain Non-Fat Greek Yogurt", amount = 200.0, unit = "g", calories = 130.0, proteinG = 22.0, carbsG = 7.0, fatG = 0.5, fiberG = 0.0),
                RecipeIngredient(name = "Fresh Blueberries", amount = 75.0, unit = "g", calories = 43.0, proteinG = 0.6, carbsG = 11.0, fatG = 0.2, fiberG = 1.8),
                RecipeIngredient(name = "Chia Seeds", amount = 10.0, unit = "g", calories = 49.0, proteinG = 1.7, carbsG = 4.2, fatG = 3.1, fiberG = 3.4),
                RecipeIngredient(name = "Crushed Almonds", amount = 15.0, unit = "g", calories = 87.0, proteinG = 3.2, carbsG = 3.1, fatG = 7.5, fiberG = 1.8),
                RecipeIngredient(name = "Pure Raw Honey", amount = 1.0, unit = "tsp (7g)", calories = 21.0, proteinG = 0.0, carbsG = 5.7, fatG = 0.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Spoon cold Greek yogurt into a chilled bowl.", 
                "Scatter fresh blueberries and crushed almonds over the top.", 
                "Sprinkle chia seeds evenly and finish with a light drizzle of honey."
                ),
                popularityScore = 94.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_03",
                name = "Spinach & Feta Egg White Omelet",
                description = "Fluffy egg whites whisked with fresh baby spinach, crumbled Greek feta, and diced tomatoes.",
                emoji = "🍳",
                mealType = "breakfast",
                prepTimeMinutes = 12,
                totalCalories = 201.0,
                proteinG = 26.2,
                carbsG = 5.9,
                fatG = 8.0,
                fiberG = 1.6,
                dietTags = listOf("High-Protein", "Low-Carb", "Vegetarian", "Quick <15min", "Mediterranean"),
                ingredients = listOf(
                RecipeIngredient(name = "Liquid Egg Whites", amount = 180.0, unit = "g", calories = 94.0, proteinG = 20.0, carbsG = 1.3, fatG = 0.3, fiberG = 0.0),
                RecipeIngredient(name = "Fresh Baby Spinach", amount = 50.0, unit = "g", calories = 12.0, proteinG = 1.5, carbsG = 1.8, fatG = 0.2, fiberG = 1.1),
                RecipeIngredient(name = "Crumbled Feta Cheese", amount = 30.0, unit = "g", calories = 79.0, proteinG = 4.3, carbsG = 1.2, fatG = 6.4, fiberG = 0.0),
                RecipeIngredient(name = "Diced Roma Tomato", amount = 40.0, unit = "g", calories = 7.0, proteinG = 0.4, carbsG = 1.6, fatG = 0.1, fiberG = 0.5),
                RecipeIngredient(name = "Extra Virgin Olive Oil Spray", amount = 1.0, unit = "g", calories = 9.0, proteinG = 0.0, carbsG = 0.0, fatG = 1.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Coat a non-stick skillet with olive oil spray over medium heat.", 
                "Sauté baby spinach and diced tomato for 90 seconds until spinach just wilts.", 
                "Pour in egg whites, tilting the skillet to cover the bottom evenly.", 
                "Cook until edges set, sprinkle feta cheese across one half, and fold the omelet over.", 
                "Slide onto a warm plate and season with freshly cracked black pepper."
                ),
                popularityScore = 91.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_04",
                name = "Peanut Butter Banana Overnight Oats",
                description = "Rolled oats steeped in almond milk, pure peanut butter, sliced banana, and hemp hearts.",
                emoji = "🥣",
                mealType = "breakfast",
                prepTimeMinutes = 8,
                totalCalories = 440.0,
                proteinG = 16.0,
                carbsG = 53.1,
                fatG = 19.6,
                fiberG = 9.1,
                dietTags = listOf("Vegetarian", "Vegan", "Budget-Friendly", "High-Protein"),
                ingredients = listOf(
                RecipeIngredient(name = "Rolled Old Fashioned Oats", amount = 50.0, unit = "g", calories = 190.0, proteinG = 6.5, carbsG = 34.0, fatG = 3.2, fiberG = 5.0),
                RecipeIngredient(name = "Unsweetened Almond Milk", amount = 150.0, unit = "ml", calories = 20.0, proteinG = 0.6, carbsG = 0.5, fatG = 1.5, fiberG = 0.5),
                RecipeIngredient(name = "All-Natural Peanut Butter", amount = 20.0, unit = "g", calories = 120.0, proteinG = 5.0, carbsG = 4.0, fatG = 10.0, fiberG = 1.6),
                RecipeIngredient(name = "Sliced Banana", amount = 60.0, unit = "g", calories = 53.0, proteinG = 0.7, carbsG = 13.7, fatG = 0.2, fiberG = 1.6),
                RecipeIngredient(name = "Hemp Hearts", amount = 10.0, unit = "g", calories = 57.0, proteinG = 3.2, carbsG = 0.9, fatG = 4.7, fiberG = 0.4)
                ),
                instructions = listOf(
                "Combine rolled oats, almond milk, and natural peanut butter in a mason jar.", 
                "Stir thoroughly until the peanut butter is smoothly incorporated.", 
                "Seal and refrigerate overnight (or at least 4 hours).", 
                "Top with freshly sliced banana and hemp hearts before serving cold."
                ),
                popularityScore = 95.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_05",
                name = "Masala Tofu Scramble",
                description = "Crumbled firm organic tofu seasoned with turmeric, cumin seeds, green chilies, and coriander.",
                emoji = "🥘",
                mealType = "breakfast",
                prepTimeMinutes = 14,
                totalCalories = 218.0,
                proteinG = 17.2,
                carbsG = 10.6,
                fatG = 13.4,
                fiberG = 3.7,
                dietTags = listOf("High-Protein", "Vegan", "Vegetarian", "Budget-Friendly", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Firm Tofu", amount = 175.0, unit = "g", calories = 145.0, proteinG = 16.0, carbsG = 3.5, fatG = 8.5, fiberG = 1.8),
                RecipeIngredient(name = "Red Onion", amount = 40.0, unit = "g", calories = 16.0, proteinG = 0.4, carbsG = 3.7, fatG = 0.1, fiberG = 0.7),
                RecipeIngredient(name = "Tomato", amount = 50.0, unit = "g", calories = 9.0, proteinG = 0.5, carbsG = 2.0, fatG = 0.1, fiberG = 0.6),
                RecipeIngredient(name = "Canola Oil", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Spices (Turmeric, Cumin, Garam Masala)", amount = 1.0, unit = "tsp", calories = 8.0, proteinG = 0.3, carbsG = 1.4, fatG = 0.2, fiberG = 0.6)
                ),
                instructions = listOf(
                "Heat canola oil in a pan, sizzle cumin seeds, then sauté diced red onion until translucent.", 
                "Add chopped tomato and ground turmeric, cooking until softened.", 
                "Crumble firm tofu directly into the pan and toss to coat evenly in warm golden spices.", 
                "Cook for 4-5 minutes, finish with fresh coriander leaves and sea salt."
                ),
                popularityScore = 88.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_06",
                name = "Smoked Salmon & Capers Everything Bagel Thin",
                description = "Toasted multi-grain bagel thin spread with light cream cheese, wild smoked salmon, and briny capers.",
                emoji = "🥯",
                mealType = "breakfast",
                prepTimeMinutes = 7,
                totalCalories = 236.0,
                proteinG = 20.4,
                carbsG = 26.7,
                fatG = 7.1,
                fiberG = 5.4,
                dietTags = listOf("High-Protein", "Mediterranean", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Multi-Grain Bagel Thin", amount = 1.0, unit = "bagel (46g)", calories = 110.0, proteinG = 5.0, carbsG = 24.0, fatG = 1.0, fiberG = 5.0),
                RecipeIngredient(name = "Light Whipped Cream Cheese", amount = 25.0, unit = "g", calories = 50.0, proteinG = 2.0, carbsG = 1.5, fatG = 4.0, fiberG = 0.0),
                RecipeIngredient(name = "Wild Sockeye Smoked Salmon", amount = 60.0, unit = "g", calories = 70.0, proteinG = 13.0, carbsG = 0.0, fatG = 2.0, fiberG = 0.0),
                RecipeIngredient(name = "Capers & Red Onion", amount = 15.0, unit = "g", calories = 6.0, proteinG = 0.4, carbsG = 1.2, fatG = 0.1, fiberG = 0.4)
                ),
                instructions = listOf(
                "Toast bagel thin halves until crunchy and golden.", 
                "Spread light whipped cream cheese across both halves.", 
                "Layer ribbons of wild smoked salmon on top.", 
                "Garnish with thinly shaved red onion rings, capers, and a pinch of fresh dill."
                ),
                popularityScore = 92.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_07",
                name = "Keto Bacon & Cheddar Egg Cups",
                description = "Baked mini frittatas with crisp smoked bacon lardons, aged sharp cheddar, and chives.",
                emoji = "🥓",
                mealType = "breakfast",
                prepTimeMinutes = 20,
                totalCalories = 361.0,
                proteinG = 27.1,
                carbsG = 1.6,
                fatG = 27.1,
                fiberG = 0.1,
                dietTags = listOf("High-Protein", "Low-Carb"),
                ingredients = listOf(
                RecipeIngredient(name = "Whole Eggs", amount = 2.0, unit = "large (100g)", calories = 144.0, proteinG = 12.6, carbsG = 0.8, fatG = 9.6, fiberG = 0.0),
                RecipeIngredient(name = "Smoked Bacon", amount = 25.0, unit = "g", calories = 115.0, proteinG = 8.0, carbsG = 0.3, fatG = 9.2, fiberG = 0.0),
                RecipeIngredient(name = "Sharp Cheddar Cheese", amount = 25.0, unit = "g", calories = 100.0, proteinG = 6.3, carbsG = 0.3, fatG = 8.3, fiberG = 0.0),
                RecipeIngredient(name = "Fresh Chopped Chives", amount = 5.0, unit = "g", calories = 2.0, proteinG = 0.2, carbsG = 0.2, fatG = 0.0, fiberG = 0.1)
                ),
                instructions = listOf(
                "Preheat oven to 375°F (190°C) and lightly grease two muffin tins.", 
                "Pan-crisp bacon until browned, then chop into bite-sized bits.", 
                "Whisk eggs with a splash of water, black pepper, and fresh chives.", 
                "Divide bacon and grated cheddar between muffin cups, pour in eggs, and bake for 14 minutes."
                ),
                popularityScore = 93.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_08",
                name = "Steel-Cut Oats with Caramelized Apple",
                description = "Slow-simmered Irish steel-cut oats topped with warm cinnamon sautéed Honeycrisp apples and walnuts.",
                emoji = "🍎",
                mealType = "breakfast",
                prepTimeMinutes = 25,
                totalCalories = 310.0,
                proteinG = 7.5,
                carbsG = 45.2,
                fatG = 12.4,
                fiberG = 7.4,
                dietTags = listOf("Vegetarian", "Vegan", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Steel-Cut Oats", amount = 40.0, unit = "g", calories = 150.0, proteinG = 5.0, carbsG = 27.0, fatG = 2.5, fiberG = 4.0),
                RecipeIngredient(name = "Honeycrisp Apple Diced", amount = 80.0, unit = "g", calories = 42.0, proteinG = 0.2, carbsG = 11.0, fatG = 0.1, fiberG = 1.9),
                RecipeIngredient(name = "Raw Walnut Halves", amount = 15.0, unit = "g", calories = 98.0, proteinG = 2.3, carbsG = 2.0, fatG = 9.8, fiberG = 1.0),
                RecipeIngredient(name = "Pure Maple Syrup", amount = 1.0, unit = "tsp (5ml)", calories = 17.0, proteinG = 0.0, carbsG = 4.4, fatG = 0.0, fiberG = 0.0),
                RecipeIngredient(name = "Ground Ceylon Cinnamon", amount = 1.0, unit = "g", calories = 3.0, proteinG = 0.0, carbsG = 0.8, fatG = 0.0, fiberG = 0.5)
                ),
                instructions = listOf(
                "Simmer steel-cut oats in 1 cup water with a pinch of salt for 20 minutes until creamy and nutty.", 
                "In a small skillet, warm apple dice with cinnamon and maple syrup for 4 minutes.", 
                "Pour hot porridge into a bowl, ladle spiced apples on top, and scatter toasted walnuts."
                ),
                popularityScore = 89.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_09",
                name = "High-Protein Cottage Cheese Pancakes",
                description = "Tender golden hotcakes made with blended cottage cheese, rolled oats, egg, and vanilla.",
                emoji = "🥞",
                mealType = "breakfast",
                prepTimeMinutes = 15,
                totalCalories = 303.0,
                proteinG = 23.8,
                carbsG = 29.5,
                fatG = 9.5,
                fiberG = 3.5,
                dietTags = listOf("High-Protein", "Vegetarian", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Low-Fat Cottage Cheese (2%)", amount = 110.0, unit = "g", calories = 90.0, proteinG = 13.0, carbsG = 4.5, fatG = 2.5, fiberG = 0.0),
                RecipeIngredient(name = "Rolled Oats Ground", amount = 35.0, unit = "g", calories = 133.0, proteinG = 4.5, carbsG = 23.8, fatG = 2.2, fiberG = 3.5),
                RecipeIngredient(name = "Whole Egg", amount = 1.0, unit = "large (50g)", calories = 72.0, proteinG = 6.3, carbsG = 0.4, fatG = 4.8, fiberG = 0.0),
                RecipeIngredient(name = "Pure Vanilla Extract", amount = 0.5, unit = "tsp (2.5ml)", calories = 6.0, proteinG = 0.0, carbsG = 0.3, fatG = 0.0, fiberG = 0.0),
                RecipeIngredient(name = "Baking Powder & Stevia", amount = 1.0, unit = "tsp", calories = 2.0, proteinG = 0.0, carbsG = 0.5, fatG = 0.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Blend cottage cheese, rolled oats, egg, vanilla extract, and baking powder in a blender until smooth.", 
                "Heat a non-stick griddle over medium-low heat.", 
                "Pour batter into 3 palm-sized circles and cook 3 minutes until bubbles burst.", 
                "Carefully flip and cook 2 more minutes until golden and puffed."
                ),
                popularityScore = 91.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_10",
                name = "Mediterranean Shakshuka with Feta",
                description = "Gently poached eggs in a rich tomato, bell pepper, garlic, and cumin sauce with sheep's milk feta.",
                emoji = "🍳",
                mealType = "breakfast",
                prepTimeMinutes = 20,
                totalCalories = 313.0,
                proteinG = 18.8,
                carbsG = 15.3,
                fatG = 19.8,
                fiberG = 3.8,
                dietTags = listOf("Vegetarian", "High-Protein", "Mediterranean", "Low-Carb"),
                ingredients = listOf(
                RecipeIngredient(name = "Whole Large Eggs", amount = 2.0, unit = "eggs (100g)", calories = 144.0, proteinG = 12.6, carbsG = 0.8, fatG = 9.6, fiberG = 0.0),
                RecipeIngredient(name = "Crushed Canned Tomatoes", amount = 150.0, unit = "g", calories = 48.0, proteinG = 2.1, carbsG = 10.5, fatG = 0.3, fiberG = 2.7),
                RecipeIngredient(name = "Red Bell Pepper Diced", amount = 50.0, unit = "g", calories = 15.0, proteinG = 0.5, carbsG = 3.0, fatG = 0.1, fiberG = 1.1),
                RecipeIngredient(name = "Olive Oil", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Feta Cheese Crumbles", amount = 25.0, unit = "g", calories = 66.0, proteinG = 3.6, carbsG = 1.0, fatG = 5.3, fiberG = 0.0)
                ),
                instructions = listOf(
                "Warm olive oil in a skillet, soften bell peppers and minced garlic for 3 minutes.", 
                "Pour in crushed tomatoes, ground cumin, paprika, salt, and simmer into a thick savory stew.", 
                "Make two small indentations and crack an egg into each well.", 
                "Cover and simmer on low for 6 minutes until egg whites solidify while yolks remain soft.", 
                "Crumble fresh feta and chopped parsley across the top."
                ),
                popularityScore = 95.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_11",
                name = "South Indian Steamed Idli & Sambar",
                description = "Fluffy fermented rice and lentil steamed cakes paired with a spicy, aromatic vegetable pigeon pea stew.",
                emoji = "🍛",
                mealType = "breakfast",
                prepTimeMinutes = 18,
                totalCalories = 286.0,
                proteinG = 10.2,
                carbsG = 49.3,
                fatG = 5.8,
                fiberG = 6.6,
                dietTags = listOf("Vegetarian", "Vegan", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Steamed Rice-Lentil Idlis", amount = 3.0, unit = "pieces (120g)", calories = 156.0, proteinG = 4.8, carbsG = 33.0, fatG = 0.6, fiberG = 2.4),
                RecipeIngredient(name = "Toor Dal Sambar Stew", amount = 150.0, unit = "ml", calories = 85.0, proteinG = 4.2, carbsG = 13.5, fatG = 1.8, fiberG = 3.2),
                RecipeIngredient(name = "Roasted Chana Coconut Chutney", amount = 20.0, unit = "g", calories = 45.0, proteinG = 1.2, carbsG = 2.8, fatG = 3.4, fiberG = 1.0)
                ),
                instructions = listOf(
                "Steam fermented batter in idli molds for 10 minutes until light and spongy.", 
                "Simmer cooked toor dal with tamarind extract, drumstick, sambar powder, and mustard seed tadka.", 
                "Serve steaming hot idlis immersed in a bowl of hot fragrant sambar with a spoonful of chutney."
                ),
                popularityScore = 90.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_12",
                name = "Vanilla Almond Chia Seed Pudding",
                description = "Gel-set chia seeds steeped in vanilla almond milk, layered with strawberry compote.",
                emoji = "🍓",
                mealType = "breakfast",
                prepTimeMinutes = 5,
                totalCalories = 165.0,
                proteinG = 5.4,
                carbsG = 15.7,
                fatG = 9.7,
                fiberG = 10.3,
                dietTags = listOf("Vegetarian", "Vegan", "Budget-Friendly", "Low-Carb"),
                ingredients = listOf(
                RecipeIngredient(name = "Black Chia Seeds", amount = 25.0, unit = "g", calories = 122.0, proteinG = 4.2, carbsG = 10.5, fatG = 7.7, fiberG = 8.5),
                RecipeIngredient(name = "Unsweetened Vanilla Almond Milk", amount = 160.0, unit = "ml", calories = 24.0, proteinG = 0.8, carbsG = 0.6, fatG = 1.8, fiberG = 0.6),
                RecipeIngredient(name = "Fresh Sliced Strawberries", amount = 60.0, unit = "g", calories = 19.0, proteinG = 0.4, carbsG = 4.6, fatG = 0.2, fiberG = 1.2),
                RecipeIngredient(name = "Liquid Stevia / Monk Fruit", amount = 2.0, unit = "drops", calories = 0.0, proteinG = 0.0, carbsG = 0.0, fatG = 0.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Whisk chia seeds and sweetener vigorously into chilled almond milk in a jar.", 
                "Let stand 5 minutes, whisk again to prevent settling, then chill in fridge for at least 3 hours.", 
                "Top with sweet freshly sliced strawberries before spooning."
                ),
                popularityScore = 87.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_bf_13",
                name = "Sautéed Mushroom & Goat Cheese Toast",
                description = "Earthly cremini mushrooms sautéed in thyme butter, spread over toasted seeded rye with tart goat cheese.",
                emoji = "🍄",
                mealType = "breakfast",
                prepTimeMinutes = 12,
                totalCalories = 209.0,
                proteinG = 10.5,
                carbsG = 22.7,
                fatG = 9.3,
                fiberG = 3.8,
                dietTags = listOf("Vegetarian", "Mediterranean", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Dark Rye Bread Slice", amount = 1.0, unit = "slice (40g)", calories = 95.0, proteinG = 3.2, carbsG = 19.0, fatG = 0.8, fiberG = 2.8),
                RecipeIngredient(name = "Cremini Mushrooms Sliced", amount = 90.0, unit = "g", calories = 20.0, proteinG = 2.8, carbsG = 3.0, fatG = 0.2, fiberG = 0.9),
                RecipeIngredient(name = "Soft Goat Cheese (Chèvre)", amount = 25.0, unit = "g", calories = 75.0, proteinG = 4.5, carbsG = 0.5, fatG = 6.3, fiberG = 0.0),
                RecipeIngredient(name = "Salted Butter", amount = 0.5, unit = "tsp (2.5g)", calories = 18.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.0, fiberG = 0.0),
                RecipeIngredient(name = "Fresh Thyme Leaves", amount = 1.0, unit = "sprig", calories = 1.0, proteinG = 0.0, carbsG = 0.2, fatG = 0.0, fiberG = 0.1)
                ),
                instructions = listOf(
                "Melt butter in a skillet over high heat; sauté cremini mushrooms and thyme until deeply caramelized.", 
                "Toast rye slice until firm.", 
                "Spread tangy goat cheese across hot toast.", 
                "Mound golden warm mushrooms over top and finish with cracked sea salt."
                ),
                popularityScore = 89.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_01",
                name = "Grilled Lemon Herb Chicken Salad",
                description = "Tender herb-marinated chicken breast slices over crisp romaine, cucumber, cherry tomatoes, and vinaigrette.",
                emoji = "🥗",
                mealType = "lunch",
                prepTimeMinutes = 15,
                totalCalories = 287.0,
                proteinG = 33.1,
                carbsG = 8.6,
                fatG = 13.3,
                fiberG = 3.4,
                dietTags = listOf("High-Protein", "Low-Carb", "Mediterranean", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Boneless Skinless Chicken Breast", amount = 150.0, unit = "g", calories = 165.0, proteinG = 31.0, carbsG = 0.0, fatG = 3.6, fiberG = 0.0),
                RecipeIngredient(name = "Crisp Romaine Hearts Chopped", amount = 100.0, unit = "g", calories = 17.0, proteinG = 1.2, carbsG = 3.3, fatG = 0.3, fiberG = 2.1),
                RecipeIngredient(name = "English Cucumber Slices", amount = 60.0, unit = "g", calories = 9.0, proteinG = 0.4, carbsG = 2.2, fatG = 0.1, fiberG = 0.5),
                RecipeIngredient(name = "Sweet Cherry Tomatoes", amount = 60.0, unit = "g", calories = 11.0, proteinG = 0.5, carbsG = 2.3, fatG = 0.1, fiberG = 0.7),
                RecipeIngredient(name = "Extra Virgin Olive Oil & Lemon Dressing", amount = 15.0, unit = "ml", calories = 85.0, proteinG = 0.0, carbsG = 0.8, fatG = 9.2, fiberG = 0.1)
                ),
                instructions = listOf(
                "Season chicken breast with oregano, garlic powder, salt, and grill 6 minutes per side until 165°F.", 
                "Let chicken rest 5 minutes, then slice against the grain.", 
                "Toss chopped romaine, cucumber, and halved cherry tomatoes in a large bowl.", 
                "Arrange sliced warm chicken on top and drizzle with lemon vinaigrette."
                ),
                popularityScore = 97.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_02",
                name = "Mediterranean Quinoa & Chickpea Bowl",
                description = "Nutty tri-color quinoa tossed with garbanzo beans, kalamata olives, diced cucumbers, and lemon tahini.",
                emoji = "🥙",
                mealType = "lunch",
                prepTimeMinutes = 12,
                totalCalories = 406.0,
                proteinG = 15.0,
                carbsG = 52.0,
                fatG = 16.2,
                fiberG = 10.7,
                dietTags = listOf("Vegetarian", "Vegan", "Mediterranean", "High-Protein", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Cooked Tri-Color Quinoa", amount = 120.0, unit = "g", calories = 144.0, proteinG = 5.3, carbsG = 26.0, fatG = 2.3, fiberG = 3.4),
                RecipeIngredient(name = "Canned Chickpeas Rinsed", amount = 90.0, unit = "g", calories = 125.0, proteinG = 6.5, carbsG = 20.0, fatG = 2.0, fiberG = 5.0),
                RecipeIngredient(name = "Kalamata Olives Pitted", amount = 20.0, unit = "g", calories = 40.0, proteinG = 0.3, carbsG = 1.0, fatG = 4.0, fiberG = 0.6),
                RecipeIngredient(name = "Persian Cucumbers Diced", amount = 50.0, unit = "g", calories = 8.0, proteinG = 0.4, carbsG = 1.8, fatG = 0.1, fiberG = 0.5),
                RecipeIngredient(name = "Creamy Tahini Dressing", amount = 15.0, unit = "g", calories = 89.0, proteinG = 2.5, carbsG = 3.2, fatG = 7.8, fiberG = 1.2)
                ),
                instructions = listOf(
                "Fluff chilled cooked quinoa with a fork.", 
                "Toss chickpeas, chopped kalamata olives, and diced cucumber in a bowl.", 
                "Whisk tahini with lemon juice, warm water, and garlic to create a velvety dressing.", 
                "Drizzle dressing generously across the grain bowl and serve."
                ),
                popularityScore = 93.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_03",
                name = "Wild Albacore Tuna Salad Wrap",
                description = "Pole-caught albacore tuna mixed with celery, dijon mustard, and light avocado mayo in a spinach tortilla.",
                emoji = "🌯",
                mealType = "lunch",
                prepTimeMinutes = 10,
                totalCalories = 295.0,
                proteinG = 31.6,
                carbsG = 20.2,
                fatG = 13.2,
                fiberG = 11.8,
                dietTags = listOf("High-Protein", "Quick <15min", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Albacore White Tuna in Water", amount = 110.0, unit = "g", calories = 120.0, proteinG = 26.0, carbsG = 0.0, fatG = 1.5, fiberG = 0.0),
                RecipeIngredient(name = "Avocado Oil Mayonnaise", amount = 15.0, unit = "g", calories = 90.0, proteinG = 0.0, carbsG = 0.0, fatG = 10.0, fiberG = 0.0),
                RecipeIngredient(name = "Finely Chopped Celery", amount = 30.0, unit = "g", calories = 5.0, proteinG = 0.2, carbsG = 1.0, fatG = 0.0, fiberG = 0.5),
                RecipeIngredient(name = "Spinach Herb Tortilla (High-Fiber)", amount = 1.0, unit = "wrap (45g)", calories = 70.0, proteinG = 5.0, carbsG = 18.0, fatG = 1.5, fiberG = 11.0),
                RecipeIngredient(name = "Dijon Mustard & Pickles", amount = 15.0, unit = "g", calories = 10.0, proteinG = 0.4, carbsG = 1.2, fatG = 0.2, fiberG = 0.3)
                ),
                instructions = listOf(
                "Drain tuna thoroughly and flake with a fork.", 
                "Fold in avocado mayo, dijon mustard, chopped celery, diced dill pickles, and black pepper.", 
                "Warm tortilla for 10 seconds to make it pliable.", 
                "Spread tuna mixture across center, roll tightly tucking the edges, and slice on a bias."
                ),
                popularityScore = 92.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_04",
                name = "Chana Masala with Jeera Brown Rice",
                description = "Authentic North Indian spiced chickpea curry simmered in onion-tomato gravy, served over fragrant cumin rice.",
                emoji = "🍛",
                mealType = "lunch",
                prepTimeMinutes = 20,
                totalCalories = 409.0,
                proteinG = 12.1,
                carbsG = 65.8,
                fatG = 10.2,
                fiberG = 9.4,
                dietTags = listOf("Vegetarian", "Vegan", "Budget-Friendly", "High-Protein"),
                ingredients = listOf(
                RecipeIngredient(name = "Cooked Chickpeas in Spiced Gravy", amount = 180.0, unit = "g", calories = 210.0, proteinG = 8.5, carbsG = 33.0, fatG = 4.5, fiberG = 6.8),
                RecipeIngredient(name = "Cooked Cumin Brown Rice", amount = 130.0, unit = "g", calories = 155.0, proteinG = 3.4, carbsG = 32.0, fatG = 1.2, fiberG = 2.3),
                RecipeIngredient(name = "Canola Oil & Whole Spices", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Chopped Cilantro & Ginger Matchsticks", amount = 10.0, unit = "g", calories = 4.0, proteinG = 0.2, carbsG = 0.8, fatG = 0.0, fiberG = 0.3)
                ),
                instructions = listOf(
                "Sauté onions, ginger, garlic, tomatoes, and chana masala blend in a skillet until oil separates.", 
                "Add boiled chickpeas with cooking liquor and simmer 12 minutes until gravy thickens.", 
                "Temper brown rice with ghee, cumin seeds, and a pinch of salt.", 
                "Serve hot curry spooned beside fragrant rice with cilantro garnish."
                ),
                popularityScore = 94.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_05",
                name = "Smoked Turkey & Avocado Lettuce Wraps",
                description = "Sliced lean smoked turkey breast, ripe avocado, vine tomato, and spicy mustard wrapped in butterhead leaves.",
                emoji = "🥬",
                mealType = "lunch",
                prepTimeMinutes = 8,
                totalCalories = 207.0,
                proteinG = 24.5,
                carbsG = 9.6,
                fatG = 8.7,
                fiberG = 4.6,
                dietTags = listOf("High-Protein", "Low-Carb", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Sliced Deli Smoked Turkey Breast", amount = 120.0, unit = "g", calories = 110.0, proteinG = 22.0, carbsG = 2.0, fatG = 1.5, fiberG = 0.0),
                RecipeIngredient(name = "Hass Avocado Slices", amount = 45.0, unit = "g", calories = 72.0, proteinG = 0.9, carbsG = 3.8, fatG = 6.6, fiberG = 3.0),
                RecipeIngredient(name = "Butterhead Boston Lettuce Cups", amount = 4.0, unit = "leaves (40g)", calories = 6.0, proteinG = 0.5, carbsG = 1.0, fatG = 0.1, fiberG = 0.5),
                RecipeIngredient(name = "Roma Tomato Slices", amount = 50.0, unit = "g", calories = 9.0, proteinG = 0.5, carbsG = 2.0, fatG = 0.1, fiberG = 0.6),
                RecipeIngredient(name = "Spicy Brown Mustard", amount = 1.0, unit = "tbsp (15g)", calories = 10.0, proteinG = 0.6, carbsG = 0.8, fatG = 0.4, fiberG = 0.5)
                ),
                instructions = listOf(
                "Wash and separate four crisp butterhead lettuce cups.", 
                "Distribute smoked turkey slices evenly across the cups.", 
                "Top each with creamy avocado slices, tomato, and dollops of spicy brown mustard.", 
                "Wrap like small tacos and enjoy crunchy freshness."
                ),
                popularityScore = 91.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_06",
                name = "Warm Lentil & Roasted Beet Salad",
                description = "French green Puy lentils tossed with roasted ruby beets, arugula, goat cheese, and balsamic reduction.",
                emoji = "🥗",
                mealType = "lunch",
                prepTimeMinutes = 15,
                totalCalories = 323.0,
                proteinG = 17.6,
                carbsG = 37.6,
                fatG = 11.9,
                fiberG = 10.5,
                dietTags = listOf("Vegetarian", "Mediterranean", "High-Protein", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Cooked French Green Lentils", amount = 130.0, unit = "g", calories = 150.0, proteinG = 11.0, carbsG = 24.5, fatG = 0.8, fiberG = 7.8),
                RecipeIngredient(name = "Roasted Beet Wedges", amount = 80.0, unit = "g", calories = 35.0, proteinG = 1.3, carbsG = 8.0, fatG = 0.1, fiberG = 2.2),
                RecipeIngredient(name = "Wild Baby Arugula", amount = 30.0, unit = "g", calories = 8.0, proteinG = 0.8, carbsG = 1.1, fatG = 0.2, fiberG = 0.5),
                RecipeIngredient(name = "Creamy Goat Cheese", amount = 25.0, unit = "g", calories = 75.0, proteinG = 4.5, carbsG = 0.5, fatG = 6.3, fiberG = 0.0),
                RecipeIngredient(name = "Aged Balsamic Vinaigrette", amount = 15.0, unit = "ml", calories = 55.0, proteinG = 0.0, carbsG = 3.5, fatG = 4.5, fiberG = 0.0)
                ),
                instructions = listOf(
                "Warm cooked Puy lentils slightly in a saucepan.", 
                "Toss arugula with half of the balsamic vinaigrette and plate.", 
                "Mound warm lentils in center, arrange sweet roasted beet wedges around sides.", 
                "Crumble goat cheese over the top and finish with remaining balsamic dressing."
                ),
                popularityScore = 88.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_07",
                name = "Shrimp & Soba Noodle Edamame Salad",
                description = "Plump chilled shrimp, Japanese buckwheat soba, steamed edamame, and sesame ginger scallion dressing.",
                emoji = "🍤",
                mealType = "lunch",
                prepTimeMinutes = 15,
                totalCalories = 343.0,
                proteinG = 36.7,
                carbsG = 28.9,
                fatG = 9.9,
                fiberG = 4.5,
                dietTags = listOf("High-Protein", "Mediterranean", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Cooked Tail-Off Shrimp", amount = 120.0, unit = "g", calories = 120.0, proteinG = 25.0, carbsG = 0.2, fatG = 1.4, fiberG = 0.0),
                RecipeIngredient(name = "Buckwheat Soba Noodles (Cooked)", amount = 100.0, unit = "g", calories = 100.0, proteinG = 5.0, carbsG = 21.0, fatG = 0.5, fiberG = 1.5),
                RecipeIngredient(name = "Shelled Steamed Edamame", amount = 50.0, unit = "g", calories = 60.0, proteinG = 6.0, carbsG = 4.5, fatG = 2.5, fiberG = 2.5),
                RecipeIngredient(name = "Sesame Ginger Dressing", amount = 15.0, unit = "ml", calories = 60.0, proteinG = 0.5, carbsG = 2.5, fatG = 5.5, fiberG = 0.2),
                RecipeIngredient(name = "Thinly Sliced Scallions", amount = 10.0, unit = "g", calories = 3.0, proteinG = 0.2, carbsG = 0.7, fatG = 0.0, fiberG = 0.3)
                ),
                instructions = listOf(
                "Boil soba noodles for 4 minutes, drain and rinse under ice-cold running water.", 
                "Combine soba, sweet edamame, and succulent chilled shrimp in a bowl.", 
                "Toss with ginger sesame dressing until evenly coated.", 
                "Garnish with sliced scallions and toasted sesame seeds."
                ),
                popularityScore = 93.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_08",
                name = "Grilled Paneer & Bell Pepper Tikka",
                description = "Marinated paneer cubes skewered with vibrant bell peppers and red onions, charred over high heat.",
                emoji = "🍢",
                mealType = "lunch",
                prepTimeMinutes = 20,
                totalCalories = 372.0,
                proteinG = 21.6,
                carbsG = 11.6,
                fatG = 26.2,
                fiberG = 1.8,
                dietTags = listOf("High-Protein", "Vegetarian", "Low-Carb"),
                ingredients = listOf(
                RecipeIngredient(name = "Paneer (Indian Cottage Cheese)", amount = 110.0, unit = "g", calories = 290.0, proteinG = 18.0, carbsG = 3.5, fatG = 22.0, fiberG = 0.0),
                RecipeIngredient(name = "Bell Peppers & Red Onion Chunks", amount = 80.0, unit = "g", calories = 25.0, proteinG = 1.0, carbsG = 5.5, fatG = 0.2, fiberG = 1.5),
                RecipeIngredient(name = "Yogurt Tikka Marinade", amount = 30.0, unit = "g", calories = 32.0, proteinG = 2.5, carbsG = 2.0, fatG = 1.5, fiberG = 0.2),
                RecipeIngredient(name = "Mustard Oil", amount = 0.5, unit = "tsp (2.5ml)", calories = 22.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.5, fiberG = 0.0),
                RecipeIngredient(name = "Chaat Masala & Lemon", amount = 1.0, unit = "dash", calories = 3.0, proteinG = 0.1, carbsG = 0.6, fatG = 0.0, fiberG = 0.1)
                ),
                instructions = listOf(
                "Cut paneer and peppers into uniform 1-inch squares.", 
                "Whisk Greek yogurt with kashmiri red chili, garam masala, kasuri methi, and mustard oil.", 
                "Coat paneer and veggies in marinade for 10 minutes.", 
                "Grill on a smoking hot grill pan until charred edges develop; sprinkle chaat masala and lemon juice."
                ),
                popularityScore = 95.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_09",
                name = "Classic Tuscan White Bean & Kale Soup",
                description = "Hearty Italian cannellini beans simmered in savory vegetable broth with Tuscan lacinato kale and rosemary.",
                emoji = "🍲",
                mealType = "lunch",
                prepTimeMinutes = 22,
                totalCalories = 277.0,
                proteinG = 14.9,
                carbsG = 42.5,
                fatG = 5.8,
                fiberG = 12.8,
                dietTags = listOf("Vegetarian", "Vegan", "Budget-Friendly", "Mediterranean"),
                ingredients = listOf(
                RecipeIngredient(name = "Cannellini Beans (Cooked)", amount = 160.0, unit = "g", calories = 180.0, proteinG = 12.0, carbsG = 32.0, fatG = 0.6, fiberG = 9.0),
                RecipeIngredient(name = "Tuscan Lacinato Kale (Ribs Removed)", amount = 60.0, unit = "g", calories = 20.0, proteinG = 1.8, carbsG = 3.0, fatG = 0.4, fiberG = 2.0),
                RecipeIngredient(name = "Mirepoix (Carrot, Onion, Celery)", amount = 60.0, unit = "g", calories = 22.0, proteinG = 0.6, carbsG = 5.0, fatG = 0.1, fiberG = 1.6),
                RecipeIngredient(name = "Extra Virgin Olive Oil", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Rich Vegetable Broth", amount = 250.0, unit = "ml", calories = 15.0, proteinG = 0.5, carbsG = 2.5, fatG = 0.2, fiberG = 0.2)
                ),
                instructions = listOf(
                "Warm olive oil in a Dutch oven and sauté diced carrots, celery, and onion until tender.", 
                "Pour in vegetable broth, minced rosemary, and cannellini beans; bring to a rolling simmer.", 
                "Mash a ladleful of beans against the pot wall to naturally thicken the broth.", 
                "Stir in ribbon-cut kale and cook 5 minutes until tender-crisp."
                ),
                popularityScore = 90.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_10",
                name = "Crispy Black Bean Quesadilla",
                description = "Spiced black beans and melted Monterey Jack in a griddled whole wheat tortilla, served with pico de gallo.",
                emoji = "🧀",
                mealType = "lunch",
                prepTimeMinutes = 12,
                totalCalories = 376.0,
                proteinG = 19.0,
                carbsG = 43.0,
                fatG = 14.6,
                fiberG = 10.3,
                dietTags = listOf("Vegetarian", "Budget-Friendly", "Quick <15min", "High-Protein"),
                ingredients = listOf(
                RecipeIngredient(name = "Whole Wheat Tortilla (8-inch)", amount = 1.0, unit = "tortilla (50g)", calories = 130.0, proteinG = 4.0, carbsG = 24.0, fatG = 2.5, fiberG = 4.0),
                RecipeIngredient(name = "Seasoned Black Beans", amount = 80.0, unit = "g", calories = 95.0, proteinG = 6.0, carbsG = 16.0, fatG = 0.5, fiberG = 5.5),
                RecipeIngredient(name = "Shredded Monterey Jack Cheese", amount = 35.0, unit = "g", calories = 130.0, proteinG = 8.5, carbsG = 0.5, fatG = 10.5, fiberG = 0.0),
                RecipeIngredient(name = "Fresh Pico de Gallo Salsa", amount = 40.0, unit = "g", calories = 12.0, proteinG = 0.5, carbsG = 2.5, fatG = 0.1, fiberG = 0.8),
                RecipeIngredient(name = "Olive Oil Spray", amount = 1.0, unit = "g", calories = 9.0, proteinG = 0.0, carbsG = 0.0, fatG = 1.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Scatter half the shredded cheese across one side of the tortilla.", 
                "Layer spiced black beans over cheese and top with remaining cheese.", 
                "Fold in half and griddle in a hot skillet misted with olive oil spray.", 
                "Cook 3 minutes per side until golden, crispy, and cheese is melted thoroughly; serve with fresh salsa."
                ),
                popularityScore = 92.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_11",
                name = "Sesame Ginger Chicken Rice Bowl",
                description = "Glazed diced chicken breast over jasmine rice with steamed broccoli florets and toasted sesame oil.",
                emoji = "🍗",
                mealType = "lunch",
                prepTimeMinutes = 18,
                totalCalories = 393.0,
                proteinG = 35.5,
                carbsG = 44.6,
                fatG = 7.6,
                fiberG = 2.9,
                dietTags = listOf("High-Protein", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Chicken Breast Diced", amount = 140.0, unit = "g", calories = 154.0, proteinG = 29.0, carbsG = 0.0, fatG = 3.4, fiberG = 0.0),
                RecipeIngredient(name = "Cooked Jasmine Rice", amount = 120.0, unit = "g", calories = 156.0, proteinG = 3.0, carbsG = 34.0, fatG = 0.4, fiberG = 0.6),
                RecipeIngredient(name = "Steamed Broccoli Florets", amount = 80.0, unit = "g", calories = 28.0, proteinG = 2.3, carbsG = 5.6, fatG = 0.3, fiberG = 2.1),
                RecipeIngredient(name = "Low-Sodium Soy Sesame Sauce", amount = 15.0, unit = "ml", calories = 35.0, proteinG = 1.2, carbsG = 5.0, fatG = 1.2, fiberG = 0.2),
                RecipeIngredient(name = "Toasted Sesame Oil", amount = 0.5, unit = "tsp (2.5ml)", calories = 20.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.3, fiberG = 0.0)
                ),
                instructions = listOf(
                "Sear chicken pieces in a hot skillet until lightly browned.", 
                "Pour in soy sesame sauce and simmer 2 minutes until chicken is glazed and cooked through.", 
                "Arrange fluffy jasmine rice and steamed tender broccoli in a wide bowl.", 
                "Spoon savory glazed chicken over rice and drizzle with toasted sesame oil."
                ),
                popularityScore = 96.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_12",
                name = "Spicy Thai Basil Tofu Stir-Fry",
                description = "Crispy golden tofu cubes tossed with fragrant Thai holy basil, birds eye chili, and garlic soy sauce.",
                emoji = "🌿",
                mealType = "lunch",
                prepTimeMinutes = 14,
                totalCalories = 238.0,
                proteinG = 18.8,
                carbsG = 13.0,
                fatG = 12.9,
                fiberG = 3.9,
                dietTags = listOf("Vegetarian", "Vegan", "High-Protein", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Pressed Extra Firm Tofu", amount = 160.0, unit = "g", calories = 145.0, proteinG = 16.0, carbsG = 3.0, fatG = 8.0, fiberG = 2.0),
                RecipeIngredient(name = "Thai Holy Basil & Chili", amount = 20.0, unit = "g", calories = 8.0, proteinG = 0.5, carbsG = 1.5, fatG = 0.1, fiberG = 0.5),
                RecipeIngredient(name = "Green Bell Pepper & Onion", amount = 60.0, unit = "g", calories = 20.0, proteinG = 0.8, carbsG = 4.5, fatG = 0.1, fiberG = 1.2),
                RecipeIngredient(name = "Sesame Oil", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Stir-Fry Garlic Tamari Glaze", amount = 15.0, unit = "ml", calories = 25.0, proteinG = 1.5, carbsG = 4.0, fatG = 0.2, fiberG = 0.2)
                ),
                instructions = listOf(
                "Cube pressed tofu and sear in smoking sesame oil until all sides are crunchy.", 
                "Add crushed garlic, birds eye chili, sliced onion, and bell peppers; flash fry 90 seconds.", 
                "Stir in tamari glaze, turn off heat, and fold in generous handfuls of Thai holy basil until wilted."
                ),
                popularityScore = 90.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_13",
                name = "Roast Beef & Horseradish Sourdough Panini",
                description = "Thinly shaved lean roast beef, sharp provolone, peppery watercress, and horseradish aioli pressed crisp.",
                emoji = "🥪",
                mealType = "lunch",
                prepTimeMinutes = 10,
                totalCalories = 390.0,
                proteinG = 36.2,
                carbsG = 33.2,
                fatG = 11.6,
                fiberG = 2.1,
                dietTags = listOf("High-Protein", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Artisan Sourdough Slices", amount = 2.0, unit = "slices (65g)", calories = 160.0, proteinG = 5.5, carbsG = 31.0, fatG = 1.0, fiberG = 2.0),
                RecipeIngredient(name = "Lean Deli Roast Beef Shaved", amount = 100.0, unit = "g", calories = 130.0, proteinG = 24.0, carbsG = 0.5, fatG = 3.2, fiberG = 0.0),
                RecipeIngredient(name = "Sharp Provolone Slice", amount = 1.0, unit = "slice (21g)", calories = 75.0, proteinG = 5.2, carbsG = 0.5, fatG = 6.0, fiberG = 0.0),
                RecipeIngredient(name = "Horseradish Greek Yogurt Spread", amount = 15.0, unit = "g", calories = 25.0, proteinG = 1.5, carbsG = 1.2, fatG = 1.4, fiberG = 0.1)
                ),
                instructions = listOf(
                "Spread tangy horseradish yogurt spread over inside surfaces of sourdough.", 
                "Layer shaved roast beef and a slice of sharp provolone.", 
                "Close sandwich and press in a panini press (or skillet with a heavy weight) for 4 minutes until crust is crunchy."
                ),
                popularityScore = 93.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_lu_14",
                name = "Southwest Chipotle Pinto Bean Bowl",
                description = "Stewed chipotle pinto beans, sweet roasted corn, avocado salsa, and brown basmati rice.",
                emoji = "🥑",
                mealType = "lunch",
                prepTimeMinutes = 15,
                totalCalories = 421.0,
                proteinG = 15.2,
                carbsG = 70.5,
                fatG = 9.7,
                fiberG = 14.4,
                dietTags = listOf("Vegetarian", "Vegan", "Budget-Friendly", "High-Protein"),
                ingredients = listOf(
                RecipeIngredient(name = "Cooked Pinto Beans", amount = 130.0, unit = "g", calories = 155.0, proteinG = 9.5, carbsG = 27.0, fatG = 0.8, fiberG = 8.5),
                RecipeIngredient(name = "Cooked Brown Basmati Rice", amount = 110.0, unit = "g", calories = 135.0, proteinG = 3.0, carbsG = 28.5, fatG = 1.0, fiberG = 2.0),
                RecipeIngredient(name = "Fire Roasted Sweet Corn", amount = 50.0, unit = "g", calories = 45.0, proteinG = 1.5, carbsG = 9.5, fatG = 0.5, fiberG = 1.2),
                RecipeIngredient(name = "Fresh Hass Avocado Diced", amount = 35.0, unit = "g", calories = 56.0, proteinG = 0.7, carbsG = 3.0, fatG = 5.2, fiberG = 2.3),
                RecipeIngredient(name = "Chipotle Lime Crema (Plant-Based)", amount = 15.0, unit = "g", calories = 30.0, proteinG = 0.5, carbsG = 2.5, fatG = 2.2, fiberG = 0.4)
                ),
                instructions = listOf(
                "Simmer pinto beans with chipotle pepper, cumin, and sea salt until aromatic.", 
                "Layer warm brown basmati rice and beans side-by-side in a wide bowl.", 
                "Scatter sweet roasted corn and diced creamy avocado across top.", 
                "Drizzle with zesty chipotle lime crema."
                ),
                popularityScore = 91.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_01",
                name = "Pan-Seared Atlantic Salmon & Asparagus",
                description = "Crispy skin Atlantic salmon fillet basted with garlic lemon butter alongside roasted tender asparagus.",
                emoji = "🐟",
                mealType = "dinner",
                prepTimeMinutes = 18,
                totalCalories = 385.0,
                proteinG = 36.7,
                carbsG = 5.7,
                fatG = 24.2,
                fiberG = 2.7,
                dietTags = listOf("High-Protein", "Low-Carb", "Mediterranean"),
                ingredients = listOf(
                RecipeIngredient(name = "Atlantic Salmon Fillet", amount = 170.0, unit = "g", calories = 280.0, proteinG = 34.0, carbsG = 0.0, fatG = 15.5, fiberG = 0.0),
                RecipeIngredient(name = "Fresh Green Asparagus Spears", amount = 120.0, unit = "g", calories = 24.0, proteinG = 2.6, carbsG = 4.5, fatG = 0.2, fiberG = 2.5),
                RecipeIngredient(name = "Grass-Fed Butter", amount = 1.0, unit = "tsp (5g)", calories = 36.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.0, fiberG = 0.0),
                RecipeIngredient(name = "Extra Virgin Olive Oil", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Lemon Juice & Fresh Dill", amount = 10.0, unit = "ml", calories = 5.0, proteinG = 0.1, carbsG = 1.2, fatG = 0.0, fiberG = 0.2)
                ),
                instructions = listOf(
                "Pat salmon skin completely dry and score lightly; season with sea salt and cracked pepper.", 
                "Sear salmon skin-side down in smoking olive oil for 4 minutes until intensely crispy.", 
                "Flip, toss asparagus spears into the skillet, and add butter and crushed garlic.", 
                "Baste salmon continuously with foaming butter for 3 minutes until medium rare.", 
                "Finish with fresh lemon juice and dill sprigs."
                ),
                popularityScore = 98.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_02",
                name = "Grass-Fed Beef Sirloin & Roasted Sweet Potato",
                description = "Seared top sirloin steak served with roasted sweet potato wedges and steamed French green beans.",
                emoji = "🥩",
                mealType = "dinner",
                prepTimeMinutes = 22,
                totalCalories = 433.0,
                proteinG = 45.8,
                carbsG = 32.8,
                fatG = 13.4,
                fiberG = 7.0,
                dietTags = listOf("High-Protein", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Beef Top Sirloin Steak (Lean)", amount = 170.0, unit = "g", calories = 250.0, proteinG = 42.0, carbsG = 0.0, fatG = 8.5, fiberG = 0.0),
                RecipeIngredient(name = "Sweet Potato Wedges", amount = 130.0, unit = "g", calories = 112.0, proteinG = 2.0, carbsG = 26.0, fatG = 0.2, fiberG = 4.0),
                RecipeIngredient(name = "Steamed Haricots Verts (Green Beans)", amount = 90.0, unit = "g", calories = 28.0, proteinG = 1.7, carbsG = 6.2, fatG = 0.2, fiberG = 2.8),
                RecipeIngredient(name = "Olive Oil", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Coarse Sea Salt & Rosemary", amount = 2.0, unit = "g", calories = 3.0, proteinG = 0.1, carbsG = 0.6, fatG = 0.0, fiberG = 0.2)
                ),
                instructions = listOf(
                "Toss sweet potato wedges with rosemary and roast at 400°F (200°C) for 20 minutes until caramelized.", 
                "Season steak generously with coarse sea salt and cracked black pepper.", 
                "Sear steak in a cast-iron skillet over high heat for 3-4 minutes per side for a juicy medium finish.", 
                "Rest steak 5 minutes before slicing against the grain; serve alongside roasted potatoes and steamed green beans."
                ),
                popularityScore = 96.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_03",
                name = "Mediterranean Baked Cod with Kalamata Tapenade",
                description = "Flaky wild cod fillets topped with a fragrant tapenade of kalamata olives, capers, garlic, and diced tomatoes.",
                emoji = "🐟",
                mealType = "dinner",
                prepTimeMinutes = 18,
                totalCalories = 260.0,
                proteinG = 33.4,
                carbsG = 5.5,
                fatG = 11.3,
                fiberG = 2.2,
                dietTags = listOf("High-Protein", "Low-Carb", "Mediterranean"),
                ingredients = listOf(
                RecipeIngredient(name = "Pacific Wild Cod Fillet", amount = 180.0, unit = "g", calories = 150.0, proteinG = 32.0, carbsG = 0.0, fatG = 1.5, fiberG = 0.0),
                RecipeIngredient(name = "Diced Roma Tomatoes", amount = 80.0, unit = "g", calories = 14.0, proteinG = 0.7, carbsG = 3.1, fatG = 0.2, fiberG = 1.0),
                RecipeIngredient(name = "Pitted Kalamata Olives Chopped", amount = 25.0, unit = "g", calories = 50.0, proteinG = 0.4, carbsG = 1.3, fatG = 5.0, fiberG = 0.8),
                RecipeIngredient(name = "Extra Virgin Olive Oil", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Capers & Fresh Oregano", amount = 10.0, unit = "g", calories = 6.0, proteinG = 0.3, carbsG = 1.1, fatG = 0.1, fiberG = 0.4)
                ),
                instructions = listOf(
                "Preheat oven to 380°F (195°C).", 
                "Place cod fillets in a lightly oiled baking dish and season with sea salt.", 
                "Mix chopped olives, capers, diced tomatoes, minced garlic, oregano, and olive oil.", 
                "Spoon tapenade evenly over the cod fillets.", 
                "Bake for 12-14 minutes until cod flakes effortlessly with a fork."
                ),
                popularityScore = 94.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_04",
                name = "Chicken Tikka Masala with Whole Wheat Naan",
                description = "Marinated tandoori chicken chunks simmered in a spiced tomato cream sauce, paired with warm whole wheat naan.",
                emoji = "🍛",
                mealType = "dinner",
                prepTimeMinutes = 25,
                totalCalories = 493.0,
                proteinG = 41.8,
                carbsG = 40.5,
                fatG = 18.0,
                fiberG = 5.5,
                dietTags = listOf("High-Protein"),
                ingredients = listOf(
                RecipeIngredient(name = "Chicken Breast Chunks", amount = 160.0, unit = "g", calories = 176.0, proteinG = 33.0, carbsG = 0.0, fatG = 3.8, fiberG = 0.0),
                RecipeIngredient(name = "Spiced Tomato Masala Sauce", amount = 120.0, unit = "g", calories = 90.0, proteinG = 2.5, carbsG = 9.5, fatG = 4.8, fiberG = 2.0),
                RecipeIngredient(name = "Heavy Cream / Greek Yogurt", amount = 20.0, unit = "ml", calories = 45.0, proteinG = 0.8, carbsG = 1.0, fatG = 4.4, fiberG = 0.0),
                RecipeIngredient(name = "Whole Wheat Tandoori Roti/Naan", amount = 1.0, unit = "piece (60g)", calories = 160.0, proteinG = 5.5, carbsG = 30.0, fatG = 2.5, fiberG = 3.5),
                RecipeIngredient(name = "Ghee for brushing", amount = 0.5, unit = "tsp (2.5g)", calories = 22.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.5, fiberG = 0.0)
                ),
                instructions = listOf(
                "Char marinated yogurt-spiced chicken under the broiler for 8 minutes.", 
                "Simmer onions, ginger, garlic, tomatoes, and ground spices into a rich masala sauce.", 
                "Stir in cream and drop roasted chicken into the bubbling sauce for 5 minutes.", 
                "Garnish with cilantro and serve warm with lightly brushed tandoori roti."
                ),
                popularityScore = 97.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_05",
                name = "Plant-Based Lentil Shepherd's Pie",
                description = "Rich brown lentil and vegetable stew crowned with golden-brown roasted cauliflower potato mash.",
                emoji = "🥧",
                mealType = "dinner",
                prepTimeMinutes = 35,
                totalCalories = 385.0,
                proteinG = 18.7,
                carbsG = 64.5,
                fatG = 7.1,
                fiberG = 14.5,
                dietTags = listOf("Vegetarian", "Vegan", "Budget-Friendly", "High-Protein"),
                ingredients = listOf(
                RecipeIngredient(name = "Cooked Brown Lentils", amount = 140.0, unit = "g", calories = 160.0, proteinG = 12.5, carbsG = 27.0, fatG = 0.6, fiberG = 8.0),
                RecipeIngredient(name = "Yukon Gold & Cauliflower Mash", amount = 150.0, unit = "g", calories = 110.0, proteinG = 3.0, carbsG = 22.0, fatG = 1.5, fiberG = 3.2),
                RecipeIngredient(name = "Diced Carrots, Peas, and Corn", amount = 70.0, unit = "g", calories = 55.0, proteinG = 2.2, carbsG = 11.5, fatG = 0.4, fiberG = 2.5),
                RecipeIngredient(name = "Olive Oil", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Tomato Paste & Herb Broth", amount = 30.0, unit = "ml", calories = 20.0, proteinG = 1.0, carbsG = 4.0, fatG = 0.1, fiberG = 0.8)
                ),
                instructions = listOf(
                "Sauté onions, carrots, and peas with tomato paste, thyme, and vegetable broth.", 
                "Add cooked brown lentils and simmer until saucy and deeply savory.", 
                "Transfer lentil filling to a baking dish.", 
                "Pipe or smooth cauliflower potato mash over top, scoring with a fork.", 
                "Broil at 425°F (220°C) for 10 minutes until peak ridges are browned and bubbling."
                ),
                popularityScore = 89.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_06",
                name = "Turkey Meatballs in San Marzano Marinara",
                description = "Lean baked ground turkey meatballs simmered in slow-cooked San Marzano marinara over spaghetti squash.",
                emoji = "🍝",
                mealType = "dinner",
                prepTimeMinutes = 25,
                totalCalories = 427.0,
                proteinG = 38.0,
                carbsG = 25.7,
                fatG = 19.4,
                fiberG = 4.6,
                dietTags = listOf("High-Protein", "Low-Carb", "Mediterranean"),
                ingredients = listOf(
                RecipeIngredient(name = "Lean Ground Turkey (93/7)", amount = 150.0, unit = "g", calories = 220.0, proteinG = 28.0, carbsG = 0.0, fatG = 11.5, fiberG = 0.0),
                RecipeIngredient(name = "San Marzano Marinara Sauce", amount = 120.0, unit = "g", calories = 65.0, proteinG = 1.8, carbsG = 9.2, fatG = 2.5, fiberG = 2.0),
                RecipeIngredient(name = "Roasted Spaghetti Squash Strands", amount = 150.0, unit = "g", calories = 42.0, proteinG = 1.0, carbsG = 10.0, fatG = 0.4, fiberG = 2.2),
                RecipeIngredient(name = "Grated Parmigiano-Reggiano", amount = 15.0, unit = "g", calories = 65.0, proteinG = 5.7, carbsG = 0.5, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Italian Herb Breadcrumbs & Egg", amount = 10.0, unit = "g", calories = 35.0, proteinG = 1.5, carbsG = 6.0, fatG = 0.5, fiberG = 0.4)
                ),
                instructions = listOf(
                "Combine ground turkey, herbs, garlic, breadcrumbs, and egg white; roll into 4 meatballs.", 
                "Bake meatballs at 400°F (200°C) for 15 minutes until browned.", 
                "Drop meatballs into simmering marinara sauce for 5 minutes.", 
                "Serve spooned over tender spaghetti squash strands with grated Parmigiano-Reggiano."
                ),
                popularityScore = 93.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_07",
                name = "Honey Soy Glazed Pork Tenderloin",
                description = "Succulent roasted pork tenderloin with a garlic honey ginger glaze, served with steamed snap peas.",
                emoji = "🥩",
                mealType = "dinner",
                prepTimeMinutes = 24,
                totalCalories = 340.0,
                proteinG = 39.8,
                carbsG = 19.9,
                fatG = 10.9,
                fiberG = 2.6,
                dietTags = listOf("High-Protein", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Pork Tenderloin Medallions", amount = 160.0, unit = "g", calories = 195.0, proteinG = 36.0, carbsG = 0.0, fatG = 4.8, fiberG = 0.0),
                RecipeIngredient(name = "Honey Garlic Tamari Glaze", amount = 20.0, unit = "ml", calories = 55.0, proteinG = 1.0, carbsG = 13.0, fatG = 0.1, fiberG = 0.1),
                RecipeIngredient(name = "Sugar Snap Peas", amount = 80.0, unit = "g", calories = 34.0, proteinG = 2.2, carbsG = 6.0, fatG = 0.2, fiberG = 2.1),
                RecipeIngredient(name = "Avocado Oil", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Sesame Seeds & Scallions", amount = 5.0, unit = "g", calories = 16.0, proteinG = 0.6, carbsG = 0.9, fatG = 1.3, fiberG = 0.4)
                ),
                instructions = listOf(
                "Sear seasoned pork tenderloin medallions in hot avocado oil for 2 minutes each side.", 
                "Pour honey garlic glaze over meat, spooning constantly as it caramelizes.", 
                "Transfer skillet to oven at 375°F for 8 minutes until internal temp reaches 145°F.", 
                "Rest meat 5 minutes before serving with blistered sweet snap peas."
                ),
                popularityScore = 91.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_08",
                name = "Palak Paneer with Garlic Roti",
                description = "Fresh cottage cheese cubes gently simmered in a vibrant spiced spinach purée, paired with garlic whole wheat roti.",
                emoji = "🍲",
                mealType = "dinner",
                prepTimeMinutes = 22,
                totalCalories = 524.0,
                proteinG = 27.0,
                carbsG = 40.5,
                fatG = 28.5,
                fiberG = 8.2,
                dietTags = listOf("Vegetarian", "High-Protein"),
                ingredients = listOf(
                RecipeIngredient(name = "Paneer Cubes", amount = 100.0, unit = "g", calories = 265.0, proteinG = 16.5, carbsG = 3.0, fatG = 20.0, fiberG = 0.0),
                RecipeIngredient(name = "Blanched Spinach Purée", amount = 180.0, unit = "g", calories = 45.0, proteinG = 5.0, carbsG = 6.5, fatG = 0.8, fiberG = 4.2),
                RecipeIngredient(name = "Onion-Tomato-Ginger Tadka", amount = 40.0, unit = "g", calories = 35.0, proteinG = 1.0, carbsG = 5.0, fatG = 1.2, fiberG = 1.0),
                RecipeIngredient(name = "Ghee", amount = 1.0, unit = "tsp (5g)", calories = 44.0, proteinG = 0.0, carbsG = 0.0, fatG = 5.0, fiberG = 0.0),
                RecipeIngredient(name = "Garlic Whole Wheat Roti", amount = 1.0, unit = "piece (50g)", calories = 135.0, proteinG = 4.5, carbsG = 26.0, fatG = 1.5, fiberG = 3.0)
                ),
                instructions = listOf(
                "Blanch baby spinach in boiling water for 90 seconds, plunge into ice bath, and blend until smooth.", 
                "Temper cumin, garlic, onions, and tomatoes in golden ghee.", 
                "Pour in emerald spinach purée with garam masala and a dash of cream; simmer 4 minutes.", 
                "Drop in paneer cubes and let them absorb the flavors without overcooking.", 
                "Serve hot with warm garlic-infused roti."
                ),
                popularityScore = 95.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_09",
                name = "Grilled Lemon Herb Lamb Chops",
                description = "Rosemary and garlic crusted Australian lamb loin chops grilled to medium pink with charred zucchini.",
                emoji = "🥩",
                mealType = "dinner",
                prepTimeMinutes = 20,
                totalCalories = 355.0,
                proteinG = 32.5,
                carbsG = 4.6,
                fatG = 22.4,
                fiberG = 1.5,
                dietTags = listOf("High-Protein", "Low-Carb", "Mediterranean"),
                ingredients = listOf(
                RecipeIngredient(name = "Lamb Loin Chops (Trimmed)", amount = 150.0, unit = "g", calories = 290.0, proteinG = 31.0, carbsG = 0.0, fatG = 17.5, fiberG = 0.0),
                RecipeIngredient(name = "Grilled Zucchini Ribbons", amount = 100.0, unit = "g", calories = 17.0, proteinG = 1.2, carbsG = 3.1, fatG = 0.3, fiberG = 1.0),
                RecipeIngredient(name = "Extra Virgin Olive Oil", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                RecipeIngredient(name = "Fresh Rosemary, Garlic, Lemon Zest", amount = 10.0, unit = "g", calories = 8.0, proteinG = 0.3, carbsG = 1.5, fatG = 0.1, fiberG = 0.5)
                ),
                instructions = listOf(
                "Rub lamb chops with minced rosemary, minced garlic, lemon zest, olive oil, and coarse salt.", 
                "Grill over direct high heat for 3-4 minutes per side until deeply caramelized with a pink center.", 
                "Char zucchini ribbons on the side of the grill.", 
                "Rest chops 5 minutes and squeeze fresh lemon juice over the meat before serving."
                ),
                popularityScore = 94.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_10",
                name = "Golden Turmeric Lentil Dal (Tarka Dal)",
                description = "Creamy yellow moong and masoor dal simmered with turmeric and tempered with sizzling garlic cumin ghee.",
                emoji = "🍲",
                mealType = "dinner",
                prepTimeMinutes = 22,
                totalCalories = 391.0,
                proteinG = 17.7,
                carbsG = 65.8,
                fatG = 6.4,
                fiberG = 7.6,
                dietTags = listOf("Vegetarian", "Budget-Friendly", "High-Protein"),
                ingredients = listOf(
                RecipeIngredient(name = "Split Yellow Moong & Red Masoor Dal", amount = 60.0, unit = "g dry", calories = 205.0, proteinG = 14.5, carbsG = 36.0, fatG = 0.8, fiberG = 6.5),
                RecipeIngredient(name = "Aromatic Ghee Tadka", amount = 1.0, unit = "tsp (5g)", calories = 44.0, proteinG = 0.0, carbsG = 0.0, fatG = 5.0, fiberG = 0.0),
                RecipeIngredient(name = "Cumin Seeds, Garlic, Dried Red Chilies", amount = 8.0, unit = "g", calories = 12.0, proteinG = 0.5, carbsG = 1.8, fatG = 0.3, fiberG = 0.6),
                RecipeIngredient(name = "Steamed Basmati Rice", amount = 100.0, unit = "g", calories = 130.0, proteinG = 2.7, carbsG = 28.0, fatG = 0.3, fiberG = 0.5)
                ),
                instructions = listOf(
                "Boil washed lentils with water, turmeric, ginger, and salt until completely soft and velvety.", 
                "In a small tempering ladle, heat ghee and crackle cumin seeds, sliced garlic, and whole dried red chili.", 
                "Pour sizzling tadka directly into the pot of dal with a dramatic hiss.", 
                "Cover immediately to trap the smoky aromatics; serve over steaming basmati rice."
                ),
                popularityScore = 92.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_11",
                name = "Chimichurri Flank Steak & Grilled Peppers",
                description = "Charbroiled lean flank steak sliced thin against the grain, topped with vibrant Argentine parsley chimichurri.",
                emoji = "🥩",
                mealType = "dinner",
                prepTimeMinutes = 20,
                totalCalories = 351.0,
                proteinG = 37.6,
                carbsG = 6.4,
                fatG = 19.2,
                fiberG = 2.0,
                dietTags = listOf("High-Protein", "Low-Carb"),
                ingredients = listOf(
                RecipeIngredient(name = "Beef Flank Steak", amount = 160.0, unit = "g", calories = 240.0, proteinG = 36.0, carbsG = 0.0, fatG = 10.0, fiberG = 0.0),
                RecipeIngredient(name = "Fresh Parsley Oregano Chimichurri", amount = 20.0, unit = "ml", calories = 85.0, proteinG = 0.5, carbsG = 1.0, fatG = 9.0, fiberG = 0.4),
                RecipeIngredient(name = "Grilled Poblano & Bell Peppers", amount = 80.0, unit = "g", calories = 24.0, proteinG = 1.0, carbsG = 5.0, fatG = 0.2, fiberG = 1.5),
                RecipeIngredient(name = "Sea Salt & Red Pepper Flakes", amount = 2.0, unit = "g", calories = 2.0, proteinG = 0.1, carbsG = 0.4, fatG = 0.0, fiberG = 0.1)
                ),
                instructions = listOf(
                "Season flank steak with coarse salt and grill over high heat for 4 minutes per side.", 
                "Grill pepper strips until blistered.", 
                "Rest flank steak 8 minutes, then slice across the muscle grain into thin ribbons.", 
                "Drizzle herbaceous garlic chimichurri generously over the sliced steak."
                ),
                popularityScore = 95.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_12",
                name = "Thai Coconut Green Curry with Tofu & Veggies",
                description = "Fragrant Thai green curry broth with coconut milk, bamboo shoots, eggplant, and seared organic tofu.",
                emoji = "🥥",
                mealType = "dinner",
                prepTimeMinutes = 20,
                totalCalories = 361.0,
                proteinG = 19.5,
                carbsG = 37.0,
                fatG = 15.8,
                fiberG = 4.5,
                dietTags = listOf("Vegetarian", "Vegan", "High-Protein"),
                ingredients = listOf(
                RecipeIngredient(name = "Firm Tofu Cubes Seared", amount = 150.0, unit = "g", calories = 135.0, proteinG = 15.0, carbsG = 3.0, fatG = 7.5, fiberG = 1.5),
                RecipeIngredient(name = "Light Coconut Milk", amount = 120.0, unit = "ml", calories = 75.0, proteinG = 1.0, carbsG = 2.5, fatG = 7.0, fiberG = 0.0),
                RecipeIngredient(name = "Thai Eggplant & Bamboo Shoots", amount = 70.0, unit = "g", calories = 25.0, proteinG = 1.0, carbsG = 5.0, fatG = 0.2, fiberG = 2.0),
                RecipeIngredient(name = "Green Curry Paste & Lime Leaf", amount = 15.0, unit = "g", calories = 22.0, proteinG = 0.5, carbsG = 3.5, fatG = 0.8, fiberG = 0.6),
                RecipeIngredient(name = "Jasmine Rice", amount = 80.0, unit = "g cooked", calories = 104.0, proteinG = 2.0, carbsG = 23.0, fatG = 0.3, fiberG = 0.4)
                ),
                instructions = listOf(
                "Fry green curry paste in 2 tablespoons of coconut cream until fragrant and oil separates.", 
                "Pour in remaining coconut milk, vegetable broth, and vegetables; simmer 6 minutes.", 
                "Drop seared tofu into curry, season with coconut sugar and lime leaves.", 
                "Serve hot with a side of jasmine rice."
                ),
                popularityScore = 93.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_13",
                name = "Balsamic Glazed Chicken Thighs & Brussels",
                description = "Crispy skin-on boneless chicken thighs roasted on a sheet pan with caramelized maple balsamic Brussels sprouts.",
                emoji = "🍗",
                mealType = "dinner",
                prepTimeMinutes = 25,
                totalCalories = 388.0,
                proteinG = 35.0,
                carbsG = 19.5,
                fatG = 19.4,
                fiberG = 4.2,
                dietTags = listOf("High-Protein", "Low-Carb"),
                ingredients = listOf(
                RecipeIngredient(name = "Boneless Skin-On Chicken Thigh", amount = 160.0, unit = "g", calories = 260.0, proteinG = 31.0, carbsG = 0.0, fatG = 14.5, fiberG = 0.0),
                RecipeIngredient(name = "Halved Brussels Sprouts", amount = 110.0, unit = "g", calories = 48.0, proteinG = 3.8, carbsG = 10.0, fatG = 0.4, fiberG = 4.2),
                RecipeIngredient(name = "Aged Balsamic Reduction", amount = 15.0, unit = "ml", calories = 40.0, proteinG = 0.2, carbsG = 9.5, fatG = 0.0, fiberG = 0.0),
                RecipeIngredient(name = "Olive Oil", amount = 1.0, unit = "tsp (5ml)", calories = 40.0, proteinG = 0.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0)
                ),
                instructions = listOf(
                "Toss halved Brussels sprouts with olive oil, salt, and spread on a baking sheet.", 
                "Nestle seasoned chicken thighs amongst sprouts skin-side up.", 
                "Roast at 425°F (220°C) for 22 minutes until chicken skin is blistered crisp and sprouts are deeply browned.", 
                "Drizzle sweet balsamic glaze over everything right before serving."
                ),
                popularityScore = 94.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_14",
                name = "Sesame Crusted Ahi Tuna Steak",
                description = "Sashimi-grade yellowfin tuna coated in black and white sesame seeds, flash seared rare with wasabi soy drizzle.",
                emoji = "🐟",
                mealType = "dinner",
                prepTimeMinutes = 10,
                totalCalories = 300.0,
                proteinG = 43.2,
                carbsG = 6.0,
                fatG = 10.9,
                fiberG = 2.0,
                dietTags = listOf("High-Protein", "Low-Carb", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Yellowfin Ahi Tuna Loin", amount = 160.0, unit = "g", calories = 175.0, proteinG = 39.0, carbsG = 0.0, fatG = 1.0, fiberG = 0.0),
                RecipeIngredient(name = "Mixed Sesame Seeds", amount = 15.0, unit = "g", calories = 85.0, proteinG = 2.7, carbsG = 3.5, fatG = 7.5, fiberG = 1.8),
                RecipeIngredient(name = "Toasted Sesame Oil", amount = 0.5, unit = "tsp (2.5ml)", calories = 20.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.3, fiberG = 0.0),
                RecipeIngredient(name = "Tamari Wasabi Dipping Sauce", amount = 15.0, unit = "ml", calories = 20.0, proteinG = 1.5, carbsG = 2.5, fatG = 0.1, fiberG = 0.2)
                ),
                instructions = listOf(
                "Press dry ahi tuna steak firmly into sesame seeds on all sides.", 
                "Heat sesame oil in a cast-iron skillet over high heat until smoking.", 
                "Sear tuna for exactly 45-60 seconds per side so the exterior is toasted while the interior stays cool ruby red.", 
                "Slice gently with a sharp knife and serve with wasabi tamari."
                ),
                popularityScore = 96.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_dn_15",
                name = "Greek Lemon Oregano Chicken & Potatoes",
                description = "Slow-roasted chicken breast and yellow potatoes infused with lemon juice, garlic, extra virgin olive oil, and oregano.",
                emoji = "🍋",
                mealType = "dinner",
                prepTimeMinutes = 28,
                totalCalories = 336.0,
                proteinG = 35.6,
                carbsG = 22.8,
                fatG = 10.7,
                fiberG = 2.3,
                dietTags = listOf("High-Protein", "Mediterranean"),
                ingredients = listOf(
                RecipeIngredient(name = "Chicken Breast Fillet", amount = 160.0, unit = "g", calories = 176.0, proteinG = 33.0, carbsG = 0.0, fatG = 3.8, fiberG = 0.0),
                RecipeIngredient(name = "Yukon Gold Potato Wedges", amount = 120.0, unit = "g", calories = 92.0, proteinG = 2.4, carbsG = 21.0, fatG = 0.1, fiberG = 2.0),
                RecipeIngredient(name = "Extra Virgin Olive Oil", amount = 1.5, unit = "tsp (7.5ml)", calories = 60.0, proteinG = 0.0, carbsG = 0.0, fatG = 6.8, fiberG = 0.0),
                RecipeIngredient(name = "Fresh Lemon Juice & Greek Oregano", amount = 15.0, unit = "ml", calories = 8.0, proteinG = 0.2, carbsG = 1.8, fatG = 0.0, fiberG = 0.3)
                ),
                instructions = listOf(
                "Toss chicken and par-boiled potato wedges in lemon juice, olive oil, minced garlic, oregano, and salt.", 
                "Roast in oven at 400°F (200°C) for 22 minutes until potatoes are crispy golden and chicken is juicy.", 
                "Spoon pan drippings over the top and enjoy."
                ),
                popularityScore = 95.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_sn_01",
                name = "No-Bake Peanut Butter Protein Energy Balls",
                description = "Chewy bite-sized power bites made with natural peanut butter, rolled oats, whey protein, and dark chocolate chips.",
                emoji = "🥜",
                mealType = "snack",
                prepTimeMinutes = 10,
                totalCalories = 324.0,
                proteinG = 21.2,
                carbsG = 25.3,
                fatG = 16.8,
                fiberG = 4.6,
                dietTags = listOf("High-Protein", "Vegetarian", "Quick <15min", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "All-Natural Peanut Butter", amount = 25.0, unit = "g", calories = 148.0, proteinG = 6.2, carbsG = 5.0, fatG = 12.5, fiberG = 2.0),
                RecipeIngredient(name = "Rolled Oats", amount = 20.0, unit = "g", calories = 76.0, proteinG = 2.6, carbsG = 13.6, fatG = 1.3, fiberG = 2.0),
                RecipeIngredient(name = "Vanilla Whey Protein Powder", amount = 15.0, unit = "g", calories = 60.0, proteinG = 12.0, carbsG = 1.2, fatG = 0.8, fiberG = 0.0),
                RecipeIngredient(name = "Mini Dark Chocolate Chips", amount = 8.0, unit = "g", calories = 40.0, proteinG = 0.4, carbsG = 5.5, fatG = 2.2, fiberG = 0.6)
                ),
                instructions = listOf(
                "Mix peanut butter, rolled oats, whey protein, and chocolate chips in a small bowl until a sticky dough forms.", 
                "Roll firmly into 2 bite-sized balls.", 
                "Chill in freezer for 10 minutes to set before eating."
                ),
                popularityScore = 97.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_sn_02",
                name = "Crispy Air-Fried Spiced Chickpeas",
                description = "Crunchy roasted chickpeas dusted with smoked paprika, sea salt, ground cumin, and a whisper of cayenne.",
                emoji = "🧆",
                mealType = "snack",
                prepTimeMinutes = 15,
                totalCalories = 154.0,
                proteinG = 7.4,
                carbsG = 23.4,
                fatG = 3.3,
                fiberG = 5.9,
                dietTags = listOf("Vegetarian", "Vegan", "High-Protein", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Cooked Chickpeas (Dried thoroughly)", amount = 100.0, unit = "g", calories = 140.0, proteinG = 7.2, carbsG = 22.5, fatG = 2.2, fiberG = 5.5),
                RecipeIngredient(name = "Olive Oil Spray", amount = 1.0, unit = "g", calories = 9.0, proteinG = 0.0, carbsG = 0.0, fatG = 1.0, fiberG = 0.0),
                RecipeIngredient(name = "Smoked Paprika, Cumin & Sea Salt", amount = 2.0, unit = "g", calories = 5.0, proteinG = 0.2, carbsG = 0.9, fatG = 0.1, fiberG = 0.4)
                ),
                instructions = listOf(
                "Pat cooked chickpeas between paper towels until completely dry.", 
                "Toss with olive oil spray and seasoning blend.", 
                "Air fry at 390°F (200°C) for 12-14 minutes, shaking the basket halfway until shatteringly crisp."
                ),
                popularityScore = 92.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_sn_03",
                name = "Avocado & Tajín Cucumber Rounds",
                description = "Crisp thick-sliced English cucumber rounds topped with mashed ripe avocado and Mexican chili-lime seasoning.",
                emoji = "🥒",
                mealType = "snack",
                prepTimeMinutes = 5,
                totalCalories = 84.0,
                proteinG = 1.6,
                carbsG = 8.2,
                fatG = 6.0,
                fiberG = 3.8,
                dietTags = listOf("Vegetarian", "Vegan", "Low-Carb", "Quick <15min", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "English Cucumber", amount = 120.0, unit = "g", calories = 18.0, proteinG = 0.8, carbsG = 4.4, fatG = 0.2, fiberG = 1.0),
                RecipeIngredient(name = "Hass Avocado", amount = 40.0, unit = "g", calories = 64.0, proteinG = 0.8, carbsG = 3.4, fatG = 5.8, fiberG = 2.7),
                RecipeIngredient(name = "Tajín Clásico Seasoning", amount = 1.0, unit = "g", calories = 2.0, proteinG = 0.0, carbsG = 0.4, fatG = 0.0, fiberG = 0.1)
                ),
                instructions = listOf(
                "Slice cucumber into thick 1/2-inch coins.", 
                "Mash ripe avocado with a touch of lime juice.", 
                "Dollop avocado onto cucumber discs and dust with Tajín."
                ),
                popularityScore = 90.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_sn_04",
                name = "Apple Slices with Cinnamon Almond Butter",
                description = "Crisp Honeycrisp apple wedges paired with creamy roasted almond butter and aromatic cinnamon.",
                emoji = "🍏",
                mealType = "snack",
                prepTimeMinutes = 5,
                totalCalories = 187.0,
                proteinG = 4.5,
                carbsG = 20.2,
                fatG = 11.4,
                fiberG = 5.2,
                dietTags = listOf("Vegetarian", "Vegan", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Fresh Honeycrisp Apple", amount = 120.0, unit = "g", calories = 62.0, proteinG = 0.3, carbsG = 16.0, fatG = 0.2, fiberG = 2.9),
                RecipeIngredient(name = "All-Natural Almond Butter", amount = 20.0, unit = "g", calories = 124.0, proteinG = 4.2, carbsG = 3.8, fatG = 11.2, fiberG = 2.1),
                RecipeIngredient(name = "Ground Cinnamon", amount = 0.5, unit = "g", calories = 1.0, proteinG = 0.0, carbsG = 0.4, fatG = 0.0, fiberG = 0.2)
                ),
                instructions = listOf(
                "Core and slice apple into wedges.", 
                "Stir cinnamon into natural almond butter.", 
                "Dip crunchy apple slices and enjoy."
                ),
                popularityScore = 94.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_sn_05",
                name = "Whipped Ricotta & Honey Crostini",
                description = "Part-skim ricotta whipped until silky, spread over toasted baguette with a drizzle of wildflower honey.",
                emoji = "🥖",
                mealType = "snack",
                prepTimeMinutes = 8,
                totalCalories = 163.0,
                proteinG = 8.0,
                carbsG = 24.0,
                fatG = 4.2,
                fiberG = 1.5,
                dietTags = listOf("Vegetarian", "Quick <15min", "Mediterranean"),
                ingredients = listOf(
                RecipeIngredient(name = "Whole Wheat French Baguette", amount = 30.0, unit = "g", calories = 80.0, proteinG = 3.0, carbsG = 16.0, fatG = 0.6, fiberG = 1.5),
                RecipeIngredient(name = "Part-Skim Ricotta Cheese", amount = 45.0, unit = "g", calories = 62.0, proteinG = 5.0, carbsG = 2.3, fatG = 3.6, fiberG = 0.0),
                RecipeIngredient(name = "Wildflower Honey", amount = 1.0, unit = "tsp (7g)", calories = 21.0, proteinG = 0.0, carbsG = 5.7, fatG = 0.0, fiberG = 0.0),
                RecipeIngredient(name = "Crushed Black Pepper & Sea Salt", amount = 1.0, unit = "pinch", calories = 0.0, proteinG = 0.0, carbsG = 0.0, fatG = 0.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Toast baguette slice until crisp.", 
                "Whip ricotta with salt and black pepper using a fork until fluffy.", 
                "Spread ricotta onto crostini and drizzle honey across top."
                ),
                popularityScore = 88.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_sn_06",
                name = "Keto Guacamole & Bell Pepper Dippers",
                description = "Chunky homemade guacamole with lime, cilantro, and tomato, scooped up with sweet red pepper spears.",
                emoji = "🥑",
                mealType = "snack",
                prepTimeMinutes = 8,
                totalCalories = 124.0,
                proteinG = 2.2,
                carbsG = 10.7,
                fatG = 9.0,
                fiberG = 6.0,
                dietTags = listOf("Vegetarian", "Vegan", "Low-Carb", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Ripe Avocado", amount = 60.0, unit = "g", calories = 96.0, proteinG = 1.2, carbsG = 5.1, fatG = 8.8, fiberG = 4.0),
                RecipeIngredient(name = "Red Bell Pepper Spears", amount = 80.0, unit = "g", calories = 24.0, proteinG = 0.8, carbsG = 4.8, fatG = 0.2, fiberG = 1.8),
                RecipeIngredient(name = "Lime Juice, Cilantro & Jalapeño", amount = 10.0, unit = "g", calories = 4.0, proteinG = 0.2, carbsG = 0.8, fatG = 0.0, fiberG = 0.2)
                ),
                instructions = listOf(
                "Mash avocado coarsely with fresh lime juice, sea salt, minced jalapeño, and chopped cilantro.", 
                "Cut red bell pepper into sturdy dipping spears.", 
                "Scoop guacamole using crisp pepper sticks."
                ),
                popularityScore = 93.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_sn_07",
                name = "Greek Yogurt Herb Dip & Veggie Sticks",
                description = "Tangy Greek yogurt infused with fresh dill, garlic, and chives, served with baby carrots and celery.",
                emoji = "🥕",
                mealType = "snack",
                prepTimeMinutes = 7,
                totalCalories = 98.0,
                proteinG = 12.1,
                carbsG = 10.9,
                fatG = 0.4,
                fiberG = 2.6,
                dietTags = listOf("High-Protein", "Vegetarian", "Low-Carb", "Quick <15min", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Plain Non-Fat Greek Yogurt", amount = 100.0, unit = "g", calories = 65.0, proteinG = 11.0, carbsG = 3.5, fatG = 0.2, fiberG = 0.0),
                RecipeIngredient(name = "Baby Carrots & Celery Sticks", amount = 90.0, unit = "g", calories = 30.0, proteinG = 0.9, carbsG = 6.8, fatG = 0.2, fiberG = 2.4),
                RecipeIngredient(name = "Fresh Dill, Garlic Powder & Lemon", amount = 5.0, unit = "g", calories = 3.0, proteinG = 0.2, carbsG = 0.6, fatG = 0.0, fiberG = 0.2)
                ),
                instructions = listOf(
                "Stir fresh dill, garlic powder, onion powder, lemon juice, and salt into Greek yogurt.", 
                "Serve cold dip alongside crunchy baby carrots and crisp celery sticks."
                ),
                popularityScore = 91.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_sn_08",
                name = "Hard-Boiled Eggs with Everything Bagel Seasoning",
                description = "Two perfectly boiled eggs sliced in half and dusted with toasted garlic, onion flakes, and sesame seeds.",
                emoji = "🥚",
                mealType = "snack",
                prepTimeMinutes = 10,
                totalCalories = 156.0,
                proteinG = 13.0,
                carbsG = 1.6,
                fatG = 10.5,
                fiberG = 0.3,
                dietTags = listOf("High-Protein", "Low-Carb", "Vegetarian", "Quick <15min", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Large Eggs Hard-Boiled", amount = 2.0, unit = "eggs (100g)", calories = 144.0, proteinG = 12.6, carbsG = 0.8, fatG = 9.6, fiberG = 0.0),
                RecipeIngredient(name = "Everything Bagel Seasoning", amount = 1.0, unit = "tsp (3g)", calories = 12.0, proteinG = 0.4, carbsG = 0.8, fatG = 0.9, fiberG = 0.3)
                ),
                instructions = listOf(
                "Peel cooled hard-boiled eggs and slice lengthwise.", 
                "Generously dust yolks with everything bagel seasoning and cracked black pepper."
                ),
                popularityScore = 95.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_sn_09",
                name = "Roasted Salted Edamame Pods",
                description = "Steamed whole soybean pods tossed in toasted sesame oil and flaky Maldon sea salt.",
                emoji = "🫛",
                mealType = "snack",
                prepTimeMinutes = 8,
                totalCalories = 110.0,
                proteinG = 8.5,
                carbsG = 6.0,
                fatG = 5.8,
                fiberG = 4.0,
                dietTags = listOf("High-Protein", "Vegetarian", "Vegan", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Edamame Pods in Shell", amount = 140.0, unit = "g (yields 70g beans)", calories = 90.0, proteinG = 8.5, carbsG = 6.0, fatG = 3.5, fiberG = 4.0),
                RecipeIngredient(name = "Toasted Sesame Oil", amount = 0.5, unit = "tsp (2.5ml)", calories = 20.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.3, fiberG = 0.0),
                RecipeIngredient(name = "Flaky Sea Salt", amount = 1.0, unit = "pinch", calories = 0.0, proteinG = 0.0, carbsG = 0.0, fatG = 0.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Steam edamame pods for 5 minutes until hot and bright green.", 
                "Toss in a bowl with sesame oil and coarse sea salt.", 
                "Pop tender beans directly into mouth from pod."
                ),
                popularityScore = 91.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_sn_10",
                name = "Turmeric Spiced Roasted Cashews",
                description = "Whole cashews pan-toasted with ghee, crushed black pepper, ground turmeric, and rock salt.",
                emoji = "🥜",
                mealType = "snack",
                prepTimeMinutes = 8,
                totalCalories = 166.0,
                proteinG = 4.6,
                carbsG = 7.8,
                fatG = 13.7,
                fiberG = 0.9,
                dietTags = listOf("Vegetarian", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Whole Raw Cashews", amount = 25.0, unit = "g", calories = 142.0, proteinG = 4.5, carbsG = 7.5, fatG = 11.2, fiberG = 0.8),
                RecipeIngredient(name = "Pure Desi Ghee", amount = 0.5, unit = "tsp (2.5g)", calories = 22.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.5, fiberG = 0.0),
                RecipeIngredient(name = "Turmeric, Black Pepper & Salt", amount = 1.0, unit = "pinch", calories = 2.0, proteinG = 0.1, carbsG = 0.3, fatG = 0.0, fiberG = 0.1)
                ),
                instructions = listOf(
                "Warm ghee in a small pan on low heat.", 
                "Add cashews and roast stirring constantly for 4 minutes until golden and nutty.", 
                "Toss with turmeric, black pepper, and salt; let cool to crisp up."
                ),
                popularityScore = 89.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_de_01",
                name = "Dark Chocolate Avocado Silk Mousse",
                description = "Velvety plant-based chocolate pudding made from blended ripe avocado, raw cacao powder, and pure maple syrup.",
                emoji = "🍫",
                mealType = "dessert",
                prepTimeMinutes = 10,
                totalCalories = 201.0,
                proteinG = 4.5,
                carbsG = 27.3,
                fatG = 12.0,
                fiberG = 9.5,
                dietTags = listOf("Vegetarian", "Vegan", "Quick <15min"),
                ingredients = listOf(
                RecipeIngredient(name = "Ripe Avocado", amount = 65.0, unit = "g", calories = 104.0, proteinG = 1.3, carbsG = 5.5, fatG = 9.6, fiberG = 4.4),
                RecipeIngredient(name = "Raw Unsweetened Cacao Powder", amount = 15.0, unit = "g", calories = 34.0, proteinG = 3.0, carbsG = 8.0, fatG = 2.0, fiberG = 5.0),
                RecipeIngredient(name = "Pure Maple Syrup", amount = 1.0, unit = "tbsp (15ml)", calories = 52.0, proteinG = 0.0, carbsG = 13.4, fatG = 0.0, fiberG = 0.0),
                RecipeIngredient(name = "Unsweetened Almond Milk", amount = 30.0, unit = "ml", calories = 5.0, proteinG = 0.2, carbsG = 0.1, fatG = 0.4, fiberG = 0.1),
                RecipeIngredient(name = "Pure Vanilla Extract", amount = 0.5, unit = "tsp (2.5ml)", calories = 6.0, proteinG = 0.0, carbsG = 0.3, fatG = 0.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Add avocado flesh, raw cacao, maple syrup, almond milk, and vanilla to a high-speed blender.", 
                "Blend until completely smooth and glossy without any green flecks.", 
                "Chill in a ramekin for 15 minutes and top with a fresh raspberry."
                ),
                popularityScore = 95.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_de_02",
                name = "Warm Berry Crumble with Rolled Oats",
                description = "Simmered raspberries and blackberries crowned with a golden cinnamon oat and almond flour crisp.",
                emoji = "🫐",
                mealType = "dessert",
                prepTimeMinutes = 20,
                totalCalories = 211.0,
                proteinG = 4.6,
                carbsG = 34.2,
                fatG = 7.1,
                fiberG = 9.0,
                dietTags = listOf("Vegetarian", "Vegan", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Mixed Blackberries & Raspberries", amount = 100.0, unit = "g", calories = 52.0, proteinG = 1.4, carbsG = 12.0, fatG = 0.5, fiberG = 6.0),
                RecipeIngredient(name = "Rolled Oats", amount = 25.0, unit = "g", calories = 95.0, proteinG = 3.2, carbsG = 17.0, fatG = 1.6, fiberG = 2.5),
                RecipeIngredient(name = "Coconut Oil Melted", amount = 1.0, unit = "tsp (5g)", calories = 44.0, proteinG = 0.0, carbsG = 0.0, fatG = 5.0, fiberG = 0.0),
                RecipeIngredient(name = "Pure Maple Syrup", amount = 1.0, unit = "tsp (5ml)", calories = 17.0, proteinG = 0.0, carbsG = 4.4, fatG = 0.0, fiberG = 0.0),
                RecipeIngredient(name = "Cinnamon & Pinch of Salt", amount = 1.0, unit = "g", calories = 3.0, proteinG = 0.0, carbsG = 0.8, fatG = 0.0, fiberG = 0.5)
                ),
                instructions = listOf(
                "Toss berries in a mini baking dish.", 
                "In a small bowl, mix rolled oats, melted coconut oil, maple syrup, and cinnamon until crumbly.", 
                "Scatter oat topping over berries.", 
                "Bake at 375°F (190°C) for 15 minutes until fruit bubbles and oats turn fragrant and golden brown."
                ),
                popularityScore = 92.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_de_03",
                name = "High-Protein Strawberry Greek Fro-Yo",
                description = "Instant soft-serve made by blending frozen strawberries with thick non-fat Greek yogurt and stevia.",
                emoji = "🍦",
                mealType = "dessert",
                prepTimeMinutes = 5,
                totalCalories = 118.0,
                proteinG = 14.0,
                carbsG = 13.6,
                fatG = 0.7,
                fiberG = 2.4,
                dietTags = listOf("High-Protein", "Vegetarian", "Quick <15min", "Budget-Friendly", "Low-Carb"),
                ingredients = listOf(
                RecipeIngredient(name = "Frozen Whole Strawberries", amount = 120.0, unit = "g", calories = 38.0, proteinG = 0.8, carbsG = 9.2, fatG = 0.4, fiberG = 2.4),
                RecipeIngredient(name = "Plain Non-Fat Greek Yogurt", amount = 120.0, unit = "g", calories = 78.0, proteinG = 13.2, carbsG = 4.2, fatG = 0.3, fiberG = 0.0),
                RecipeIngredient(name = "Vanilla Extract & Stevia", amount = 2.0, unit = "drops", calories = 2.0, proteinG = 0.0, carbsG = 0.2, fatG = 0.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Add frozen strawberries, chilled Greek yogurt, and vanilla stevia to a food processor.", 
                "Pulse until broken down, then blend on high for 60 seconds until thick and velvety soft-serve.", 
                "Spoon into a bowl and eat immediately."
                ),
                popularityScore = 96.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_de_04",
                name = "Baked Cinnamon Honey Peach",
                description = "Halved ripe peach roasted with sweet wildflower honey, Saigon cinnamon, and crushed toasted pecans.",
                emoji = "🍑",
                mealType = "dessert",
                prepTimeMinutes = 18,
                totalCalories = 142.0,
                proteinG = 2.1,
                carbsG = 19.9,
                fatG = 7.5,
                fiberG = 3.2,
                dietTags = listOf("Vegetarian", "Mediterranean"),
                ingredients = listOf(
                RecipeIngredient(name = "Fresh Ripe Peach (Halved & Pitted)", amount = 130.0, unit = "g", calories = 51.0, proteinG = 1.2, carbsG = 12.4, fatG = 0.3, fiberG = 2.0),
                RecipeIngredient(name = "Pure Wildflower Honey", amount = 1.0, unit = "tsp (7g)", calories = 21.0, proteinG = 0.0, carbsG = 5.7, fatG = 0.0, fiberG = 0.0),
                RecipeIngredient(name = "Pecan Halves Crushed", amount = 10.0, unit = "g", calories = 69.0, proteinG = 0.9, carbsG = 1.4, fatG = 7.2, fiberG = 1.0),
                RecipeIngredient(name = "Ground Cinnamon", amount = 0.5, unit = "g", calories = 1.0, proteinG = 0.0, carbsG = 0.4, fatG = 0.0, fiberG = 0.2)
                ),
                instructions = listOf(
                "Cut peach in half and remove the pit.", 
                "Place cut-side up in a baking dish, drizzle honey, and sprinkle cinnamon and crushed pecans over cavity.", 
                "Bake at 375°F (190°C) for 15 minutes until tender and caramelized."
                ),
                popularityScore = 89.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_de_05",
                name = "One-Bowl Microwave Protein Mug Cake",
                description = "Fluffy single-serve chocolate mug cake packed with chocolate protein, oat flour, and unsweetened applesauce.",
                emoji = "🧁",
                mealType = "dessert",
                prepTimeMinutes = 5,
                totalCalories = 180.0,
                proteinG = 22.3,
                carbsG = 16.9,
                fatG = 3.0,
                fiberG = 2.6,
                dietTags = listOf("High-Protein", "Vegetarian", "Quick <15min", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Chocolate Whey/Casein Protein Powder", amount = 25.0, unit = "g", calories = 100.0, proteinG = 20.0, carbsG = 2.0, fatG = 1.5, fiberG = 0.5),
                RecipeIngredient(name = "Oat Flour", amount = 15.0, unit = "g", calories = 57.0, proteinG = 2.0, carbsG = 10.2, fatG = 1.0, fiberG = 1.5),
                RecipeIngredient(name = "Unsweetened Applesauce", amount = 30.0, unit = "g", calories = 16.0, proteinG = 0.1, carbsG = 4.1, fatG = 0.1, fiberG = 0.5),
                RecipeIngredient(name = "Unsweetened Almond Milk", amount = 30.0, unit = "ml", calories = 5.0, proteinG = 0.2, carbsG = 0.1, fatG = 0.4, fiberG = 0.1),
                RecipeIngredient(name = "Baking Powder", amount = 0.5, unit = "tsp", calories = 2.0, proteinG = 0.0, carbsG = 0.5, fatG = 0.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Whisk protein powder, oat flour, and baking powder in a microwave-safe mug.", 
                "Stir in applesauce and almond milk until smooth batter forms.", 
                "Microwave on high for 50-60 seconds (do not overcook).", 
                "Let cool 2 minutes before digging in."
                ),
                popularityScore = 93.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_de_06",
                name = "Almond Flour Chocolate Chip Cookie Bar",
                description = "Chewy golden cookie square baked with unbleached almond flour, dark chocolate morsels, and coconut oil.",
                emoji = "🍪",
                mealType = "dessert",
                prepTimeMinutes = 18,
                totalCalories = 256.0,
                proteinG = 5.8,
                carbsG = 15.9,
                fatG = 20.5,
                fiberG = 4.0,
                dietTags = listOf("Vegetarian", "Low-Carb"),
                ingredients = listOf(
                RecipeIngredient(name = "Finely Ground Blanched Almond Flour", amount = 25.0, unit = "g", calories = 145.0, proteinG = 5.3, carbsG = 5.0, fatG = 12.5, fiberG = 3.0),
                RecipeIngredient(name = "Virgin Coconut Oil", amount = 1.0, unit = "tsp (5g)", calories = 44.0, proteinG = 0.0, carbsG = 0.0, fatG = 5.0, fiberG = 0.0),
                RecipeIngredient(name = "Pure Maple Syrup", amount = 1.0, unit = "tsp (5ml)", calories = 17.0, proteinG = 0.0, carbsG = 4.4, fatG = 0.0, fiberG = 0.0),
                RecipeIngredient(name = "70% Dark Chocolate Chips", amount = 10.0, unit = "g", calories = 50.0, proteinG = 0.5, carbsG = 6.5, fatG = 3.0, fiberG = 1.0)
                ),
                instructions = listOf(
                "Combine almond flour, melted coconut oil, maple syrup, and vanilla until a soft dough comes together.", 
                "Fold in dark chocolate chips.", 
                "Press into a mini square dish and bake at 350°F (175°C) for 11 minutes until edges are golden brown.", 
                "Allow to cool completely to achieve signature chewy bite."
                ),
                popularityScore = 91.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_de_07",
                name = "Chilled Mango Kheer (Rice Pudding)",
                description = "Fragrant basmati rice gently simmered in low-fat milk with green cardamom, topped with sweet Alfonso mango purée.",
                emoji = "🥭",
                mealType = "dessert",
                prepTimeMinutes = 22,
                totalCalories = 175.0,
                proteinG = 6.9,
                carbsG = 29.1,
                fatG = 3.9,
                fiberG = 1.6,
                dietTags = listOf("Vegetarian", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Cooked Basmati Rice", amount = 50.0, unit = "g", calories = 65.0, proteinG = 1.4, carbsG = 14.0, fatG = 0.2, fiberG = 0.3),
                RecipeIngredient(name = "Low-Fat Milk (1%)", amount = 120.0, unit = "ml", calories = 52.0, proteinG = 4.1, carbsG = 6.2, fatG = 1.2, fiberG = 0.0),
                RecipeIngredient(name = "Fresh Mango Pulp", amount = 50.0, unit = "g", calories = 30.0, proteinG = 0.4, carbsG = 7.5, fatG = 0.2, fiberG = 0.8),
                RecipeIngredient(name = "Cardamom & Sliced Pistachios", amount = 5.0, unit = "g", calories = 28.0, proteinG = 1.0, carbsG = 1.4, fatG = 2.3, fiberG = 0.5)
                ),
                instructions = listOf(
                "Simmer cooked rice in milk with bruised cardamom pods for 15 minutes until creamy and luscious.", 
                "Remove from heat, chill thoroughly in refrigerator.", 
                "Layer with sweet chilled mango pulp and top with vibrant green slivered pistachios."
                ),
                popularityScore = 94.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_de_08",
                name = "Chia Coconut Panna Cotta",
                description = "Silky chilled coconut milk and chia cream molded and inverted, served with passion fruit coulis.",
                emoji = "🍮",
                mealType = "dessert",
                prepTimeMinutes = 10,
                totalCalories = 164.0,
                proteinG = 3.9,
                carbsG = 16.7,
                fatG = 10.0,
                fiberG = 7.7,
                dietTags = listOf("Vegetarian", "Vegan", "Low-Carb"),
                ingredients = listOf(
                RecipeIngredient(name = "Light Coconut Milk", amount = 90.0, unit = "ml", calories = 56.0, proteinG = 0.8, carbsG = 1.9, fatG = 5.2, fiberG = 0.0),
                RecipeIngredient(name = "White Chia Seeds", amount = 15.0, unit = "g", calories = 73.0, proteinG = 2.5, carbsG = 6.3, fatG = 4.6, fiberG = 5.1),
                RecipeIngredient(name = "Fresh Passion Fruit Pulp", amount = 25.0, unit = "g", calories = 24.0, proteinG = 0.6, carbsG = 5.8, fatG = 0.2, fiberG = 2.6),
                RecipeIngredient(name = "Agave Nectar", amount = 0.5, unit = "tsp (3.5g)", calories = 11.0, proteinG = 0.0, carbsG = 2.7, fatG = 0.0, fiberG = 0.0)
                ),
                instructions = listOf(
                "Blend white chia seeds and coconut milk with agave until creamy.", 
                "Pour into a small glass mold and set in the fridge for 2 hours.", 
                "Spoon tangy fresh passion fruit pulp over the glistening surface."
                ),
                popularityScore = 89.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_de_09",
                name = "Greek Yogurt Tiramisu Cup",
                description = "Espresso-dipped ladyfinger layered with vanilla whipped Greek yogurt, dusted with Dutch process cocoa.",
                emoji = "☕",
                mealType = "dessert",
                prepTimeMinutes = 8,
                totalCalories = 119.0,
                proteinG = 12.7,
                carbsG = 14.3,
                fatG = 1.1,
                fiberG = 1.2,
                dietTags = listOf("High-Protein", "Vegetarian", "Quick <15min", "Mediterranean"),
                ingredients = listOf(
                RecipeIngredient(name = "Plain Non-Fat Greek Yogurt", amount = 100.0, unit = "g", calories = 65.0, proteinG = 11.0, carbsG = 3.5, fatG = 0.2, fiberG = 0.0),
                RecipeIngredient(name = "Italian Ladyfinger (Savoiardi)", amount = 1.0, unit = "cookie (11g)", calories = 42.0, proteinG = 1.0, carbsG = 8.5, fatG = 0.5, fiberG = 0.2),
                RecipeIngredient(name = "Brewed Espresso", amount = 20.0, unit = "ml", calories = 2.0, proteinG = 0.1, carbsG = 0.3, fatG = 0.0, fiberG = 0.0),
                RecipeIngredient(name = "Pure Vanilla & Stevia", amount = 1.0, unit = "dash", calories = 3.0, proteinG = 0.0, carbsG = 0.3, fatG = 0.0, fiberG = 0.0),
                RecipeIngredient(name = "Dutch Cocoa Powder", amount = 1.0, unit = "tsp (3g)", calories = 7.0, proteinG = 0.6, carbsG = 1.7, fatG = 0.4, fiberG = 1.0)
                ),
                instructions = listOf(
                "Dip ladyfinger briefly into hot espresso and place at the bottom of a dessert glass.", 
                "Whip Greek yogurt with vanilla and sweetener until silky.", 
                "Spoon yogurt cream over the coffee-soaked biscuit.", 
                "Dust with unsweetened Dutch cocoa powder through a fine sieve; chill 10 minutes before eating."
                ),
                popularityScore = 93.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_de_10",
                name = "Spiced Baked Apple Rings",
                description = "Honeycrisp apple rings baked with nutmeg, allspice, and rolled oats, served warm with Greek yogurt dollop.",
                emoji = "🍏",
                mealType = "dessert",
                prepTimeMinutes = 15,
                totalCalories = 146.0,
                proteinG = 2.3,
                carbsG = 27.4,
                fatG = 3.8,
                fiberG = 4.2,
                dietTags = listOf("Vegetarian", "Budget-Friendly"),
                ingredients = listOf(
                RecipeIngredient(name = "Fresh Apple Rings", amount = 110.0, unit = "g", calories = 57.0, proteinG = 0.3, carbsG = 14.7, fatG = 0.2, fiberG = 2.6),
                RecipeIngredient(name = "Rolled Oats", amount = 15.0, unit = "g", calories = 57.0, proteinG = 2.0, carbsG = 10.2, fatG = 1.0, fiberG = 1.5),
                RecipeIngredient(name = "Coconut Oil", amount = 0.5, unit = "tsp (2.5g)", calories = 22.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.5, fiberG = 0.0),
                RecipeIngredient(name = "Pure Maple Syrup", amount = 0.5, unit = "tsp (2.5ml)", calories = 8.0, proteinG = 0.0, carbsG = 2.2, fatG = 0.0, fiberG = 0.0),
                RecipeIngredient(name = "Ground Nutmeg & Allspice", amount = 0.5, unit = "g", calories = 2.0, proteinG = 0.0, carbsG = 0.3, fatG = 0.1, fiberG = 0.1)
                ),
                instructions = listOf(
                "Core and slice apple into round donuts.", 
                "Toss with spices, maple syrup, coconut oil, and rolled oats.", 
                "Bake on parchment at 375°F (190°C) for 12 minutes until fragrant, caramelized, and tender."
                ),
                popularityScore = 88.0,
                isFavorite = false
            ),
            // --- INDIAN & INTERNATIONAL SPECIALTY RECIPES ---
            RecipeEntity(
                id = "recipe_in_01",
                name = "Homestyle Palak Paneer",
                description = "Nutrient-dense spinach gravy cooked with cumin, garlic, and seared cubes of cottage cheese (paneer).",
                emoji = "🥬",
                mealType = "dinner",
                prepTimeMinutes = 20,
                totalCalories = 320.0,
                proteinG = 22.5,
                carbsG = 12.0,
                fatG = 20.0,
                fiberG = 6.5,
                dietTags = listOf("High-Protein", "Vegetarian", "Indian", "Low-Carb", "Gluten Free", "Muscle Gain"),
                ingredients = listOf(
                    RecipeIngredient(name = "Fresh Spinach (Palak)", amount = 200.0, unit = "g", calories = 46.0, proteinG = 5.8, carbsG = 7.2, fatG = 0.8, fiberG = 4.4),
                    RecipeIngredient(name = "Low-Fat Paneer", amount = 100.0, unit = "g", calories = 190.0, proteinG = 16.0, carbsG = 2.0, fatG = 13.0, fiberG = 0.0),
                    RecipeIngredient(name = "Onion & Tomato Gravy Base", amount = 60.0, unit = "g", calories = 40.0, proteinG = 0.8, carbsG = 6.0, fatG = 1.5, fiberG = 1.5),
                    RecipeIngredient(name = "Ghee / Cold Pressed Mustard Oil", amount = 0.5, unit = "tsp (2.5ml)", calories = 22.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.5, fiberG = 0.0),
                    RecipeIngredient(name = "Garlic, Ginger & Garam Masala", amount = 1.0, unit = "tsp (5g)", calories = 10.0, proteinG = 0.3, carbsG = 1.8, fatG = 0.2, fiberG = 0.6),
                    RecipeIngredient(name = "Kasuri Methi (Fenugreek)", amount = 1.0, unit = "pinch", calories = 2.0, proteinG = 0.1, carbsG = 0.4, fatG = 0.0, fiberG = 0.2)
                ),
                instructions = listOf(
                    "Blanch washed spinach leaves in boiling salted water for 2 minutes, then plunge into ice water to preserve vibrant green color.",
                    "Blend blanched spinach with green chili and ginger into a velvety smooth puree.",
                    "Sauté minced garlic and onions in 1/2 tsp ghee until golden, add chopped tomatoes and spices (turmeric, coriander, cumin).",
                    "Pour in the spinach puree and simmer on low heat for 5 minutes.",
                    "Gently fold in paneer cubes and crushed kasuri methi; simmer for 2 minutes and serve warm."
                ),
                popularityScore = 98.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_in_02",
                name = "Yellow Dal Tadka & Jeera Brown Rice",
                description = "Golden yellow toor dal tempered with cumin seeds, Kashmiri red chili, garlic, and served with aromatic cumin brown rice.",
                emoji = "🍲",
                mealType = "lunch",
                prepTimeMinutes = 25,
                totalCalories = 385.0,
                proteinG = 16.0,
                carbsG = 64.0,
                fatG = 6.5,
                fiberG = 11.0,
                dietTags = listOf("Vegetarian", "Vegan", "Indian", "High Fiber", "Weight Loss", "Gluten Free"),
                ingredients = listOf(
                    RecipeIngredient(name = "Split Pigeon Peas (Toor Dal)", amount = 50.0, unit = "g (raw)", calories = 170.0, proteinG = 11.0, carbsG = 30.0, fatG = 0.8, fiberG = 7.5),
                    RecipeIngredient(name = "Cooked Brown Basmati Rice", amount = 100.0, unit = "g", calories = 120.0, proteinG = 2.6, carbsG = 25.0, fatG = 1.0, fiberG = 1.8),
                    RecipeIngredient(name = "Ghee for Tempering", amount = 0.5, unit = "tsp (2.5ml)", calories = 22.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.5, fiberG = 0.0),
                    RecipeIngredient(name = "Cumin Seeds, Mustard Seeds & Hing", amount = 1.0, unit = "tsp", calories = 12.0, proteinG = 0.4, carbsG = 1.5, fatG = 0.5, fiberG = 0.4),
                    RecipeIngredient(name = "Tomatoes, Onions & Fresh Cilantro", amount = 80.0, unit = "g", calories = 35.0, proteinG = 1.0, carbsG = 6.5, fatG = 0.2, fiberG = 1.3),
                    RecipeIngredient(name = "Lemon Juice & Turmeric", amount = 1.0, unit = "dash", calories = 6.0, proteinG = 0.1, carbsG = 1.0, fatG = 0.0, fiberG = 0.0)
                ),
                instructions = listOf(
                    "Pressure cook toor dal with water, turmeric, and sea salt until soft (3 whistles). Whisk gently.",
                    "Heat ghee in a tadka pan, crackle cumin seeds, dried red chili, chopped garlic, and asafoetida (hing).",
                    "Add chopped tomatoes and chili powder, sauté until soft, then pour sizzled tadka over the dal.",
                    "Garnish with freshly chopped cilantro and a squeeze of fresh lemon; serve with warm cumin brown rice."
                ),
                popularityScore = 95.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_in_03",
                name = "Protein-Packed Moong Dal Chilla",
                description = "Crispy, savory golden crepes made from yellow moong lentils, studded with grated paneer, onions, and coriander.",
                emoji = "🥞",
                mealType = "breakfast",
                prepTimeMinutes = 12,
                totalCalories = 280.0,
                proteinG = 18.2,
                carbsG = 34.0,
                fatG = 7.0,
                fiberG = 6.5,
                dietTags = listOf("High-Protein", "Vegetarian", "Indian", "Quick <15min", "Gluten Free", "Weight Loss"),
                ingredients = listOf(
                    RecipeIngredient(name = "Soaked Yellow Moong Dal", amount = 60.0, unit = "g (raw)", calories = 195.0, proteinG = 14.0, carbsG = 33.0, fatG = 0.8, fiberG = 5.0),
                    RecipeIngredient(name = "Grated Low-Fat Paneer", amount = 30.0, unit = "g", calories = 55.0, proteinG = 4.0, carbsG = 0.5, fatG = 4.0, fiberG = 0.0),
                    RecipeIngredient(name = "Chopped Onions & Green Chili", amount = 30.0, unit = "g", calories = 15.0, proteinG = 0.4, carbsG = 3.2, fatG = 0.1, fiberG = 0.6),
                    RecipeIngredient(name = "Olive Oil Spray", amount = 1.0, unit = "g", calories = 9.0, proteinG = 0.0, carbsG = 0.0, fatG = 1.0, fiberG = 0.0),
                    RecipeIngredient(name = "Ajwain, Cumin & Pink Salt", amount = 1.0, unit = "pinch", calories = 6.0, proteinG = 0.2, carbsG = 0.8, fatG = 0.2, fiberG = 0.3)
                ),
                instructions = listOf(
                    "Grind soaked moong dal with ginger, green chili, and water into a smooth, pourable crepe batter.",
                    "Heat a non-stick tawa or cast-iron skillet, pour a ladle of batter and spread into a thin round.",
                    "Sprinkle grated paneer, chopped onions, cilantro, and ajwain on top; lightly press into batter.",
                    "Mist with olive oil spray, cook until bottom is golden and crisp (2 mins), flip and cook 1 min. Serve with mint chutney."
                ),
                popularityScore = 94.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_in_04",
                name = "Spiced Tandoori Chicken Breast",
                description = "Skinless chicken breast marinated in Greek yogurt, Kashmiri paprika, roasted cumin, and grilled to smoky perfection.",
                emoji = "🍗",
                mealType = "dinner",
                prepTimeMinutes = 20,
                totalCalories = 310.0,
                proteinG = 44.0,
                carbsG = 5.0,
                fatG = 11.5,
                fiberG = 1.2,
                dietTags = listOf("High-Protein", "Indian", "Low-Carb", "Muscle Gain", "Gluten Free", "Post Workout"),
                ingredients = listOf(
                    RecipeIngredient(name = "Boneless Skinless Chicken Breast", amount = 180.0, unit = "g", calories = 220.0, proteinG = 40.0, carbsG = 0.0, fatG = 5.5, fiberG = 0.0),
                    RecipeIngredient(name = "Non-Fat Plain Greek Yogurt", amount = 40.0, unit = "g", calories = 25.0, proteinG = 4.0, carbsG = 1.5, fatG = 0.1, fiberG = 0.0),
                    RecipeIngredient(name = "Mustard Oil", amount = 0.5, unit = "tsp (2.5ml)", calories = 22.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.5, fiberG = 0.0),
                    RecipeIngredient(name = "Kashmiri Red Chili & Garam Masala", amount = 1.0, unit = "tsp (5g)", calories = 15.0, proteinG = 0.5, carbsG = 2.5, fatG = 0.5, fiberG = 1.0),
                    RecipeIngredient(name = "Ginger-Garlic Paste & Lemon", amount = 1.0, unit = "tbsp", calories = 12.0, proteinG = 0.3, carbsG = 2.2, fatG = 0.1, fiberG = 0.2)
                ),
                instructions = listOf(
                    "Make deep diagonal slashes in chicken breast so the marinade penetrates deeply.",
                    "Whisk Greek yogurt, ginger-garlic paste, mustard oil, Kashmiri chili, roasted cumin, kasuri methi, and lemon juice.",
                    "Coat chicken thoroughly in marinade and rest for 15-30 minutes.",
                    "Grill on high heat or bake in oven at 220°C (425°F) for 16-18 minutes until charred at edges and internal temp reaches 75°C (165°F).",
                    "Rest for 4 minutes, slice, and dust with chaat masala."
                ),
                popularityScore = 99.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_in_05",
                name = "Hearty Amritsari Chana Masala",
                description = "Slow-simmered chickpeas in a robust tomato, onion, and pomegranate-spiced sauce with roasted coriander.",
                emoji = "🧆",
                mealType = "lunch",
                prepTimeMinutes = 20,
                totalCalories = 345.0,
                proteinG = 15.5,
                carbsG = 52.0,
                fatG = 7.5,
                fiberG = 12.5,
                dietTags = listOf("Vegetarian", "Vegan", "Indian", "High Fiber", "Post Workout", "Gluten Free"),
                ingredients = listOf(
                    RecipeIngredient(name = "Cooked Kabuli Chickpeas", amount = 180.0, unit = "g", calories = 240.0, proteinG = 13.0, carbsG = 38.0, fatG = 3.5, fiberG = 10.0),
                    RecipeIngredient(name = "Onion, Tomato & Ginger Gravy", amount = 100.0, unit = "g", calories = 60.0, proteinG = 1.5, carbsG = 9.0, fatG = 1.8, fiberG = 2.0),
                    RecipeIngredient(name = "Cold Pressed Mustard Oil", amount = 0.5, unit = "tsp (2.5ml)", calories = 22.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.5, fiberG = 0.0),
                    RecipeIngredient(name = "Anardana (Pomegranate) & Chana Masala", amount = 1.0, unit = "tsp (5g)", calories = 15.0, proteinG = 0.5, carbsG = 2.5, fatG = 0.3, fiberG = 0.8),
                    RecipeIngredient(name = "Fresh Green Chili & Cilantro", amount = 10.0, unit = "g", calories = 5.0, proteinG = 0.2, carbsG = 0.8, fatG = 0.1, fiberG = 0.3)
                ),
                instructions = listOf(
                    "Simmer cooked chickpeas with a tea bag or dried amla to give the classic dark Amritsari hue.",
                    "Heat oil in a heavy-bottomed kadai, sauté diced onions until deeply caramelized.",
                    "Add ginger-garlic, tomato puree, roasted coriander powder, anardana powder, and chana masala blend.",
                    "Add chickpeas and 1/2 cup cooking liquor. Mash a few chickpeas with back of spoon to thicken gravy.",
                    "Simmer 10 minutes; finish with ginger juliennes, slit green chilies, and fresh coriander."
                ),
                popularityScore = 93.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_in_06",
                name = "Desi Scrambled Egg Bhurji & Multigrain Roti",
                description = "Street-style spiced scrambled eggs tossed with red onions, ripe tomatoes, green chilies, and fresh mint.",
                emoji = "🍳",
                mealType = "breakfast",
                prepTimeMinutes = 10,
                totalCalories = 295.0,
                proteinG = 18.5,
                carbsG = 22.0,
                fatG = 13.5,
                fiberG = 4.0,
                dietTags = listOf("High-Protein", "Indian", "Quick <15min", "Weight Loss"),
                ingredients = listOf(
                    RecipeIngredient(name = "Whole Eggs", amount = 2.0, unit = "large (100g)", calories = 144.0, proteinG = 12.6, carbsG = 0.8, fatG = 9.6, fiberG = 0.0),
                    RecipeIngredient(name = "Egg White", amount = 1.0, unit = "large (33g)", calories = 17.0, proteinG = 3.6, carbsG = 0.2, fatG = 0.1, fiberG = 0.0),
                    RecipeIngredient(name = "Multigrain Phulka / Roti", amount = 1.0, unit = "piece (35g)", calories = 85.0, proteinG = 2.5, carbsG = 17.0, fatG = 0.8, fiberG = 3.0),
                    RecipeIngredient(name = "Onions, Tomatoes, Green Chilies", amount = 60.0, unit = "g", calories = 26.0, proteinG = 0.8, carbsG = 4.5, fatG = 0.2, fiberG = 1.0),
                    RecipeIngredient(name = "Butter / Ghee", amount = 0.5, unit = "tsp (2.5g)", calories = 22.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.5, fiberG = 0.0)
                ),
                instructions = listOf(
                    "Whisk eggs and egg white with salt, black pepper, and turmeric until light and bubbly.",
                    "Melt butter in a skillet, sauté finely chopped onions and green chilies until translucent.",
                    "Toss in chopped tomatoes and cook for 1 minute until tender.",
                    "Pour whisked eggs into pan, lower heat, and gently fold with a spatula until soft curds form.",
                    "Garnish with chopped mint and cilantro; serve with a warm, dry multigrain roti."
                ),
                popularityScore = 96.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_in_07",
                name = "North Indian Rajma Masala",
                description = "Velvety Kashmiri red kidney beans slow cooked with caramelized onions, tomatoes, and aromatic whole spices.",
                emoji = "🍲",
                mealType = "lunch",
                prepTimeMinutes = 25,
                totalCalories = 330.0,
                proteinG = 16.0,
                carbsG = 51.0,
                fatG = 6.0,
                fiberG = 13.0,
                dietTags = listOf("Vegetarian", "Vegan", "Indian", "High Fiber", "Weight Loss", "Gluten Free"),
                ingredients = listOf(
                    RecipeIngredient(name = "Cooked Kashmiri Red Kidney Beans", amount = 180.0, unit = "g", calories = 225.0, proteinG = 14.0, carbsG = 38.0, fatG = 1.0, fiberG = 11.5),
                    RecipeIngredient(name = "Onion Tomato Masala Paste", amount = 80.0, unit = "g", calories = 65.0, proteinG = 1.5, carbsG = 9.0, fatG = 2.5, fiberG = 1.5),
                    RecipeIngredient(name = "Ghee / Mustard Oil", amount = 0.5, unit = "tsp (2.5ml)", calories = 22.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.5, fiberG = 0.0),
                    RecipeIngredient(name = "Rajma Spice Blend & Cumin", amount = 1.0, unit = "tsp (5g)", calories = 12.0, proteinG = 0.4, carbsG = 1.8, fatG = 0.4, fiberG = 0.6),
                    RecipeIngredient(name = "Ginger Juliennes & Cilantro", amount = 10.0, unit = "g", calories = 5.0, proteinG = 0.2, carbsG = 0.8, fatG = 0.0, fiberG = 0.2)
                ),
                instructions = listOf(
                    "Soak Kashmiri rajma overnight and pressure cook until tender and melt-in-mouth soft.",
                    "Sauté crushed ginger, garlic, and onions until dark golden-brown in a heavy pan.",
                    "Add tomato puree, turmeric, coriander powder, cumin, and red chili powder; roast until oil separates.",
                    "Fold in cooked kidney beans with liquor. Mash a cupful against the pan walls to build rich body.",
                    "Simmer for 15 minutes on medium-low heat. Finish with fresh ginger juliennes and cilantro."
                ),
                popularityScore = 97.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_in_08",
                name = "Crispy Tandoori Paneer Tikka Skewers",
                description = "Juicy cubes of cottage cheese skewered with crunchy bell peppers, red onions, and seasoned with ajwain and chaat masala.",
                emoji = "🍢",
                mealType = "snack",
                prepTimeMinutes = 15,
                totalCalories = 240.0,
                proteinG = 17.5,
                carbsG = 9.0,
                fatG = 15.0,
                fiberG = 2.8,
                dietTags = listOf("High-Protein", "Vegetarian", "Indian", "Low-Carb", "Gluten Free", "Muscle Gain"),
                ingredients = listOf(
                    RecipeIngredient(name = "Low-Fat Paneer Cubes", amount = 100.0, unit = "g", calories = 180.0, proteinG = 15.0, carbsG = 2.0, fatG = 12.0, fiberG = 0.0),
                    RecipeIngredient(name = "Bell Peppers & Diced Red Onion", amount = 80.0, unit = "g", calories = 28.0, proteinG = 1.0, carbsG = 5.5, fatG = 0.2, fiberG = 1.8),
                    RecipeIngredient(name = "Greek Yogurt Marinade Base", amount = 30.0, unit = "g", calories = 18.0, proteinG = 2.5, carbsG = 1.2, fatG = 0.1, fiberG = 0.0),
                    RecipeIngredient(name = "Mustard Oil & Tikka Spices", amount = 1.0, unit = "tsp", calories = 25.0, proteinG = 0.2, carbsG = 1.0, fatG = 2.5, fiberG = 0.3)
                ),
                instructions = listOf(
                    "Mix Greek yogurt, mustard oil, roasted gram flour (besan), ajwain, turmeric, and chili powder into marinade.",
                    "Gently toss paneer cubes, onion squares, and bell peppers until evenly coated; rest 10 minutes.",
                    "Thread onto wooden or metal skewers alternating between paneer, pepper, and onion.",
                    "Air fry at 200°C (390°F) for 8-10 minutes or sear in a ridged grill pan until charred at corners.",
                    "Sprinkle generously with tangy chaat masala and fresh lemon juice."
                ),
                popularityScore = 95.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_in_09",
                name = "Roasted Masala Makhana (Fox Nuts)",
                description = "Crunchy roasted lotus seeds tossed in virgin olive oil, turmeric, pink Himalayan salt, and crushed black pepper.",
                emoji = "🍿",
                mealType = "snack",
                prepTimeMinutes = 8,
                totalCalories = 135.0,
                proteinG = 4.2,
                carbsG = 22.0,
                fatG = 3.5,
                fiberG = 3.8,
                dietTags = listOf("Vegetarian", "Vegan", "Indian", "Low-Calorie", "Weight Loss", "Gluten Free", "Quick <15min"),
                ingredients = listOf(
                    RecipeIngredient(name = "Fox Nuts (Phool Makhana)", amount = 35.0, unit = "g", calories = 110.0, proteinG = 4.0, carbsG = 21.0, fatG = 0.4, fiberG = 3.5),
                    RecipeIngredient(name = "Extra Virgin Olive Oil / Ghee", amount = 0.5, unit = "tsp (2.5ml)", calories = 22.0, proteinG = 0.0, carbsG = 0.0, fatG = 2.5, fiberG = 0.0),
                    RecipeIngredient(name = "Turmeric, Chaat Masala & Pink Salt", amount = 1.0, unit = "pinch", calories = 3.0, proteinG = 0.1, carbsG = 0.6, fatG = 0.1, fiberG = 0.2)
                ),
                instructions = listOf(
                    "Heat oil/ghee in a broad pan over low-medium flame.",
                    "Add makhana and dry roast continuously for 6-8 minutes until crisp and snapping easily between fingers.",
                    "Turn off heat, immediately sprinkle turmeric, rock salt, chaat masala, and black pepper.",
                    "Toss vigorously so the warm spices adhere to the crispy makhana. Enjoy immediately or store in airtight jar."
                ),
                popularityScore = 91.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_in_10",
                name = "Masala Oats with Greens & Flaxseed",
                description = "Hearty rolled oats simmered in spiced vegetable broth with green peas, diced carrots, and roasted ground flaxseeds.",
                emoji = "🥣",
                mealType = "breakfast",
                prepTimeMinutes = 12,
                totalCalories = 265.0,
                proteinG = 9.8,
                carbsG = 42.0,
                fatG = 6.2,
                fiberG = 8.5,
                dietTags = listOf("Vegetarian", "Vegan", "Indian", "High Fiber", "Weight Loss", "Quick <15min"),
                ingredients = listOf(
                    RecipeIngredient(name = "Rolled Whole Oats", amount = 50.0, unit = "g", calories = 185.0, proteinG = 6.5, carbsG = 33.0, fatG = 3.2, fiberG = 5.0),
                    RecipeIngredient(name = "Green Peas & Diced Carrots", amount = 60.0, unit = "g", calories = 40.0, proteinG = 2.0, carbsG = 7.5, fatG = 0.2, fiberG = 2.2),
                    RecipeIngredient(name = "Ground Roasted Flaxseed", amount = 5.0, unit = "g", calories = 26.0, proteinG = 0.9, carbsG = 1.4, fatG = 2.1, fiberG = 1.3),
                    RecipeIngredient(name = "Cumin, Mustard, Turmeric & Curry Leaves", amount = 1.0, unit = "tsp", calories = 8.0, proteinG = 0.3, carbsG = 1.0, fatG = 0.3, fiberG = 0.4),
                    RecipeIngredient(name = "Lemon Juice & Chopped Coriander", amount = 1.0, unit = "tbsp", calories = 4.0, proteinG = 0.1, carbsG = 0.8, fatG = 0.0, fiberG = 0.1)
                ),
                instructions = listOf(
                    "Temper mustard seeds, cumin, green chili, and curry leaves in 2 drops of oil until crackling.",
                    "Sauté onions, carrots, and peas for 2 minutes until bright.",
                    "Add rolled oats, turmeric, garam masala, salt, and 1.5 cups water or vegetable stock.",
                    "Simmer on medium heat for 4-5 minutes until oats are thick and creamy.",
                    "Stir in ground flaxseeds, drizzle fresh lemon juice, and garnish with fresh coriander."
                ),
                popularityScore = 92.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_int_01",
                name = "Mediterranean Herb-Crusted Wild Salmon",
                description = "Tender pan-seared Atlantic salmon fillet with fresh dill, oregano, lemon zest, and roasted green asparagus spears.",
                emoji = "🐟",
                mealType = "dinner",
                prepTimeMinutes = 18,
                totalCalories = 365.0,
                proteinG = 38.5,
                carbsG = 6.5,
                fatG = 20.5,
                fiberG = 3.2,
                dietTags = listOf("High-Protein", "Mediterranean", "Low-Carb", "Gluten Free", "Muscle Gain", "Post Workout"),
                ingredients = listOf(
                    RecipeIngredient(name = "Fresh Atlantic Salmon Fillet", amount = 160.0, unit = "g", calories = 280.0, proteinG = 34.0, carbsG = 0.0, fatG = 15.5, fiberG = 0.0),
                    RecipeIngredient(name = "Tender Green Asparagus", amount = 100.0, unit = "g", calories = 22.0, proteinG = 2.4, carbsG = 4.0, fatG = 0.2, fiberG = 2.2),
                    RecipeIngredient(name = "Extra Virgin Olive Oil", amount = 1.0, unit = "tsp (5ml)", calories = 44.0, proteinG = 0.0, carbsG = 0.0, fatG = 5.0, fiberG = 0.0),
                    RecipeIngredient(name = "Fresh Dill, Oregano & Garlic", amount = 1.0, unit = "tbsp", calories = 8.0, proteinG = 0.4, carbsG = 1.4, fatG = 0.1, fiberG = 0.5),
                    RecipeIngredient(name = "Lemon Zest & Sea Salt", amount = 1.0, unit = "pinch", calories = 3.0, proteinG = 0.0, carbsG = 0.8, fatG = 0.0, fiberG = 0.2)
                ),
                instructions = listOf(
                    "Pat salmon fillet dry and season both sides with sea salt, cracked black pepper, and lemon zest.",
                    "Press minced garlic, fresh dill, and dried oregano firmly onto salmon flesh.",
                    "Heat olive oil in a stainless steel skillet over medium-high heat. Place salmon skin-side down.",
                    "Sear undisturbed for 4 minutes until skin is golden and crispy, flip and sear 3 minutes.",
                    "Toss asparagus in pan drippings for 2 minutes until tender-crisp. Serve with lemon wedges."
                ),
                popularityScore = 98.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_int_02",
                name = "Mexican Chicken & Black Bean Quinoa Bowl",
                description = "Grilled lime-chipotle chicken breast, organic tri-color quinoa, black beans, sweet corn, and creamy avocado puree.",
                emoji = "🥗",
                mealType = "lunch",
                prepTimeMinutes = 20,
                totalCalories = 425.0,
                proteinG = 41.0,
                carbsG = 45.0,
                fatG = 10.5,
                fiberG = 9.5,
                dietTags = listOf("High-Protein", "High Fiber", "Post Workout", "Gluten Free", "Muscle Gain"),
                ingredients = listOf(
                    RecipeIngredient(name = "Grilled Chicken Breast Strips", amount = 140.0, unit = "g", calories = 175.0, proteinG = 33.0, carbsG = 0.0, fatG = 4.0, fiberG = 0.0),
                    RecipeIngredient(name = "Cooked Organic Quinoa", amount = 80.0, unit = "g", calories = 95.0, proteinG = 3.5, carbsG = 17.5, fatG = 1.5, fiberG = 2.2),
                    RecipeIngredient(name = "Cooked Black Beans", amount = 60.0, unit = "g", calories = 75.0, proteinG = 4.5, carbsG = 13.5, fatG = 0.3, fiberG = 4.5),
                    RecipeIngredient(name = "Fresh Hass Avocado", amount = 30.0, unit = "g", calories = 48.0, proteinG = 0.6, carbsG = 2.5, fatG = 4.5, fiberG = 2.0),
                    RecipeIngredient(name = "Sweet Corn & Pico de Gallo", amount = 50.0, unit = "g", calories = 30.0, proteinG = 1.0, carbsG = 6.5, fatG = 0.3, fiberG = 1.2),
                    RecipeIngredient(name = "Lime & Chipotle Dressing", amount = 1.0, unit = "tbsp", calories = 12.0, proteinG = 0.2, carbsG = 2.0, fatG = 0.4, fiberG = 0.1)
                ),
                instructions = listOf(
                    "Season chicken breast with chipotle powder, cumin, lime juice, and garlic; grill until juicy.",
                    "Layer warm cooked quinoa at the bottom of an artisan meal bowl.",
                    "Arrange black beans, sweet corn kernels, and colorful pico de gallo in sections around quinoa.",
                    "Top with sliced grilled chicken breast and fresh avocado slices.",
                    "Drizzle fresh lime juice and salsa verde over the bowl; garnish with chopped cilantro."
                ),
                popularityScore = 97.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_int_03",
                name = "Thai Holy Basil Chicken (Pad Krapow)",
                description = "Lean minced chicken breast flash-wokked with fragrant holy basil, bird's eye chili, garlic, and served with steamed jasmine rice.",
                emoji = "🍛",
                mealType = "dinner",
                prepTimeMinutes = 14,
                totalCalories = 350.0,
                proteinG = 36.0,
                carbsG = 30.0,
                fatG = 9.0,
                fiberG = 2.5,
                dietTags = listOf("High-Protein", "Quick <15min", "Muscle Gain", "Weight Loss"),
                ingredients = listOf(
                    RecipeIngredient(name = "Lean Minced Chicken Breast", amount = 150.0, unit = "g", calories = 180.0, proteinG = 33.0, carbsG = 0.0, fatG = 4.5, fiberG = 0.0),
                    RecipeIngredient(name = "Fresh Thai Holy Basil Leaves", amount = 25.0, unit = "g", calories = 6.0, proteinG = 0.8, carbsG = 0.7, fatG = 0.1, fiberG = 0.4),
                    RecipeIngredient(name = "Steamed Jasmine Rice", amount = 80.0, unit = "g", calories = 105.0, proteinG = 2.0, carbsG = 23.5, fatG = 0.2, fiberG = 0.6),
                    RecipeIngredient(name = "Low-Sodium Soy & Fish Sauce Blend", amount = 1.0, unit = "tbsp", calories = 18.0, proteinG = 1.5, carbsG = 2.0, fatG = 0.0, fiberG = 0.0),
                    RecipeIngredient(name = "Garlic, Chili & Sesame Oil", amount = 1.0, unit = "tsp", calories = 41.0, proteinG = 0.2, carbsG = 1.2, fatG = 4.0, fiberG = 0.3)
                ),
                instructions = listOf(
                    "Crush garlic and fresh Thai chilies roughly in a mortar and pestle.",
                    "Heat a smoking hot wok with sesame oil, stir-fry chili and garlic paste for 20 seconds until fragrant.",
                    "Add minced chicken breast, breaking up clumps vigorously with spatula over high heat for 3-4 minutes.",
                    "Pour in low-sodium soy sauce, oyster sauce, and touch of coconut sugar; toss to coat.",
                    "Turn off heat, throw in a generous handful of fresh holy basil; toss until gently wilted. Serve with jasmine rice."
                ),
                popularityScore = 95.5,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_int_04",
                name = "Post-Workout Whey Power Oats",
                description = "Warm rolled oats infused with vanilla whey isolate, cinnamon, raw cocoa nibs, and sliced Cavendish banana.",
                emoji = "🥣",
                mealType = "breakfast",
                prepTimeMinutes = 7,
                totalCalories = 360.0,
                proteinG = 32.0,
                carbsG = 45.0,
                fatG = 5.5,
                fiberG = 7.0,
                dietTags = listOf("High-Protein", "Post Workout", "Muscle Gain", "Quick <15min", "Vegetarian"),
                ingredients = listOf(
                    RecipeIngredient(name = "Rolled Whole Oats", amount = 45.0, unit = "g", calories = 165.0, proteinG = 6.0, carbsG = 30.0, fatG = 2.8, fiberG = 4.5),
                    RecipeIngredient(name = "Vanilla Whey Protein Isolate", amount = 30.0, unit = "g", calories = 115.0, proteinG = 25.0, carbsG = 1.5, fatG = 0.8, fiberG = 0.0),
                    RecipeIngredient(name = "Fresh Sliced Banana", amount = 50.0, unit = "g", calories = 45.0, proteinG = 0.6, carbsG = 11.5, fatG = 0.1, fiberG = 1.3),
                    RecipeIngredient(name = "Unsweetened Almond Milk", amount = 120.0, unit = "ml", calories = 18.0, proteinG = 0.5, carbsG = 0.8, fatG = 1.4, fiberG = 0.5),
                    RecipeIngredient(name = "Ceylon Cinnamon & Raw Cocoa Nibs", amount = 5.0, unit = "g", calories = 17.0, proteinG = 0.4, carbsG = 1.2, fatG = 1.2, fiberG = 0.9)
                ),
                instructions = listOf(
                    "Simmer rolled oats in almond milk and water for 3 minutes until thickened and creamy.",
                    "Remove saucepan from heat and allow to cool for 60 seconds (prevents whey protein clumping).",
                    "Vigorously stir in vanilla whey protein isolate until completely dissolved and silky.",
                    "Pour into bowl, arrange banana coins on top, and dust with cinnamon and crunchy cocoa nibs."
                ),
                popularityScore = 96.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_int_05",
                name = "Mango Coconut Chia Seed Parfait",
                description = "Hydrated black chia seeds in creamy coconut milk, layered with pureed Alphonso mango pulp and toasted coconut chips.",
                emoji = "🥭",
                mealType = "dessert",
                prepTimeMinutes = 10,
                totalCalories = 210.0,
                proteinG = 5.2,
                carbsG = 26.0,
                fatG = 9.8,
                fiberG = 8.5,
                dietTags = listOf("Vegetarian", "Vegan", "Desserts", "Gluten Free", "Low-Calorie", "High Fiber"),
                ingredients = listOf(
                    RecipeIngredient(name = "Black Chia Seeds", amount = 20.0, unit = "g", calories = 98.0, proteinG = 3.4, carbsG = 8.5, fatG = 6.2, fiberG = 7.0),
                    RecipeIngredient(name = "Light Coconut Milk", amount = 80.0, unit = "ml", calories = 45.0, proteinG = 0.8, carbsG = 1.5, fatG = 4.2, fiberG = 0.0),
                    RecipeIngredient(name = "Fresh Alphonso Mango Puree", amount = 60.0, unit = "g", calories = 42.0, proteinG = 0.5, carbsG = 10.5, fatG = 0.2, fiberG = 1.0),
                    RecipeIngredient(name = "Toasted Unsweetened Coconut Flakes", amount = 5.0, unit = "g", calories = 25.0, proteinG = 0.3, carbsG = 1.0, fatG = 2.4, fiberG = 0.6)
                ),
                instructions = listOf(
                    "Whisk chia seeds and coconut milk with a drop of pure vanilla extract in a glass.",
                    "Refrigerate for at least 30 minutes (or overnight) until seeds swell into a thick, luxurious pudding.",
                    "Layer mango puree over the chilled coconut chia pudding.",
                    "Garnish with golden toasted coconut flakes and fresh mint leaves."
                ),
                popularityScore = 94.0,
                isFavorite = false
            ),
            RecipeEntity(
                id = "recipe_int_06",
                name = "1-Minute Chocolate Whey Protein Mug Cake",
                description = "Decadent, guilt-free microwave chocolate cake made with rich Dutch cocoa, whey protein, and oat flour.",
                emoji = "🧁",
                mealType = "dessert",
                prepTimeMinutes = 5,
                totalCalories = 175.0,
                proteinG = 21.0,
                carbsG = 14.0,
                fatG = 3.5,
                fiberG = 3.8,
                dietTags = listOf("High-Protein", "Desserts", "Quick <15min", "Vegetarian", "Weight Loss"),
                ingredients = listOf(
                    RecipeIngredient(name = "Chocolate Whey Protein Powder", amount = 20.0, unit = "g", calories = 78.0, proteinG = 16.0, carbsG = 1.5, fatG = 0.8, fiberG = 0.2),
                    RecipeIngredient(name = "Finely Ground Oat Flour", amount = 15.0, unit = "g", calories = 55.0, proteinG = 2.0, carbsG = 10.0, fatG = 1.0, fiberG = 1.5),
                    RecipeIngredient(name = "Unsweetened Dutch Cocoa Powder", amount = 1.0, unit = "tbsp (7g)", calories = 16.0, proteinG = 1.4, carbsG = 3.8, fatG = 0.9, fiberG = 2.1),
                    RecipeIngredient(name = "Egg White", amount = 1.0, unit = "large (33g)", calories = 17.0, proteinG = 3.6, carbsG = 0.2, fatG = 0.1, fiberG = 0.0),
                    RecipeIngredient(name = "Baking Powder & Stevia", amount = 0.5, unit = "tsp", calories = 2.0, proteinG = 0.0, carbsG = 0.5, fatG = 0.0, fiberG = 0.0),
                    RecipeIngredient(name = "Almond Milk", amount = 30.0, unit = "ml", calories = 5.0, proteinG = 0.1, carbsG = 0.2, fatG = 0.4, fiberG = 0.1)
                ),
                instructions = listOf(
                    "Whisk oat flour, chocolate whey, cocoa powder, stevia, and baking powder in a standard ceramic mug.",
                    "Add egg white and almond milk; stir with a small fork until a smooth cake batter forms.",
                    "Microwave on high for 50-60 seconds (do not overcook to keep center moist and fudgy).",
                    "Let rest 1 minute, dust with cocoa powder, and enjoy warm with a spoon."
                ),
                popularityScore = 93.5,
                isFavorite = false
            )

        )
    }
}
