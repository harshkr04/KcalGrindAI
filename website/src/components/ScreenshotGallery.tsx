import React, { useRef, useState } from 'react'
import { ArrowLeft, ArrowRight, X, Sparkles, Database } from 'lucide-react'
import { APP_CONFIG } from '../config'

interface FallbackScreenProps {
  id: string
  title: string
  category: string
}

const CodeRenderedScreenFallback: React.FC<FallbackScreenProps> = ({ id, title, category }) => {
  return (
    <div className="w-full h-full bg-[#121513] text-[#eef2ef] p-3 flex flex-col justify-between text-left select-none overflow-hidden rounded-[26px] font-sans">
      {/* Status Bar */}
      <div className="flex items-center justify-between text-[10px] text-[#828e86] font-mono pt-1 px-1">
        <span>9:41</span>
        <div className="flex items-center gap-1.5">
          <span className="text-[#3ea877] text-[9px] font-medium">5G</span>
          <div className="w-3 h-1.5 rounded-[2px] border border-white/40 p-[1px] flex items-center">
            <div className="h-full w-2 bg-[#3ea877] rounded-[1px]" />
          </div>
        </div>
      </div>

      {/* Screen Header */}
      <div className="pt-2 px-1 border-b border-white/10 pb-2">
        <span className="text-[9px] uppercase tracking-wider font-mono text-[#3ea877]">{category}</span>
        <h4 className="text-sm font-medium text-white">{title}</h4>
      </div>

      {/* Dynamic Screen Content Mockup */}
      <div className="flex-1 py-3 space-y-2 overflow-hidden">
        {id === 'home' && (
          <>
            <div className="p-2.5 rounded-xl bg-[#191d1b] border border-white/5 space-y-1.5">
              <div className="flex justify-between items-center text-xs">
                <span className="text-[#a6b2aa]">Remaining</span>
                <span className="text-[#3ea877] font-medium font-mono">527 kcal</span>
              </div>
              <div className="h-1.5 w-full bg-[#272e2a] rounded-full overflow-hidden">
                <div className="h-full bg-[#3ea877] rounded-full" style={{ width: '75%' }} />
              </div>
            </div>
            <div className="grid grid-cols-3 gap-1.5 text-center">
              <div className="p-2 rounded-lg bg-[#191d1b] border border-white/5">
                <span className="text-[9px] text-[#828e86] block">Protein</span>
                <span className="text-xs font-mono text-white">120g</span>
              </div>
              <div className="p-2 rounded-lg bg-[#191d1b] border border-white/5">
                <span className="text-[9px] text-[#828e86] block">Carbs</span>
                <span className="text-xs font-mono text-white">180g</span>
              </div>
              <div className="p-2 rounded-lg bg-[#191d1b] border border-white/5">
                <span className="text-[9px] text-[#828e86] block">Fats</span>
                <span className="text-xs font-mono text-white">47g</span>
              </div>
            </div>
          </>
        )}

        {id === 'diary' && (
          <div className="space-y-1.5 text-xs">
            <div className="p-2 rounded-lg bg-[#191d1b] border border-white/5 flex justify-between items-center">
              <div>
                <p className="font-medium text-white">Chicken breast & rice</p>
                <span className="text-[10px] text-[#828e86]">Lunch &bull; 400g</span>
              </div>
              <span className="font-mono text-[#3ea877]">540 kcal</span>
            </div>
            <div className="p-2 rounded-lg bg-[#191d1b] border border-white/5 flex justify-between items-center">
              <div>
                <p className="font-medium text-white">Dal tadka & roti</p>
                <span className="text-[10px] text-[#828e86]">Breakfast &bull; 280g</span>
              </div>
              <span className="font-mono text-[#3ea877]">420 kcal</span>
            </div>
          </div>
        )}

        {id === 'search' && (
          <div className="space-y-2 text-xs">
            <div className="p-1.5 rounded-lg bg-[#191d1b] border border-white/10 text-[#828e86] text-[11px]">
              Search ICMR-NIN & USDA...
            </div>
            <div className="p-2 rounded-lg bg-[#191d1b] border border-white/5">
              <div className="flex items-center gap-1">
                <span className="px-1 py-0.2 bg-[#1b3d2b] text-[#55d996] rounded text-[8px] font-mono">ICMR-NIN</span>
                <span className="font-medium text-white text-[11px]">Paneer Bhurji</span>
              </div>
              <span className="text-[10px] text-[#828e86] font-mono block mt-0.5">380 kcal &bull; 24g P &bull; 28g F</span>
            </div>
          </div>
        )}

        {id === 'coach' && (
          <div className="space-y-2 text-xs">
            <div className="p-2 rounded-xl bg-[#222825] border border-white/5 text-[11px] text-[#a6b2aa]">
              What should I eat for dinner with 40g protein?
            </div>
            <div className="p-2 rounded-xl bg-[#192b21] border border-[#3ea877]/20 text-[11px] text-[#eef2ef] space-y-1">
              <div className="flex items-center gap-1 text-[#3ea877] font-medium text-[10px]">
                <Sparkles className="w-3 h-3" />
                <span>Sproutling</span>
              </div>
              <p className="leading-snug">Paneer bhurji (200g) with 2 rotis provides 38g protein and 420 kcal.</p>
            </div>
          </div>
        )}

        {id === 'insights' && (
          <div className="space-y-2 text-xs">
            <div className="p-2.5 rounded-xl bg-[#191d1b] border border-white/5 space-y-1.5">
              <span className="text-[10px] text-[#828e86] font-mono uppercase">7-Day Consistency</span>
              <div className="flex items-end justify-between h-14 pt-2 gap-1 px-1">
                {[65, 80, 90, 85, 100, 95, 75].map((val, idx) => (
                  <div key={idx} className="flex-1 bg-[#272e2a] rounded-t flex items-end h-full">
                    <div className="w-full bg-[#3ea877] rounded-t" style={{ height: `${val}%` }} />
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {id === 'profile' && (
          <div className="space-y-1.5 text-xs">
            <div className="p-2 rounded-lg bg-[#191d1b] border border-white/5 flex justify-between items-center">
              <span className="text-white text-[11px]">Daily Calorie Goal</span>
              <span className="font-mono text-[#3ea877] text-[11px]">2,150 kcal</span>
            </div>
            <div className="p-2 rounded-lg bg-[#191d1b] border border-white/5 flex justify-between items-center">
              <span className="text-white text-[11px]">Cloud Backup</span>
              <span className="text-[10px] text-[#828e86]">Sync now</span>
            </div>
          </div>
        )}

        {!['home', 'diary', 'search', 'coach', 'insights', 'profile'].includes(id) && (
          <div className="p-3 rounded-xl bg-[#191d1b] border border-white/5 text-center space-y-1">
            <Database className="w-5 h-5 text-[#3ea877] mx-auto" />
            <p className="text-xs text-white font-medium">{title}</p>
            <p className="text-[10px] text-[#828e86]">Native Dark OLED Interface</p>
          </div>
        )}
      </div>

      {/* Screen Footer Badge */}
      <div className="pt-1.5 pb-1 text-center border-t border-white/5">
        <span className="text-[9px] text-[#828e86] font-mono">Native Dark OLED &bull; Offline Log</span>
      </div>
    </div>
  )
}

export const ScreenshotGallery: React.FC = () => {
  const scrollContainerRef = useRef<HTMLDivElement>(null)
  const [selectedScreenshot, setSelectedScreenshot] = useState<(typeof APP_CONFIG.appScreenshots)[0] | null>(null)
  const [imageErrors, setImageErrors] = useState<Record<string, boolean>>({})

  const handleImageError = (id: string) => {
    setImageErrors((prev) => ({ ...prev, [id]: true }))
  }

  const scroll = (direction: 'left' | 'right') => {
    if (scrollContainerRef.current) {
      const scrollAmount = direction === 'left' ? -340 : 340
      scrollContainerRef.current.scrollBy({ left: scrollAmount, behavior: 'smooth' })
    }
  }

  return (
    <section id="screenshots" className="w-full py-24 sm:py-28 lg:py-32 text-left">
      <div className="w-full max-w-[1440px] mx-auto px-6 sm:px-8 lg:px-12">
        
        {/* Section Header */}
        <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-10 gap-6">
          <div className="space-y-3 max-w-2xl">
            <div className="inline-flex items-center gap-2">
              <span className="hairline-tag">
                <span>App interface</span>
              </span>
              <span className="hairline-tag">
                <span>Demo data</span>
              </span>
            </div>
            <h2 className="font-medium tracking-tight text-[var(--v3-text-primary)]">
              Real device captures
            </h2>
            <p className="text-[17px] sm:text-[18px] text-[var(--v3-text-secondary)] leading-relaxed font-normal">
              Native Android interface running on device with system dark mode and populated tracking data.
            </p>
          </div>

          {/* Controls */}
          <div className="flex items-center gap-2.5 shrink-0">
            <button
              type="button"
              onClick={() => scroll('left')}
              className="w-11 h-11 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] flex items-center justify-center text-[var(--v3-text-secondary)] hover:text-[var(--v3-text-primary)] hover:border-[var(--v3-accent)] transition-colors cursor-pointer"
              aria-label="Previous preview"
            >
              <ArrowLeft className="w-5 h-5 stroke-[1.5]" />
            </button>
            <button
              type="button"
              onClick={() => scroll('right')}
              className="w-11 h-11 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] flex items-center justify-center text-[var(--v3-text-secondary)] hover:text-[var(--v3-text-primary)] hover:border-[var(--v3-accent)] transition-colors cursor-pointer"
              aria-label="Next preview"
            >
              <ArrowRight className="w-5 h-5 stroke-[1.5]" />
            </button>
          </div>
        </div>

        {/* Carousel with scrollbar hidden via .no-scrollbar */}
        <div
          ref={scrollContainerRef}
          className="flex gap-6 overflow-x-auto no-scrollbar py-3 scroll-smooth"
        >
          {APP_CONFIG.appScreenshots.map((screen) => {
            const hasError = imageErrors[screen.id]
            return (
              <div
                key={screen.id}
                className="shrink-0 w-[240px] sm:w-[270px] flex flex-col space-y-3 group cursor-pointer"
                onClick={() => setSelectedScreenshot(screen)}
              >
                {/* Phone Frame */}
                <div className="relative w-full aspect-[9/19] rounded-[32px] bg-[#101211] p-1.5 border-[3px] border-[#222825] overflow-hidden group-hover:border-[var(--v3-border-highlight)] transition-colors shadow-lg">
                  {hasError ? (
                    <CodeRenderedScreenFallback
                      id={screen.id}
                      title={screen.title}
                      category={screen.category}
                    />
                  ) : (
                    <img
                      src={screen.src}
                      alt={screen.alt || screen.title}
                      width={screen.width || 1080}
                      height={screen.height || 2400}
                      className="w-full h-full object-cover object-top rounded-[26px]"
                      loading="lazy"
                      onError={() => handleImageError(screen.id)}
                    />
                  )}
                </div>

                {/* Caption */}
                <div className="pt-1 space-y-0.5">
                  <span className="text-xs text-[var(--v3-text-muted)] font-mono uppercase tracking-wider">
                    {screen.category}
                  </span>
                  <h3 className="text-base font-medium text-[var(--v3-text-primary)]">
                    {screen.title}
                  </h3>
                  <p className="text-xs text-[var(--v3-text-secondary)] line-clamp-2">
                    {screen.desc}
                  </p>
                </div>
              </div>
            )
          })}
        </div>

        {/* Modal lightbox */}
        {selectedScreenshot && (
          <div
            role="dialog"
            aria-modal="true"
            className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-xs"
            onClick={() => setSelectedScreenshot(null)}
          >
            <div
              className="relative max-w-sm w-full p-5 rounded-2xl bg-[var(--v3-surface-elevated)] border border-[var(--v3-border)] space-y-4 shadow-2xl"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-base font-medium text-[var(--v3-text-primary)]">
                    {selectedScreenshot.title}
                  </h3>
                  <p className="text-xs text-[var(--v3-text-secondary)]">
                    {selectedScreenshot.desc}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => setSelectedScreenshot(null)}
                  className="p-1.5 rounded-lg text-[var(--v3-text-muted)] hover:text-[var(--v3-text-primary)] bg-[var(--v3-surface-secondary)]"
                  aria-label="Close"
                >
                  <X className="w-5 h-5 stroke-[1.5]" />
                </button>
              </div>

              <div className="relative w-full aspect-[9/19] rounded-2xl overflow-hidden border border-[var(--v3-border)] bg-black">
                {imageErrors[selectedScreenshot.id] ? (
                  <CodeRenderedScreenFallback
                    id={selectedScreenshot.id}
                    title={selectedScreenshot.title}
                    category={selectedScreenshot.category}
                  />
                ) : (
                  <img
                    src={selectedScreenshot.src}
                    alt={selectedScreenshot.alt || selectedScreenshot.title}
                    width={selectedScreenshot.width || 1080}
                    height={selectedScreenshot.height || 2400}
                    className="w-full h-full object-cover object-top"
                  />
                )}
              </div>
            </div>
          </div>
        )}

      </div>
    </section>
  )
}
