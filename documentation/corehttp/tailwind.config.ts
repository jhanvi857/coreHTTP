import type { Config } from "tailwindcss";

const config: Config = {
    content: [
        "./app/**/*.{js,ts,jsx,tsx,mdx}",
        "./components/**/*.{js,ts,jsx,tsx,mdx}",
        "./src/**/*.{js,ts,jsx,tsx,mdx}",
    ],
    theme: {
        extend: {
            colors: {
                accent: {
                    DEFAULT: "#0070f3",
                    hover: "#0051af",
                },
            },
            backgroundColor: {
                primary: "var(--bg-primary)",
                card: "var(--bg-card)",
                muted: "var(--bg-muted)",
            },
            textColor: {
                primary: "var(--fg-primary)",
            },
            borderColor: {
                muted: "var(--border-muted)",
            },
            animation: {
                float: "float 6s ease-in-out infinite",
            },
            keyframes: {
                float: {
                    "0%, 100%": { transform: "translateY(0)" },
                    "50%": { transform: "translateY(-10px)" },
                },
            },
        },
    },
    plugins: [],
};
export default config;
