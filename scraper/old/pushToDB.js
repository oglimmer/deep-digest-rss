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
    // console.log(systemContent);

    if (!systemContent.trim()) {
      console.error('No content to push to db');
      process.exit(1);
    }

    const parsedContent = JSON.parse(systemContent);
    const { summary, tags, advertising } = parsedContent;

    const response = await fetch(URL + '/api/v1/news', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + Buffer.from(USERNAME + ':' + PASSWORD).toString('base64')
      },
      body: JSON.stringify({
        feedId: feedId,
        originalFeedItemId: id,
        text: summary,
        tags: tags,
        advertising: advertising
      })
    });

    if (response.status != 200) {
      console.error('Call to news-api failed. ', response);
      process.exit(1);
    }

  } catch (error) {
    console.error(error);
    process.exit(1);
  }
});