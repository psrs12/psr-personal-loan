import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    lib: {
      entry: 'src/main.tsx',
      name: 'ApplicationManagementUI',
      fileName: (format) => `application-management-ui.${format}.js`,
    },
    rollupOptions: {
      // Keep React external so the shell can provide it, or bundle it for true isolation
      // For Web Components we bundle everything for maximum isolation
    },
  },
  define: {
    'process.env.NODE_ENV': '"production"',
  },
})
