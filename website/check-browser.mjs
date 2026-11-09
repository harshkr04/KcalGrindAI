import puppeteer from 'puppeteer-core'
import path from 'path'

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'
const SCREENSHOTS_DIR = 'C:\\Users\\Harsh\\.gemini\\antigravity\\brain\\7a699ae8-3d97-46a7-a307-f369c4fceb99'

async function run() {
  console.log('🚀 Launching Chrome Headless via puppeteer-core...')
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-gpu']
  })

  const page = await browser.newPage()

  const consoleErrors = []
  const failedRequests = []

  page.on('console', msg => {
    if (msg.type() === 'error') {
      consoleErrors.push(msg.text())
      console.error('❌ Console Error:', msg.text())
    }
  })

  page.on('pageerror', err => {
    consoleErrors.push(err.toString())
    console.error('❌ Page Error:', err.toString())
  })

  page.on('response', resp => {
    const status = resp.status()
    const url = resp.url()
    if (status >= 400) {
      failedRequests.push({ url, status })
      console.error(`❌ HTTP ${status}: ${url}`)
    }
  })

  // 1. Desktop Test
  console.log('\n🖥️ Testing Desktop View (1440x900)...')
  await page.setViewport({ width: 1440, height: 900 })
  await page.goto('http://127.0.0.1:4173/', { waitUntil: 'networkidle0' })

  await page.screenshot({
    path: path.join(SCREENSHOTS_DIR, '01_desktop_hero.png')
  })
  console.log('📸 Captured 01_desktop_hero.png')

  // Capture full page
  await page.screenshot({
    path: path.join(SCREENSHOTS_DIR, '02_desktop_fullpage.png'),
    fullPage: true
  })
  console.log('📸 Captured 02_desktop_fullpage.png')

  // 2. Test Hero Tab Switching
  console.log('\n🔄 Testing Hero Tabs...')
  const coachTab = await page.$('button ::-p-text(AI Coach)') || await page.evaluateHandle(() => {
    const btns = Array.from(document.querySelectorAll('button'))
    return btns.find(b => b.textContent.includes('AI Coach'))
  })
  if (coachTab) {
    await coachTab.click()
    await new Promise(r => setTimeout(r, 400))
    await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '03_hero_tab_coach.png') })
    console.log('📸 Clicked AI Coach tab & captured 03_hero_tab_coach.png')
  }

  // 3. Test Download Modal & QR Code
  console.log('\n📦 Testing Download Modal & Scannable QR Code...')
  const qrButton = await page.evaluateHandle(() => {
    const btns = Array.from(document.querySelectorAll('button'))
    return btns.find(b => b.textContent.includes('Scan QR for Phone'))
  })
  if (qrButton) {
    await qrButton.click()
    await new Promise(r => setTimeout(r, 500))
    await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '04_download_modal_qr.png') })
    console.log('📸 Opened Download Modal & captured 04_download_modal_qr.png')

    // Close modal
    const closeBtn = await page.$('button[aria-label="Close modal"]')
    if (closeBtn) {
      await closeBtn.click()
      await new Promise(r => setTimeout(r, 300))
    }
  }

  // 4. Test Theme Toggle (Dark -> Light)
  console.log('\n☀️ Testing Theme Switcher (Light Theme)...')
  const themeBtn = await page.$('button[aria-label*="mode"]')
  if (themeBtn) {
    await themeBtn.click()
    await new Promise(r => setTimeout(r, 400))
    await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '05_light_theme_hero.png') })
    console.log('📸 Toggled Light Mode & captured 05_light_theme_hero.png')

    // Switch back to dark mode
    await themeBtn.click()
    await new Promise(r => setTimeout(r, 300))
  }

  // 5. Test Mobile Responsive View
  console.log('\n📱 Testing Mobile View (390x844 - iPhone 14 / Pixel)...')
  await page.setViewport({ width: 390, height: 844, isMobile: true })
  await page.goto('http://127.0.0.1:4173/', { waitUntil: 'networkidle0' })
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '06_mobile_hero.png') })
  console.log('📸 Captured 06_mobile_hero.png')

  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '07_mobile_fullpage.png'), fullPage: true })
  console.log('📸 Captured 07_mobile_fullpage.png')

  await browser.close()

  console.log('\n================ BROWSER VERIFICATION SUMMARY ================')
  console.log(`Console Errors: ${consoleErrors.length}`)
  console.log(`Failed HTTP Requests (4xx/5xx): ${failedRequests.length}`)
  if (consoleErrors.length > 0) {
    console.log('Errors:', consoleErrors)
  }
  if (failedRequests.length > 0) {
    console.log('Failed Requests:', failedRequests)
  }
  console.log('==============================================================\n')
}

run().catch(err => {
  console.error('Fatal testing error:', err)
  process.exit(1)
})

