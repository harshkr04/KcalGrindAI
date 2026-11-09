import React from 'react'
import { Camera, Database, Bot, LayoutGrid } from 'lucide-react'

export const FeaturesSection: React.FC = () => {
  const features = [
    {
      id: 'offline',
      title: 'On-device food diary',
      desc: 'Your food log is stored on your device and works offline. Quick food search and daily logging stay fast whether you have cell reception or not.',
      icon: <Database className="w-6 h-6 text-[var(--v3-accent)] stroke-[1.5]" />,
      tag: 'Offline storage',
    },
    {
      id: 'scanner',
      title: 'Photo, voice & text logging',
      desc: 'Snap a plate, speak what you ate, or type natural meal notes. Analysis passes through our server to Gemini to extract ingredients and portions.',
      icon: <Camera className="w-6 h-6 text-[var(--v3-accent)] stroke-[1.5]" />,
      tag: 'Multi-modal AI',
    },
    {
      id: 'databases',
      title: 'USDA & ICMR-NIN food data',
      desc: 'Combines USDA FoodData Central and ICMR-NIN IFCT staple data with curated regional recipe estimates for authentic dishes.',
      icon: <Bot className="w-6 h-6 text-[var(--v3-accent)] stroke-[1.5]" />,
      tag: 'Nutritional data',
    },
    {
      id: 'widgets',
      title: 'Home-screen widgets',
      desc: 'Native 4Ã—2, 2Ã—2, and 4Ã—1 Android home-screen widgets with one-tap water logging, Energy Ring status, and a public privacy toggle.',
      icon: <LayoutGrid className="w-6 h-6 text-[var(--v3-accent)] stroke-[1.5]" />,
      tag: 'Android 10+',
    },
  ]

  return (
    <section id="features" className="w-full py-24 sm:py-28 lg:py-32 text-left">
      <div className="w-full max-w-[1440px] mx-auto px-6 sm:px-8 lg:px-12">
        
        {/* Section Header */}
        <div className="max-w-3xl mb-12 space-y-3">
          <div className="inline-flex items-center gap-2">
            <span className="hairline-tag">
              <span>Features</span>
            </span>
          </div>
          <h2 className="font-medium tracking-tight text-[var(--v3-text-primary)]">
            Designed for Android, engineered for daily use
          </h2>
          <p className="text-[17px] sm:text-[18px] text-[var(--v3-text-secondary)] leading-relaxed font-normal">
            A native utility focused on speed, reliable offline logging, and respectful privacy.
          </p>
        </div>

        {/* 4 Feature Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {features.map((item) => (
            <div
              key={item.id}
              className="v3-card p-6 flex flex-col justify-between space-y-5"
            >
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="w-12 h-12 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] flex items-center justify-center">
                    {item.icon}
                  </div>
                  <span className="text-xs font-mono px-2.5 py-1 rounded-md bg-[var(--v3-surface-secondary)] text-[var(--v3-text-secondary)] border border-[var(--v3-border)]">
                    {item.tag}
                  </span>
                </div>

                <h3 className="text-lg font-medium text-[var(--v3-text-primary)]">
                  {item.title}
                </h3>
                <p className="text-sm sm:text-[15px] text-[var(--v3-text-secondary)] leading-relaxed font-normal">
                  {item.desc}
                </p>
              </div>
            </div>
          ))}
        </div>

      </div>
    </section>
  )
}
