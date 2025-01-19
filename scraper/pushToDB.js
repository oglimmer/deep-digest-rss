import fetch from 'node-fetch';
import dotenv from 'dotenv';

dotenv.config();
const { USERNAME, PASSWORD, URL } = process.env;

const feedId = process.argv[2];
const id = process.argv[3];

let systemContent = '';
process.stdin.setEncoding('utf8');

process.stdin.on('data', function(chunk) {
  systemContent += chunk;
});

process.stdin.on('end', async () => {
  try {
    const response = await fetch(URL + '/api/v1/news', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + Buffer.from(USERNAME + ':' + PASSWORD).toString('base64')
      },
      body: JSON.stringify({
        feedId: feedId,
        originalFeedItemId: id,
        text: systemContent
      })
    });
    // const result = await response.json();
    if (response.status != 200) {
      console.error('Call to news-api failed. ', response);
      process.exit(1);
    }

  } catch (error) {
    console.error(error);
    process.exit(1);
  }
});
