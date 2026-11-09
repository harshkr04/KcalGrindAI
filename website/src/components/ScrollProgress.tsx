import React, { useEffect, useState } from 'react'

export const ScrollProgress: React.FC = () => {
  const [progress, setProgress] = useState(0)

  useEffect(() => {
    const handleScroll = () => {
      const totalHeight = document.documentElement.scrollHeight - window.innerHeight
      if (totalHeight > 0) {
        const currentProgress = (window.scrollY / totalHeight) * 100
        setProgress(Math.min(100, Math.max(0, currentProgress)))
      }
    }

    window.addEventListener('scroll', handleScroll, { passive: true })
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  return (
    <div
      className="fixed top-0 left-0 right-0 h-[2.5px] bg-transparent z-50 pointer-events-none"
      role="progressbar"
      aria-valuenow={Math.round(progress)}
      aria-valuemin={0}
      aria-valuemax={100}
    >
      <div
        className="h-full bg-gradient-to-r from-[var(--v3-emerald-deep)] via-[var(--v3-emerald)] to-[#4ADE80] transition-all duration-150 ease-out shadow-[0_0_8px_var(--v3-emerald)]"
        style={{ width: `${progress}%` }}
      />
    </div>
  )
}
