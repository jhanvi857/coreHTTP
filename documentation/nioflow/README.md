# NioFlow Documentation Portal

This folder contains the standalone documentation web app for NioFlow, built with Next.js (App Router), TypeScript, and Tailwind CSS.

## Purpose

The portal is intended to provide a product-style documentation experience for:

- framework overview and architecture
- getting started flows
- routing, auth, and environment setup
- deployment and operations guidance

## Local Development

From this folder:

```bash
npm install
npm run dev
```

Open http://localhost:3000.

## Build and Preview

```bash
npm run build
npm run start
```

## Lint

```bash
npm run lint
```

## Project Structure

```text
documentation/nioflow/
├── app/
│   ├── page.tsx                # marketing/home page
│   ├── docs/                   # documentation sections
│   ├── roadmap/                # roadmap page
│   ├── showcase/               # feature showcase page
│   ├── components/             # shared UI components
│   └── lib/docsContent.ts      # shared content models/data
├── public/                     # static assets
├── eslint.config.mjs
├── next.config.ts
├── package.json
└── tsconfig.json
```

## Authoring Notes

- Keep examples aligned with live behavior in the Java modules at repository root.
- When runtime defaults change, update both:
	- root README
	- docs pages under app/docs
- Avoid product claims that exceed implementation details.

## Related Docs

- Root overview: ../../README.md
- Operations runbook: ../../runbook.md
