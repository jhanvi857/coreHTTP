import Link from "next/link";

export default function Footer() {
  return (
    <footer className="w-full border-t border-muted bg-white dark:bg-black py-12 md:py-16 text-sm">
      <div className="max-w-[1200px] mx-auto px-6">
        <div className="grid grid-cols-2 gap-8 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 mb-12">
          <div className="col-span-2 lg:col-span-2 xl:col-span-3">
            <Link href="/" className="flex items-center gap-2 group mb-4">
              <div className="flex h-6 w-6 items-center justify-center rounded-sm bg-black text-white dark:bg-white dark:text-black">
                <span className="text-xs font-bold leading-none">C</span>
              </div>
              <span className="font-semibold tracking-tight text-black dark:text-white">
                coreHTTP
              </span>
            </Link>
            <p className="max-w-xs text-gray-500 dark:text-gray-400">
              Runtime-first HTTP framework with secure middleware.
            </p>
          </div>

          <div>
            <h3 className="mb-4 font-semibold text-black dark:text-white">Resources</h3>
            <ul className="space-y-3">
              <li><Link href="/docs" className="text-gray-500 hover:text-black dark:text-gray-400 dark:hover:text-white transition-colors">Documentation</Link></li>
              <li><Link href="/showcase" className="text-gray-500 hover:text-black dark:text-gray-400 dark:hover:text-white transition-colors">Operations</Link></li>
              <li><Link href="/roadmap" className="text-gray-500 hover:text-black dark:text-gray-400 dark:hover:text-white transition-colors">Roadmap</Link></li>
            </ul>
          </div>

          <div>
            <h3 className="mb-4 font-semibold text-black dark:text-white">Runtime</h3>
            <ul className="space-y-3">
              <li><span className="text-gray-500 dark:text-gray-400">Middleware Chain</span></li>
              <li><span className="text-gray-500 dark:text-gray-400">JWT + BCrypt Auth</span></li>
              <li><span className="text-gray-500 dark:text-gray-400">SQL Persistence</span></li>
            </ul>
          </div>

          <div>
            <h3 className="mb-4 font-semibold text-black dark:text-white">Legal</h3>
            <ul className="space-y-3">
              <li><Link href="https://github.com/jhanvi857/coreHTTP" target="_blank" rel="noreferrer" className="text-gray-500 hover:text-black dark:text-gray-400 dark:hover:text-white transition-colors">GitHub</Link></li>
              <li><span className="text-gray-500 dark:text-gray-400">Privacy Policy</span></li>
              <li><span className="text-gray-500 dark:text-gray-400">Terms of Service</span></li>
            </ul>
          </div>
        </div>

        <div className="pt-8 flex flex-col items-start justify-between gap-4 md:flex-row md:items-center border-t border-muted">
          <p className="text-gray-500 dark:text-gray-400">
            &copy; {new Date().getFullYear()} coreHTTP. Open source under MIT.
          </p>
          <div className="flex items-center gap-2 group">
            <span className="flex h-2 w-2 rounded-full bg-green-500" />
            <span className="text-gray-600 dark:text-gray-300 group-hover:text-black dark:group-hover:text-white transition-colors">All systems normal</span>
          </div>
        </div>
      </div>
    </footer>
  );
}