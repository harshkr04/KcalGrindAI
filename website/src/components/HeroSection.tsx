import React from 'react'
import {
  ChevronRight,
  Database,
  Shield,
  LayoutGrid,
} from 'lucide-react'
import { PhoneMockup } from './PhoneMockup'
import { HeroPhoneScreen } from './HeroPhoneScreen'

export const HeroSection: React.FC = () => {
  const scrollTo = (e: React.MouseEvent<HTMLAnchorElement>, href: string) => {
    e.preventDefault()
    const target = document.querySelector(href)
    if (target) {
      target.scrollIntoView({ behavior: 'smooth' })
    }
  }

  return (
    <section
      id="hero"
      className="relative w-full pt-[8px] pb-6 sm:pb-8 lg:pb-10 text-left flex flex-col justify-start"
    >
      <div className="w-full max-w-[1440px] mx-auto px-6 sm:px-8 lg:px-12">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-12 items-start">
          
          {/* Left Column: Headline, Plain Subhead, Actions & Desktop Pillars */}
          <div className="lg:col-span-7 space-y-3 sm:space-y-4 lg:space-y-5">
            
            {/* Tag */}
            <div className="inline-flex items-center gap-2">
              <span className="hairline-tag">
                <span className="w-2 h-2 rounded-full bg-[var(--v3-accent)]" />
                <span>Android &bull; USDA &amp; ICMR-NIN &bull; Offline storage</span>
              </span>
            </div>

            {/* Headline with responsive clamp (>=36px at 390, >=56px at 1366) */}
            <h1
              className="font-medium tracking-tight text-[var(--v3-text-primary)] leading-[1.12]"
              style={{ fontSize: 'clamp(36px, 2.2vw + 27.5px, 68px)' }}
            >
              Calorie and nutrition tracking built for Android
            </h1>

            {/* Subhead stating plainly that it includes Indian food data (ICMR-NIN) alongside USDA */}
            <p className="text-[14px] sm:text-[16px] lg:text-[17px] text-[var(--v3-text-secondary)] leading-snug sm:leading-relaxed max-w-2xl font-normal">
              Tracks calories and macros with Indian food data from ICMR-NIN alongside USDA benchmarks. Your food log is stored on your device and works offline.
            </p>

            {/* Actions: Primary button "See how it works" */}
            <div className="pt-0.5 flex flex-wrap items-center gap-3">
              <a
                href="#features"
                onClick={(e) => scrollTo(e, '#features')}
                className="inline-flex items-center gap-2 px-4 py-2.5 sm:px-6 sm:py-3.5 rounded-xl bg-[#16834d] text-[#ffffff] text-sm sm:text-[17px] font-medium hover:brightness-110 transition-colors shadow-sm"
              >
                <span>See how it works</span>
                <ChevronRight className="w-4 h-4 stroke-[2]" />
              </a>
            </div>

            {/* Core Product Pillars (Desktop display) */}
            <div className="hidden lg:grid grid-cols-3 gap-3.5 pt-4 border-t border-[var(--v3-border)]">
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-sm font-medium text-[var(--v3-text-primary)]">
                  <Database className="w-4 h-4 text-[var(--v3-accent)] stroke-[1.5]" />
                  <span>On-device log</span>
                </div>
                <p className="text-xs sm:text-sm text-[var(--v3-text-secondary)] leading-normal">
                  Your food diary stays on your phone and logs meals without an internet connection.
                </p>
              </div>

              <div className="space-y-1">
                <div className="flex items-center gap-2 text-sm font-medium text-[var(--v3-text-primary)]">
                  <Shield className="w-4 h-4 text-[var(--v3-accent)] stroke-[1.5]" />
                  <span>USDA &amp; ICMR-NIN</span>
                </div>
                <p className="text-xs sm:text-sm text-[var(--v3-text-secondary)] leading-normal">
                  Official Indian food composition tables for staples paired with curated regional recipes and USDA data.
                </p>
              </div>

              <div className="space-y-1">
                <div className="flex items-center gap-2 text-sm font-medium text-[var(--v3-text-primary)]">
                  <LayoutGrid className="w-4 h-4 text-[var(--v3-accent)] stroke-[1.5]" />
                  <span>Home-screen widgets</span>
                </div>
                <p className="text-xs sm:text-sm text-[var(--v3-text-secondary)] leading-normal">
                  Native 4×2, 2×2, and 4×1 sizes with public privacy mode and water quick-log.
                </p>
              </div>
            </div>

          </div>

          {/* Right Column: Phone Mockup with soft green radial backdrop (No tab row above) */}
          <div className="lg:col-span-5 relative flex justify-center items-center py-2">
            
            {/* One soft green radial behind the phone at <= 25% opacity */}
            <div
              className="absolute w-[280px] h-[280px] sm:w-[380px] sm:h-[380px] rounded-full pointer-events-none"
              style={{
                background: 'var(--v3-radial-glow)',
                filter: 'blur(30px)',
                zIndex: 0,
              }}
            />

            {/* Phone Chassis displaying real app Home screen with unified demo day */}
            <div className="relative z-10 w-full flex justify-center">
              <PhoneMockup>
                <HeroPhoneScreen />
              </PhoneMockup>
            </div>

          </div>

        </div>

        {/* Mobile Product Pillars (Placed below phone on small screens) */}
        <div className="lg:hidden grid grid-cols-1 sm:grid-cols-3 gap-3 pt-6 mt-4 border-t border-[var(--v3-border)]">
          <div className="flex items-start gap-2.5">
            <Database className="w-4 h-4 text-[var(--v3-accent)] stroke-[1.5] mt-0.5 shrink-0" />
            <div>
              <h4 className="text-sm font-medium text-[var(--v3-text-primary)]">On-device log</h4>
              <p className="text-xs text-[var(--v3-text-secondary)] leading-normal">
                Your food diary stays on your phone and works offline.
              </p>
            </div>
          </div>

          <div className="flex items-start gap-2.5">
            <Shield className="w-4 h-4 text-[var(--v3-accent)] stroke-[1.5] mt-0.5 shrink-0" />
            <div>
              <h4 className="text-sm font-medium text-[var(--v3-text-primary)]">USDA &amp; ICMR-NIN</h4>
              <p className="text-xs text-[var(--v3-text-secondary)] leading-normal">
                Staple tables and curated regional recipes paired with USDA benchmarks.
              </p>
            </div>
          </div>

          <div className="flex items-start gap-2.5">
            <LayoutGrid className="w-4 h-4 text-[var(--v3-accent)] stroke-[1.5] mt-0.5 shrink-0" />
            <div>
              <h4 className="text-sm font-medium text-[var(--v3-text-primary)]">Home-screen widgets</h4>
              <p className="text-xs text-[var(--v3-text-secondary)] leading-normal">
                4×2, 2×2, and 4×1 sizes with public privacy mode.
              </p>
            </div>
          </div>
        </div>

      </div>
    </section>
  )
}
