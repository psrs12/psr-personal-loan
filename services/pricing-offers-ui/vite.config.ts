import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    lib: {
      entry: 'src/main.tsx',
      name: 'PricingOffersUI',
      fileName: (format) => `pricing-offers-ui.${format}.js`,
    },
  },
  define: {
    'process.env.NODE_ENV': '"production"',
  },
})
