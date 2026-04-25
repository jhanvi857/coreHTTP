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
      <div className="overflow-x-auto my-6">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-muted">
              <th className="py-2 font-semibold">Metric</th>
              <th className="py-2 font-semibold">Value</th>
              <th className="py-2 font-semibold">Notes</th>
            </tr>
          </thead>
          <tbody>
            <tr className="border-b border-muted/50">
              <td className="py-3">Throughput</td>
              <td className="py-3 font-mono">501.12 req/s</td>
              <td className="py-3">Sustained under peak load</td>
            </tr>
            <tr className="border-b border-muted/50">
              <td className="py-3">Median Latency (p50)</td>
              <td className="py-3 font-mono">1.52 ms</td>
              <td className="py-3">Very low framework overhead</td>
            </tr>
            <tr className="border-b border-muted/50">
              <td className="py-3">p95 Latency</td>
              <td className="py-3 font-mono">47.75 ms</td>
              <td className="py-3">Acceptable tail latency</td>
            </tr>
            <tr className="border-b border-muted/50">
              <td className="py-3">p99 Latency</td>
              <td className="py-3 font-mono">74.62 ms</td>
              <td className="py-3">Queuing delays at max worker capacity</td>
            </tr>
            <tr>
              <td className="py-3">Success Rate</td>
              <td className="py-3 font-mono">99.84%</td>
              <td className="py-3">0.16% connection drops at peak</td>
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
