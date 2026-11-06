package com.kcalgrindai.app.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KcalGrindDatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        KcalGrindDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate4To5_preservesDataAndAddsNullFirstName() {
        // 1. Seed database at version 4 (pre-migration schema without firstName)
        var db = helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """
                INSERT INTO user_profile (
                    id, firebaseUid, email, goal, units, age, heightCm, weightKg,
                    goalWeightKg, activityLevel, dietTags, allergies, targetsSource,
                    createdAt, updatedAt
                ) VALUES (
                    1, 'fb_123', 'user@example.com', 'lose', 'metric', 29, 178.0, 82.5,
                    75.0, 'moderate', '["high_protein"]', '["gluten"]', 'recommended',
                    1700000000, 1700000000
                )
                """.trimIndent()
            )
            close()
        }

        // 2. Execute the app's actual AutoMigration(from = 4, to = 5) and validate against schema 5.json
        db = helper.runMigrationsAndValidate(TEST_DB, 5, true)

        // 3. Query migrated row
        val cursor = db.query("SELECT * FROM user_profile WHERE id = 1")
        assertTrue("Migrated pre-existing row exists", cursor.moveToFirst())

        // 4. Assert firstName column exists and reads back as NULL (not empty string, not a default placeholder)
        val firstNameIndex = cursor.getColumnIndex("firstName")
        assertTrue("firstName column exists in migrated table", firstNameIndex != -1)
        assertTrue("firstName is SQL NULL", cursor.isNull(firstNameIndex))
        assertNull("firstName string value is null", cursor.getString(firstNameIndex))

        // 5. Assert all pre-existing column values are unchanged
        assertEquals("fb_123", cursor.getString(cursor.getColumnIndexOrThrow("firebaseUid")))
        assertEquals("user@example.com", cursor.getString(cursor.getColumnIndexOrThrow("email")))
        assertEquals("lose", cursor.getString(cursor.getColumnIndexOrThrow("goal")))
        assertEquals("metric", cursor.getString(cursor.getColumnIndexOrThrow("units")))
        assertEquals(29, cursor.getInt(cursor.getColumnIndexOrThrow("age")))
        assertEquals(178.0, cursor.getDouble(cursor.getColumnIndexOrThrow("heightCm")), 0.001)
        assertEquals(82.5, cursor.getDouble(cursor.getColumnIndexOrThrow("weightKg")), 0.001)
        assertEquals(75.0, cursor.getDouble(cursor.getColumnIndexOrThrow("goalWeightKg")), 0.001)
        assertEquals("moderate", cursor.getString(cursor.getColumnIndexOrThrow("activityLevel")))
        assertEquals("[\"high_protein\"]", cursor.getString(cursor.getColumnIndexOrThrow("dietTags")))
        assertEquals("[\"gluten\"]", cursor.getString(cursor.getColumnIndexOrThrow("allergies")))
        assertEquals("recommended", cursor.getString(cursor.getColumnIndexOrThrow("targetsSource")))
        assertEquals(1700000000L, cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")))
        assertEquals(1700000000L, cursor.getLong(cursor.getColumnIndexOrThrow("updatedAt")))
        cursor.close()

        // 6. Assert new record with populated firstName can be inserted and queried cleanly in v5
        db.execSQL(
            """
            INSERT INTO user_profile (
                id, firebaseUid, email, firstName, goal, units, age, heightCm, weightKg,
                goalWeightKg, activityLevel, dietTags, allergies, targetsSource,
                createdAt, updatedAt
            ) VALUES (
                2, 'fb_456', 'jordan@example.com', 'Jordan', 'maintain', 'metric', 25, 170.0, 65.0,
                65.0, 'sedentary', '[]', '[]', 'manual',
                1700001000, 1700001000
            )
            """.trimIndent()
        )
        val newCursor = db.query("SELECT firstName FROM user_profile WHERE id = 2")
        assertTrue(newCursor.moveToFirst())
        assertEquals("Jordan", newCursor.getString(0))
        newCursor.close()

        db.close()
    }

    @Test
    fun migrate5To6_preservesDataAndAddsSugarAndSodium() {
        var db = helper.createDatabase(TEST_DB + "_v6", 5).apply {
            execSQL(
                """
                INSERT INTO foods (
                    id, source, externalId, name, brand, servingDescription, servingGrams,
                    calories, proteinG, carbsG, fatG, fiberG, barcodeUpc, isUserCreated, createdAt
                ) VALUES (
                    1, 'USER_ENTERED', NULL, 'Custom Roti', 'Home', '1 piece', 40.0,
                    120.0, 3.5, 24.0, 1.0, 2.0, NULL, 1, 1700000000
                )
                """.trimIndent()
            )
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB + "_v6", 6, true)

        val cursor = db.query("SELECT * FROM foods WHERE id = 1")
        assertTrue("Migrated pre-existing food row exists", cursor.moveToFirst())

        val sugarIndex = cursor.getColumnIndex("sugarG")
        val sodiumIndex = cursor.getColumnIndex("sodiumMg")
        assertTrue("sugarG column exists in migrated table", sugarIndex != -1)
        assertTrue("sodiumMg column exists in migrated table", sodiumIndex != -1)
        assertEquals(0.0, cursor.getDouble(sugarIndex), 0.001)
        assertEquals(0.0, cursor.getDouble(sodiumIndex), 0.001)

        assertEquals("Custom Roti", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals(120.0, cursor.getDouble(cursor.getColumnIndexOrThrow("calories")), 0.001)
        assertEquals(3.5, cursor.getDouble(cursor.getColumnIndexOrThrow("proteinG")), 0.001)
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("isUserCreated")))
        cursor.close()

        db.close()
    }

    @Test
    fun migrate6To7_addsSavedMealsTables() {
        var db = helper.createDatabase(TEST_DB + "_v7", 6).apply {
            execSQL(
                """
                INSERT INTO foods (
                    id, source, externalId, name, brand, servingDescription, servingGrams,
                    calories, proteinG, carbsG, fatG, fiberG, barcodeUpc, isUserCreated, createdAt,
                    sugarG, sodiumMg
                ) VALUES (
                    1, 'USER_ENTERED', NULL, 'Pre-existing Oats', 'BrandX', '1 cup', 80.0,
                    300.0, 10.0, 54.0, 5.0, 8.0, NULL, 1, 1700000000, 1.0, 5.0
                )
                """.trimIndent()
            )
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB + "_v7", 7, true)

        // Verify pre-existing food is preserved
        val foodCursor = db.query("SELECT name, sugarG, sodiumMg FROM foods WHERE id = 1")
        assertTrue("Pre-existing food exists", foodCursor.moveToFirst())
        assertEquals("Pre-existing Oats", foodCursor.getString(0))
        assertEquals(1.0, foodCursor.getDouble(1), 0.001)
        assertEquals(5.0, foodCursor.getDouble(2), 0.001)
        foodCursor.close()

        // Verify saved_meals table exists and accepts inserts
        db.execSQL(
            """
            INSERT INTO saved_meals (id, name, defaultMealType, createdAt, updatedAt)
            VALUES (1, 'Morning Power Oatmeal', 'BREAKFAST', 1700000000, 1700000000)
            """.trimIndent()
        )
        val mealCursor = db.query("SELECT name, defaultMealType FROM saved_meals WHERE id = 1")
        assertTrue("Inserted saved meal exists", mealCursor.moveToFirst())
        assertEquals("Morning Power Oatmeal", mealCursor.getString(0))
        assertEquals("BREAKFAST", mealCursor.getString(1))
        mealCursor.close()

        // Verify saved_meal_items table exists and accepts inserts with FK
        db.execSQL(
            """
            INSERT INTO saved_meal_items (
                id, savedMealId, foodId, name, brand, servingDescription, servingGrams,
                calories, proteinG, carbsG, fatG, fiberG, sugarG, sodiumMg, source
            ) VALUES (
                1, 1, 1, 'Rolled Oats', 'Quaker', '1 cup', 80.0,
                300.0, 10.0, 54.0, 5.0, 8.0, 1.0, 5.0, 'USER_ENTERED'
            )
            """.trimIndent()
        )
        val itemCursor = db.query("SELECT name, calories, proteinG FROM saved_meal_items WHERE id = 1")
        assertTrue("Inserted saved meal item exists", itemCursor.moveToFirst())
        assertEquals("Rolled Oats", itemCursor.getString(0))
        assertEquals(300.0, itemCursor.getDouble(1), 0.001)
        assertEquals(10.0, itemCursor.getDouble(2), 0.001)
        itemCursor.close()

        db.close()
    }
}
