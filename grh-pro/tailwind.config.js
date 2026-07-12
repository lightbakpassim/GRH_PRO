/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                // Vos couleurs personnalisées
                primary: {
                    DEFAULT: '#1a7a7a',
                    dark: '#145f5f',
                    light: '#2a9a9a',
                    50: '#f0fafa',
                    100: '#d0eeee',
                    200: '#a1dddd',
                    300: '#72cccc',
                    400: '#43bbbb',
                    500: '#1a7a7a',
                    600: '#176e6e',
                    700: '#145f5f',
                    800: '#105050',
                    900: '#0c4141',
                },
                sidebar: '#1a6b6b',
                accent: '#f5a623',
                success: '#4caf7d',
                danger: '#e57373',
                warning: '#f5c842',
                surface: '#f0f5f5',
            },
            animation: {
                'slide-in': 'slide-in 0.3s ease-out',
                'fade-in': 'fade-in 0.2s ease-out',
                'scale-up': 'scale-up 0.2s ease-out',
            },
            keyframes: {
                'slide-in': {
                    '0%': { transform: 'translateX(-100%)' },
                    '100%': { transform: 'translateX(0)' },
                },
                'fade-in': {
                    '0%': { opacity: '0' },
                    '100%': { opacity: '1' },
                },
                'scale-up': {
                    '0%': { transform: 'scale(0.95)' },
                    '100%': { transform: 'scale(1)' },
                },
            },
        },
    },
    plugins: [],
}