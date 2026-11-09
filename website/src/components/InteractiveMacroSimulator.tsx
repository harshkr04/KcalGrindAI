import React, { useState } from 'react'
import { Calculator, AlertCircle, ShieldCheck } from 'lucide-react'
import { APP_CONFIG } from '../config'
import { calculateNutrition, ACTIVITY_FACTORS } from '../calculator'

type GoalTab = 'lose' | 'maintain' | 'muscle'
type SexOption = 'female' | 'male' | 'unspecified'
type ActivityOption = 'sedentary' | 'light' | 'moderate' | 'very' | 'extreme'

export const InteractiveMacroSimulator: React.FC = () => {
  const [goal, setGoal] = useState<GoalTab>('lose')
  const [sex, setSex] = useState<SexOption>('male')
  const [age, setAge] = useState<number>(28)
  const [heightCm, setHeightCm] = useState<number>(175)
  const [weightKg, setWeightKg] = useState<number>(75)
  const [activity, setActivity] = useState<ActivityOption>('moderate')

  // Calculated strictly using the app's real formula in NutritionCalculator.kt
  const result = calculateNutrition({
    weightKg,
    heightCm,
    age,
    goal,
    activity,
    sex: sex === 'unspecified' ? null : sex,
  })

  const {
    proteinG: proteinGrams,
    carbsG: carbsGrams,
    fatG: fatGrams,
    waterLiters,
    bmr,
    tdee,
    delta,
    floored: floorApplied,
  } = result

  // Reconciled calorie check
  const reconciledCalories = proteinGrams * 4 + carbsGrams * 4 + fatGrams * 9
  const sexFloor = sex === 'male' ? 1500 : 1200

  return (
    <section id="macro-simulator" className="w-full py-24 sm:py-28 lg:py-32 text-left">
      <div className="w-full max-w-[1440px] mx-auto px-6 sm:px-8 lg:px-12">
        
        {/* Section Header */}
        <div className="max-w-3xl mb-12 space-y-3">
          <div className="inline-flex items-center gap-2">
            <span className="hairline-tag">
              <Calculator className="w-4 h-4 text-[var(--v3-accent)] stroke-[1.5]" />
              <span>Target calculator</span>
            </span>
          </div>

          <h2 className="font-medium tracking-tight text-[var(--v3-text-primary)]">
            Daily calorie and macro calculator
          </h2>

          <p className="text-[17px] sm:text-[18px] text-[var(--v3-text-secondary)] leading-relaxed font-normal">
            An estimate based on Mifflin-St Jeor and activity level. Weekly change is limited to about 1% of body weight, with baseline minimums of 1,200 kcal per day (1,500 for men).
          </p>
        </div>

        {/* Calculator Main Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-start">
          
          {/* Left Form: Inputs */}
          <div className="lg:col-span-7 v3-card p-6 sm:p-8 space-y-6">
            
            {/* Goal Tabs: "Lose weight" / "Maintain" / "Build muscle" */}
            <div className="space-y-2">
              <label className="text-sm font-medium text-[var(--v3-text-primary)]">
                Select your fitness goal
              </label>
              <div className="grid grid-cols-3 gap-2">
                <button
                  type="button"
                  onClick={() => setGoal('lose')}
                  className={`py-3 px-3 rounded-xl text-sm font-medium transition-colors cursor-pointer text-center ${
                    goal === 'lose'
                      ? 'bg-[var(--v3-surface-secondary)] text-[var(--v3-accent)] border border-[var(--v3-accent)]'
                      : 'bg-[var(--v3-surface-elevated)] text-[var(--v3-text-secondary)] border border-[var(--v3-border)] hover:text-[var(--v3-text-primary)]'
                  }`}
                >
                  Lose weight
                </button>
                <button
                  type="button"
                  onClick={() => setGoal('maintain')}
                  className={`py-3 px-3 rounded-xl text-sm font-medium transition-colors cursor-pointer text-center ${
                    goal === 'maintain'
                      ? 'bg-[var(--v3-surface-secondary)] text-[var(--v3-accent)] border border-[var(--v3-accent)]'
                      : 'bg-[var(--v3-surface-elevated)] text-[var(--v3-text-secondary)] border border-[var(--v3-border)] hover:text-[var(--v3-text-primary)]'
                  }`}
                >
                  Maintain
                </button>
                <button
                  type="button"
                  onClick={() => setGoal('muscle')}
                  className={`py-3 px-3 rounded-xl text-sm font-medium transition-colors cursor-pointer text-center ${
                    goal === 'muscle'
                      ? 'bg-[var(--v3-surface-secondary)] text-[var(--v3-accent)] border border-[var(--v3-accent)]'
                      : 'bg-[var(--v3-surface-elevated)] text-[var(--v3-text-secondary)] border border-[var(--v3-border)] hover:text-[var(--v3-text-primary)]'
                  }`}
                >
                  Build muscle
                </button>
              </div>
            </div>

            {/* Sex Selection */}
            <div className="space-y-2">
              <label className="text-sm font-medium text-[var(--v3-text-primary)]">
                Sex (for app calorie floor guardrails)
              </label>
              <div className="grid grid-cols-3 gap-2">
                {(['female', 'male', 'unspecified'] as const).map((s) => (
                  <button
                    key={s}
                    type="button"
                    onClick={() => setSex(s)}
                    className={`py-2 px-3 rounded-xl text-xs sm:text-sm font-medium capitalize transition-colors cursor-pointer text-center ${
                      sex === s
                        ? 'bg-[var(--v3-surface-secondary)] text-[var(--v3-accent)] border border-[var(--v3-accent)]'
                        : 'bg-[var(--v3-surface-elevated)] text-[var(--v3-text-secondary)] border border-[var(--v3-border)] hover:text-[var(--v3-text-primary)]'
                    }`}
                  >
                    {s}
                  </button>
                ))}
              </div>
            </div>

            {/* Sliders: Age, Weight, Height */}
            <div className="space-y-5 pt-2">
              {/* Age */}
              <div className="space-y-1.5">
                <div className="flex justify-between text-sm">
                  <span className="text-[var(--v3-text-secondary)]">Age</span>
                  <span className="font-mono font-semibold text-[var(--v3-text-primary)]">{age} years</span>
                </div>
                <input
                  type="range"
                  min={18}
                  max={80}
                  value={age}
                  onChange={(e) => setAge(Number(e.target.value))}
                  className="w-full h-1.5 rounded bg-[var(--v3-progress-track)] accent-[var(--v3-accent)] cursor-pointer"
                  aria-label="Age slider"
                />
                <div className="flex justify-between text-[11px] font-mono text-[var(--v3-text-muted)]">
                  <span>18y</span>
                  <span>50y</span>
                  <span>80y</span>
                </div>
              </div>

              {/* Weight */}
              <div className="space-y-1.5">
                <div className="flex justify-between text-sm">
                  <span className="text-[var(--v3-text-secondary)]">Current weight</span>
                  <span className="font-mono font-semibold text-[var(--v3-text-primary)]">{weightKg} kg ({Math.round(weightKg * 2.20462)} lbs)</span>
                </div>
                <input
                  type="range"
                  min={40}
                  max={150}
                  value={weightKg}
                  onChange={(e) => setWeightKg(Number(e.target.value))}
                  className="w-full h-1.5 rounded bg-[var(--v3-progress-track)] accent-[var(--v3-accent)] cursor-pointer"
                  aria-label="Weight slider"
                />
                <div className="flex justify-between text-[11px] font-mono text-[var(--v3-text-muted)]">
                  <span>40 kg</span>
                  <span>95 kg</span>
                  <span>150 kg</span>
                </div>
              </div>

              {/* Height */}
              <div className="space-y-1.5">
                <div className="flex justify-between text-sm">
                  <span className="text-[var(--v3-text-secondary)]">Height</span>
                  <span className="font-mono font-semibold text-[var(--v3-text-primary)]">
                    {heightCm} cm ({Math.floor(heightCm / 30.48)}'{Math.round((heightCm % 30.48) / 2.54)}")
                  </span>
                </div>
                <input
                  type="range"
                  min={140}
                  max={210}
                  value={heightCm}
                  onChange={(e) => setHeightCm(Number(e.target.value))}
                  className="w-full h-1.5 rounded bg-[var(--v3-progress-track)] accent-[var(--v3-accent)] cursor-pointer"
                  aria-label="Height slider"
                />
                <div className="flex justify-between text-[11px] font-mono text-[var(--v3-text-muted)]">
                  <span>140 cm</span>
                  <span>175 cm</span>
                  <span>210 cm</span>
                </div>
              </div>
            </div>

            {/* Activity Level Selector */}
            <div className="space-y-2 pt-2">
              <label className="text-sm font-medium text-[var(--v3-text-primary)]">
                Daily activity level
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                {(Object.entries(ACTIVITY_FACTORS) as [ActivityOption, { factor: number; label: string; desc: string }][]).map(([key, opt]) => (
                  <button
                    key={key}
                    type="button"
                    onClick={() => setActivity(key)}
                    className={`p-3 rounded-xl text-left border transition-colors cursor-pointer ${
                      activity === key
                        ? 'bg-[var(--v3-surface-secondary)] border-[var(--v3-accent)]'
                        : 'bg-[var(--v3-surface-elevated)] border-[var(--v3-border)] hover:border-[var(--v3-text-muted)]'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className={`text-xs font-medium ${activity === key ? 'text-[var(--v3-accent)]' : 'text-[var(--v3-text-primary)]'}`}>
                        {opt.label}
                      </span>
                      <span className="text-[11px] font-mono text-[var(--v3-text-muted)]">×{opt.factor}</span>
                    </div>
                    <p className="text-[11px] text-[var(--v3-text-secondary)] mt-0.5 leading-snug">
                      {opt.desc}
                    </p>
                  </button>
                ))}
              </div>
            </div>

            {/* App Guardrails Note */}
            <div className="p-4 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] flex items-start gap-3">
              <ShieldCheck className="w-5 h-5 text-[var(--v3-accent)] shrink-0 mt-0.5" />
              <div className="text-xs text-[var(--v3-text-secondary)] space-y-1">
                <span className="font-semibold text-[var(--v3-text-primary)]">App Guardrails:</span>
                <p>
                  Weekly change is limited to about 1% of body weight (~{Math.round(weightKg * 11)} kcal/day max deficit for {weightKg}kg), with baseline minimums of {sexFloor} kcal/day and 95% of BMR.
                </p>
              </div>
            </div>

          </div>

          {/* Right Panel: Reconciled Output */}
          <div className="lg:col-span-5 v3-card p-6 sm:p-8 space-y-6">
            
            {/* Target Header */}
            <div>
              <span className="text-xs font-mono uppercase tracking-wider text-[var(--v3-text-muted)]">
                Estimated daily energy target (Mifflin-St Jeor)
              </span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className="text-4xl sm:text-5xl font-mono tabular-nums font-bold text-[var(--v3-accent)]">
                  {reconciledCalories.toLocaleString()}
                </span>
                <span className="text-base font-mono text-[var(--v3-text-muted)]">kcal / day</span>
              </div>
              <div className="flex items-center gap-3 text-xs font-mono text-[var(--v3-text-muted)] mt-1.5">
                <span>BMR: {bmr} kcal</span>
                <span>&bull;</span>
                <span>TDEE: {tdee} kcal</span>
                <span>&bull;</span>
                <span className="text-[var(--v3-accent)]">{delta >= 0 ? `+${delta}` : delta} kcal</span>
              </div>

              {floorApplied && (
                <div className="mt-2.5 inline-flex items-center gap-1.5 px-2.5 py-1 rounded bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs">
                  <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                  <span>App guardrail applied ({sexFloor} kcal minimum)</span>
                </div>
              )}
            </div>

            {/* Macro Breakdown Triplets */}
            <div className="space-y-3 pt-3 border-t border-[var(--v3-border)]">
              <span className="text-xs font-mono uppercase tracking-wider text-[var(--v3-text-muted)]">
                Target macronutrient split
              </span>

              <div className="grid grid-cols-3 gap-3">
                {/* Protein */}
                <div className="p-3 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] space-y-1">
                  <span className="text-xs text-[var(--v3-text-muted)]">Protein</span>
                  <div className="text-xl font-mono font-bold text-[var(--v3-text-primary)]">
                    {proteinGrams}g
                  </div>
                  <span className="text-[11px] font-mono text-[var(--v3-text-secondary)]">
                    {proteinGrams * 4} kcal
                  </span>
                </div>

                {/* Carbs */}
                <div className="p-3 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] space-y-1">
                  <span className="text-xs text-[var(--v3-text-muted)]">Carbs</span>
                  <div className="text-xl font-mono font-bold text-[var(--v3-text-primary)]">
                    {carbsGrams}g
                  </div>
                  <span className="text-[11px] font-mono text-[var(--v3-text-secondary)]">
                    {carbsGrams * 4} kcal
                  </span>
                </div>

                {/* Fat */}
                <div className="p-3 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] space-y-1">
                  <span className="text-xs text-[var(--v3-text-muted)]">Fat</span>
                  <div className="text-xl font-mono font-bold text-[var(--v3-text-primary)]">
                    {fatGrams}g
                  </div>
                  <span className="text-[11px] font-mono text-[var(--v3-text-secondary)]">
                    {fatGrams * 9} kcal
                  </span>
                </div>
              </div>

              {/* Water Target */}
              <div className="p-3 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] flex items-center justify-between text-xs">
                <span className="text-[var(--v3-text-secondary)]">Hydration baseline</span>
                <span className="font-mono font-semibold text-[var(--v3-text-primary)]">~{waterLiters} Liters / day</span>
              </div>
            </div>

            {/* Medical Disclaimer Note */}
            <div className="pt-2 border-t border-[var(--v3-border)]">
              <p className="text-xs text-[var(--v3-text-muted)] leading-relaxed">
                {APP_CONFIG.notMedicalAdvice}
              </p>
            </div>

          </div>

        </div>

      </div>
    </section>
  )
}
