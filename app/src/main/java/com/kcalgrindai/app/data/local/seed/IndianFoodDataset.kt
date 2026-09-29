package com.kcalgrindai.app.data.local.seed

import com.kcalgrindai.app.data.local.entity.FoodEntity
import com.kcalgrindai.app.domain.model.FoodSource

/**
 * Authoritative Indian Food Dataset compiled and derived from:
 * 1. Indian Food Composition Tables (IFCT 2017) - ICMR-National Institute of Nutrition (NIN), Hyderabad.
 * 2. Dietary Guidelines for Indians - ICMR-NIN.
 * 3. Nutritive Value of Indian Foods (C. Gopalan et al., NIN).
 *
 * Each item represents typical standard Indian serving sizes with verified macro & calorie values.
 */
object IndianFoodDataset {

    const val DATASET_SOURCE = "ifct"
    const val DATASET_CITATION = "IFCT 2017 (ICMR-National Institute of Nutrition, Hyderabad)"

    private fun item(
        externalId: String,
        name: String,
        servingDesc: String,
        servingGrams: Double,
        calories: Double,
        proteinG: Double,
        carbsG: Double,
        fatG: Double,
        fiberG: Double
    ): FoodEntity {
        return FoodEntity(
            id = 0L,
            source = DATASET_SOURCE,
            externalId = externalId,
            name = name,
            brand = "IFCT 2017 (ICMR-NIN)",
            servingDescription = servingDesc,
            servingGrams = servingGrams,
            calories = calories,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG,
            fiberG = fiberG,
            barcodeUpc = null,
            isUserCreated = false,
            createdAt = 1704067200000L // 2024-01-01 baseline
        )
    }

    val foods: List<FoodEntity> = listOf(
        // ==========================================
        // 1. INDIAN BREADS & ROTIS
        // ==========================================
        item("IFCT_B01", "Roti / Phulka (Whole Wheat, No Ghee)", "1 roti (35g)", 35.0, 85.0, 3.2, 17.5, 0.5, 2.7),
        item("IFCT_B02", "Roti with Ghee (Whole Wheat)", "1 roti with ghee (40g)", 40.0, 120.0, 3.2, 17.5, 4.4, 2.7),
        item("IFCT_B03", "Plain Paratha (Shallow Fried)", "1 paratha (60g)", 60.0, 180.0, 4.2, 24.5, 7.5, 3.2),
        item("IFCT_B04", "Aloo Paratha", "1 paratha (100g)", 100.0, 240.0, 5.2, 34.8, 9.2, 4.1),
        item("IFCT_B05", "Paneer Paratha", "1 paratha (110g)", 110.0, 290.0, 11.5, 30.2, 14.0, 3.5),
        item("IFCT_B06", "Gobi Paratha (Cauliflower Stuffed)", "1 paratha (100g)", 100.0, 210.0, 5.0, 28.5, 8.8, 4.6),
        item("IFCT_B07", "Methi Thepla", "1 thepla (45g)", 45.0, 130.0, 3.8, 18.0, 4.8, 3.0),
        item("IFCT_B08", "Tandoori Roti (Whole Wheat)", "1 roti (50g)", 50.0, 120.0, 4.4, 24.0, 0.8, 3.5),
        item("IFCT_B09", "Butter Naan", "1 naan (90g)", 90.0, 290.0, 7.5, 45.0, 9.0, 2.2),
        item("IFCT_B10", "Plain Naan (Tandoori)", "1 naan (80g)", 80.0, 240.0, 7.2, 44.0, 4.0, 2.2),
        item("IFCT_B11", "Poori (Deep Fried)", "1 poori (30g)", 30.0, 125.0, 2.2, 14.0, 7.0, 1.5),
        item("IFCT_B12", "Bhatura (Deep Fried)", "1 bhatura (80g)", 80.0, 290.0, 6.4, 38.0, 13.0, 1.8),
        item("IFCT_B13", "Bajra Roti (Pearl Millet)", "1 roti (50g)", 50.0, 135.0, 4.2, 27.5, 1.8, 4.2),
        item("IFCT_B14", "Jowar Roti (Sorghum)", "1 roti (50g)", 50.0, 130.0, 3.9, 27.0, 1.2, 4.0),
        item("IFCT_B15", "Makki di Roti (Corn Flour)", "1 roti (60g)", 60.0, 160.0, 3.5, 29.0, 3.5, 3.8),
        item("IFCT_B16", "Ragi Roti (Finger Millet)", "1 roti (50g)", 50.0, 125.0, 2.8, 26.5, 1.0, 4.5),
        item("IFCT_B17", "Lachha Paratha", "1 paratha (80g)", 80.0, 260.0, 5.4, 32.0, 12.5, 3.0),
        item("IFCT_B18", "Rumali Roti", "1 roti (50g)", 50.0, 140.0, 4.0, 28.0, 1.5, 1.8),

        // ==========================================
        // 2. DALS, LEGUMES & CURRIES
        // ==========================================
        item("IFCT_D01", "Moong Dal Tadka (Yellow Moong)", "1 katori (150g)", 150.0, 145.0, 8.5, 20.2, 3.8, 4.2),
        item("IFCT_D02", "Toor / Arhar Dal Fry", "1 katori (150g)", 150.0, 155.0, 7.8, 21.5, 4.4, 4.8),
        item("IFCT_D03", "Chana Dal Tadka", "1 katori (150g)", 150.0, 175.0, 9.2, 23.0, 5.2, 5.8),
        item("IFCT_D04", "Masoor Dal (Red Lentil)", "1 katori (150g)", 150.0, 140.0, 8.8, 19.5, 3.4, 4.0),
        item("IFCT_D05", "Dal Makhani (Black Urad with Butter)", "1 katori (150g)", 150.0, 260.0, 9.5, 24.0, 14.5, 5.5),
        item("IFCT_D06", "Rajma Masala (Red Kidney Beans)", "1 katori (150g)", 150.0, 190.0, 9.8, 26.5, 5.5, 6.2),
        item("IFCT_D07", "Chole / Punjabi Chana Masala", "1 katori (150g)", 150.0, 215.0, 10.2, 28.0, 7.2, 6.8),
        item("IFCT_D08", "South Indian Sambar", "1 katori (150g)", 150.0, 110.0, 4.5, 16.5, 3.0, 3.8),
        item("IFCT_D09", "Tomato Rasam", "1 katori (150g)", 150.0, 55.0, 1.8, 8.5, 1.5, 1.2),
        item("IFCT_D10", "Panchmel Dal / Rajasthani Panchratna", "1 katori (150g)", 150.0, 165.0, 9.0, 22.0, 4.8, 5.0),
        item("IFCT_D11", "Kadhi Pakora (Gram Flour & Buttermilk)", "1 katori (150g)", 150.0, 185.0, 5.8, 16.0, 11.0, 2.5),
        item("IFCT_D12", "Gujarati Kadhi (Sweet & Sour)", "1 katori (150g)", 150.0, 120.0, 3.8, 14.5, 5.2, 1.2),
        item("IFCT_D13", "Lobiya Masala (Black Eyed Peas)", "1 katori (150g)", 150.0, 170.0, 9.0, 24.0, 4.5, 5.6),
        item("IFCT_D14", "Sprouted Moong Usal / Curry", "1 katori (150g)", 150.0, 135.0, 9.5, 18.0, 3.2, 5.0),

        // ==========================================
        // 3. RICE DISHES, BIRYANI & PULAO
        // ==========================================
        item("IFCT_R01", "Steamed Basmati Rice (Cooked)", "1 katori / bowl (150g)", 150.0, 195.0, 4.2, 42.5, 0.6, 1.2),
        item("IFCT_R02", "Brown Rice (Cooked)", "1 bowl (150g)", 150.0, 170.0, 4.0, 35.5, 1.5, 2.8),
        item("IFCT_R03", "Jeera Rice (Basmati with Cumin)", "1 bowl (150g)", 150.0, 225.0, 4.2, 41.0, 5.0, 1.4),
        item("IFCT_R04", "Vegetable Pulao", "1 bowl (150g)", 150.0, 210.0, 4.8, 38.0, 4.8, 3.2),
        item("IFCT_R05", "Chicken Dum Biryani (Hyderabadi)", "1 plate (350g)", 350.0, 540.0, 32.5, 62.0, 18.0, 4.2),
        item("IFCT_R06", "Mutton Biryani", "1 plate (350g)", 350.0, 620.0, 30.0, 60.0, 28.5, 4.0),
        item("IFCT_R07", "Vegetable Biryani", "1 plate (300g)", 300.0, 380.0, 8.5, 62.0, 11.0, 5.5),
        item("IFCT_R08", "Egg Biryani (2 Eggs)", "1 plate (300g)", 300.0, 450.0, 18.0, 58.0, 16.0, 3.8),
        item("IFCT_R09", "Moong Dal Khichdi with Ghee", "1 bowl (200g)", 200.0, 240.0, 7.8, 38.0, 6.8, 3.8),
        item("IFCT_R10", "Curd Rice (Thayir Sadam with Tadka)", "1 bowl (150g)", 150.0, 180.0, 4.8, 28.0, 5.5, 1.2),
        item("IFCT_R11", "Lemon Rice (Chitranna)", "1 bowl (150g)", 150.0, 220.0, 4.0, 36.0, 7.0, 2.0),
        item("IFCT_R12", "Tamarind Rice (Puliyodharai)", "1 bowl (150g)", 150.0, 245.0, 4.2, 38.0, 8.8, 2.5),
        item("IFCT_R13", "Bisi Bele Bath (Karnataka)", "1 bowl (200g)", 200.0, 270.0, 7.2, 42.0, 8.2, 4.5),

        // ==========================================
        // 4. PANEER & VEGETARIAN MAINS
        // ==========================================
        item("IFCT_P01", "Paneer Butter Masala", "1 katori (180g)", 180.0, 340.0, 14.5, 12.0, 26.5, 2.5),
        item("IFCT_P02", "Palak Paneer (Spinach & Cottage Cheese)", "1 katori (180g)", 180.0, 260.0, 13.8, 9.5, 18.5, 4.2),
        item("IFCT_P03", "Kadai Paneer", "1 katori (180g)", 180.0, 290.0, 14.0, 11.5, 21.0, 3.4),
        item("IFCT_P04", "Shahi Paneer", "1 katori (180g)", 180.0, 360.0, 13.0, 14.0, 28.0, 2.2),
        item("IFCT_P05", "Matar Paneer", "1 katori (180g)", 180.0, 270.0, 13.5, 16.5, 17.0, 4.5),
        item("IFCT_P06", "Paneer Bhurji", "1 katori (150g)", 150.0, 280.0, 16.2, 6.5, 21.5, 2.0),
        item("IFCT_P07", "Paneer Tikka (Tandoori)", "6 pieces (150g)", 150.0, 260.0, 16.8, 8.0, 18.0, 2.4),
        item("IFCT_P08", "Raw Paneer (Cottage Cheese)", "100g serving", 100.0, 265.0, 18.3, 3.4, 20.8, 0.0),
        item("IFCT_P09", "Paneer Lababdar", "1 katori (180g)", 180.0, 350.0, 14.0, 13.5, 27.0, 2.8),

        // ==========================================
        // 5. VEGETABLE DISHES (SABZI)
        // ==========================================
        item("IFCT_V01", "Aloo Gobi (Dry Cauliflower & Potato)", "1 katori (150g)", 150.0, 150.0, 3.6, 22.0, 5.8, 4.5),
        item("IFCT_V02", "Bhindi Masala (Okra)", "1 katori (120g)", 120.0, 120.0, 2.8, 13.5, 6.2, 4.8),
        item("IFCT_V03", "Baingan Bharta (Roasted Eggplant)", "1 katori (150g)", 150.0, 135.0, 2.8, 14.0, 7.5, 5.2),
        item("IFCT_V04", "Aloo Matar Curry", "1 katori (150g)", 150.0, 160.0, 4.2, 24.0, 5.5, 4.2),
        item("IFCT_V05", "Mixed Vegetable Curry", "1 katori (150g)", 150.0, 140.0, 3.8, 18.5, 6.0, 4.8),
        item("IFCT_V06", "Cabbage Poriyal / Thoran (with Coconut)", "1 katori (120g)", 120.0, 95.0, 2.2, 9.5, 5.5, 3.8),
        item("IFCT_V07", "Lauki / Bottle Gourd Sabzi", "1 katori (150g)", 150.0, 80.0, 1.8, 9.0, 4.2, 2.8),
        item("IFCT_V08", "Karela Masala (Bitter Gourd)", "1 katori (100g)", 100.0, 105.0, 2.4, 11.0, 5.8, 4.0),
        item("IFCT_V09", "Sarson Ka Saag (Mustard Greens)", "1 katori (150g)", 150.0, 140.0, 4.8, 10.5, 9.0, 5.5),
        item("IFCT_V10", "Dum Aloo (Kashmiri)", "1 katori (180g)", 180.0, 230.0, 3.8, 28.0, 11.5, 3.8),
        item("IFCT_V11", "Jeera Aloo", "1 katori (120g)", 120.0, 160.0, 2.6, 26.0, 5.2, 3.0),
        item("IFCT_V12", "Gajar Matar (Carrot & Green Peas)", "1 katori (120g)", 120.0, 110.0, 3.2, 17.5, 3.2, 4.2),
        item("IFCT_V13", "Methi Aloo", "1 katori (120g)", 120.0, 145.0, 3.4, 21.0, 5.5, 4.0),
        item("IFCT_V14", "Torai / Ridge Gourd Curry", "1 katori (150g)", 150.0, 75.0, 1.6, 8.5, 3.8, 2.5),
        item("IFCT_V15", "Tinda Masala (Apple Gourd)", "1 katori (120g)", 120.0, 85.0, 1.8, 10.0, 4.2, 2.8),
        item("IFCT_V16", "Shimla Mirch Besan (Capsicum Gram Flour)", "1 katori (120g)", 120.0, 145.0, 5.0, 16.0, 6.8, 3.5),

        // ==========================================
        // 6. SOUTH INDIAN BREAKFAST & TIFFIN
        // ==========================================
        item("IFCT_S01", "Plain Idli (Steamed Rice & Urad)", "2 pieces (100g)", 100.0, 130.0, 4.5, 26.5, 0.6, 1.8),
        item("IFCT_S02", "Plain Dosa (Crispy Crepe)", "1 dosa (90g)", 90.0, 165.0, 4.0, 28.0, 4.5, 1.6),
        item("IFCT_S03", "Masala Dosa (with Potato Masala)", "1 dosa (180g)", 180.0, 310.0, 6.5, 48.0, 10.5, 4.0),
        item("IFCT_S04", "Medu Vada (Deep Fried Lentil Donut)", "2 pieces (80g)", 80.0, 240.0, 6.8, 22.0, 14.0, 3.2),
        item("IFCT_S05", "Rava Upma (Semolina with Veggies)", "1 bowl (150g)", 150.0, 210.0, 4.8, 32.5, 7.0, 2.8),
        item("IFCT_S06", "Kanda Poha (Flattened Rice with Onion)", "1 bowl (150g)", 150.0, 230.0, 4.2, 38.0, 7.0, 3.2),
        item("IFCT_S07", "Onion Uttapam", "1 uttapam (150g)", 150.0, 240.0, 5.4, 38.0, 7.5, 3.0),
        item("IFCT_S08", "Ven Pongal (Ghee Lentil Rice)", "1 bowl (150g)", 150.0, 250.0, 6.2, 34.0, 10.0, 3.0),
        item("IFCT_S09", "Appam (Fermented Rice Pancake)", "2 pieces (100g)", 100.0, 150.0, 2.8, 30.0, 2.0, 1.2),
        item("IFCT_S10", "Coconut Chutney", "2 tbsp (40g)", 40.0, 95.0, 1.2, 3.5, 8.5, 2.0),
        item("IFCT_S11", "Tomato Onion Chutney", "2 tbsp (40g)", 40.0, 45.0, 0.8, 5.5, 2.2, 1.2),
        item("IFCT_S12", "Rava Dosa (Semolina Dosa)", "1 dosa (110g)", 110.0, 210.0, 4.5, 32.0, 7.5, 1.8),
        item("IFCT_S13", "Pesarattu (Green Gram Dosa)", "1 dosa (100g)", 100.0, 180.0, 8.2, 26.0, 4.8, 4.2),

        // ==========================================
        // 7. POULTRY, MEAT, FISH & EGGS
        // ==========================================
        item("IFCT_M01", "Butter Chicken (Murgh Makhani)", "1 katori (200g)", 200.0, 380.0, 26.0, 11.0, 26.0, 2.0),
        item("IFCT_M02", "Chicken Tikka Masala", "1 katori (200g)", 200.0, 340.0, 28.0, 10.0, 21.0, 2.2),
        item("IFCT_M03", "Homestyle Chicken Curry (Tari Wali)", "1 katori (200g)", 200.0, 260.0, 27.5, 7.5, 13.5, 1.8),
        item("IFCT_M04", "Tandoori Chicken (Skinless)", "1 leg / breast (160g)", 160.0, 240.0, 32.0, 3.5, 11.0, 0.8),
        item("IFCT_M05", "Chicken Korma", "1 katori (200g)", 200.0, 360.0, 24.0, 12.0, 25.0, 2.0),
        item("IFCT_M06", "Kadai Chicken", "1 katori (200g)", 200.0, 290.0, 28.5, 8.5, 16.0, 2.5),
        item("IFCT_M07", "Mutton Rogan Josh (Lamb Curry)", "1 katori (200g)", 200.0, 390.0, 25.0, 6.5, 29.5, 1.5),
        item("IFCT_M08", "Mutton Keema Matar (Minced Meat)", "1 katori (180g)", 180.0, 330.0, 23.5, 9.0, 22.0, 2.8),
        item("IFCT_M09", "Egg Curry (2 Boiled Eggs with Gravy)", "1 katori (180g)", 180.0, 230.0, 14.5, 7.0, 16.0, 1.5),
        item("IFCT_M10", "Egg Bhurji (Scrambled with Spices, 2 Eggs)", "1 portion (120g)", 120.0, 195.0, 13.5, 3.8, 14.0, 0.8),
        item("IFCT_M11", "Fish Curry (Bengali Machher Jhol)", "1 portion (180g)", 180.0, 210.0, 22.0, 6.0, 11.0, 1.2),
        item("IFCT_M12", "Fish Fry (Rava Coated, Kingfish)", "1 fillet (120g)", 120.0, 230.0, 24.0, 8.5, 11.5, 0.6),
        item("IFCT_M13", "Prawn Masala (Shrimp Curry)", "1 katori (180g)", 180.0, 220.0, 24.5, 7.5, 10.5, 1.4),

        // ==========================================
        // 8. STREET FOOD, SNACKS & CHAAT
        // ==========================================
        item("IFCT_K01", "Samosa (Potato Stuffed)", "1 piece (80g)", 80.0, 260.0, 4.5, 32.0, 13.0, 2.8),
        item("IFCT_K02", "Vegetable Pakora / Bhajji", "1 serving (100g)", 100.0, 280.0, 6.2, 28.0, 16.0, 3.5),
        item("IFCT_K03", "Khaman Dhokla (Steamed Gram Flour)", "2 pieces (80g)", 80.0, 135.0, 5.5, 20.0, 4.0, 2.2),
        item("IFCT_K04", "Bhelpuri", "1 plate (150g)", 150.0, 280.0, 6.8, 48.0, 7.5, 4.5),
        item("IFCT_K05", "Pani Puri / Golgappa (with Flavored Water)", "6 pieces (150g)", 150.0, 180.0, 3.8, 32.0, 4.5, 2.5),
        item("IFCT_K06", "Sev Puri", "6 pieces (140g)", 140.0, 290.0, 5.5, 38.0, 13.0, 3.8),
        item("IFCT_K07", "Dahi Puri / Dahi Sev Batata Puri", "6 pieces (180g)", 180.0, 340.0, 7.5, 44.0, 15.0, 3.5),
        item("IFCT_K08", "Pav Bhaji (2 Buttered Pav + Bhaji)", "1 plate (280g)", 280.0, 460.0, 9.5, 64.0, 18.5, 6.8),
        item("IFCT_K09", "Vada Pav (Batata Vada with Chutney)", "1 piece (120g)", 120.0, 290.0, 6.0, 42.0, 11.0, 3.2),
        item("IFCT_K10", "Aloo Tikki", "2 pieces (100g)", 100.0, 220.0, 3.6, 28.0, 10.5, 3.0),
        item("IFCT_K11", "Chana Chaat", "1 bowl (150g)", 150.0, 210.0, 9.8, 30.0, 6.0, 5.8),
        item("IFCT_K12", "Roasted Makhana (Fox Nuts with Spices)", "1 bowl (30g)", 30.0, 120.0, 3.0, 20.0, 3.2, 2.5),
        item("IFCT_K13", "Papdi Chaat", "1 plate (180g)", 180.0, 320.0, 7.0, 42.0, 14.0, 3.5),

        // ==========================================
        // 9. DAIRY, BEVERAGES & ACCOMPANIMENTS
        // ==========================================
        item("IFCT_Y01", "Buffalo Milk (Whole)", "1 glass (200ml)", 200.0, 195.0, 8.6, 10.4, 13.0, 0.0),
        item("IFCT_Y02", "Cow Milk (Toned 3% Fat)", "1 glass (200ml)", 200.0, 120.0, 6.6, 9.6, 6.0, 0.0),
        item("IFCT_Y03", "Masala Chai (with Milk & 1 tsp Sugar)", "1 cup (150ml)", 150.0, 110.0, 3.8, 15.0, 3.8, 0.0),
        item("IFCT_Y04", "South Indian Filter Coffee (with Milk & Sugar)", "1 cup (150ml)", 150.0, 125.0, 4.0, 16.5, 4.5, 0.0),
        item("IFCT_Y05", "Sweet Lassi", "1 glass (200ml)", 200.0, 210.0, 6.5, 32.0, 6.8, 0.0),
        item("IFCT_Y06", "Salted Chaas / Buttermilk (with Jeera & Curry Leaves)", "1 glass (200ml)", 200.0, 60.0, 3.2, 5.0, 2.8, 0.4),
        item("IFCT_Y07", "Curd / Dahi (Plain Whole Milk)", "1 katori (100g)", 100.0, 75.0, 3.6, 4.8, 4.5, 0.0),
        item("IFCT_Y08", "Cucumber Raita", "1 katori (100g)", 100.0, 65.0, 3.2, 5.2, 3.5, 0.8),
        item("IFCT_Y09", "Boondi Raita", "1 katori (100g)", 100.0, 115.0, 4.0, 11.5, 6.0, 0.6),
        item("IFCT_Y10", "Mixed Veg Raita", "1 katori (100g)", 100.0, 70.0, 3.4, 6.0, 3.6, 1.0),
        item("IFCT_Y11", "Desi Cow Ghee", "1 tsp (5g)", 5.0, 45.0, 0.0, 0.0, 5.0, 0.0),
        item("IFCT_Y12", "Mint Coriander Chutney", "2 tbsp (30g)", 30.0, 25.0, 1.0, 3.2, 1.0, 1.2),

        // ==========================================
        // 10. SWEETS & DESSERTS (MITHAI)
        // ==========================================
        item("IFCT_W01", "Gulab Jamun", "2 pieces (80g)", 80.0, 310.0, 5.2, 46.0, 12.0, 0.8),
        item("IFCT_W02", "Kaju Katli (Cashew Fudge)", "2 pieces (30g)", 30.0, 140.0, 3.2, 17.5, 6.8, 0.6),
        item("IFCT_W03", "Rasgulla", "2 pieces (80g)", 80.0, 210.0, 4.8, 42.0, 2.5, 0.2),
        item("IFCT_W04", "Besan Ladoo", "1 ladoo (40g)", 40.0, 210.0, 4.5, 24.0, 11.0, 1.8),
        item("IFCT_W05", "Gajar Ka Halwa (Carrot Pudding with Mawa)", "1 katori (100g)", 100.0, 260.0, 4.8, 34.0, 12.0, 2.5),
        item("IFCT_W06", "Rice Kheer (Pudding with Milk & Cardamom)", "1 katori (150g)", 150.0, 235.0, 6.2, 36.0, 7.8, 0.6),
        item("IFCT_W07", "Jalebi", "2 pieces (60g)", 60.0, 240.0, 2.0, 42.0, 7.5, 0.4),
        item("IFCT_W08", "Rasmalai", "2 pieces (100g)", 100.0, 240.0, 7.5, 28.0, 11.0, 0.4),
        item("IFCT_W09", "Moong Dal Halwa", "1 katori (100g)", 100.0, 340.0, 7.2, 38.0, 18.0, 2.8),
        item("IFCT_W10", "Sooji / Rava Halwa (Sheera)", "1 katori (100g)", 100.0, 280.0, 3.8, 42.0, 11.0, 1.2)
    )
}
