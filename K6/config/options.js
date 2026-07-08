export let options = {
  vus: K6_VUS,
  duration: '10s',
  rps: 100, // requests per second
  thresholds: {
    'http_req_duration': ['p(95)<1200' , 'p(99)<1500' , 'avg<10'], // 95% and 99% of requests should be below 200ms and 150ms respectively
    'http_req_failed': ['rate<0.01'], // error rate should be less than 1%
  },
};