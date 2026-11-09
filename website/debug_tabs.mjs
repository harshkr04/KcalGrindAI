import puppeteer from 'puppeteer-core';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';

async function check() {
  const browser = await puppeteer.launch({ executablePath: CHROME_PATH, headless: true, args: ['--no-sandbox'] });
  const page = await browser.newPage();
  await page.setViewport({ width: 1440, height: 900 });
  await page.goto('http://127.0.0.1:4173/', { waitUntil: 'networkidle0' });

  const btns = await page.$$('button[aria-label*="Switch preview screen"]');
  for (const b of btns) {
    const text = await page.evaluate(el => el.textContent, b);
    const box = await page.evaluate(el => {
      const r = el.getBoundingClientRect();
      const s = window.getComputedStyle(el);
      const span = el.querySelector('span');
      const spanColor = span ? window.getComputedStyle(span).color : 'no-span';
      return {
        x: r.x, y: r.y, w: r.width, h: r.height,
        color: s.color,
        spanColor,
        bg: s.backgroundColor,
        opacity: s.opacity
      };
    }, b);
    console.log(text.trim(), box);
  }

  await browser.close();
}
check();

