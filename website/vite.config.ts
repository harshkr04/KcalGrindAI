import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig, type Plugin } from 'vite'
import { resolve } from 'path'
import fs from 'fs'

function staticPagePlugin(): Plugin {
  return {
    name: 'static-page-routing',
    configureServer(server) {
      server.middlewares.use((req, _res, next) => {
        const url = req.url?.split('?')[0]
        if (url === '/privacy') {
          req.url = '/privacy/index.html'
        } else if (url === '/delete-account') {
          req.url = '/delete-account/index.html'
        }
        next()
      })
    },
    configurePreviewServer(server) {
      server.middlewares.use((req, _res, next) => {
        const url = req.url?.split('?')[0]
        if (url === '/privacy') {
          req.url = '/privacy/index.html'
        } else if (url === '/delete-account') {
          req.url = '/delete-account/index.html'
        }
        next()
      })
    },
    closeBundle() {
      const distDir = resolve(import.meta.dirname, 'dist')
      const privacySrc = resolve(distDir, 'privacy', 'index.html')
      const privacyDst = resolve(distDir, 'privacy.html')
      if (fs.existsSync(privacySrc)) {
        fs.copyFileSync(privacySrc, privacyDst)
      }
      const deleteSrc = resolve(distDir, 'delete-account', 'index.html')
      const deleteDst = resolve(distDir, 'delete-account.html')
      if (fs.existsSync(deleteSrc)) {
        fs.copyFileSync(deleteSrc, deleteDst)
      }
    },
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss(), staticPagePlugin()],
  server: {
    host: '127.0.0.1',
    port: 5173,
    allowedHosts: true,
  },
  preview: {
    host: '127.0.0.1',
    port: 5173,
    allowedHosts: true,
  },
  build: {
    rollupOptions: {
      input: {
        main: resolve(import.meta.dirname, 'index.html'),
        privacy: resolve(import.meta.dirname, 'privacy/index.html'),
        deleteAccount: resolve(import.meta.dirname, 'delete-account/index.html'),
      },
    },
  },
})

