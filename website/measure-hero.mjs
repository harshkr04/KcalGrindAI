import puppeteer from 'puppeteer-core';

const chromePath = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';

const viewports = [
  { width: 1366, height: 650, name: '1366x650' },
  { width: 1440, height: 790, name: '1440x790' },
  { width: 1920, height: 950, name: '1920x950' },
  { width: 390, height: 750, name: '390x750' },
  { width: 390, height: 844, name: '390x844' },
];

async function measure() {
  const browser = await puppeteer.launch({
    executablePath: chromePath,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  console.log('=== HERO VIEWPORT MEASUREMENTS ===');
  for (const vp of viewports) {
    const page = await browser.newPage();
    await page.setViewport({ width: vp.width, height: vp.height });
    await page.goto('http://127.0.0.1:5173/', { waitUntil: 'networkidle0' });

    const metrics = await page.evaluate((vpHeight) => {
      const header = document.querySelector('header');
      const headline = document.querySelector('h1');
      const ctaBtn = document.querySelector('#hero a, #hero button');
      const phone = document.querySelector('#hero-phone-mockup');

      const headerRect = header ? header.getBoundingClientRect() : null;
      const headlineRect = headline ? headline.getBoundingClientRect() : null;
      const ctaRect = ctaBtn ? ctaBtn.getBoundingClientRect() : null;
      const phoneRect = phone ? phone.getBoundingClientRect() : null;

      const headlineStyle = headline ? window.getComputedStyle(headline) : null;
      const fontSize = headlineStyle ? headlineStyle.fontSize : null;

      // Gap between header bottom and headline top
      const gap = (headlineRect && headerRect) ? Math.round(headlineRect.top - headerRect.bottom) : null;

      return {
        fontSize,
        fontSizePx: fontSize ? parseFloat(fontSize) : null,
        gap,
        headerBottom: headerRect ? Math.round(headerRect.bottom) : null,
        headlineTop: headlineRect ? Math.round(headlineRect.top) : null,
        headlineBottom: headlineRect ? Math.round(headlineRect.bottom) : null,
        ctaTop: ctaRect ? Math.round(ctaRect.top) : null,
        ctaBottom: ctaRect ? Math.round(ctaRect.bottom) : null,
        isHeadlineAboveFold: headlineRect ? headlineRect.bottom <= vpHeight : false,
        isCtaAboveFold: ctaRect ? ctaRect.bottom <= vpHeight : false,
        phoneHeight: phoneRect ? Math.round(phoneRect.height) : null,
        phoneTop: phoneRect ? Math.round(phoneRect.top) : null,
        phoneBottom: phoneRect ? Math.round(phoneRect.bottom) : null,
        isPhonePartiallyVisible: phoneRect ? (phoneRect.top < vpHeight && phoneRect.bottom > 0) : false,
        isPhoneCropped: phoneRect ? phoneRect.bottom > vpHeight : false
      };
    }, vp.height);

    console.log(`\nViewport ${vp.name}:`);
    console.log(`  Headline font-size: ${metrics.fontSize} (${metrics.fontSizePx}px)`);
    console.log(`  Top gap (header bottom to headline top): ${metrics.gap}px`);
    console.log(`  Headline above fold: ${metrics.isHeadlineAboveFold} (top=${metrics.headlineTop}, bottom=${metrics.headlineBottom}, fold=${vp.height})`);
    console.log(`  CTA Button above fold: ${metrics.isCtaAboveFold} (top=${metrics.ctaTop}, bottom=${metrics.ctaBottom})`);
    console.log(`  Phone height: ${metrics.phoneHeight}px (top=${metrics.phoneTop}, bottom=${metrics.phoneBottom})`);
    console.log(`  Phone peeking/visible: ${metrics.isPhonePartiallyVisible}, cropped: ${metrics.isPhoneCropped}`);

    await page.close();
  }

  await browser.close();
}

measure().catch(console.error);
