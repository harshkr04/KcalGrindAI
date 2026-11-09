import puppeteer from 'puppeteer';

async function run() {
  const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox'] });
  const page = await browser.newPage();
  await page.setViewport({ width: 1440, height: 900 });
  await page.goto('http://127.0.0.1:4173/', { waitUntil: 'networkidle0' });

  const sections = ['#ai-scanner-demo', '#widgets', '#macro-simulator', '#testimonials'];
  for (const sel of sections) {
    const el = await page.$(sel);
    if (el) {
      const box = await el.boundingBox();
      if (box) {
        // Screenshot just the top 200px of the section where the header & badges are
        await page.screenshot({
          path: `website/verify_${sel.replace('#', '')}.png`,
          clip: {
            x: Math.max(0, box.x),
            y: Math.max(0, box.y),
            width: Math.min(1440, box.width),
            height: Math.min(260, box.height)
          }
        });
        console.log(`Captured header clip for ${sel}`);
      }
    }
  }

  await browser.close();
}

run().catch(console.error);

