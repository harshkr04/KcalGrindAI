import React, { useState } from 'react'
import { ShieldCheck, Eye, EyeOff, Plus, Droplets } from 'lucide-react'
import { APP_CONFIG, DEMO_DAY } from '../config'

interface WidgetShowcaseSectionProps {
  theme?: 'dark' | 'light'
}

export const WidgetShowcaseSection: React.FC<WidgetShowcaseSectionProps> = ({ theme = 'dark' }) => {
  const [activeWidgetId, setActiveWidgetId] = useState<'4x2' | '2x2' | '4x1'>('4x2')
  const [privacyMaskActive, setPrivacyMaskActive] = useState(false)

  const activeWidget = APP_CONFIG.widgets.find((w) => w.id === activeWidgetId) || APP_CONFIG.widgets[0]

  return (
    <section id="widgets" className="w-full py-24 sm:py-28 lg:py-32 text-left">
      <div className="w-full max-w-[1440px] mx-auto px-6 sm:px-8 lg:px-12">
        
        {/* Section Header */}
        <div className="max-w-3xl mb-10 space-y-3">
          <div className="inline-flex items-center gap-2">
            <span className="hairline-tag">
              <span>Android home screen</span>
            </span>
          </div>
          <h2 className="font-medium tracking-tight text-[var(--v3-text-primary)]">
            Home-screen widgets
          </h2>
          <p className="text-[17px] sm:text-[18px] text-[var(--v3-text-secondary)] leading-relaxed font-normal">
            Monitor daily calorie balance, water intake, and macros directly from your phone's home screen. Widgets read from your local device database and include a privacy mode toggle.
          </p>
        </div>

        {/* Controls Bar: Widget Size Selector + Privacy Toggle */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div className="flex items-center gap-2">
            {APP_CONFIG.widgets.map((widget) => (
              <button
                key={widget.id}
                type="button"
                onClick={() => setActiveWidgetId(widget.id as any)}
                className={`px-4 py-2 rounded-xl text-sm transition-colors cursor-pointer ${
                  activeWidgetId === widget.id
                    ? 'bg-[var(--v3-surface-secondary)] text-[var(--v3-accent)] font-medium border border-[var(--v3-border)] shadow-xs'
                    : 'text-[var(--v3-text-secondary)] hover:text-[var(--v3-text-primary)]'
                }`}
              >
                {widget.name}
              </button>
            ))}
          </div>

          {/* Working Privacy Mode Toggle Button */}
          <button
            type="button"
            onClick={() => setPrivacyMaskActive(!privacyMaskActive)}
            className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] text-sm text-[var(--v3-text-primary)] hover:border-[var(--v3-accent)] transition-colors cursor-pointer w-fit"
          >
            {privacyMaskActive ? (
              <>
                <EyeOff className="w-4 h-4 text-[var(--v3-accent)]" />
                <span>Privacy mask: <strong>ON</strong> (tap to reveal)</span>
              </>
            ) : (
              <>
                <Eye className="w-4 h-4 text-[var(--v3-text-muted)]" />
                <span>Privacy mask: <strong>OFF</strong> (tap to conceal)</span>
              </>
            )}
          </button>
        </div>

        {/* Stage: Mock Android Home Screen at Full Proportions */}
        <div className="v3-card p-6 sm:p-10 space-y-6">
          <div className="border-b border-[var(--v3-border)] pb-3 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2">
            <div>
              <h3 className="text-lg font-medium text-[var(--v3-text-primary)]">
                {activeWidget.name}
              </h3>
              <p className="text-sm text-[var(--v3-text-secondary)]">
                {activeWidget.desc}
              </p>
            </div>
            <span className="text-xs font-mono text-[var(--v3-text-muted)] px-2.5 py-1 rounded bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)]">
              Interactive preview &bull; {privacyMaskActive ? 'Masked' : 'Unmasked'}
            </span>
          </div>

          {/* Android Home Screen Simulator */}
          <div
            className={`relative rounded-3xl p-6 sm:p-10 flex flex-col items-center justify-between min-h-[380px] sm:min-h-[440px] overflow-hidden transition-colors border border-[var(--v3-border)] shadow-inner ${
              theme === 'light'
                ? 'bg-gradient-to-b from-[#d5e2d8] via-[#e2ebe4] to-[#cddcd1]'
                : 'bg-gradient-to-b from-[#141d17] via-[#0f1411] to-[#0a0d0b]'
            }`}
          >
            {/* Ambient Wallpaper Accents */}
            <div className="w-full flex items-center justify-between text-xs text-[var(--v3-text-muted)] font-mono pb-4 border-b border-white/5">
              <span>Tuesday, Nov 7 &bull; 9:41 AM</span>
              <span>72°F Mostly Sunny</span>
            </div>

            {/* Widget Stage Area */}
            <div className="w-full flex items-center justify-center py-6">
              
              {/* 4x2 Expanded Widget */}
              {activeWidgetId === '4x2' && (
                <div className="w-full max-w-[560px] p-5 rounded-[24px] bg-[var(--v3-surface-elevated)] border border-[var(--v3-border)] shadow-2xl transition-all">
                  <div className="grid grid-cols-1 sm:grid-cols-12 gap-5 items-center">
                    
                    {/* Left: Calorie Ring & Remaining Count */}
                    <div className="sm:col-span-5 flex flex-col items-center justify-center p-2 border-b sm:border-b-0 sm:border-r border-[var(--v3-border)]">
                      <div className="relative w-28 h-28 flex items-center justify-center">
                        <svg className="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
                          <circle cx="50" cy="50" r="42" fill="none" stroke="var(--v3-progress-track)" strokeWidth="8" />
                          <circle
                            cx="50"
                            cy="50"
                            r="42"
                            fill="none"
                            stroke="var(--v3-calorie-ring)"
                            strokeWidth="8"
                            strokeDasharray={2 * Math.PI * 42}
                            strokeDashoffset={2 * Math.PI * 42 * (1 - DEMO_DAY.calorieConsumed / DEMO_DAY.calorieTarget)}
                            strokeLinecap="round"
                          />
                        </svg>
                        <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
                          <span className="text-xl font-bold font-mono text-[var(--v3-text-primary)] leading-tight">
                            {privacyMaskActive ? '••••' : DEMO_DAY.calorieRemaining}
                          </span>
                          <span className="text-[10px] font-medium text-[var(--v3-accent)] uppercase">
                            kcal left
                          </span>
                        </div>
                      </div>
                      <span className="text-xs font-mono text-[var(--v3-text-muted)] mt-2">
                        {privacyMaskActive ? '•••• / •••• kcal' : `${DEMO_DAY.calorieConsumed} / ${DEMO_DAY.calorieTarget} kcal`}
                      </span>
                    </div>

                    {/* Right: Macro Progress Bars & Quick Water */}
                    <div className="sm:col-span-7 space-y-3.5">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <img
                            src="/assets/sproutling/ic_sproutling_active.svg"
                            width="20"
                            height="20"
                            alt="Sproutling"
                          />
                          <span className="text-xs font-medium text-[var(--v3-text-primary)]">Daily Targets</span>
                        </div>
                        <span className="text-[11px] font-mono text-[var(--v3-accent)] bg-[var(--v3-surface-secondary)] px-2 py-0.5 rounded border border-[var(--v3-border)]">
                          {privacyMaskActive ? 'Private mode' : 'On track'}
                        </span>
                      </div>

                      {/* 3 Macro Bars */}
                      <div className="space-y-2">
                        <div>
                          <div className="flex justify-between text-xs mb-1">
                            <span className="text-[var(--v3-text-secondary)]">Protein</span>
                            <span className="font-mono text-[var(--v3-text-primary)]">
                              {privacyMaskActive ? '•••g' : `${DEMO_DAY.proteinGrams}g / ${DEMO_DAY.proteinTarget}g`}
                            </span>
                          </div>
                          <div className="w-full h-2 rounded-full bg-[var(--v3-progress-track)] overflow-hidden">
                            <div className="h-full rounded-full bg-[var(--v3-protein-ring)] w-[86%]" />
                          </div>
                        </div>

                        <div>
                          <div className="flex justify-between text-xs mb-1">
                            <span className="text-[var(--v3-text-secondary)]">Carbs</span>
                            <span className="font-mono text-[var(--v3-text-primary)]">
                              {privacyMaskActive ? '•••g' : `${DEMO_DAY.carbsGrams}g / ${DEMO_DAY.carbsTarget}g`}
                            </span>
                          </div>
                          <div className="w-full h-2 rounded-full bg-[var(--v3-progress-track)] overflow-hidden">
                            <div className="h-full rounded-full bg-[var(--v3-carbs-ring)] w-[82%]" />
                          </div>
                        </div>

                        <div>
                          <div className="flex justify-between text-xs mb-1">
                            <span className="text-[var(--v3-text-secondary)]">Fat</span>
                            <span className="font-mono text-[var(--v3-text-primary)]">
                              {privacyMaskActive ? '•••g' : `${DEMO_DAY.fatsGrams}g / ${DEMO_DAY.fatsTarget}g`}
                            </span>
                          </div>
                          <div className="w-full h-2 rounded-full bg-[var(--v3-progress-track)] overflow-hidden">
                            <div className="h-full rounded-full bg-[var(--v3-fats-ring)] w-[78%]" />
                          </div>
                        </div>
                      </div>

                      {/* Quick Water Action Buttons */}
                      <div className="pt-1 flex items-center justify-between border-t border-[var(--v3-border)] text-xs">
                        <span className="text-[var(--v3-text-muted)] flex items-center gap-1.5">
                          <Droplets className="w-3.5 h-3.5 text-blue-400" />
                          <span>{privacyMaskActive ? '•••• ml' : `${DEMO_DAY.waterMlConsumed} ml`}</span>
                        </span>
                        <div className="flex gap-1.5">
                          <button
                            type="button"
                            className="px-2.5 py-1 rounded-md bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] text-[var(--v3-text-primary)] text-xs font-mono hover:border-[var(--v3-accent)]"
                          >
                            +250ml
                          </button>
                          <button
                            type="button"
                            className="px-2.5 py-1 rounded-md bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] text-[var(--v3-text-primary)] text-xs font-mono hover:border-[var(--v3-accent)]"
                          >
                            +500ml
                          </button>
                        </div>
                      </div>
                    </div>

                  </div>
                </div>
              )}

              {/* 2x2 Compact Widget */}
              {activeWidgetId === '2x2' && (
                <div className="w-[220px] h-[220px] p-5 rounded-[24px] bg-[var(--v3-surface-elevated)] border border-[var(--v3-border)] shadow-2xl flex flex-col justify-between transition-all">
                  <div className="flex items-center justify-between">
                    <img
                      src="/assets/sproutling/ic_sproutling_active.svg"
                      width="22"
                      height="22"
                      alt="Sproutling"
                    />
                    <span className="text-[10px] font-mono text-[var(--v3-accent)] uppercase">
                      KcalGrind
                    </span>
                  </div>

                  {/* Center Ring */}
                  <div className="relative w-24 h-24 mx-auto flex items-center justify-center">
                    <svg className="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
                      <circle cx="50" cy="50" r="40" fill="none" stroke="var(--v3-progress-track)" strokeWidth="8" />
                      <circle
                        cx="50"
                        cy="50"
                        r="40"
                        fill="none"
                        stroke="var(--v3-calorie-ring)"
                        strokeWidth="8"
                        strokeDasharray={2 * Math.PI * 40}
                        strokeDashoffset={2 * Math.PI * 40 * (1 - DEMO_DAY.calorieConsumed / DEMO_DAY.calorieTarget)}
                        strokeLinecap="round"
                      />
                    </svg>
                    <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
                      <span className="text-xl font-bold font-mono text-[var(--v3-text-primary)] leading-tight">
                        {privacyMaskActive ? '••••' : DEMO_DAY.calorieRemaining}
                      </span>
                      <span className="text-[9px] font-medium text-[var(--v3-accent)] uppercase">
                        left
                      </span>
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-xs font-mono text-[var(--v3-text-muted)] pt-1 border-t border-[var(--v3-border)]">
                    <span>{privacyMaskActive ? '•••• kcal' : `${DEMO_DAY.calorieConsumed} kcal`}</span>
                    <button
                      type="button"
                      className="w-6 h-6 rounded-full bg-[var(--v3-accent)] text-white flex items-center justify-center"
                      aria-label="Add meal"
                    >
                      <Plus className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              )}

              {/* 4x1 Minimal Bar Widget */}
              {activeWidgetId === '4x1' && (
                <div className="w-full max-w-[540px] px-5 py-3.5 rounded-[20px] bg-[var(--v3-surface-elevated)] border border-[var(--v3-border)] shadow-2xl flex items-center justify-between gap-4 transition-all">
                  <div className="flex items-center gap-3">
                    <img
                      src="/assets/sproutling/ic_sproutling_active.svg"
                      width="24"
                      height="24"
                      alt="Sproutling"
                    />
                    <div>
                      <div className="text-sm font-bold font-mono text-[var(--v3-text-primary)]">
                        {privacyMaskActive ? '•••• kcal' : `${DEMO_DAY.calorieRemaining} kcal remaining`}
                      </div>
                      <div className="text-[11px] font-mono text-[var(--v3-text-muted)]">
                        {privacyMaskActive ? '•••• / •••• kcal' : `${DEMO_DAY.calorieConsumed} / ${DEMO_DAY.calorieTarget} kcal`}
                      </div>
                    </div>
                  </div>

                  {/* Mini macro pills */}
                  <div className="flex items-center gap-2 text-xs font-mono">
                    <span className="px-2 py-1 rounded bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] text-[var(--v3-protein-ring)] font-medium">
                      P: {privacyMaskActive ? '••g' : `${DEMO_DAY.proteinGrams}g`}
                    </span>
                    <span className="px-2 py-1 rounded bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] text-[var(--v3-carbs-ring)] font-medium">
                      C: {privacyMaskActive ? '••g' : `${DEMO_DAY.carbsGrams}g`}
                    </span>
                    <span className="px-2 py-1 rounded bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] text-[var(--v3-fats-ring)] font-medium">
                      F: {privacyMaskActive ? '••g' : `${DEMO_DAY.fatsGrams}g`}
                    </span>
                  </div>
                </div>
              )}

            </div>

            {/* Android Home Screen Dock Bar */}
            <div className="w-full max-w-sm px-4 py-2 rounded-2xl bg-black/20 backdrop-blur-md border border-white/10 flex items-center justify-around">
              <div className="w-10 h-10 rounded-xl bg-[var(--v3-surface-elevated)] border border-[var(--v3-border)] flex items-center justify-center text-xs">📞</div>
              <div className="w-10 h-10 rounded-xl bg-[var(--v3-surface-elevated)] border border-[var(--v3-border)] flex items-center justify-center text-xs">💬</div>
              <div className="w-10 h-10 rounded-xl bg-[var(--v3-accent)] text-white flex items-center justify-center text-xs shadow-md">
                <img src="/assets/sproutling/ic_sproutling_active.svg" width="22" height="22" alt="KcalGrind" />
              </div>
              <div className="w-10 h-10 rounded-xl bg-[var(--v3-surface-elevated)] border border-[var(--v3-border)] flex items-center justify-center text-xs">🌐</div>
              <div className="w-10 h-10 rounded-xl bg-[var(--v3-surface-elevated)] border border-[var(--v3-border)] flex items-center justify-center text-xs">📷</div>
            </div>

          </div>

          <div className="flex items-center gap-2 text-xs text-[var(--v3-text-muted)] font-mono">
            <ShieldCheck className="w-4 h-4 text-[var(--v3-accent)] stroke-[1.5]" />
            <span>Reads on-device log &bull; System dark/light theme support &bull; Interactive privacy toggle masks figures</span>
          </div>
        </div>

      </div>
    </section>
  )
}
