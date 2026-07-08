import http from 'k6/http';
import { group, sleep, SharedArray } from 'k6';
import { Counter, Gauge, Trend, Rate } from 'k6/metrics';
import { setupTestData } from '../utils/helpers.js';
import { sharedData } from '../utils/data.json';
//export { options } from '../config/options.js';

const loadTestCounter = new Counter('load_test_counter');
const loadTestGauge = new Gauge('load_test_gauge');
const loadTestTrend = new Trend('load_test_trend');
const loadTestRate = new Rate('load_test_rate');
const successCounter = new Counter('success_counter');
const failureCounter = new Counter('failure_counter');
const loadTestCounterOne = new Counter('load_test_counter_one');
const loadTestGaugeOne = new Gauge('load_test_gauge_one');
const loadTestTrendOne = new Trend('load_test_trend_one');
const loadTestRateOne = new Rate('load_test_rate_one');
const successCounterOne = new Counter('success_counter_one');
const failureCounterOne = new Counter('failure_counter_one');

// `K6_VUS` is provided via environment variables (e.g. `setx K6_VUS 10`).
// Use `__ENV.K6_VUS` at runtime and fall back to 10 if missing/invalid.

let VUS = 10;
if (__ENV.K6_VUS) {
  const parsed = parseInt(__ENV.K6_VUS, 10);
  if (!isNaN(parsed)) {
    VUS = parsed;
  } else {
    console.log('Invalid K6_VUS value, using default 10');
  }
}

export let options = {
  vus: VUS,

  stages: [
    { duration: '10s', target: 5 }, 
    { duration: '2s', target: 10 },// Ramp up to 10 VUs over 30 seconds
    { duration: '10s', target: 10 },  // Stay at 10 VUs for 1 minute
    { duration: '1s', target: 0 },   // Ramp down to 0 VUs over 30 seconds
  ],
  
 // duration: '10s',
  //rps: 100, // requests per second
  thresholds: {
    'http_req_duration': ['p(95)<1200' , 'p(99)<1500' , 'avg<10'], // 95% and 99% of requests should be below 200ms and 150ms respectively
    'http_req_failed': ['rate<0.01'], // error rate should be less than 1%
  },
};



export function setup() {
  console.log('Setting up load test...');
  // prepare and return data for VUs
  return setupTestData();
}

const sharedDataArray = new SharedArray('test data', function() {
  return sharedData || [{ url: 'https://test.k6.io' }];
});

export function load_test() 
{
  const testData = sharedDataArray.length ? sharedDataArray[0] : { url: 'https://test.k6.io' };
  group ('Load Test Group one', function () {
    const params = {
      headers: {
        'Content-Type': 'application/json',
      },
      cookies: {
        session_id: 'abc123',
      },
    };
    const url = testData.url || 'https://test.k6.io';
    const res = http.get(url, params);
  loadTestCounter.add(res.timings.duration);
  loadTestGauge.add(res.timings.duration);
  loadTestTrend.add(res.timings.duration);
  loadTestRate.add(res.status === 200);

  const isSuccessful = res.status === 200;
  successCounter.add(isSuccessful ? 1 : 0);
  failureCounter.add(isSuccessful ? 0 : 1);
  });

  group ('Load Test Group two', function () {
  const url = 'https://test.k6.io';
  const resOne = http.get(url);
  loadTestCounterOne.add(resOne.timings.duration);
  loadTestGaugeOne.add(resOne.timings.duration);
  loadTestTrendOne.add(resOne.timings.duration);
  loadTestRateOne.add(resOne.status === 200);

  const isSuccessful = resOne.status === 200;
  successCounterOne.add(isSuccessful ? 1 : 0);
  failureCounterOne.add(isSuccessful ? 0 : 1);
  });
  // Custom90thPercentileTrend.add(res.timings.duration);

  //console.log('Load Test - Response status: ' + res.status);
  //console.log('Load Test - Response time: ' + res.timings.duration + ' ms');

  sleep(1);
}

export default load_test;

export function teardown() {
  console.log('Tearing down load test...');
}