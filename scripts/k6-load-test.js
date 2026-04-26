import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const jwtToken = __ENV.JWT_TOKEN;

export const options = {
  scenarios: {
    steady_state: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '60s', target: 20 },
        { duration: '30s', target: 0 }
      ],
      gracefulRampDown: '10s'
    },
    rate_limiting_stress: {
      executor: 'constant-vus',
      vus: 10,
      duration: '30s',
      startTime: '30s', // Starts during steady state
    }
  },
  thresholds: {
    checks: ['rate>0.95'],
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<250', 'p(99)<500']
  }
};

export function setup() {
  // If token provided via env, use it
  if (jwtToken) return { token: jwtToken };

  // Otherwise, attempt to login to get a fresh token
  console.log(`Attempting login at ${baseUrl}/api/auth/login`);
  const res = http.post(`${baseUrl}/api/auth/login`, JSON.stringify({
    username: 'admin',
    password: 'password'
  }), {
    headers: { 'Content-Type': 'application/json' }
  });

  if (res.status === 200) {
    const token = res.json().token || res.json().accessToken;
    console.log('Login successful');
    return { token: token };
  }
  
  console.warn(`Login failed with status ${res.status}. Proceeding without token.`);
  return { token: null };
}

export default function (data) {
  const params = {
    headers: {
      'Authorization': `Bearer ${data.token}`,
      'Content-Type': 'application/json'
    },
  };

  // 1. Regular authenticated request
  const tasks = http.get(`${baseUrl}/api/tasks/`, params);
  check(tasks, {
    'tasks status is 200': (r) => r.status === 200,
    'latency < 100ms': (r) => r.timings.duration < 100
  });

  // 2. High-frequency request to trigger rate limiting (429)
  // This helps demonstrate the framework's protective behavior under load
  const limited = http.get(`${baseUrl}/api/tasks/`, params);
  check(limited, {
    'request allowed or rate-limited': (r) => r.status === 200 || r.status === 429
  });

  sleep(Math.random() * 0.5 + 0.1); // Randomized pacing
}

export function handleSummary(data) {
  return {
    'stdout': JSON.stringify(data), // For CI parsing
    'summary.json': JSON.stringify(data),
  };
}
