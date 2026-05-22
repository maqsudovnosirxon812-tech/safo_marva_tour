/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './index.html',
    './src/main/resources/static/index.html',
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        brand: {
          red: '#8B0000',
          gold: '#D4AF37',
          dark: '#1a1a1a',
          cream: '#FAF8F5',
          'cream-dark': '#F3EFE9',
        },
      },
      fontFamily: {
        sans: ['Montserrat', 'sans-serif'],
        serif: ['Playfair Display', 'serif'],
      },
    },
  },
  plugins: [],
};
