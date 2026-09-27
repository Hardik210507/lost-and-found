/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#141414",
        bone: {
          DEFAULT: "#EDE9DD",
          dim: "#D8D3C2",
          muted: "#6B6B6B",
        },
        amber: "#F4C10F",
      },
      fontFamily: {
        serif: ["'Instrument Serif'", "serif"],
        mono: ["'JetBrains Mono'", "monospace"],
      },
    },
  },
  plugins: [],
};
