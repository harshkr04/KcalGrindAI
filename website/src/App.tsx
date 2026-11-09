import React, { useState, useEffect } from 'react'
import { Header } from './components/Header'
import { HeroSection } from './components/HeroSection'
import { InteractiveFoodScanner } from './components/InteractiveFoodScanner'
import { FeaturesSection } from './components/FeaturesSection'
import { ScreenshotGallery } from './components/ScreenshotGallery'
import { WidgetShowcaseSection } from './components/WidgetShowcaseSection'
import { InteractiveMacroSimulator } from './components/InteractiveMacroSimulator'
import { WhyKcalGrindSection } from './components/WhyKcalGrindSection'
import { FAQAccordion } from './components/FAQAccordion'
import { DownloadCTA } from './components/DownloadCTA'
import { Footer } from './components/Footer'
import { BackToTop } from './components/BackToTop'

function resolveInitialTheme(): 'dark' | 'light' {
  if (typeof window === 'undefined') return 'dark'
  const saved = localStorage.getItem('kcalgrind-theme')
  if (saved === 'dark' || saved === 'light') return saved
  if (window.matchMedia?.('(prefers-color-scheme: light)').matches) return 'light'
  return 'dark'
}

export const App: React.FC = () => {
  const [theme, setTheme] = useState<'dark' | 'light'>(resolveInitialTheme)

  useEffect(() => {
    localStorage.setItem('kcalgrind-theme', theme)
    if (theme === 'light') {
      document.documentElement.classList.add('theme-light')
    } else {
      document.documentElement.classList.remove('theme-light')
    }
  }, [theme])

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'))
  }

  return (
    <div className="min-h-screen bg-[var(--v3-bg)] text-[var(--v3-text-primary)] font-sans antialiased selection:bg-[var(--v3-accent)]/20 selection:text-[var(--v3-text-primary)]">
      {/* 1. Header Navigation */}
      <Header
        theme={theme}
        onToggleTheme={toggleTheme}
      />

      {/* 2. Main Sections */}
      <main className="w-full flex flex-col">
        {/* Section 1: Hero with Energy Ring signature and phone preview */}
        <HeroSection />

        {/* Section 2: Interactive Food Scanner (Example with editable portion check) */}
        <InteractiveFoodScanner />

        {/* Section 3: Core Architecture Features */}
        <FeaturesSection />

        {/* Section 4: App Screenshot Gallery (Actual Device Screens) */}
        <ScreenshotGallery />

        {/* Section 5: Widget Showcase (4x2, 2x2, 4x1) */}
        <WidgetShowcaseSection theme={theme} />

        {/* Section 6: Interactive Calorie & Macro Simulator */}
        <InteractiveMacroSimulator />

        {/* Section 7: Why KcalGrind (Privacy & Storage) */}
        <WhyKcalGrindSection />

        {/* Section 8: FAQs */}
        <FAQAccordion />

        {/* Section 9: Pre-launch Call to Action */}
        <DownloadCTA />
      </main>

      {/* 3. Footer */}
      <Footer />

      {/* Back to top helper */}
      <BackToTop />
    </div>
  )
}

export default App
