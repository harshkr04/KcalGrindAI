import React from 'react'
import { DEMO_DAY } from '../config'

interface EnergyRingProps {
  size?: number
  strokeWidth?: number
  className?: string
}

export const EnergyRing: React.FC<EnergyRingProps> = ({
  size = 148,
  strokeWidth = 9,
  className = '',
}) => {
  const center = size / 2
  const radius = center - strokeWidth / 2 - 2
  const circumference = 2 * Math.PI * radius
  const percent = Math.min(1, DEMO_DAY.calorieConsumed / DEMO_DAY.calorieTarget)
  const offset = circumference * (1 - percent)

  return (
    <div className={`flex flex-col items-center justify-center select-none ${className}`}>
      {/* Ring with centered remaining count */}
      <div className="relative" style={{ width: size, height: size }}>
        <svg
          width={size}
          height={size}
          viewBox={`0 0 ${size} ${size}`}
          className="transform -rotate-90"
        >
          {/* Background track */}
          <circle
            cx={center}
            cy={center}
            r={radius}
            fill="none"
            stroke="var(--v3-progress-track)"
            strokeWidth={strokeWidth}
          />
          {/* Progress arc */}
          <circle
            cx={center}
            cy={center}
            r={radius}
            fill="none"
            stroke="var(--v3-calorie-ring)"
            strokeWidth={strokeWidth}
            strokeDasharray={circumference}
            strokeDashoffset={offset}
            strokeLinecap="round"
          />
        </svg>

        {/* Centered Remaining Number — strictly inside ring */}
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center pointer-events-none">
          <span className="text-2xl sm:text-3xl font-bold font-mono tracking-tight text-[var(--v3-text-primary)] leading-none">
            {DEMO_DAY.calorieRemaining}
          </span>
          <span className="text-[11px] font-medium tracking-wide uppercase text-[var(--v3-accent)] mt-1 leading-none">
            kcal left
          </span>
        </div>
      </div>

      {/* Target & Consumed Readout — below the ring with clear spacing, never overlapping */}
      <div className="mt-2.5 text-center">
        <span className="text-xs font-mono text-[var(--v3-text-muted)]">
          {DEMO_DAY.calorieConsumed.toLocaleString()} / {DEMO_DAY.calorieTarget.toLocaleString()} kcal
        </span>
      </div>

      {/* 3 Macro Bars matching real app Home screen */}
      <div className="w-full grid grid-cols-3 gap-2 mt-3 pt-2.5 border-t border-[var(--v3-border)]">
        {/* Protein */}
        <div className="flex flex-col space-y-1">
          <div className="flex items-center justify-between text-[11px]">
            <span className="text-[var(--v3-text-muted)] font-medium">Protein</span>
            <span className="font-mono text-[var(--v3-text-primary)] font-semibold">{DEMO_DAY.proteinGrams}g</span>
          </div>
          <div className="w-full h-1.5 rounded-full bg-[var(--v3-progress-track)] overflow-hidden">
            <div
              className="h-full rounded-full bg-[var(--v3-protein-ring)]"
              style={{ width: `${Math.min(100, (DEMO_DAY.proteinGrams / DEMO_DAY.proteinTarget) * 100)}%` }}
            />
          </div>
        </div>

        {/* Carbs */}
        <div className="flex flex-col space-y-1">
          <div className="flex items-center justify-between text-[11px]">
            <span className="text-[var(--v3-text-muted)] font-medium">Carbs</span>
            <span className="font-mono text-[var(--v3-text-primary)] font-semibold">{DEMO_DAY.carbsGrams}g</span>
          </div>
          <div className="w-full h-1.5 rounded-full bg-[var(--v3-progress-track)] overflow-hidden">
            <div
              className="h-full rounded-full bg-[var(--v3-carbs-ring)]"
              style={{ width: `${Math.min(100, (DEMO_DAY.carbsGrams / DEMO_DAY.carbsTarget) * 100)}%` }}
            />
          </div>
        </div>

        {/* Fat */}
        <div className="flex flex-col space-y-1">
          <div className="flex items-center justify-between text-[11px]">
            <span className="text-[var(--v3-text-muted)] font-medium">Fat</span>
            <span className="font-mono text-[var(--v3-text-primary)] font-semibold">{DEMO_DAY.fatsGrams}g</span>
          </div>
          <div className="w-full h-1.5 rounded-full bg-[var(--v3-progress-track)] overflow-hidden">
            <div
              className="h-full rounded-full bg-[var(--v3-fats-ring)]"
              style={{ width: `${Math.min(100, (DEMO_DAY.fatsGrams / DEMO_DAY.fatsTarget) * 100)}%` }}
            />
          </div>
        </div>
      </div>
    </div>
  )
}
