import { defineConfig } from 'vite';

export default defineConfig({
  build: {
    lib: {
      entry: 'src/document-upload-manager.js',
      name: 'DocumentManagementUi',
      fileName: 'document-upload-manager',
      formats: ['iife'],
    },
    rollupOptions: {
      output: {
        inlineDynamicImports: true,
      },
    },
  },
});
