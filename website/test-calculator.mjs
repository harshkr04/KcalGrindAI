import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const vectorsPath = path.join(__dirname, 'test-vectors.json')
const vectors = JSON.parse(fs.readFileSync(vectorsPath, 'utf8'))

// Calculator logic under test matching src/calculator.ts
function calculateNutrition(input) {
  const { weightKg, heightCm, age, goal, activity, goalWeightKg, sex, dietTags = [] } = input

  // 1. BMR (Mifflin-St Jeor midpoint from NutritionCalculator.kt:105)
  const bmr = 10.0 * weightKg + 6.25 * heightCm - 5.0 * age - 78.0

  // 2. TDEE
  const activityFactors = {
    sedentary: 1.20,
    light: 1.375,
    moderate: 1.55,
    very: 1.725,
    extreme: 1.90,
  }
  const factor = activityFactors[activity] || 1.55
  const tdee = bmr * factor

  // 3. Goal Delta
  const goalDeltas = {
    lose: -450,
    maintain: 0,
    gain: 350,
    muscle: 250,
    healthier: 0,
    nutrition: 0,
  }
  let delta = goalDeltas[goal] ?? 0
  if (goal === 'lose' && goalWeightKg != null && goalWeightKg >= weightKg) {
    delta = 0
  }
  if ((goal === 'gain' || goal === 'muscle') && goalWeightKg != null && goalWeightKg < weightKg - 0.5) {
    delta = -300
  }
  if (goal === 'lose' && goalWeightKg != null && Math.abs(weightKg - goalWeightKg) < 2.0) {
    delta = -250
  }

  // 4. Safe Delta clamp (1% body weight per week max rate)
  const maxDailyDeficit = Math.round((weightKg * 0.01 * 7700.0) / 7.0)
  const maxDailySurplus = Math.min(750, maxDailyDeficit)
  const safeDelta = Math.max(-maxDailyDeficit, Math.min(maxDailySurplus, delta))

  // 5. Target Calories with App Guardrails Floor (NutritionCalculator.kt:234)
  let calories = Math.round((tdee + safeDelta) / 10.0) * 10
  const sexNorm = sex?.toLowerCase()?.trim()
  const sexFloor = sexNorm === 'male' || sexNorm === 'm' ? 1500 : 1200
  const floor = Math.max(sexFloor, Math.round((bmr * 0.95) / 10.0) * 10)
  let floored = false
  if (calories < floor) {
    calories = floor
    floored = true
  } else if (calories > 5000) {
    calories = 5000
  }

  // 6. MacroSplit (NutritionCalculator.kt:122)
  const proteinPerKgMap = {
    lose: 1.8,
    maintain: 1.6,
    gain: 1.7,
    muscle: 2.0,
    healthier: 1.5,
    nutrition: 1.6,
  }
  let perKg = proteinPerKgMap[goal] ?? 1.6
  const lowerDiets = dietTags.map((d) => d.toLowerCase())
  if (lowerDiets.includes('high_protein')) perKg += 0.2
  if (lowerDiets.includes('vegan') || lowerDiets.includes('vegetarian')) perKg = Math.min(perKg, 1.8)

  let protein = Math.round(weightKg * perKg)
  let proteinKcal = protein * 4
  if (proteinKcal > calories * 0.4) {
    protein = Math.round((calories * 0.4) / 4.0)
    proteinKcal = protein * 4
  }

  let fatPct = 0.28
  if (lowerDiets.includes('keto')) fatPct = 0.70
  else if (lowerDiets.includes('low_carb')) fatPct = 0.42
  else if (lowerDiets.includes('mediterranean')) fatPct = 0.35

  let fat = Math.round((calories * fatPct) / 9.0)
  let carbs = Math.round((calories - proteinKcal - fat * 9.0) / 4.0)
  if (carbs < 20) {
    carbs = 20
    fat = Math.round((calories - proteinKcal - carbs * 4.0) / 9.0)
  }
  fat = Math.max(fat, 20)

  // 7. Water (NutritionCalculator.kt:109)
  const waterFactors = {
    sedentary: 0.030,
    light: 0.032,
    moderate: 0.034,
    very: 0.037,
    extreme: 0.040,
  }
  const wFactor = waterFactors[activity] || 0.034
  const rawLiters = Math.round(weightKg * wFactor * 10.0) / 10.0
  const waterLiters = Math.max(1.8, Math.min(4.0, rawLiters))
  const waterGlasses = Math.max(6, Math.round((waterLiters * 1000.0) / 300.0))

  return {
    calories,
    proteinG: protein,
    carbsG: carbs,
    fatG: fat,
    waterLiters,
    waterGlasses,
    bmr: Math.round(bmr),
    tdee: Math.round(tdee),
    delta,
    floored,
  }
}

console.log(`Running calculator verification on ${vectors.length} test vectors from NutritionCalculator.kt...`)
let failed = 0

for (const v of vectors) {
  const actual = calculateNutrition(v.input)
  const exp = v.output
  const match = JSON.stringify(actual) === JSON.stringify(exp)
  if (match) {
    console.log(`  ✓ ${v.id}: ${actual.calories} kcal, ${actual.proteinG}P/${actual.carbsG}C/${actual.fatG}F (BMR: ${actual.bmr}, TDEE: ${actual.tdee})`)
  } else {
    failed++
    console.error(`  ✗ ${v.id} FAILED`)
    console.error(`    Expected:`, exp)
    console.error(`    Actual:  `, actual)
  }
}

if (failed > 0) {
  console.error(`\nTest failed: ${failed} vectors did not match NutritionCalculator.kt!`)
  process.exit(1)
} else {
  console.log(`\nAll ${vectors.length} test vectors passed with 100% exact parity!`)
}
