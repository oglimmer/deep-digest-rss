import fetch from 'node-fetch';
import { promises as fs } from 'fs';
import dotenv from 'dotenv';

dotenv.config();
const { USERNAME, PASSWORD, URL, MODEL } = process.env;

const sendMessage = async (refId, url, title, systemContent) => {
  try {

    // Prepare the request payload
    const payload = {
      model: MODEL,
      messages: [
        {
          role: 'system',
          content: 'Dies ist der Inhalt einer vollständigen HTML-Seite, die deine Kenntnisse definiert: ' + systemContent,
        },
        {
          role: 'user',
          content: 'Fasse den den Hauptartikel auf der Seite zusammen. Verzichte daran dass der Text beginnt mit Der Artikel',
        },
      ],
      stream: false,
      options: {
        num_ctx: 50024
      }
    };

    // console.log(payload)

    // Use fetch to send the POST request
    const response = await fetch('http://localhost:11434/api/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    // Parse the JSON response
    const result = await response.json();
    const summary = result.message.content;
    console.log(summary);

    try {
      const response = await fetch(URL + '/api/v1/news', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Basic ' + Buffer.from(USERNAME + ':' + PASSWORD).toString('base64')
        },
        body: JSON.stringify({
          refId: refId,
          url: url,
          title: title,
          text: summary
        })
      });
      const result = await response.json();
      if (result.status != 200) {
        console.error('Error:', response);
      }

    } catch (error) {
      console.error(error);
    }

  } catch (error) {
    console.error('Error:', error);
  }
};


const refId = process.argv[2];
const url = process.argv[3];
const title = process.argv[4];

let systemContent = '';
process.stdin.setEncoding('utf8');

process.stdin.on('data', function(chunk) {
  systemContent += chunk;
});

process.stdin.on('end', function() {
  sendMessage(refId, url, title, systemContent);
});

