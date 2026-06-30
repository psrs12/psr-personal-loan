import { defineConfig } from 'vite';

export default defineConfig({
  build: {
    lib: {
      entry: 'src/pricing-offer-selector.js',
      name: 'PricingOffersUi',
      fileName: 'pricing-offer-selector',
      formats: ['iife'],
    },
    rollupOptions: {
      output: {
        inlineDynamicImports: true,
      },
    },
  },
});
