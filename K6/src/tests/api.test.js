import http from 'k6/http';
import { check } from 'k6';
import { options } from '../../config/options';

export default function () {
  const url = 'https://api.example.com/endpoint'; // Replace with your API endpoint
  const res = http.get(url);

  check(res, {
    'is status 200': (r) => r.status === 200,
    'response time is less than 200ms': (r) => r.timings.duration < 200,
  });
}