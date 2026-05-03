import Link from "next/link";

export const H2 = ({ id, children }: { id: string; children: React.ReactNode }) => (
  <h2 id={id} className="text-2xl font-semibold tracking-tight text-primary mt-12 mb-5 scroll-mt-28 border-b border-muted pb-3">
    {children}
  </h2>
);

export const H3 = ({ children }: { children: React.ReactNode }) => (
  <h3 className="text-lg font-semibold tracking-tight text-primary mt-8 mb-3">{children}</h3>
);

export const P = ({ children }: { children: React.ReactNode }) => (
  <p className="text-gray-600 dark:text-gray-400 mb-4 text-[15px] leading-relaxed">{children}</p>
);

export const CodeBlock = ({ code, language = "bash", title }: { code: string; language?: string; title?: string }) => {
  const getTokenClass = (token: string) => {
    const t = token.trim();
    if (!t) return "";
    if (/^(import|public|class|static|void|new|return|if|else|try|catch|throw|throws|private|protected|final|extends|implements|int|boolean|String|var|const|function|async|await|export|from|default)$/.test(t)) {
      return "text-[#ff7b72]";
    }
    if (/^(NioFlowApp|HttpStatus|Map|List|Runtime|Thread|PasswordHasher|JwtProvider|AuthMiddleware|CorsMiddleware|RateLimitMiddleware|LoggerMiddleware|fetch|JSON|localStorage)$/.test(t)) {
      return "text-[#d2a8ff]";
    }
    if (/^\d+$/.test(t)) {
      return "text-[#79c0ff]";
    }
    if (t.startsWith('"') || t.startsWith("'") || t.startsWith("`")) {
      return "text-[#a5d6ff]";
    }
    if (t.startsWith("//") || t.startsWith("#")) {
      return "text-[#8b949e] italic";
    }
    return "text-gray-300";
  };

  return (
    <div className="my-5 rounded-xl border border-muted bg-[#0e0e11] overflow-hidden shadow-md">
      <div className="flex items-center px-4 py-3 border-b border-[#222] bg-[#1a1a1e] relative">
        <div className="flex space-x-2 absolute left-4">
          <div className="w-3 h-3 rounded-full bg-[#ff5f56]" />
          <div className="w-3 h-3 rounded-full bg-[#ffbd2e]" />
          <div className="w-3 h-3 rounded-full bg-[#27c93f]" />
        </div>
        <div className="w-full text-center">
          <span className="text-xs font-mono text-gray-400">{title || language}</span>
        </div>
      </div>
      <div className="overflow-x-auto p-4 md:p-6 pb-6 text-[13px] md:text-sm leading-relaxed">
        <pre className="font-mono whitespace-pre-wrap">
          {code.split("\n").map((line, i) => {
            const isComment = line.trim().startsWith("//") || line.trim().startsWith("#");
            return (
              <div key={i} className="table-row">
                <span className="table-cell select-none pr-6 text-right text-gray-600 border-r border-[#333]">{i + 1}</span>
                <span className="table-cell whitespace-pre pl-6">
                  {isComment
                    ? <span className="text-[#8b949e] italic">{line}</span>
                    : line.split(/(\s+|[,;{}()<>.=:+\-])/g).map((part, idx) => (
                        <span key={idx} className={getTokenClass(part)}>{part}</span>
                      ))}
                </span>
              </div>
            );
          })}
        </pre>
      </div>
    </div>
  );
};

export const SectionCard = ({ href, title, description }: { href: string; title: string; description: string }) => (
  <Link href={href} className="rounded-xl border border-muted bg-card p-5 transition hover:bg-muted/20 hover:shadow-sm">
    <h3 className="text-base font-semibold text-gray-900 dark:text-gray-100">{title}</h3>
    <p className="text-sm text-gray-600 dark:text-gray-400 mt-2">{description}</p>
  </Link>
);

export const InfoCallout = ({ title, children }: { title: string; children: React.ReactNode }) => (
  <div className="my-6 flex items-start space-x-4 rounded-xl border border-blue-200 bg-blue-50/50 p-6 dark:border-blue-500/20 dark:bg-blue-500/5">
    <div className="mt-1 flex-shrink-0">
      <div className="flex h-5 w-5 items-center justify-center rounded-full bg-blue-500 text-white">
        <span className="text-xs font-bold font-mono">i</span>
      </div>
    </div>
    <div className="flex-1 space-y-2">
      <h4 className="font-bold text-blue-900 dark:text-blue-400">{title}</h4>
      <div className="text-[14px] text-blue-800/80 dark:text-blue-300/80 leading-relaxed">{children}</div>
    </div>
  </div>
);

export const WarningCallout = ({ title, children }: { title: string; children: React.ReactNode }) => (
  <div className="my-6 flex items-start space-x-4 rounded-xl border border-pink-200 bg-pink-50/50 p-6 dark:border-pink-500/20 dark:bg-pink-500/5">
    <div className="mt-1 flex-shrink-0">
      <div className="flex h-5 w-5 items-center justify-center rounded-full bg-pink-500 text-white font-bold font-mono text-xs">
        !
      </div>
    </div>
    <div className="flex-1 space-y-2">
      <h4 className="font-bold text-pink-900 dark:text-pink-400">{title}</h4>
      <div className="text-[14px] text-pink-800/80 dark:text-pink-300/80 leading-relaxed">{children}</div>
    </div>
  </div>
);

export const Pagination = ({ 
  prev, 
  next 
}: { 
  prev?: { href: string; label: string }; 
  next?: { href: string; label: string } 
}) => (
  <div className="mt-20 pt-10 border-t border-muted flex flex-col sm:flex-row items-center justify-between gap-4">
    {prev ? (
      <Link 
        href={prev.href} 
        className="group flex flex-col items-start p-4 rounded-xl border border-muted hover:border-blue-500/50 hover:bg-blue-500/5 transition-all w-full sm:w-auto min-w-[200px]"
      >
        <span className="text-xs text-gray-500 uppercase tracking-widest mb-1 group-hover:text-blue-500 transition-colors">Previous</span>
        <span className="text-base font-semibold text-primary">{prev.label}</span>
      </Link>
    ) : <div />}
    
    {next ? (
      <Link 
        href={next.href} 
        className="group flex flex-col items-end p-4 rounded-xl border border-muted hover:border-blue-500/50 hover:bg-blue-500/5 transition-all w-full sm:w-auto min-w-[200px] text-right"
      >
        <span className="text-xs text-gray-500 uppercase tracking-widest mb-1 group-hover:text-blue-500 transition-colors">Next</span>
        <span className="text-base font-semibold text-primary">{next.label}</span>
      </Link>
    ) : <div />}
  </div>
);
