import { defineConfig } from 'vite';
import angular from '@analogjs/vite-plugin-angular';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    // Angular plugin for Vite - handles standalone component dynamic imports (@ng/component)
    angular(),
  ],
  optimizeDeps: {
    include: ['@angular/core', '@angular/common', '@angular/router', '@angular/forms'],
  },
  server: {
    host: '0.0.0.0',
    port: 4200,
    hmr: {
      host: 'localhost',
    },
  },
});
