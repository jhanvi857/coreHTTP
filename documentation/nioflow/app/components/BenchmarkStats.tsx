import React from 'react';

const BenchmarkStats = () => {
  const stats = [
    { label: 'Throughput', value: '501.12', unit: 'req/s', color: 'text-blue-400', glow: 'shadow-[0_0_15px_rgba(59,130,246,0.1)]' },
    { label: 'p50 Latency', value: '1.52', unit: 'ms', color: 'text-green-400', glow: 'shadow-[0_0_15px_rgba(74,222,128,0.1)]' },
    { label: 'p99 Latency', value: '74.62', unit: 'ms', color: 'text-yellow-400', glow: 'shadow-[0_0_15px_rgba(250,204,21,0.1)]' },
    { label: 'Success Rate', value: '99.84', unit: '%', color: 'text-purple-400', glow: 'shadow-[0_0_15px_rgba(192,132,252,0.1)]' },
  ];

  return (
    <div className="mt-12 w-full max-w-lg">
      <div className="flex items-center gap-3 mb-6">
        <div className="h-[1px] flex-1 bg-gradient-to-r from-transparent to-[#333]"></div>
        <span className="text-[11px] font-mono uppercase tracking-[0.3em] text-gray-400 font-semibold whitespace-nowrap">Verified Performance</span>
        <div className="h-[1px] flex-1 bg-gradient-to-l from-transparent to-[#333]"></div>
      </div>
      
      <div className="grid grid-cols-1 min-[400px]:grid-cols-2 gap-4">
        {stats.map((stat, index) => (
          <div 
            key={index} 
            className={`p-4 md:p-5 rounded-2xl border border-[#222] bg-gradient-to-br from-[#0f0f0f] to-[#050505] flex flex-col gap-2 hover:border-[#444] transition-all duration-300 group ${stat.glow}`}
          >
            <span className="text-[12px] text-gray-500 font-medium group-hover:text-gray-400 transition-colors whitespace-nowrap">{stat.label}</span>
            <div className="flex items-baseline gap-1.5 flex-wrap">
              <span className={`text-2xl md:text-3xl font-bold tracking-tight ${stat.color} drop-shadow-sm`}>
                {stat.value}
              </span>
              <span className="text-[10px] text-gray-600 font-mono font-bold uppercase">{stat.unit}</span>
            </div>
          </div>
        ))}
      </div>
      
      <div className="mt-6 flex flex-wrap items-center justify-center gap-2 text-[11px] text-gray-500 font-mono text-center">
        <span className="flex h-1.5 w-1.5 rounded-full bg-green-500 animate-pulse flex-shrink-0" />
        <span>Hardware-validated via k6 v1.7.1 (100 VUs)</span>
      </div>
    </div>
  );
};

export default BenchmarkStats;
