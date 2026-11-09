import React from 'react'
import { Database, ShieldCheck, Lock, Trash2 } from 'lucide-react'

export const WhyKcalGrindSection: React.FC = () => {
  const cards = [
    {
      id: 'offline-sync',
      title: 'On-device log & account sync',
      desc: 'Your food log is stored on your device and works offline. When you sign in, tapping "Sync now" in Profile backs up your logs, targets, and history to your private cloud account.',
      icon: <Database className="w-6 h-6 text-[var(--v3-accent)] stroke-[1.5]" />,
    },
    {
      id: 'media-privacy',
      title: 'Photos & audio not stored',
      desc: 'Photos sent for meal analysis and voice recordings for food logging are processed in memory through Google\'s Gemini API under Google\'s terms. Photo and audio files are not saved to your account or stored on our servers.',
      icon: <ShieldCheck className="w-6 h-6 text-[var(--v3-accent)] stroke-[1.5]" />,
    },
    {
      id: 'chats-saved',
      title: 'Analysis results & coach chats',
      desc: 'When you tap "Sync now", structured nutrition analysis results and Sproutling AI coach conversations are backed up to your account alongside your diary so your history remains available across sessions.',
      icon: <Lock className="w-6 h-6 text-[var(--v3-accent)] stroke-[1.5]" />,
    },
    {
      id: 'delete-anytime',
      title: 'Delete anytime',
      desc: 'Delete your account and all associated data at any time directly in the app (Profile → Delete Account) or online. We wipe your profile, food logs, goals, and history across all systems.',
      icon: <Trash2 className="w-6 h-6 text-[var(--v3-accent)] stroke-[1.5]" />,
    },
  ]

  return (
    <section id="why-different" className="w-full py-24 sm:py-28 lg:py-32 text-left">
      <div className="w-full max-w-[1440px] mx-auto px-6 sm:px-8 lg:px-12">
        
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-start">
          
          {/* Left Column: Heading & Summary */}
          <div className="lg:col-span-5 space-y-4">
            <div className="inline-flex items-center gap-2">
              <span className="hairline-tag">
                <span>Privacy &amp; data handling</span>
              </span>
            </div>

            <h2 className="font-medium tracking-tight text-[var(--v3-text-primary)]">
              How KcalGrind handles your data
            </h2>

            <p className="text-[17px] sm:text-[18px] text-[var(--v3-text-secondary)] leading-relaxed font-normal">
              We believe a daily health tool must be completely transparent about data storage and cloud processing. Clear boundaries on where your records live:
            </p>

            <div className="pt-3">
              <a
                href="/privacy"
                className="inline-flex items-center gap-2 text-sm sm:text-base text-[var(--v3-accent)] font-medium hover:underline"
              >
                <span>Read our full Privacy Policy</span>
                <span>&rarr;</span>
              </a>
            </div>
          </div>

          {/* Right Column: 4 Cards */}
          <div className="lg:col-span-7 grid grid-cols-1 sm:grid-cols-2 gap-5">
            {cards.map((card) => (
              <div
                key={card.id}
                className="v3-card p-6 space-y-3.5"
              >
                <div className="w-12 h-12 rounded-xl bg-[var(--v3-surface-secondary)] border border-[var(--v3-border)] flex items-center justify-center">
                  {card.icon}
                </div>
                <h3 className="text-lg font-medium text-[var(--v3-text-primary)]">
                  {card.title}
                </h3>
                <p className="text-sm sm:text-[15px] text-[var(--v3-text-secondary)] leading-relaxed font-normal">
                  {card.desc}
                </p>
              </div>
            ))}
          </div>

        </div>

      </div>
    </section>
  )
}
