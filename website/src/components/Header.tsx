import React, { useState, useEffect, useRef, useCallback } from 'react'
import { Menu, X, Sun, Moon } from 'lucide-react'
import { APP_CONFIG } from '../config'

interface HeaderProps {
  theme: 'dark' | 'light'
  onToggleTheme: () => void
}

interface NavLinkItem {
  label: string
  href: string
  id: string
}

export const Header: React.FC<HeaderProps> = ({ theme, onToggleTheme }) => {
  const [isScrolled, setIsScrolled] = useState(() =>
    typeof window !== 'undefined' ? window.scrollY > 40 : false
  )
  const [isMorphing, setIsMorphing] = useState(false)
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [activeSection, setActiveSection] = useState('hero')

  const sentinelRef = useRef<HTMLDivElement>(null)
  const menuButtonRef = useRef<HTMLButtonElement>(null)
  const mobileSheetRef = useRef<HTMLDivElement>(null)
  const prevScrolledRef = useRef(isScrolled)

  // Primary 4 navigation links requested: Home, Food scanner, Features, FAQ
  const navLinks: NavLinkItem[] = [
    { label: 'Home', href: '#hero', id: 'hero' },
    { label: 'Food scanner', href: '#ai-scanner-demo', id: 'ai-scanner-demo' },
    { label: 'Features', href: '#features', id: 'features' },
    { label: 'FAQ', href: '#faqs', id: 'faqs' },
  ]

  // Track active morphing animation (420ms window) for selective GPU promotion
  useEffect(() => {
    if (prevScrolledRef.current !== isScrolled) {
      prevScrolledRef.current = isScrolled
      setIsMorphing(true)
      const timer = window.setTimeout(() => {
        setIsMorphing(false)
      }, 420)
      return () => window.clearTimeout(timer)
    }
  }, [isScrolled])

  // rAF-throttled scroll listener with hysteresis: shrink after >40px, expand below <8px
  useEffect(() => {
    let ticking = false

    const handleScroll = () => {
      if (!ticking) {
        window.requestAnimationFrame(() => {
          const currentY = window.scrollY
          if (currentY > 40) {
            setIsScrolled(true)
          } else if (currentY < 8) {
            setIsScrolled(false)
          }

          // Active section detection
          const scrollPos = currentY + 160
          const sectionIds = ['hero', 'ai-scanner-demo', 'features', 'faqs']
          for (let i = sectionIds.length - 1; i >= 0; i--) {
            const el = document.getElementById(sectionIds[i])
            if (el && scrollPos >= el.offsetTop) {
              setActiveSection(sectionIds[i])
              break
            }
          }

          ticking = false
        })
        ticking = true
      }
    }

    handleScroll()

    window.addEventListener('scroll', handleScroll, { passive: true })
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  // Smooth scroll handler
  const scrollTo = useCallback((e: React.MouseEvent<HTMLAnchorElement>, href: string) => {
    e.preventDefault()
    setMobileMenuOpen(false)
    const target = document.querySelector(href)
    if (target) {
      target.scrollIntoView({ behavior: 'smooth' })
      if (window.history.replaceState) {
        window.history.replaceState(null, '', href)
      }
    }
  }, [])

  // Mobile menu body scroll lock & Escape key / focus trap
  useEffect(() => {
    if (!mobileMenuOpen) return

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setMobileMenuOpen(false)
        menuButtonRef.current?.focus()
        return
      }

      if (e.key === 'Tab' && mobileSheetRef.current) {
        const focusableElements = mobileSheetRef.current.querySelectorAll<HTMLElement>(
          'a[href], button:not([disabled]), input:not([disabled]), [tabindex]:not([tabindex="-1"])'
        )
        if (focusableElements.length === 0) return

        const firstElement = focusableElements[0]
        const lastElement = focusableElements[focusableElements.length - 1]

        if (e.shiftKey && document.activeElement === firstElement) {
          e.preventDefault()
          lastElement.focus()
        } else if (!e.shiftKey && document.activeElement === lastElement) {
          e.preventDefault()
          firstElement.focus()
        }
      }
    }

    document.addEventListener('keydown', handleKeyDown)

    // Focus first focusable item in sheet on open
    const focusTimer = window.setTimeout(() => {
      const focusable = mobileSheetRef.current?.querySelector<HTMLElement>(
        'button, a[href]'
      )
      focusable?.focus()
    }, 50)

    return () => {
      document.body.style.overflow = previousOverflow
      document.removeEventListener('keydown', handleKeyDown)
      window.clearTimeout(focusTimer)
    }
  }, [mobileMenuOpen])

  return (
    <>
      {/* Top of page sentinel for IntersectionObserver / hysteresis reference */}
      <div
        ref={sentinelRef}
        className="absolute top-0 left-0 w-full h-[1px] pointer-events-none"
        aria-hidden="true"
      />

      {/* Accessible skip link for keyboard navigation */}
      <a href="#hero" className="skip-to-content">
        Skip to content
      </a>

      {/* Reserved height so page layout never shifts when header morphs */}
      <div className="w-full h-[88px] pointer-events-none" aria-hidden="true" />

      {/* Fixed outer positioning wrapper */}
      <div className="site-header-fixed-container">
        <header
          className={`site-header ${isScrolled ? 'is-scrolled' : ''} ${
            isMorphing ? 'is-morphing' : ''
          }`}
        >
          <div className="w-full h-full flex items-center justify-between">

            {/* Left: Mascot Mark + KcalGrind Brand */}
            <div className="flex items-center gap-3 sm:gap-4 shrink-0">
              <a
                href="#hero"
                onClick={(e) => scrollTo(e, '#hero')}
                className="flex items-center gap-2.5 sm:gap-3 text-left select-none group min-h-[44px] min-w-[44px]"
                aria-label="KcalGrind home"
              >
                <img
                  src="/assets/sproutling/ic_sproutling_active.svg"
                  width={isScrolled ? '28' : '32'}
                  height={isScrolled ? '28' : '32'}
                  alt="KcalGrind mascot"
                  className="shrink-0 transition-transform duration-200 group-hover:scale-105"
                />
                <span
                  className={`font-medium tracking-tight text-[var(--v3-text-primary)] transition-all duration-300 ${
                    isScrolled
                      ? 'text-lg sm:text-xl'
                      : 'text-xl sm:text-2xl'
                  }`}
                >
                  {APP_CONFIG.name}
                </span>
              </a>

              {/* Top-state Nav: Immediately follows brand name on desktop */}
              <nav
                className={`hidden md:flex items-center whitespace-nowrap transition-all duration-300 ${
                  isScrolled
                    ? 'ml-2 lg:ml-4 gap-1 lg:gap-2'
                    : 'ml-6 lg:ml-10 gap-2 lg:gap-5'
                }`}
                aria-label="Primary"
              >
                {navLinks.map((link) => {
                  const isActive = activeSection === link.id
                  return (
                    <a
                      key={link.id}
                      href={link.href}
                      onClick={(e) => scrollTo(e, link.href)}
                      aria-current={isActive ? 'page' : undefined}
                      className={`min-h-[44px] inline-flex items-center px-3 py-1.5 rounded-full transition-colors duration-150 text-[16px] lg:text-[17px] whitespace-nowrap ${
                        isActive
                          ? 'text-[var(--v3-accent)] font-medium bg-[var(--v3-accent)]/10'
                          : 'text-[var(--v3-text-secondary)] hover:text-[var(--v3-text-primary)] hover:bg-[var(--v3-surface-secondary)]/50 font-normal'
                      }`}
                    >
                      {link.label}
                    </a>
                  )
                })}
              </nav>
            </div>

            {/* Right: Theme Toggle + Google Play Chip (fades in scrolled state) + Mobile Menu */}
            <div className="flex items-center gap-2 sm:gap-3 shrink-0">
              
              {/* Theme Toggle (44px min touch target) */}
              <button
                type="button"
                onClick={onToggleTheme}
                className={`min-w-[44px] min-h-[44px] rounded-xl flex items-center justify-center transition-all duration-200 cursor-pointer ${
                  isScrolled
                    ? 'w-10 h-10 bg-transparent text-[var(--v3-text-secondary)] hover:text-[var(--v3-text-primary)] hover:bg-[var(--v3-surface-secondary)]/70'
                    : 'w-11 h-11 bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] text-[var(--v3-text-secondary)] hover:text-[var(--v3-text-primary)] hover:border-[var(--v3-border-highlight)]'
                }`}
                aria-label={`Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`}
              >
                {theme === 'dark' ? (
                  <Sun className="w-4 h-4 sm:w-5 sm:h-5 stroke-[1.5]" />
                ) : (
                  <Moon className="w-4 h-4 sm:w-5 sm:h-5 stroke-[1.5]" />
                )}
              </button>

              {/* Google Play "Coming soon" chip: visible at top, smoothly fades out and becomes inert when scrolled */}
              <div
                className={`hidden sm:inline-flex items-center transition-all duration-300 ease-out ${
                  isScrolled
                    ? 'opacity-0 max-w-0 pointer-events-none invisible overflow-hidden -mr-2 scale-95'
                    : 'opacity-100 max-w-[260px] pointer-events-auto visible scale-100'
                }`}
                aria-hidden={isScrolled}
                tabIndex={-1}
                inert={isScrolled ? true : undefined}
              >
                <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] text-sm text-[var(--v3-text-secondary)] whitespace-nowrap select-none">
                  <span className="w-2 h-2 rounded-full bg-[var(--v3-accent)]" />
                  <span>Google Play &bull; Coming soon</span>
                </div>
              </div>

              {/* Mobile menu trigger button (<= 768px) */}
              <button
                ref={menuButtonRef}
                type="button"
                onClick={() => setMobileMenuOpen(true)}
                className={`md:hidden min-w-[44px] min-h-[44px] rounded-xl flex items-center justify-center text-[var(--v3-text-primary)] transition-all cursor-pointer ${
                  isScrolled
                    ? 'w-10 h-10 bg-transparent hover:bg-[var(--v3-surface-secondary)]/70'
                    : 'w-11 h-11 bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)]'
                }`}
                aria-label="Open navigation menu"
                aria-expanded={mobileMenuOpen}
                aria-controls="mobile-navigation-sheet"
              >
                <Menu className="w-5 h-5 stroke-[1.75]" />
              </button>

            </div>

          </div>
        </header>
      </div>

      {/* Accessible Mobile Menu Sheet (Dialog + Focus Trap + Esc Close) */}
      {mobileMenuOpen && (
        <div
          id="mobile-navigation-sheet"
          className="fixed inset-0 z-50 flex flex-col justify-start md:hidden"
          role="dialog"
          aria-modal="true"
          aria-label="Navigation Menu"
        >
          {/* Backdrop overlay with blur */}
          <div
            className="fixed inset-0 bg-black/60 backdrop-blur-sm transition-opacity duration-200"
            onClick={() => setMobileMenuOpen(false)}
            aria-hidden="true"
          />

          {/* Sheet Drawer */}
          <div
            ref={mobileSheetRef}
            className="relative z-10 w-full max-h-[85vh] bg-[var(--v3-surface-elevated)] border-b border-[var(--v3-border)] shadow-2xl p-6 sm:p-8 flex flex-col gap-6 overflow-y-auto"
          >
            {/* Sheet Top Bar: Logo + Close Button */}
            <div className="flex items-center justify-between pb-4 border-b border-[var(--v3-border)]">
              <div className="flex items-center gap-2.5">
                <img
                  src="/assets/sproutling/ic_sproutling_active.svg"
                  width="28"
                  height="28"
                  alt="KcalGrind logo"
                  className="shrink-0"
                />
                <span className="text-xl font-medium text-[var(--v3-text-primary)]">
                  {APP_CONFIG.name}
                </span>
              </div>
              <button
                type="button"
                onClick={() => {
                  setMobileMenuOpen(false)
                  menuButtonRef.current?.focus()
                }}
                className="min-w-[44px] min-h-[44px] w-11 h-11 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] flex items-center justify-center text-[var(--v3-text-primary)] hover:border-[var(--v3-border-highlight)] transition-colors cursor-pointer"
                aria-label="Close navigation menu"
              >
                <X className="w-5 h-5 stroke-[1.75]" />
              </button>
            </div>

            {/* Mobile Sheet Nav Links */}
            <nav className="flex flex-col space-y-1" aria-label="Mobile primary navigation">
              {navLinks.map((link) => {
                const isActive = activeSection === link.id
                return (
                  <a
                    key={link.id}
                    href={link.href}
                    onClick={(e) => scrollTo(e, link.href)}
                    aria-current={isActive ? 'page' : undefined}
                    className={`min-h-[48px] text-[18px] flex items-center justify-between px-3.5 py-3 rounded-xl transition-colors ${
                      isActive
                        ? 'text-[var(--v3-accent)] font-medium bg-[var(--v3-accent)]/10'
                        : 'text-[var(--v3-text-primary)] hover:bg-[var(--v3-surface-secondary)] font-normal'
                    }`}
                  >
                    <span>{link.label}</span>
                    {isActive && (
                      <span className="w-2 h-2 rounded-full bg-[var(--v3-accent)]" />
                    )}
                  </a>
                )
              })}
            </nav>

            {/* Sheet Footer Details */}
            <div className="pt-4 border-t border-[var(--v3-border)] flex flex-col gap-3">
              <div className="inline-flex items-center gap-2 px-3.5 py-2.5 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] text-sm text-[var(--v3-text-secondary)]">
                <span className="w-2 h-2 rounded-full bg-[var(--v3-accent)]" />
                <span>Google Play &bull; Coming soon</span>
              </div>
              <div className="text-xs text-[var(--v3-text-muted)] px-1">
                Offline-first calorie and nutrition tracker for Android
              </div>
            </div>

          </div>
        </div>
      )}
    </>
  )
}
