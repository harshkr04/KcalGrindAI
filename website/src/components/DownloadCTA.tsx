import React from 'react'
import { ShieldCheck, ArrowRight } from 'lucide-react'

export const DownloadCTA: React.FC = () => {
  return (
    <section id="download" className="w-full py-24 sm:py-28 lg:py-32 text-left">
      <div className="w-full max-w-[1440px] mx-auto px-6 sm:px-8 lg:px-12">
        
        <div className="v3-card p-8 sm:p-12 lg:p-14 flex flex-col md:flex-row items-center justify-between gap-8">
          
          {/* Left: Sproutling cameo & text */}
          <div className="flex items-start gap-5 max-w-2xl">
            <img
              src="/assets/sproutling/ic_sproutling_waving.svg"
              width="56"
              height="56"
              alt="Sproutling"
              className="shrink-0 mt-1"
            />
            <div className="space-y-3">
              <h2 className="text-2xl sm:text-3xl font-medium tracking-tight text-[var(--v3-text-primary)]">
                Join the Android Closed Beta
              </h2>
              <p className="text-[17px] sm:text-[18px] text-[var(--v3-text-secondary)] leading-relaxed font-normal">
                KcalGrind is running a closed test track on Google Play. Submit your Google account email via our test intake form to receive an invitation.
              </p>
              <div className="pt-1 flex flex-wrap items-center gap-3 text-xs sm:text-sm text-[var(--v3-text-muted)] font-mono">
                <div className="flex items-center gap-1.5">
                  <ShieldCheck className="w-4 h-4 text-[var(--v3-accent)]" />
                  <span>On-device diary</span>
                </div>
                <span>&bull;</span>
                <span>USDA &amp; ICMR-NIN</span>
                <span>&bull;</span>
                <span>Health Connect</span>
                <span>&bull;</span>
                <span>Home-screen widgets</span>
              </div>
            </div>
          </div>

          {/* Right: Link to Google Form */}
          <div className="w-full md:w-auto md:min-w-[340px] shrink-0 space-y-2.5">
            <a
              href="https://forms.gle/placeholder"
              target="_blank"
              rel="noopener noreferrer"
              className="w-full px-6 py-3.5 rounded-xl bg-[#16834D] hover:bg-[#126b3f] text-white text-sm font-medium shadow-sm flex items-center justify-center gap-2 cursor-pointer transition-colors"
            >
              <span>Join Closed Beta (Google Form)</span>
              <ArrowRight className="w-4 h-4" />
            </a>
            <p className="text-[11px] text-[var(--v3-text-muted)] leading-relaxed">
              We only use this to invite you to the Android closed test on Google Play and delete it when testing finishes. See our{' '}
              <a href="/privacy" className="text-[var(--v3-accent)] hover:underline">
                Privacy Policy
              </a>.
            </p>
          </div>

        </div>

      </div>
    </section>
  )
}
