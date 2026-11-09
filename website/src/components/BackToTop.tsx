import React, { useEffect, useState } from 'react'
import { ArrowUp } from 'lucide-react'

export const BackToTop: React.FC = () => {
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const toggleVisible = () => {
      setVisible(window.scrollY > 400)
    }

    window.addEventListener('scroll', toggleVisible, { passive: true })
    return () => window.removeEventListener('scroll', toggleVisible)
  }, [])

  const scrollToTop = () => {
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    })
  }

  return (
    <button
      type="button"
      onClick={scrollToTop}
      aria-label="Scroll to top"
      className={`fixed bottom-6 right-6 z-40 p-2.5 rounded-lg bg-[var(--v3-surface-secondary)] text-[var(--v3-text-secondary)] border border-[var(--v3-border)] hover:text-[var(--v3-text-primary)] hover:border-[var(--v3-accent)] transition-opacity duration-150 cursor-pointer ${
        visible ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none'
      }`}
    >
      <ArrowUp className="w-4 h-4 stroke-[1.5]" />
    </button>
  )
}
