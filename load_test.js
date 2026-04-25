import http from 'k6/http';
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
}
