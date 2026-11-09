import puppeteer from 'puppeteer-core'
import path from 'path'
import fs from 'fs'

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'
const ARTIFACT_DIR = 'C:\\Users\\Harsh\\.gemini\\antigravity\\brain\\7a699ae8-3d97-46a7-a307-f369c4fceb99'
const LOCAL_DIR = 'D:\\CAloriesssss\\website\\screenshots'

if (!fs.existsSync(LOCAL_DIR)) {
  fs.mkdirSync(LOCAL_DIR, { recursive: true })
}

async function capture() {
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  })

  const page = await browser.newPage()

  async function saveScreenshot(filename, options = {}) {
    const localPath = path.join(LOCAL_DIR, filename)
    const artifactPath = path.join(ARTIFACT_DIR, filename)
    await page.screenshot({ path: localPath, ...options })
    fs.copyFileSync(localPath, artifactPath)
    console.log(`Saved ${filename}`)
  }

  async function ensureTheme(target) {
    const currentTheme = await page.evaluate(() => localStorage.getItem('kcalgrind-theme') || 'dark')
    if (currentTheme !== target) {
      await page.click('button[aria-label*="Switch to"]')
      await new Promise(r => setTimeout(r, 500))
    }
    const verified = await page.evaluate(() => ({
      theme: localStorage.getItem('kcalgrind-theme'),
      cls: document.documentElement.className,
      bg: getComputedStyle(document.body).backgroundColor
    }))
    console.log(`Verified theme '${target}':`, verified)
  }

  // 1. Desktop 1440x900 - Dark Mode
  await page.setViewport({ width: 1440, height: 900, isMobile: false })
  await page.goto('http://127.0.0.1:5173/', { waitUntil: 'networkidle0' })
  await ensureTheme('dark')
  await saveScreenshot('v2_1440_desktop_dark_hero.png', { fullPage: false })
  await saveScreenshot('v2_1440_desktop_dark_fullpage.png', { fullPage: true })

  // 2. Desktop 1440x900 - Light Mode
  await ensureTheme('light')
  await saveScreenshot('v2_1440_desktop_light_hero.png', { fullPage: false })
  await saveScreenshot('v2_1440_desktop_light_fullpage.png', { fullPage: true })

  // 3. Mobile 390x844 - Dark Mode
  await page.setViewport({ width: 390, height: 844, isMobile: true })
  await new Promise(r => setTimeout(r, 300))
  await ensureTheme('dark')
  await saveScreenshot('v2_390_mobile_dark_hero.png', { fullPage: false })
  await saveScreenshot('v2_390_mobile_dark_fullpage.png', { fullPage: true })

  // 4. Mobile 390x844 - Light Mode
  await ensureTheme('light')
  await saveScreenshot('v2_390_mobile_light_hero.png', { fullPage: false })
  await saveScreenshot('v2_390_mobile_light_fullpage.png', { fullPage: true })

  // 5. Desktop 1366x768 - Dark Mode Hero Viewport Check
  await page.setViewport({ width: 1366, height: 768, isMobile: false })
  await new Promise(r => setTimeout(r, 300))
  await ensureTheme('dark')
  await saveScreenshot('v2_1366_desktop_dark_hero.png', { fullPage: false })

  // 6. Desktop 1920x1080 - Dark Mode Hero Viewport Check
  await page.setViewport({ width: 1920, height: 1080, isMobile: false })
  await new Promise(r => setTimeout(r, 300))
  await ensureTheme('dark')
  await saveScreenshot('v2_1920_desktop_dark_hero.png', { fullPage: false })

  // 7. Static /privacy and /delete-account pages check
  await page.setViewport({ width: 1200, height: 800, isMobile: false })
  await page.goto('http://127.0.0.1:5173/privacy', { waitUntil: 'networkidle0' })
  await saveScreenshot('v2_privacy_page.png', { fullPage: false })

  await page.goto('http://127.0.0.1:5173/delete-account', { waitUntil: 'networkidle0' })
  await saveScreenshot('v2_delete_account_page.png', { fullPage: false })

  await browser.close()
  console.log('All screenshots captured successfully!')
}

capture().catch(err => {
  console.error('Capture failed:', err)
  process.exit(1)
})
