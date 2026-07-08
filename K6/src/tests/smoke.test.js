import http from 'k6/http';
import { check, sleep } from 'k6';

export default function () {
  const url = 'https://test.k6.io';
  const res = http.get(url);

  check(res, {
    'is status 200': (r) => r.status === 200,
    'response time is less than 200ms': (r) => r.timings.duration < 200,
  });

  sleep(1);
}