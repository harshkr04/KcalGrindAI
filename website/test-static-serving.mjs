import http from 'http'
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const distDir = path.resolve(__dirname, 'dist')

// Simple static server simulating standard static file server behavior
const server = http.createServer((req, res) => {
  let reqPath = req.url.split('?')[0]
  let filePath = path.join(distDir, reqPath)

  // If path is a directory or ends with /, look for index.html
  if (fs.existsSync(filePath) && fs.statSync(filePath).isDirectory()) {
    filePath = path.join(filePath, 'index.html')
  } else if (!fs.existsSync(filePath)) {
    // Check if path + '.html' exists (cleanUrls)
    if (fs.existsSync(filePath + '.html')) {
      filePath = filePath + '.html'
    } else if (fs.existsSync(path.join(filePath, 'index.html'))) {
      filePath = path.join(filePath, 'index.html')
    }
  }

  if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
    const ext = path.extname(filePath)
    const contentTypes = {
      '.html': 'text/html; charset=utf-8',
      '.css': 'text/css',
      '.js': 'application/javascript',
      '.svg': 'image/svg+xml',
      '.json': 'application/json',
      '.txt': 'text/plain',
    }
    res.writeHead(200, { 'Content-Type': contentTypes[ext] || 'application/octet-stream' })
    fs.createReadStream(filePath).pipe(res)
  } else {
    res.writeHead(404, { 'Content-Type': 'text/plain' })
    res.end('404 Not Found')
  }
})

const PORT = 4173

server.listen(PORT, async () => {
  console.log(`Static server running at http://localhost:${PORT}`)
  
  const testUrls = [
    { url: '/privacy', expectedText: 'Privacy Policy for KcalGrind' },
    { url: '/privacy/', expectedText: 'Privacy Policy for KcalGrind' },
    { url: '/delete-account', expectedText: 'Request Account &amp; Data Deletion' },
    { url: '/delete-account/', expectedText: 'Request Account &amp; Data Deletion' },
  ]

  let allPassed = true

  for (const t of testUrls) {
    try {
      const res = await fetch(`http://localhost:${PORT}${t.url}`, {
        headers: { 'Accept': 'text/html' }
      })
      const html = await res.text()
      const status = res.status
      const hasText = html.includes(t.expectedText)
      const isHtml = res.headers.get('content-type')?.includes('text/html')

      if (status === 200 && hasText && isHtml) {
        console.log(`✓ [PASS] GET ${t.url} -> 200 OK (${html.length} bytes, found "${t.expectedText}")`)
      } else {
        console.error(`✗ [FAIL] GET ${t.url} -> status=${status}, hasText=${hasText}`)
        allPassed = false
      }
    } catch (err) {
      console.error(`✗ [ERROR] GET ${t.url} failed:`, err.message)
      allPassed = false
    }
  }

  server.close(() => {
    if (allPassed) {
      console.log('\nAll 4 static routes verified successfully without JavaScript!')
      process.exit(0)
    } else {
      console.error('\nStatic route verification failed!')
      process.exit(1)
    }
  })
})
