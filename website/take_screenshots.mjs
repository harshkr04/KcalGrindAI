import puppeteer from 'puppeteer-core';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';

async function verify() {
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox']
  });
  const page = await browser.newPage();
  await page.setViewport({ width: 1440, height: 900 });
  await page.goto('http://127.0.0.1:4173/', { waitUntil: 'networkidle0' });

  const sections = [
    { id: '#ai-scanner-demo', name: 'scanner' },
    { id: '#widgets', name: 'widgets' },
    { id: '#macro-simulator', name: 'macro' },
    { id: '#testimonials', name: 'testimonials' },
    { id: '#faq', name: 'faq' }
  ];

  for (const s of sections) {
    const el = await page.$(s.id);
    if (el) {
      const box = await el.boundingBox();
      await page.screenshot({
        path: `C:/Users/Harsh/.gemini/antigravity/brain/7a699ae8-3d97-46a7-a307-f369c4fceb99/verify_${s.name}.png`,
        clip: { x: 0, y: Math.max(0, box.y - 20), width: 1440, height: 350 }
      });
      console.log(`Saved verify_${s.name}.png`);
    }
  }

  await browser.close();
}
verify();

