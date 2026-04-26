import { CodeBlock, H2, H3, P } from "../_components";

export default function PerformancePage() {
  return (
    <>
      <h1 className="text-3xl md:text-4xl font-bold tracking-tight mb-4 text-gray-900 dark:text-white">Performance Benchmarks</h1>
      <P>NioFlow is built for speed and efficiency. Below are the results of our standard load tests targeting the unauthenticated health check endpoint.</P>

      <H2 id="test-overview">Test Overview</H2>
      <P>The test was conducted using **k6** against the `task-planner-app` reference implementation. We used a graduated load profile to observe the framework's behavior from zero to peak capacity.</P>
      
      <ul className="list-disc pl-6 space-y-2 my-4 text-gray-700 dark:text-gray-300">
        <li><strong>Tooling:</strong> k6 v1.7.1</li>
        <li><strong>Profile:</strong> 0 to 100 Virtual Users (VUs) over 2 minutes</li>
        <li><strong>Endpoint:</strong> GET /_health</li>
        <li><strong>Hardware:</strong> Localhost (Standard Developer Workstation)</li>
      </ul>

      <H2 id="results">Results Matrix</H2>
      <div className="my-8 rounded-2xl border border-muted bg-surface/50 overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-muted/30 border-b border-muted">
              <th className="py-4 px-6 font-semibold text-sm uppercase tracking-wider text-gray-500">Metric</th>
              <th className="py-4 px-6 font-semibold text-sm uppercase tracking-wider text-gray-500">Value</th>
              <th className="py-4 px-6 font-semibold text-sm uppercase tracking-wider text-gray-500">Notes</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-muted/50">
            <tr className="hover:bg-muted/10 transition-colors">
              <td className="py-5 px-6 font-medium text-gray-900 dark:text-white">Throughput</td>
              <td className="py-5 px-6">
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-mono font-bold bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300">
                  501.12 req/s
                </span>
              </td>
              <td className="py-5 px-6 text-sm text-gray-600 dark:text-gray-400">Sustained under peak load (100 VUs)</td>
            </tr>
            <tr className="hover:bg-muted/10 transition-colors">
              <td className="py-5 px-6 font-medium text-gray-900 dark:text-white">Median Latency (p50)</td>
              <td className="py-5 px-6">
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-mono font-bold bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300">
                  1.52 ms
                </span>
              </td>
              <td className="py-5 px-6 text-sm text-gray-600 dark:text-gray-400">Minimal framework overhead for routing</td>
            </tr>
            <tr className="hover:bg-muted/10 transition-colors">
              <td className="py-5 px-6 font-medium text-gray-900 dark:text-white">p95 Latency</td>
              <td className="py-5 px-6 font-mono text-sm">47.75 ms</td>
              <td className="py-5 px-6 text-sm text-gray-600 dark:text-gray-400">Efficient queue management</td>
            </tr>
            <tr className="hover:bg-muted/10 transition-colors">
              <td className="py-5 px-6 font-medium text-gray-900 dark:text-white">p99 Latency</td>
              <td className="py-5 px-6 font-mono text-sm text-yellow-600 dark:text-yellow-400">74.62 ms</td>
              <td className="py-5 px-6 text-sm text-gray-600 dark:text-gray-400">Queuing delay at max worker capacity</td>
            </tr>
            <tr className="hover:bg-muted/10 transition-colors">
              <td className="py-5 px-6 font-medium text-gray-900 dark:text-white">Success Rate</td>
              <td className="py-5 px-6">
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-mono font-bold bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-300">
                  99.84%
                </span>
              </td>
              <td className="py-5 px-6 text-sm text-gray-600 dark:text-gray-400">High reliability under saturation</td>
            </tr>
          </tbody>
        </table>
      </div>


      <H2 id="analysis">Technical Analysis</H2>
      <H3>Efficiency and Overhead</H3>
      <P>The median latency of **1.52ms** indicates that NioFlow's NIO-based request parsing and routing engine is extremely lightweight. The framework does not hide complexity behind heavy reflection, allowing for direct and fast request resolution.</P>

      <H3>Scaling Limits</H3>
      <P>At 100 concurrent VUs, the server reached the limits of its default worker pool (64 threads). This is visible in the jump between p50 and p99 latency, which is caused by requests waiting in the internal 1000-slot queue. The 0.16% error rate occurred when the OS-level listen backlog was momentarily saturated during peak concurrent connections.</P>

      <H2 id="repro">Reproducing the Results</H2>
      <P>You can run this test yourself using the provided k6 script in the repository root.</P>
      <CodeBlock
        title="load_test.js"
        language="javascript"
        code={`import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '60s', target: 100 },
    { duration: '30s', target: 0 },
  ],
};

export default function () {
  http.get('http://localhost:8080/_health');
  sleep(0.1);
}`}
      />
      <CodeBlock
        title="Run Command"
        language="bash"
        code={`k6 run load_test.js`}
      />
    </>
  );
}
