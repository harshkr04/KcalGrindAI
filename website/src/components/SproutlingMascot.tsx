import React from 'react'

interface SproutlingMascotProps {
  mood?: 'active' | 'content' | 'waving' | 'resting' | 'energized' | 'secure'
  size?: number
  className?: string
}

export const SproutlingMascot: React.FC<SproutlingMascotProps> = ({
  mood = 'active',
  size = 32,
  className = '',
}) => {
  return (
    <img
      src={`/assets/sproutling/ic_sproutling_${mood}.svg`}
      width={size}
      height={size}
      alt="Sproutling"
      className={`inline-block ${className}`}
      loading="lazy"
    />
  )
}
