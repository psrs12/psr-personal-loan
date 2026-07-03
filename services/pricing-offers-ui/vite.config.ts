import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import cssInjectedByJsPlugin from 'vite-plugin-css-injected-by-js'

export default defineConfig({
  plugins: [react(), cssInjectedByJsPlugin()],
  build: {
    lib: {
      entry: 'src/main.tsx',
      name: 'PricingOffersUI',
      formats: ['iife'],
      fileName: () => 'pricing-offer-selector.iife.js',
    },
  },
  define: {
    'process.env.NODE_ENV': '"production"',
  },
})
