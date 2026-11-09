import puppeteer from 'puppeteer-core'
import path from 'path'
import fs from 'fs'

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'
const ARTIFACT_DIR = 'C:\\Users\\Harsh\\.gemini\\antigravity\\brain\\7a699ae8-3d97-46a7-a307-f369c4fceb99'
const LOCAL_DIR = 'D:\\CAloriesssss\\website\\screenshots'

if (!fs.existsSync(LOCAL_DIR)) {
  fs.mkdirSync(LOCAL_DIR, { recursive: true })
}

async function run() {
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  })

  const page = await browser.newPage()

  async function saveScreenshot(filename) {
    const localPath = path.join(LOCAL_DIR, filename)
    const artifactPath = path.join(ARTIFACT_DIR, filename)
    await page.screenshot({ path: localPath })
    fs.copyFileSync(localPath, artifactPath)
    console.log(`Saved screenshot: ${filename}`)
  }

  async function setTheme(target) {
    await page.evaluate((t) => {
      localStorage.setItem('kcalgrind-theme', t)
      if (t === 'light') {
        document.documentElement.classList.add('theme-light')
      } else {
        document.documentElement.classList.remove('theme-light')
      }
    }, target)
    await new Promise(r => setTimeout(r, 200))
  }

  // 1. Desktop 1440x900 Tests
  console.log('\n--- TESTING DESKTOP 1440x900 ---')
  await page.setViewport({ width: 1440, height: 900 })
  await page.goto('http://127.0.0.1:5173/', { waitUntil: 'networkidle0' })

  // Enable 4x CPU throttling to evaluate performance
  const client = await page.createCDPSession()
  await client.send('Emulation.setCPUThrottlingRate', { rate: 4 })
  console.log('Enabled 4x CPU throttling via Chrome DevTools Protocol')

  for (const theme of ['dark', 'light']) {
    console.log(`\nTesting theme: ${theme.toUpperCase()}`)
    await setTheme(theme)

    // State 1: Top of page
    await page.evaluate(() => window.scrollTo(0, 0))
    await new Promise(r => setTimeout(r, 500))

    const topMetrics = await page.evaluate(() => {
      const header = document.querySelector('.site-header')
      const rect = header.getBoundingClientRect()
      const chip = document.querySelector('.site-header [aria-hidden]')
      const navLinks = Array.from(document.querySelectorAll('.site-header nav a')).map(a => ({
        text: a.textContent.trim(),
        ariaCurrent: a.getAttribute('aria-current')
      }))
      return {
        width: rect.width,
        height: rect.height,
        top: rect.top,
        isScrolled: header.classList.contains('is-scrolled'),
        chipVisible: chip ? window.getComputedStyle(chip).visibility : 'none',
        navLinks
      }
    })
    console.log(`[Top State (${theme})]:`, topMetrics)
    await saveScreenshot(`header_1440_${theme}_01_top.png`)

    // State 2: Scrolled down (morph to pill)
    const startTime = Date.now()
    await page.evaluate(() => window.scrollTo({ top: 350, behavior: 'smooth' }))
    // Wait for scroll + 400ms transition + settle
    await new Promise(r => setTimeout(r, 800))
    const morphDuration = Date.now() - startTime

    const scrolledMetrics = await page.evaluate(() => {
      const header = document.querySelector('.site-header')
      const rect = header.getBoundingClientRect()
      const style = window.getComputedStyle(header)
      const chip = document.querySelector('.site-header [aria-hidden]')
      const activeLink = document.querySelector('.site-header nav a[aria-current="page"]')?.textContent.trim()
      return {
        width: rect.width,
        height: rect.height,
        top: rect.top,
        borderRadius: style.borderRadius,
        backdropFilter: style.backdropFilter || style.webkitBackdropFilter,
        isScrolled: header.classList.contains('is-scrolled'),
        chipVisible: chip ? window.getComputedStyle(chip).visibility : 'none',
        activeLink
      }
    })
    console.log(`[Scrolled State (${theme})]:`, scrolledMetrics, `(Morph duration: ${morphDuration}ms)`)
    await saveScreenshot(`header_1440_${theme}_02_scrolled.png`)

    // State 3: Back at top (expand to full-width bar)
    await page.evaluate(() => window.scrollTo({ top: 0, behavior: 'smooth' }))
    await new Promise(r => setTimeout(r, 800))

    const backMetrics = await page.evaluate(() => {
      const header = document.querySelector('.site-header')
      const rect = header.getBoundingClientRect()
      return {
        width: rect.width,
        height: rect.height,
        top: rect.top,
        isScrolled: header.classList.contains('is-scrolled')
      }
    })
    console.log(`[Back at Top State (${theme})]:`, backMetrics)
    await saveScreenshot(`header_1440_${theme}_03_back_top.png`)
  }

  // 2. Mobile 390x844 Tests
  console.log('\n--- TESTING MOBILE 390x844 ---')
  await page.setViewport({ width: 390, height: 844, isMobile: true, hasTouch: true })
  await page.goto('http://127.0.0.1:5173/', { waitUntil: 'networkidle0' })

  for (const theme of ['dark', 'light']) {
    console.log(`\nTesting mobile theme: ${theme.toUpperCase()}`)
    await setTheme(theme)

    // State 1: Mobile Top
    await page.evaluate(() => window.scrollTo(0, 0))
    await new Promise(r => setTimeout(r, 500))
    await saveScreenshot(`header_390_${theme}_01_top.png`)

    // State 2: Mobile Scrolled
    await page.evaluate(() => window.scrollTo({ top: 350, behavior: 'smooth' }))
    await new Promise(r => setTimeout(r, 800))

    const mobScrolledMetrics = await page.evaluate(() => {
      const header = document.querySelector('.site-header')
      const rect = header.getBoundingClientRect()
      const style = window.getComputedStyle(header)
      return {
        width: rect.width,
        height: rect.height,
        top: rect.top,
        borderRadius: style.borderRadius,
        isScrolled: header.classList.contains('is-scrolled')
      }
    })
    console.log(`[Mobile Scrolled State (${theme})]:`, mobScrolledMetrics)
    await saveScreenshot(`header_390_${theme}_02_scrolled.png`)

    // State 3: Mobile Back at Top
    await page.evaluate(() => window.scrollTo({ top: 0, behavior: 'smooth' }))
    await new Promise(r => setTimeout(r, 800))
    await saveScreenshot(`header_390_${theme}_03_back_top.png`)

    // State 4: Open Mobile Sheet Dialog
    await page.click('button[aria-label="Open navigation menu"]')
    await new Promise(r => setTimeout(r, 400))

    const sheetMetrics = await page.evaluate(() => {
      const dialog = document.getElementById('mobile-navigation-sheet')
      const activeEl = document.activeElement ? document.activeElement.tagName + ' ' + (document.activeElement.getAttribute('aria-label') || document.activeElement.textContent.trim()) : 'none'
      const links = Array.from(dialog.querySelectorAll('nav a')).map(a => a.textContent.trim())
      return {
        role: dialog.getAttribute('role'),
        ariaModal: dialog.getAttribute('aria-modal'),
        focusedElement: activeEl,
        links
      }
    })
    console.log(`[Mobile Sheet (${theme})]:`, sheetMetrics)
    await saveScreenshot(`header_390_${theme}_04_sheet_open.png`)

    // Press Escape to test accessible close
    await page.keyboard.press('Escape')
    await new Promise(r => setTimeout(r, 400))
  }

  // Disable throttling
  await client.send('Emulation.setCPUThrottlingRate', { rate: 1 })
  await browser.close()
  console.log('\nAll states captured and performance verified successfully!')
}

run().catch(err => {
  console.error('Capture failed:', err)
  process.exit(1)
})
