import React from 'react'

export const BotanicalAccents: React.FC = () => {
  return (
    <div className="absolute inset-0 pointer-events-none overflow-hidden z-0 select-none">
      {/* Top Right Subtle Emerald Leaf Silhouette */}
      <div className="absolute -top-12 -right-16 w-72 sm:w-96 h-72 sm:h-96 opacity-20 sm:opacity-25 blur-[3px] transform rotate-12 transition-all">
        <svg viewBox="0 0 400 400" className="w-full h-full" fill="none">
          <g filter="url(#leaf-blur-v3)">
            {/* Main stem */}
            <path
              d="M380 20 C300 120, 240 220, 160 360"
              stroke="#16834D"
              strokeWidth="5"
              strokeLinecap="round"
              opacity="0.4"
            />
            {/* Leaf 1 */}
            <path
              d="M320 80 C360 40, 420 70, 390 120 C360 160, 310 130, 320 80 Z"
              fill="url(#emerald-v3-grad)"
            />
            {/* Leaf 2 */}
            <path
              d="M270 140 C230 90, 270 40, 330 70 C370 100, 320 160, 270 140 Z"
              fill="url(#emerald-v3-grad)"
            />
            {/* Leaf 3 */}
            <path
              d="M230 210 C180 180, 200 120, 270 140 C310 160, 280 230, 230 210 Z"
              fill="url(#emerald-v3-grad)"
            />
          </g>
          <defs>
            <filter id="leaf-blur-v3" x="-20%" y="-20%" width="140%" height="140%">
              <feGaussianBlur stdDeviation="5" />
            </filter>
            <linearGradient id="emerald-v3-grad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#38C77B" stopOpacity="0.4" />
              <stop offset="60%" stopColor="#16834D" stopOpacity="0.25" />
              <stop offset="100%" stopColor="#101211" stopOpacity="0.1" />
            </linearGradient>
          </defs>
        </svg>
      </div>

      {/* Mid Left Subtle Atmospheric Silhouette */}
      <div className="absolute top-1/3 -left-12 w-48 sm:w-64 h-48 sm:h-64 opacity-15 sm:opacity-20 blur-[4px] transform -rotate-45">
        <svg viewBox="0 0 200 200" className="w-full h-full" fill="none">
          <path
            d="M20 180 C50 120, 120 100, 170 30 C150 90, 100 160, 20 180 Z"
            fill="#16834D"
            opacity="0.3"
          />
        </svg>
      </div>
    </div>
  )
}
