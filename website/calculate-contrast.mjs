function hexToRgb(hex) {
  hex = hex.replace('#', '')
  if (hex.length === 3) hex = hex.split('').map(c => c + c).join('')
  const num = parseInt(hex, 16)
  return [num >> 16, (num >> 8) & 255, num & 255]
}

function srgbToLinear(c) {
  const v = c / 255
  return v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4)
}

function relativeLuminance([r, g, b]) {
  return 0.2126 * srgbToLinear(r) + 0.7152 * srgbToLinear(g) + 0.0722 * srgbToLinear(b)
}

function contrastRatio(hex1, hex2) {
  const lum1 = relativeLuminance(hexToRgb(hex1))
  const lum2 = relativeLuminance(hexToRgb(hex2))
  const lighter = Math.max(lum1, lum2)
  const darker = Math.min(lum1, lum2)
  return (lighter + 0.05) / (darker + 0.05)
}

function blend(fgHex, bgHex, alpha) {
  const fg = hexToRgb(fgHex)
  const bg = hexToRgb(bgHex)
  const r = Math.round(alpha * fg[0] + (1 - alpha) * bg[0])
  const g = Math.round(alpha * fg[1] + (1 - alpha) * bg[1])
  const b = Math.round(alpha * fg[2] + (1 - alpha) * bg[2])
  return '#' + [r, g, b].map(x => x.toString(16).padStart(2, '0')).join('').toUpperCase()
}

const darkColors = {
  bg: '#101211',
  surface: '#191D1B',
  surfaceElevated: '#222825',
  surfaceSecondary: '#1C221E',
  textPrimary: '#F4F6F4',
  textSecondary: '#B2BAB5',
  textMuted: '#858F89',
  accent: '#38C77B',
  buttonBg: '#16834D',
  buttonText: '#FFFFFF',
  bgHero: '#14201A',
  bgLightSection: '#FFFFFF',
  bgScreenshot: '#101211'
}

const lightColors = {
  bg: '#F2F4F2',
  surface: '#FFFFFF',
  surfaceElevated: '#E7EBE8',
  surfaceSecondary: '#EBF0EC',
  textPrimary: '#171C19',
  textSecondary: '#505A53',
  textMuted: '#5E6962',
  accent: '#0F6B3F',
  buttonBg: '#16834D',
  buttonText: '#FFFFFF',
  bgHero: '#EAF4EE',
  bgLightSection: '#F2F4F2',
  bgDarkScreenshot: '#101211'
}

// 84% glass opacity blends for the scroll-morphing pill
const darkPillAlpha = 0.84
const darkPillHero = blend(darkColors.surface, darkColors.bgHero, darkPillAlpha)
const darkPillLightSec = blend(darkColors.surface, darkColors.bgLightSection, darkPillAlpha)
const darkPillScreenshot = blend(darkColors.surface, darkColors.bgScreenshot, darkPillAlpha)

const lightPillAlpha = 0.84
const lightPillHero = blend(lightColors.surface, lightColors.bgHero, lightPillAlpha)
const lightPillLightSec = blend(lightColors.surface, lightColors.bgLightSection, lightPillAlpha)
const lightPillScreenshot = blend(lightColors.surface, lightColors.bgDarkScreenshot, lightPillAlpha)

let failureCount = 0

console.log('=== DARK THEME CONTRAST RATIOS ===')
const darkPairs = [
  { text: 'textPrimary (#F4F6F4)', textColor: darkColors.textPrimary, bg: 'bg (#101211)', bgColor: darkColors.bg },
  { text: 'textPrimary (#F4F6F4)', textColor: darkColors.textPrimary, bg: 'surface (#191D1B)', bgColor: darkColors.surface },
  { text: 'textPrimary (#F4F6F4)', textColor: darkColors.textPrimary, bg: 'surfaceElevated (#222825)', bgColor: darkColors.surfaceElevated },
  { text: 'textSecondary (#B2BAB5)', textColor: darkColors.textSecondary, bg: 'bg (#101211)', bgColor: darkColors.bg },
  { text: 'textSecondary (#B2BAB5)', textColor: darkColors.textSecondary, bg: 'surface (#191D1B)', bgColor: darkColors.surface },
  { text: 'textSecondary (#B2BAB5)', textColor: darkColors.textSecondary, bg: 'surfaceElevated (#222825)', bgColor: darkColors.surfaceElevated },
  { text: 'textMuted (#858F89)', textColor: darkColors.textMuted, bg: 'bg (#101211)', bgColor: darkColors.bg },
  { text: 'textMuted (#858F89)', textColor: darkColors.textMuted, bg: 'surface (#191D1B)', bgColor: darkColors.surface },
  { text: 'accent (#38C77B)', textColor: darkColors.accent, bg: 'bg (#101211)', bgColor: darkColors.bg },
  { text: 'accent (#38C77B)', textColor: darkColors.accent, bg: 'surface (#191D1B)', bgColor: darkColors.surface },
  { text: 'accent (#38C77B)', textColor: darkColors.accent, bg: 'surfaceElevated (#222825)', bgColor: darkColors.surfaceElevated },
  { text: 'buttonText (#FFFFFF) on buttonBg (#16834D)', textColor: darkColors.buttonText, bg: 'buttonBg (#16834D)', bgColor: darkColors.buttonBg },

  // Header Scroll-Morphing Pill Pairs (Dark Theme)
  { text: 'headerTextPrimary on dark pill (over hero gradient)', textColor: darkColors.textPrimary, bg: `darkPillHero (${darkPillHero})`, bgColor: darkPillHero },
  { text: 'headerTextPrimary on dark pill (over light sections)', textColor: darkColors.textPrimary, bg: `darkPillLightSec (${darkPillLightSec})`, bgColor: darkPillLightSec },
  { text: 'headerTextPrimary on dark pill (over screenshots)', textColor: darkColors.textPrimary, bg: `darkPillScreenshot (${darkPillScreenshot})`, bgColor: darkPillScreenshot },
  { text: 'headerTextSecondary on dark pill (over hero gradient)', textColor: darkColors.textSecondary, bg: `darkPillHero (${darkPillHero})`, bgColor: darkPillHero },
  { text: 'headerTextSecondary on dark pill (over light sections)', textColor: darkColors.textSecondary, bg: `darkPillLightSec (${darkPillLightSec})`, bgColor: darkPillLightSec },
  { text: 'headerTextSecondary on dark pill (over screenshots)', textColor: darkColors.textSecondary, bg: `darkPillScreenshot (${darkPillScreenshot})`, bgColor: darkPillScreenshot },
  { text: 'headerAccent on dark pill (over hero gradient)', textColor: darkColors.accent, bg: `darkPillHero (${darkPillHero})`, bgColor: darkPillHero },
  { text: 'headerAccent on dark pill (over light sections)', textColor: darkColors.accent, bg: `darkPillLightSec (${darkPillLightSec})`, bgColor: darkPillLightSec },
  { text: 'headerAccent on dark pill (over screenshots)', textColor: darkColors.accent, bg: `darkPillScreenshot (${darkPillScreenshot})`, bgColor: darkPillScreenshot },
]

for (const p of darkPairs) {
  const cr = contrastRatio(p.textColor, p.bgColor)
  const pass = cr >= 4.5
  if (!pass) failureCount++
  console.log(`${p.text} on ${p.bg}: ${cr.toFixed(2)}:1 -> ${pass ? 'PASS (>= 4.5:1)' : 'FAIL (< 4.5:1)'}`)
}

console.log('\n=== LIGHT THEME CONTRAST RATIOS ===')
const lightPairs = [
  { text: 'textPrimary (#171C19)', textColor: lightColors.textPrimary, bg: 'bg (#F2F4F2)', bgColor: lightColors.bg },
  { text: 'textPrimary (#171C19)', textColor: lightColors.textPrimary, bg: 'surface (#FFFFFF)', bgColor: lightColors.surface },
  { text: 'textPrimary (#171C19)', textColor: lightColors.textPrimary, bg: 'surfaceElevated (#E7EBE8)', bgColor: lightColors.surfaceElevated },
  { text: 'textSecondary (#505A53)', textColor: lightColors.textSecondary, bg: 'bg (#F2F4F2)', bgColor: lightColors.bg },
  { text: 'textSecondary (#505A53)', textColor: lightColors.textSecondary, bg: 'surface (#FFFFFF)', bgColor: lightColors.surface },
  { text: 'textSecondary (#505A53)', textColor: lightColors.textSecondary, bg: 'surfaceElevated (#E7EBE8)', bgColor: lightColors.surfaceElevated },
  { text: 'textMuted (#5E6962)', textColor: lightColors.textMuted, bg: 'bg (#F2F4F2)', bgColor: lightColors.bg },
  { text: 'textMuted (#5E6962)', textColor: lightColors.textMuted, bg: 'surface (#FFFFFF)', bgColor: lightColors.surface },
  { text: 'accent (#0F6B3F)', textColor: lightColors.accent, bg: 'bg (#F2F4F2)', bgColor: lightColors.bg },
  { text: 'accent (#0F6B3F)', textColor: lightColors.accent, bg: 'surface (#FFFFFF)', bgColor: lightColors.surface },
  { text: 'accent (#0F6B3F)', textColor: lightColors.accent, bg: 'surfaceElevated (#E7EBE8)', bgColor: lightColors.surfaceElevated },
  { text: 'buttonText (#FFFFFF) on buttonBg (#16834D)', textColor: lightColors.buttonText, bg: 'buttonBg (#16834D)', bgColor: lightColors.buttonBg },

  // Header Scroll-Morphing Pill Pairs (Light Theme)
  { text: 'headerTextPrimary on light pill (over hero gradient)', textColor: lightColors.textPrimary, bg: `lightPillHero (${lightPillHero})`, bgColor: lightPillHero },
  { text: 'headerTextPrimary on light pill (over light sections)', textColor: lightColors.textPrimary, bg: `lightPillLightSec (${lightPillLightSec})`, bgColor: lightPillLightSec },
  { text: 'headerTextPrimary on light pill (over dark screenshots)', textColor: lightColors.textPrimary, bg: `lightPillScreenshot (${lightPillScreenshot})`, bgColor: lightPillScreenshot },
  { text: 'headerTextSecondary on light pill (over hero gradient)', textColor: lightColors.textSecondary, bg: `lightPillHero (${lightPillHero})`, bgColor: lightPillHero },
  { text: 'headerTextSecondary on light pill (over light sections)', textColor: lightColors.textSecondary, bg: `lightPillLightSec (${lightPillLightSec})`, bgColor: lightPillLightSec },
  { text: 'headerTextSecondary on light pill (over dark screenshots)', textColor: lightColors.textSecondary, bg: `lightPillScreenshot (${lightPillScreenshot})`, bgColor: lightPillScreenshot },
  { text: 'headerAccent on light pill (over hero gradient)', textColor: lightColors.accent, bg: `lightPillHero (${lightPillHero})`, bgColor: lightPillHero },
  { text: 'headerAccent on light pill (over light sections)', textColor: lightColors.accent, bg: `lightPillLightSec (${lightPillLightSec})`, bgColor: lightPillLightSec },
  { text: 'headerAccent on light pill (over dark screenshots)', textColor: lightColors.accent, bg: `lightPillScreenshot (${lightPillScreenshot})`, bgColor: lightPillScreenshot },
]

for (const p of lightPairs) {
  const cr = contrastRatio(p.textColor, p.bgColor)
  const pass = cr >= 4.5
  if (!pass) failureCount++
  console.log(`${p.text} on ${p.bg}: ${cr.toFixed(2)}:1 -> ${pass ? 'PASS (>= 4.5:1)' : 'FAIL (< 4.5:1)'}`)
}

if (failureCount > 0) {
  console.error(`\nFAILED: ${failureCount} text pair(s) failed WCAG 4.5:1 contrast requirement!`)
  process.exit(1)
} else {
  console.log('\nSUCCESS: All text pairs meet WCAG AA (>= 4.5:1) requirement!')
  process.exit(0)
}
