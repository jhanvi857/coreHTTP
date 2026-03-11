"use client";

import { useState } from "react";

const CODE_SNIPPET = `public class ServerApp {
    public static void main(String[] args) {
        Router router = new Router();

        // Middleware Stack
        router.use(new LoggerMiddleware());
        router.use(new CorsMiddleware());

        // Auth Protected Route
        router.get("/api/orders", 
            Auth.require(Role.ADMIN), 
            new OrderController()::list
        );

        new HttpServer(8080).start(router);
    }
}`;

export default function CodeDemo() {
    const [copied, setCopied] = useState(false);

    const copyToClipboard = () => {
        navigator.clipboard.writeText(CODE_SNIPPET);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <div className="relative mx-auto w-full max-w-4xl rounded-xl border border-muted bg-[#0a0a0a] shadow-2xl overflow-hidden mt-8">
            <div className="absolute top-0 right-0 h-px w-full bg-gradient-to-r from-transparent via-blue-500/50 to-transparent" />

            <div className="relative overflow-hidden">
                {/* Window controls */}
                <div className="flex items-center justify-between border-b border-[#333] px-4 py-3 bg-[#111]">
                    <div className="flex items-center gap-2">
                        <div className="flex gap-1.5 mr-4">
                            <div className="h-2.5 w-2.5 rounded-full bg-[#ff5f56]" />
                            <div className="h-2.5 w-2.5 rounded-full bg-[#ffbd2e]" />
                            <div className="h-2.5 w-2.5 rounded-full bg-[#27c93f]" />
                        </div>
                        <div className="text-xs text-gray-400 font-mono flex items-center gap-2">
                            <svg className="w-3 h-3 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" /></svg>
                            ServerApp.java
                        </div>
                    </div>
                    <button
                        onClick={copyToClipboard}
                        className="flex items-center justify-center h-7 w-7 rounded-md bg-transparent hover:bg-gray-800 text-gray-400 transition-colors"
                        title={copied ? "Copied" : "Copy code"}
                    >
                        {copied ? (
                            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-green-400"><polyline points="20 6 9 17 4 12" /></svg>
                        ) : (
                            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect width="14" height="14" x="8" y="8" rx="2" ry="2" /><path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2" /></svg>
                        )}
                    </button>
                </div>

                {/* Code content */}
                <div className="overflow-x-auto p-4 md:p-6 pb-6 text-[13px] md:text-sm leading-relaxed">
                    <pre className="text-gray-300 font-mono">
                        {CODE_SNIPPET.split('\n').map((line, i) => (
                            <div key={i} className="table-row">
                                <span className="table-cell select-none pr-6 text-right text-gray-600 border-r border-[#333]">{i + 1}</span>
                                <span className="table-cell whitespace-pre pl-6">
                                    {line.split(/(\s+)/).map((part, j) => {
                                        if (['public', 'class', 'static', 'void', 'new', 'return'].includes(part.trim())) {
                                            return <span key={j} className="text-[#f97583]">{part}</span>;
                                        }
                                        if (['Router', 'HttpServer', 'Auth', 'Role', 'OrderController'].includes(part.trim())) {
                                            return <span key={j} className="text-[#b392f0]">{part}</span>;
                                        }
                                        if (part.startsWith('"') || part.endsWith('"')) {
                                            return <span key={j} className="text-[#9ecbff]">{part}</span>;
                                        }
                                        if (part.trim().startsWith('//')) {
                                            return <span key={j} className="italic text-[#6a737d]">{part}</span>;
                                        }
                                        return <span key={j}>{part}</span>;
                                    })}
                                </span>
                            </div>
                        ))}
                    </pre>
                </div>
            </div>
        </div>
    );
}
