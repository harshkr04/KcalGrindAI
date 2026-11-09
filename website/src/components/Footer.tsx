import React from 'react'
import { Shield, Trash2, Mail } from 'lucide-react'
import { APP_CONFIG } from '../config'

const CURRENT_YEAR = 2026

export const Footer: React.FC = () => {
  const scrollTo = (e: React.MouseEvent<HTMLAnchorElement>, href: string) => {
    e.preventDefault()
    const target = document.querySelector(href)
    if (target) {
      target.scrollIntoView({ behavior: 'smooth' })
    }
  }

  return (
    <footer className="w-full bg-[var(--v3-surface-elevated)] border-t border-[var(--v3-border)] text-[var(--v3-text-primary)] pt-16 pb-12 text-left transition-colors duration-150">
      <div className="w-full max-w-[1440px] mx-auto px-6 sm:px-8 lg:px-12">
        
        {/* Top Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-12 gap-10 pb-12 border-b border-[var(--v3-border)]">
          
          {/* Brand & Dataset Attribution Column (4 cols) */}
          <div className="lg:col-span-5 space-y-4">
            <div className="flex items-center gap-3">
              <img
                src="/assets/sproutling/ic_sproutling_active.svg"
                width="32"
                height="32"
                alt="KcalGrind"
              />
              <span className="text-xl font-medium tracking-tight text-[var(--v3-text-primary)]">
                {APP_CONFIG.name}
              </span>
            </div>

            <p className="text-sm text-[var(--v3-text-secondary)] leading-relaxed max-w-sm">
              {APP_CONFIG.description}
            </p>

            <div className="pt-1 text-xs text-[var(--v3-text-muted)] space-y-1">
              <p className="font-semibold text-[var(--v3-text-secondary)]">Dataset Attribution:</p>
              <p className="leading-relaxed">
                {APP_CONFIG.icmrCitation}
              </p>
              <p className="leading-relaxed">
                USDA FoodData Central, Agricultural Research Service, U.S. Department of Agriculture.
              </p>
            </div>
          </div>

          {/* Navigation Column (2 cols) */}
          <div className="lg:col-span-3 space-y-3">
            <h4 className="text-sm font-medium text-[var(--v3-text-primary)]">
              Navigation
            </h4>
            <ul className="space-y-2 text-sm text-[var(--v3-text-secondary)]">
              <li>
                <a href="#hero" onClick={(e) => scrollTo(e, '#hero')} className="hover:text-[var(--v3-accent)]">
                  Overview
                </a>
              </li>
              <li>
                <a href="#ai-scanner-demo" onClick={(e) => scrollTo(e, '#ai-scanner-demo')} className="hover:text-[var(--v3-accent)]">
                  Food scanner
                </a>
              </li>
              <li>
                <a href="#features" onClick={(e) => scrollTo(e, '#features')} className="hover:text-[var(--v3-accent)]">
                  Features
                </a>
              </li>
              <li>
                <a href="#screenshots" onClick={(e) => scrollTo(e, '#screenshots')} className="hover:text-[var(--v3-accent)]">
                  Screenshots
                </a>
              </li>
              <li>
                <a href="#widgets" onClick={(e) => scrollTo(e, '#widgets')} className="hover:text-[var(--v3-accent)]">
                  Widgets
                </a>
              </li>
              <li>
                <a href="#macro-simulator" onClick={(e) => scrollTo(e, '#macro-simulator')} className="hover:text-[var(--v3-accent)]">
                  Target calculator
                </a>
              </li>
              <li>
                <a href="#faqs" onClick={(e) => scrollTo(e, '#faqs')} className="hover:text-[var(--v3-accent)]">
                  FAQs
                </a>
              </li>
            </ul>
          </div>

          {/* Privacy & Legal Column (4 cols) */}
          <div className="lg:col-span-4 space-y-3">
            <h4 className="text-sm font-medium text-[var(--v3-text-primary)]">
              Data &amp; Privacy
            </h4>
            <ul className="space-y-2.5 text-sm text-[var(--v3-text-secondary)]">
              <li>
                <a
                  href="/privacy"
                  className="inline-flex items-center gap-2 hover:text-[var(--v3-accent)]"
                >
                  <Shield className="w-4 h-4 text-[var(--v3-accent)]" />
                  <span>Privacy Policy</span>
                </a>
              </li>
              <li>
                <a
                  href="/delete-account"
                  className="inline-flex items-center gap-2 hover:text-[var(--v3-accent)]"
                >
                  <Trash2 className="w-4 h-4 text-[var(--v3-accent)]" />
                  <span>Delete account &amp; data</span>
                </a>
              </li>
              <li className="pt-1">
                <a
                  href={`mailto:${APP_CONFIG.contactEmail}`}
                  className="inline-flex items-center gap-2 hover:text-[var(--v3-accent)]"
                >
                  <Mail className="w-4 h-4" />
                  <span>Support: {APP_CONFIG.contactEmail}</span>
                </a>
              </li>
              <li>
                <a
                  href={`mailto:${APP_CONFIG.privacyEmail}`}
                  className="inline-flex items-center gap-2 hover:text-[var(--v3-accent)]"
                >
                  <Mail className="w-4 h-4" />
                  <span>Privacy: {APP_CONFIG.privacyEmail}</span>
                </a>
              </li>
            </ul>
          </div>

        </div>

        {/* Bottom Strip */}
        <div className="pt-8 flex flex-col sm:flex-row items-start sm:items-center justify-between text-sm text-[var(--v3-text-muted)] gap-3">
          <div>
            &copy; {CURRENT_YEAR} {APP_CONFIG.name}. All rights reserved.
          </div>
          <div className="flex items-center gap-3 text-xs sm:text-sm">
            <span>On-device storage</span>
            <span>&bull;</span>
            <span>USDA &amp; ICMR-NIN</span>
            <span>&bull;</span>
            <span>Home-screen widgets</span>
          </div>
        </div>

      </div>
    </footer>
  )
}
