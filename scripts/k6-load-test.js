import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    steady_state: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '60s', target: 20 },
        { duration: '20s', target: 0 }
      ],
      gracefulRampDown: '10s'
    }
  },
  thresholds: {
    checks: ['rate>0.99'],
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<250', 'p(99)<500']
  }
};

export default function () {
  const health = http.get(`${baseUrl}/_health`);
  check(health, {
    'health status is 200': (r) => r.status === 200
  });

  const ready = http.get(`${baseUrl}/_ready`);
  check(ready, {
    'ready status is 200 or 503': (r) => r.status === 200 || r.status === 503
  });

  const metrics = http.get(`${baseUrl}/metrics`);
  check(metrics, {
    'metrics status is 200': (r) => r.status === 200
  });

  sleep(0.2);
}
