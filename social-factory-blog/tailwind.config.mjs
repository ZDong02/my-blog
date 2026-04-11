/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{astro,html,js,jsx,md,mdx,svelte,ts,tsx,vue}'],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        serif: ['Georgia', 'Cambria', 'Times New Roman', 'serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
        display: ['Inter', 'system-ui', 'sans-serif'],
      },
      colors: {
        brand: {
          red: '#DC2626',
          'red-dark': '#B91C1C',
          'red-light': '#EF4444',
        },
        neutral: {
          50: '#FAFAF9',
          100: '#F5F5F4',
          200: '#E7E5E4',
          300: '#D6D3D1',
          400: '#A8A29E',
          500: '#78716C',
          600: '#57534E',
          700: '#44403C',
          800: '#292524',
          900: '#1C1917',
        },
        accent: {
          cream: '#FDF6E3',
          warm: '#F5E6D3',
          dark: '#1C1917',
        },
      },
      fontSize: {
        'display': ['6rem', { lineHeight: '0.9', letterSpacing: '-0.05em', fontWeight: '900' }],
        'headline': ['4rem', { lineHeight: '1', letterSpacing: '-0.03em', fontWeight: '800' }],
        'title': ['2.5rem', { lineHeight: '1.1', letterSpacing: '-0.02em', fontWeight: '700' }],
        'subtitle': ['1.25rem', { lineHeight: '1.5', letterSpacing: '0.01em' }],
        'body': ['1.125rem', { lineHeight: '1.75' }],
        'caption': ['0.875rem', { lineHeight: '1.5', letterSpacing: '0.02em' }],
        'label': ['0.75rem', { lineHeight: '1', letterSpacing: '0.15em' }],
      },
      spacing: {
        '18': '4.5rem',
        '22': '5.5rem',
        '30': '7.5rem',
      },
      boxShadow: {
        'card': '8px 8px 0 #1C1917',
        'card-dark': '8px 8px 0 #F5F5F4',
        'soft': '0 4px 20px rgba(0, 0, 0, 0.08)',
        'elevated': '0 8px 40px rgba(0, 0, 0, 0.12)',
      },
      borderWidth: {
        '3': '3px',
      },
      animation: {
        'fade-in': 'fadeIn 0.6s ease-out forwards',
        'slide-up': 'slideUp 0.6s ease-out forwards',
        'slide-left': 'slideLeft 0.6s ease-out forwards',
        'scale-in': 'scaleIn 0.4s ease-out forwards',
        'float': 'float 6s ease-in-out infinite',
        'shimmer': 'shimmer 2s infinite',
      },
      backdropBlur: {
        'xs': '2px',
      },
      transitionTimingFunction: {
        'elastic': 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
      },
    },
  },
  plugins: [],
};
