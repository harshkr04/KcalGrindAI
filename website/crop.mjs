import puppeteer from 'puppeteer-core';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';

async function crop() {
  const browser = await puppeteer.launch({ executablePath: CHROME_PATH, headless: true, args: ['--no-sandbox'] });
  const page = await browser.newPage();
  await page.setViewport({ width: 1440, height: 900 });
  await page.goto('http://127.0.0.1:4173/', { waitUntil: 'networkidle0' });

  // Find the tabs container
  const btn = await page.$('button[aria-label*="Dashboard"]');
  if (btn) {
    const parent = await btn.evaluateHandle(el => el.parentElement.parentElement);
    const box = await parent.boundingBox();
    if (box) {
      await page.screenshot({
        path: 'C:/Users/Harsh/.gemini/antigravity/brain/7a699ae8-3d97-46a7-a307-f369c4fceb99/crop_tabs.png',
        clip: { x: Math.max(0, box.x - 20), y: Math.max(0, box.y - 20), width: box.width + 40, height: box.height + 40 }
      });
      console.log('Saved crop_tabs.png');
    }
  }
  await browser.close();
}
crop();

