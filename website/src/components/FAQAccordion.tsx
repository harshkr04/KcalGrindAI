import React, { useState } from 'react'
import { ChevronDown } from 'lucide-react'
import { FAQS } from '../config'

export const FAQAccordion: React.FC = () => {
  const [openIndex, setOpenIndex] = useState<number | null>(0)

  const toggle = (idx: number) => {
    setOpenIndex(openIndex === idx ? null : idx)
  }

  return (
    <section id="faqs" className="w-full py-24 sm:py-28 lg:py-32 text-left">
      <div className="w-full max-w-[1440px] mx-auto px-6 sm:px-8 lg:px-12">
        
        {/* Header */}
        <div className="max-w-3xl mb-12 space-y-3">
          <div className="inline-flex items-center gap-2">
            <span className="hairline-tag">
              <span>Frequently asked questions</span>
            </span>
          </div>
          <h2 className="font-medium tracking-tight text-[var(--v3-text-primary)]">
            Questions &amp; answers
          </h2>
          <p className="text-[17px] sm:text-[18px] text-[var(--v3-text-secondary)] leading-relaxed font-normal">
            Straightforward details about offline logging, AI inference, and database sources.
          </p>
        </div>

        {/* FAQs List */}
        <div className="max-w-4xl space-y-4">
          {FAQS.map((item, idx) => {
            const isOpen = openIndex === idx
            return (
              <div
                key={idx}
                className="v3-card overflow-hidden border border-[var(--v3-border)]"
              >
                <button
                  type="button"
                  onClick={() => toggle(idx)}
                  className="w-full flex items-center justify-between p-5 sm:p-6 text-left text-base sm:text-lg font-medium text-[var(--v3-text-primary)] hover:text-[var(--v3-accent)] transition-colors cursor-pointer"
                  aria-expanded={isOpen}
                >
                  <span className="pr-4">{item.question}</span>
                  <ChevronDown
                    className={`w-5 h-5 text-[var(--v3-text-muted)] transition-transform duration-150 shrink-0 ${
                      isOpen ? 'transform rotate-180 text-[var(--v3-accent)]' : ''
                    }`}
                  />
                </button>

                {isOpen && (
                  <div className="px-5 sm:px-6 pb-6 pt-1 text-sm sm:text-base text-[var(--v3-text-secondary)] leading-relaxed border-t border-[var(--v3-border)]/50">
                    {item.answer}
                  </div>
                )}
              </div>
            )
          })}
        </div>

      </div>
    </section>
  )
}
