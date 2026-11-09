import React from 'react'

interface PhoneMockupProps {
  imageSrc?: string
  alt?: string
  className?: string
  children?: React.ReactNode
}

export const PhoneMockup: React.FC<PhoneMockupProps> = ({
  imageSrc = '/assets/real_screenshots/home_dashboard.webp',
  alt = 'KcalGrind Android Interface',
  className = '',
  children,
}) => {
  return (
    <div className={`relative mx-auto select-none ${className}`}>
      {/* Flagship Android Chassis with clamped height and proportional width */}
      <div
        id="hero-phone-mockup"
        className="relative max-w-[calc(100vw-48px)] rounded-[32px] bg-[#101211] p-2 border-[3.5px] border-[#222825] shadow-2xl flex flex-col overflow-hidden"
        style={{
          height: 'clamp(440px, 72vh, 680px)',
          width: 'calc(clamp(440px, 72vh, 680px) * 0.49)',
        }}
      >
        
        {/* Subtle camera punch-hole */}
        <div className="absolute top-2 left-1/2 -translate-x-1/2 w-3 h-3 rounded-full bg-[#080a09] border border-white/10 z-30 flex items-center justify-center">
          <div className="w-1 h-1 rounded-full bg-[#191d1b]" />
        </div>

        {/* Status Bar */}
        <div className="relative z-20 flex items-center justify-between px-3 pt-0.5 pb-1 text-[10px] font-mono text-[#b2bab5] select-none">
          <span className="font-medium text-white/90">9:41</span>
          <div className="flex items-center space-x-1.5 text-[10px]">
            <span className="text-[#38c77b] font-medium text-[9px]">5G</span>
            <div className="w-3 h-1.5 rounded-[2px] border border-white/50 p-[1px] flex items-center">
              <div className="h-full w-2 bg-[#38c77b] rounded-[1px]" />
            </div>
          </div>
        </div>

        {/* Screen Display Area (Always dark OLED Android styling) */}
        <div
          className="relative flex-1 w-full bg-[#191d1b] rounded-[24px] overflow-hidden border border-white/5 text-[#f4f6f4]"
          style={{
            ['--v3-text-primary' as any]: '#f4f6f4',
            ['--v3-text-secondary' as any]: '#b2bab5',
            ['--v3-text-muted' as any]: '#858f89',
            ['--v3-progress-track' as any]: '#2b332e',
            ['--v3-calorie-ring' as any]: '#38c77b',
            ['--v3-protein-ring' as any]: '#5390d9',
            ['--v3-carbs-ring' as any]: '#a8b3ac',
            ['--v3-fats-ring' as any]: '#858f89',
            ['--v3-border' as any]: 'rgba(255, 255, 255, 0.08)',
          }}
        >
          {children ? (
            children
          ) : (
            <img
              src={imageSrc}
              alt={alt}
              width={1080}
              height={2400}
              className="w-full h-full object-cover object-top"
              loading="lazy"
            />
          )}
        </div>

        {/* Android Gesture Navigation Pill */}
        <div className="relative z-20 flex justify-center py-0.5">
          <div className="w-14 h-1 bg-white/30 rounded-full" />
        </div>
      </div>
    </div>
  )
}
