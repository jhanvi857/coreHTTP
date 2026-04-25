import Footer from "../components/Footer";
import Navbar from "../components/Navbar";

const SidebarLink = ({ href, children }: { href: string; children: React.ReactNode }) => (
  <li>
    <a href={href} className="block py-1.5 text-[14px] text-gray-500 hover:text-black dark:text-gray-400 dark:hover:text-white transition-colors">
      {children}
    </a>
  </li>
);

export default function DocsLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="app-shell bg-primary text-primary min-h-screen">
      <Navbar />
      <div className="absolute inset-0 site-grid-bg pointer-events-none" />

      <div className="w-full max-w-[1400px] mx-auto flex flex-col md:flex-row relative z-10">
        <aside className="hidden md:block w-64 shrink-0 border-r border-muted pt-16 pb-20 pr-8 sticky top-16 scroll-mt-16 h-[calc(100vh-4rem)] overflow-y-auto">
          <nav className="space-y-8 pl-4">
            <div>
              <h4 className="font-semibold mb-3 text-xs uppercase tracking-wider text-gray-900 dark:text-gray-100">Overview</h4>
              <ul className="space-y-1">
                <SidebarLink href="/docs">Docs Home</SidebarLink>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-3 text-xs uppercase tracking-wider text-gray-900 dark:text-gray-100">Guide</h4>
              <ul className="space-y-1">
                <SidebarLink href="/docs/getting-started">Getting Started</SidebarLink>
                <SidebarLink href="/docs/routing-frontend">Routing + Frontend</SidebarLink>
                <SidebarLink href="/docs/auth-security">Auth + Security</SidebarLink>
                <SidebarLink href="/docs/database-env">Database + Env</SidebarLink>
                <SidebarLink href="/docs/deployment">Operations + Deployment</SidebarLink>
                <SidebarLink href="/docs/reference">API Reference</SidebarLink>
                <SidebarLink href="/docs/performance">Performance Benchmarks</SidebarLink>
              </ul>
            </div>
          </nav>
        </aside>

        <main className="flex-1 px-6 md:px-20 py-16 max-w-[900px]">{children}</main>
      </div>
      <Footer />
    </div>
  );
}
