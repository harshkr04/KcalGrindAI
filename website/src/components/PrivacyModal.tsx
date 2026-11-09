import React from 'react'
import { X, ShieldCheck } from 'lucide-react'

export interface PrivacyModalProps {
  isOpen: boolean
  onClose: () => void
}

export const PrivacyModal: React.FC<PrivacyModalProps> = ({ isOpen, onClose }) => {
  if (!isOpen) return null

  return (
    <div
      role="dialog"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs"
      onClick={onClose}
    >
      <div
        className="relative w-full max-w-xl max-h-[80vh] v3-card rounded-2xl p-6 shadow-xl border border-[var(--v3-border)] text-left flex flex-col overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between pb-3 border-b border-[var(--v3-border)] shrink-0">
          <div className="flex items-center gap-2.5">
            <ShieldCheck className="w-5 h-5 text-[var(--v3-accent)]" />
            <div>
              <h3 className="text-base font-medium text-[var(--v3-text-primary)]">
                Privacy Policy Summary
              </h3>
              <p className="text-xs text-[var(--v3-text-muted)] font-mono">
                Effective: September 13, 2026
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded text-[var(--v3-text-muted)] hover:text-[var(--v3-text-primary)]"
            aria-label="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="overflow-y-auto py-4 space-y-3 text-xs text-[var(--v3-text-secondary)] leading-relaxed">
          <p>
            Your food log is stored on your device and works offline.
          </p>
          <p>
            Photos sent for meal analysis and voice recordings for food logging are processed in memory through Google's Gemini API under Google's terms. Photo and audio files are not saved to your account or stored on our servers.
          </p>
          <p>
            We do not sell personal data to advertisers or third-party data brokers.
          </p>
          <div className="pt-2">
            <a
              href="/privacy"
              className="text-[var(--v3-accent)] hover:underline font-medium"
            >
              View full Privacy Policy page &rarr;
            </a>
          </div>
        </div>
      </div>
    </div>
  )
}
