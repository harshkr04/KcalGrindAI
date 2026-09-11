const fs = require('fs');
const path = require('path');
const dir = 'D:/CAloriesssss/stitch_lumina_ai_nutrition/app';

const jsFiles = ['shared/diary.js','shared/insights.js','shared/ai_assistant.js'];
const htmlFiles = ['insights.html','weight_log.html'];

let errors = [];

jsFiles.forEach(f => {
  const p = path.join(dir, f);
  if (!fs.existsSync(p)) { errors.push('MISSING: ' + f); return; }
  try { new Function(fs.readFileSync(p, 'utf8')); } catch (e) { errors.push('JS SYNTAX: ' + f + ' - ' + e.message); }
});

htmlFiles.forEach(f => {
  const p = path.join(dir, f);
  if (!fs.existsSync(p)) { errors.push('MISSING: ' + f); return; }
  const html = fs.readFileSync(p, 'utf8');
  if (!html.trim().startsWith('<!DOCTYPE html>')) errors.push('NO DOCTYPE: ' + f);
  if (!html.trim().endsWith('</html>')) errors.push('NO </html>: ' + f);
});

const links = [
  ['11_home.html', 'insights.html'],
  ['diary.html', 'insights.html'],
  ['insights.html', 'weight_log.html'],
  ['insights.html', 'ai_chat.html'],
  ['ai_chat.html', 'insights.html']
];

links.forEach(([from, to]) => {
  const p = path.join(dir, from);
  if (fs.existsSync(p)) {
    const content = fs.readFileSync(p, 'utf8');
    if (!content.includes(to)) errors.push('LINK MISSING: ' + from + ' -> ' + to);
  }
});

if (errors.length === 0) {
  console.log('Phase 6 checks passed.');
} else {
  console.log('ERRORS:');
  errors.forEach(e => console.log('  ' + e));
  process.exit(1);
}
