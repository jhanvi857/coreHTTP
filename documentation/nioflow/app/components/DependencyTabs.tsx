"use client";

import { useState } from "react";

const DEPENDENCIES = {
  Maven: `<dependency>
  <groupId>io.github.jhanvi857</groupId>
  <artifactId>nioflow-framework</artifactId>
  <version>1.1.0</version>
</dependency>`,
  Gradle: `implementation 'io.github.jhanvi857:nioflow-framework:1.1.0'`,
  "Gradle (Kotlin)": `implementation("io.github.jhanvi857:nioflow-framework:1.1.0")`,
  SBT: `libraryDependencies += "io.github.jhanvi857" % "nioflow-framework" % "1.1.0"`
};

export default function DependencyTabs() {
  const [activeTab, setActiveTab] = useState<keyof typeof DEPENDENCIES>("Maven");
  const [copied, setCopied] = useState(false);

  const copyToClipboard = () => {
    navigator.clipboard.writeText(DEPENDENCIES[activeTab]);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="w-full max-w-2xl">
      <div className="flex flex-col mb-6">
        <h3 className="text-xl font-bold text-white mb-2 flex items-center gap-2">
          Available on Maven Central
          <span className="flex h-2 w-2 rounded-full bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.8)]" />
        </h3>
        <p className="text-gray-400 text-[15px]">
          Add NioFlow as a dependency in Maven, Gradle, or SBT
        </p>
        <a
          href="https://central.sonatype.com/artifact/io.github.jhanvi857/nioflow-framework"
          target="_blank"
          className="text-blue-400 text-sm mt-3 hover:underline flex items-center gap-1 w-fit"
        >
          View on Maven Central
          <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
          </svg>
        </a>
      </div>

      <div className="bg-[#111] border border-[#222] rounded-xl overflow-hidden shadow-2xl relative group/code">
        <div className="absolute top-0 right-0 h-px w-full bg-gradient-to-r from-transparent via-blue-500/50 to-transparent" />

        <div className="flex items-center justify-between px-2 py-0 border-b border-[#222] bg-[#0a0a0a]">
          <div className="flex overflow-x-auto no-scrollbar">
            {(Object.keys(DEPENDENCIES) as Array<keyof typeof DEPENDENCIES>).map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`text-[13px] font-medium px-4 py-3 transition-all relative whitespace-nowrap ${activeTab === tab
                    ? "text-white"
                    : "text-gray-500 hover:text-gray-300"
                  }`}
              >
                {tab}
                {activeTab === tab && (
                  <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.4)]" />
                )}
              </button>
            ))}
          </div>
          <button
            onClick={copyToClipboard}
            className="flex items-center gap-2 px-3 py-1.5 rounded-md bg-[#222] hover:bg-[#333] text-gray-300 text-xs transition-colors border border-[#333] mr-2"
          >
            {copied ? (
              <>
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-green-400"><polyline points="20 6 9 17 4 12" /></svg>
                <span className="font-medium">Copied!</span>
              </>
            ) : (
              <>
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect width="14" height="14" x="8" y="8" rx="2" ry="2" /><path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2" /></svg>
                <span className="font-medium">Copy</span>
              </>
            )}
          </button>
        </div>
        <div className="p-6 bg-black/40 font-mono text-[13px] md:text-sm text-blue-100/90 leading-relaxed overflow-x-auto min-h-[140px] flex items-center">
          <pre className="w-full">
            {DEPENDENCIES[activeTab]}
          </pre>
        </div>
      </div>
    </div>
  );
}
