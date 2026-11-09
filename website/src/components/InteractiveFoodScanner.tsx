import React, { useState, useRef } from 'react'
import { Camera, Edit3, CheckCircle2 } from 'lucide-react'

interface FoodSample {
  id: string
  name: string
  calories: number
  protein: number
  carbs: number
  fats: number
  totalGrams: number
  image: string
  photographerName: string
  photographerUrl: string
  photoPageUrl: string
  imageLicense: string
  items: Array<{
    name: string
    portion: string
    grams: number
    calories: number
    coords: { x: number; y: number }
  }>
}

const SAMPLES: FoodSample[] = [
  {
    id: 'tofu-protein-bowl',
    name: 'Seasoned tofu protein bowl',
    calories: 540,
    protein: 42,
    carbs: 48,
    fats: 20,
    totalGrams: 440,
    image: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=900&auto=format&fit=crop&q=80',
    photographerName: 'Chad Montano (@briewilly)',
    photographerUrl: 'https://unsplash.com/@briewilly',
    photoPageUrl: 'https://unsplash.com/photos/ba9599a7e63c',
    imageLicense: 'Unsplash License',
    items: [
      { name: 'Pan-seared seasoned tofu', portion: '160g', grams: 160, calories: 240, coords: { x: 50, y: 48 } },
      { name: 'Hard-boiled quail eggs & edamame', portion: '75g', grams: 75, calories: 105, coords: { x: 35, y: 30 } },
      { name: 'Sweet corn & diced cucumber', portion: '120g', grams: 120, calories: 75, coords: { x: 68, y: 55 } },
      { name: 'Purple cabbage & sesame dressing', portion: '85g', grams: 85, calories: 120, coords: { x: 35, y: 68 } },
    ],
  },
  {
    id: 'dal-roti',
    name: 'Dal tadka & roti',
    calories: 420,
    protein: 16,
    carbs: 68,
    fats: 9,
    totalGrams: 360,
    image: 'https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=900&auto=format&fit=crop&q=80',
    photographerName: 'chuttersnap',
    photographerUrl: 'https://unsplash.com/@chuttersnap',
    photoPageUrl: 'https://unsplash.com/photos/b9f581a1996d',
    imageLicense: 'Unsplash License',
    items: [
      { name: 'Yellow dal tadka', portion: '200g', grams: 200, calories: 210, coords: { x: 38, y: 45 } },
      { name: 'Whole wheat roti', portion: '100g', grams: 100, calories: 160, coords: { x: 65, y: 55 } },
      { name: 'Kachumber salad', portion: '60g', grams: 60, calories: 50, coords: { x: 25, y: 65 } },
    ],
  },
]

export const InteractiveFoodScanner: React.FC = () => {
  const [selectedSample, setSelectedSample] = useState<FoodSample>(SAMPLES[0])
  const [sliderPosition, setSliderPosition] = useState<number>(50)
  const [isDragging, setIsDragging] = useState<boolean>(false)
  const [portionGrams, setPortionGrams] = useState<number>(180)
  const containerRef = useRef<HTMLDivElement>(null)

  const handlePointerDown = (e: React.PointerEvent) => {
    setIsDragging(true)
    handlePointerMove(e)
  }

  const handlePointerMove = (e: React.PointerEvent) => {
    if (!containerRef.current) return
    const rect = containerRef.current.getBoundingClientRect()
    const x = Math.max(0, Math.min(e.clientX - rect.left, rect.width))
    const percent = Math.round((x / rect.width) * 100)
    setSliderPosition(percent)
  }

  const handlePointerUp = () => {
    setIsDragging(false)
  }

  // Calculate live calories when portion is edited
  const primaryItem = selectedSample.items[0] || { name: 'Item', grams: 160, calories: 240 }
  const baseCalPerGram = primaryItem.calories / primaryItem.grams
  const adjustedPrimaryCal = Math.round(portionGrams * baseCalPerGram)
  const otherItemsCal = selectedSample.items.slice(1).reduce((acc, item) => acc + item.calories, 0)
  const otherItemsGrams = selectedSample.items.slice(1).reduce((acc, item) => acc + item.grams, 0)
  const totalAdjustedCal = adjustedPrimaryCal + otherItemsCal
  const totalAdjustedGrams = portionGrams + otherItemsGrams

  return (
    <section id="ai-scanner-demo" className="w-full py-24 sm:py-28 lg:py-32 text-left">
      <div className="w-full max-w-[1440px] mx-auto px-6 sm:px-8 lg:px-12">
        
        {/* Section Header */}
        <div className="max-w-3xl mb-12 space-y-3">
          <div className="inline-flex items-center gap-2">
            <span className="hairline-tag">
              <Camera className="w-4 h-4 text-[var(--v3-accent)] stroke-[1.5]" />
              <span>Example &bull; AI food scanner</span>
            </span>
          </div>

          <h2 className="font-medium tracking-tight text-[var(--v3-text-primary)]">
            Photo logging with portion estimation
          </h2>

          <p className="text-[17px] sm:text-[18px] text-[var(--v3-text-secondary)] leading-relaxed font-normal">
            Photos sent for meal analysis are processed through Gemini to recognize ingredients and estimate portions. Photos are not saved to your account and are not stored on our servers.
          </p>

          {/* Sample Switcher */}
          <div className="flex flex-wrap items-center gap-2 pt-2">
            {SAMPLES.map((sample) => {
              const isSelected = selectedSample.id === sample.id
              return (
                <button
                  key={sample.id}
                  type="button"
                  onClick={() => {
                    setSelectedSample(sample)
                    setPortionGrams(sample.items[0]?.grams || 180)
                  }}
                  className={`px-4 py-2 rounded-xl text-sm transition-colors cursor-pointer ${
                    isSelected
                      ? 'bg-[var(--v3-surface-secondary)] text-[var(--v3-accent)] font-medium border border-[var(--v3-accent)]'
                      : 'bg-[var(--v3-surface-elevated)] text-[var(--v3-text-secondary)] border border-[var(--v3-border)] hover:text-[var(--v3-text-primary)]'
                  }`}
                >
                  {sample.name} (~{sample.totalGrams}g &bull; {sample.calories} kcal)
                </button>
              )
            })}
          </div>
        </div>

        {/* Interactive Scanner Stage */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-start">
          
          {/* Left: Drag Comparison Canvas */}
          <div className="lg:col-span-8 flex flex-col items-center">
            <div
              ref={containerRef}
              onPointerDown={handlePointerDown}
              onPointerMove={isDragging ? handlePointerMove : undefined}
              onPointerUp={handlePointerUp}
              className="relative w-full aspect-[4/3] rounded-2xl overflow-hidden border border-[var(--v3-border)] select-none cursor-ew-resize touch-none bg-black"
            >
              {/* Layer 1: Original Meal Photo */}
              <img
                src={selectedSample.image}
                alt={selectedSample.name}
                className="absolute inset-0 w-full h-full object-cover"
                draggable={false}
              />

              {/* Layer 2: Detected Ingredient Overlay (clipped by slider) */}
              <div
                className="absolute inset-0 overflow-hidden"
                style={{ clipPath: `inset(0 0 0 ${sliderPosition}%)` }}
              >
                <img
                  src={selectedSample.image}
                  alt={selectedSample.name}
                  className="absolute inset-0 w-full h-full object-cover brightness-95"
                  draggable={false}
                />

                {/* Detected Item Markers — clean label, portion, NO percentage confidence */}
                {selectedSample.items.map((item, idx) => (
                  <div
                    key={idx}
                    className="absolute transform -translate-x-1/2 -translate-y-1/2 pointer-events-none"
                    style={{ left: `${item.coords.x}%`, top: `${item.coords.y}%` }}
                  >
                    <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-[#101211]/90 border border-white/15 text-white text-xs sm:text-sm font-medium shadow-lg backdrop-blur-xs">
                      <span className="w-2 h-2 rounded-full bg-[var(--v3-accent)]" />
                      <span>{item.name}</span>
                      <span className="font-mono text-[var(--v3-text-secondary)]">({item.portion})</span>
                    </div>
                  </div>
                ))}

                {/* Example badge */}
                <div className="absolute top-4 right-4 px-3 py-1 rounded-md bg-[#101211]/90 border border-white/10 text-xs text-[var(--v3-text-secondary)]">
                  Example detection overlay
                </div>
              </div>

              {/* Divider slider line */}
              <div
                className="absolute top-0 bottom-0 w-0.5 bg-white shadow-xl cursor-ew-resize pointer-events-none"
                style={{ left: `${sliderPosition}%` }}
              >
                <div className="absolute top-1/2 -translate-y-1/2 -translate-x-1/2 w-8 h-8 rounded-full bg-[var(--v3-surface-elevated)] border-2 border-white flex items-center justify-center text-sm text-[var(--v3-text-primary)] shadow-md">
                  &harr;
                </div>
              </div>
            </div>

            <div className="w-full flex flex-col sm:flex-row items-center justify-between text-xs text-[var(--v3-text-muted)] mt-3 gap-2">
              <span>Drag slider left or right to inspect photo vs detected ingredients</span>
              <span>
                Photo by{' '}
                <a
                  href={selectedSample.photographerUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="underline hover:text-[var(--v3-text-primary)]"
                >
                  {selectedSample.photographerName}
                </a>{' '}
                on{' '}
                <a
                  href={selectedSample.photoPageUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="underline hover:text-[var(--v3-text-primary)]"
                >
                  Unsplash
                </a>{' '}
                &bull; {selectedSample.imageLicense}
              </span>
            </div>
          </div>

          {/* Right: Breakdown & "Check the Portion" Editable Card */}
          <div className="lg:col-span-4 space-y-5">
            
            {/* Meal Summary Card */}
            <div className="p-6 rounded-2xl v3-card space-y-4">
              <div className="border-b border-[var(--v3-border)] pb-3">
                <span className="text-xs font-mono text-[var(--v3-text-muted)] uppercase tracking-wider">
                  Example meal
                </span>
                <h3 className="text-lg font-medium text-[var(--v3-text-primary)] mt-1">
                  {selectedSample.name}
                </h3>
                <div className="flex items-baseline gap-2 mt-1">
                  <span className="text-2xl font-mono tabular-nums font-bold text-[var(--v3-accent)]">
                    {totalAdjustedCal}
                  </span>
                  <span className="text-sm font-mono text-[var(--v3-text-muted)]">kcal total</span>
                  <span className="text-xs text-[var(--v3-text-secondary)]">
                    (~{totalAdjustedGrams}g)
                  </span>
                </div>
              </div>

              {/* Editable portion note: "Check the portion" */}
              <div className="p-4 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] space-y-3">
                <div className="flex items-center gap-2 text-sm font-medium text-[var(--v3-text-primary)]">
                  <Edit3 className="w-4 h-4 text-[var(--v3-accent)] stroke-[1.5]" />
                  <span>Check the portion</span>
                </div>
                <p className="text-xs text-[var(--v3-text-secondary)] leading-relaxed">
                  Estimates give you a fast starting point. Adjust portion grams if your serving was larger or smaller:
                </p>

                <div className="space-y-1.5 pt-1">
                  <div className="flex justify-between text-xs font-medium text-[var(--v3-text-primary)]">
                    <span>{primaryItem.name}</span>
                    <span className="font-mono text-[var(--v3-accent)]">{portionGrams}g ({adjustedPrimaryCal} kcal)</span>
                  </div>
                  <input
                    type="range"
                    min={Math.round(primaryItem.grams * 0.5)}
                    max={Math.round(primaryItem.grams * 1.8)}
                    step={5}
                    value={portionGrams}
                    onChange={(e) => setPortionGrams(Number(e.target.value))}
                    className="w-full h-1.5 rounded bg-[var(--v3-progress-track)] accent-[var(--v3-accent)] cursor-pointer"
                    aria-label="Adjust portion grams"
                  />
                  <div className="flex justify-between text-[11px] font-mono text-[var(--v3-text-muted)]">
                    <span>{Math.round(primaryItem.grams * 0.5)}g</span>
                    <span>{primaryItem.grams}g (typical)</span>
                    <span>{Math.round(primaryItem.grams * 1.8)}g</span>
                  </div>
                </div>
              </div>

              {/* Items Breakdown List */}
              <div className="space-y-2 pt-1">
                <span className="text-xs font-mono uppercase tracking-wider text-[var(--v3-text-muted)]">
                  Identified components
                </span>
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between p-2.5 rounded-lg bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] text-sm">
                    <span className="text-[var(--v3-text-primary)]">{primaryItem.name}</span>
                    <span className="font-mono text-[var(--v3-text-secondary)]">{portionGrams}g &bull; {adjustedPrimaryCal} kcal</span>
                  </div>
                  {selectedSample.items.slice(1).map((item, idx) => (
                    <div
                      key={idx}
                      className="flex items-center justify-between p-2.5 rounded-lg bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] text-sm"
                    >
                      <span className="text-[var(--v3-text-primary)]">{item.name}</span>
                      <span className="font-mono text-[var(--v3-text-secondary)]">{item.portion} &bull; {item.calories} kcal</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Privacy badge */}
              <div className="pt-2 flex items-center gap-2 text-xs text-[var(--v3-text-muted)] border-t border-[var(--v3-border)]">
                <CheckCircle2 className="w-4 h-4 text-[var(--v3-accent)] shrink-0" />
                <span>Photos are never saved to your account or stored on disk</span>
              </div>
            </div>

          </div>

        </div>

      </div>
    </section>
  )
}
