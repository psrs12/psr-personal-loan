import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    lib: {
      entry: 'src/main.tsx',
      name: 'DocumentManagementUI',
      formats: ['iife'],
      fileName: () => 'document-upload-manager.iife.js',
    },
  },
  define: {
    'process.env.NODE_ENV': '"production"',
  },
})
