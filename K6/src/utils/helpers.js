export function setupTestData() {
  console.log('Setting up test data...');
  // You can add code here to prepare any data needed for your tests
  return { url: 'https://test.k6.io' };
}


export function logResponse(res) {
  console.log(`Response status: ${res.status}`);
}

export function validateResponse(res, expectedStatus) {
  if (res.status !== expectedStatus) {
    throw new Error(`Expected status ${expectedStatus}, but got ${res.status}`);
  }
}

export function generateRandomData(length) {
  let result = '';
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  const charactersLength = characters.length;
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength));
  }
  return result;
}