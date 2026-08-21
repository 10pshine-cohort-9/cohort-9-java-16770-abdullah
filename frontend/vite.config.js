import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        // Forward API calls to the Spring Boot backend so the browser sees
        // everything on one origin and we avoid CORS in development.
        '/api': {
          target: env.BACKEND_URL || 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
  }
})
