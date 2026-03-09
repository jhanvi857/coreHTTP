import Link from "next/link";

export default function Navbar() {
  return (
    <nav className="sticky top-0 left-0 z-50 w-full border-b border-muted bg-white/80 backdrop-blur-md dark:bg-black/80 transition-colors">
      <div className="w-full max-w-[1200px] mx-auto px-6 h-16 flex items-center justify-between">
        <div className="flex items-center gap-8">
          <Link href="/" className="flex items-center gap-2 group transition-opacity hover:opacity-80">
            <div className="flex h-8 w-8 items-center justify-center rounded bg-black text-white dark:bg-white dark:text-black">
              <span className="font-bold leading-none font-sans drop-shadow-sm">C</span>
            </div>
            <span className="text-xl font-bold tracking-tight">
              coreHTTP
            </span>
          </Link>

          <div className="hidden md:flex items-center gap-6 text-sm font-medium text-gray-500 dark:text-gray-400">
            <Link href="/docs" className="transition-colors hover:text-black dark:hover:text-white">Docs</Link>
            <Link href="/showcase" className="transition-colors hover:text-black dark:hover:text-white">Operations</Link>
            <Link href="/roadmap" className="transition-colors hover:text-black dark:hover:text-white">Roadmap</Link>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="hidden md:flex items-center pr-4 h-6">
            <Link
              href="https://github.com/jhanvi857/coreHTTP"
              target="_blank"
              rel="noreferrer"
              className="text-sm font-medium text-gray-500 transition-colors hover:text-white"
            >
              GitHub
            </Link>
          </div>
          <Link
            href="/docs"
            className="btn btn-primary"
          >
            Read Docs
          </Link>
        </div>
      </div>
    </nav>
  );
}