import fetch from 'node-fetch';
import { promises as fs } from 'fs';
import dotenv from 'dotenv';

dotenv.config();
const { MODEL } = process.env;

const f2 = async (systemContent) => {
  
  const payload = {
    model: MODEL,
    messages: [
      {
        role: 'system',
        content: 'Dies ist der Inhalt einer vollständigen HTML-Seite, die deine Kenntnisse definiert: ' + systemContent,
      },
      {
        role: 'user',
        content: 'Fasse den den Hauptartikel auf der Seite zusammen. Starte deine Antwort nicht mit der Artikel. Kommentiere nur den Hauptartikel. Antworte journalistisch.',
      },
    ],
    stream: false,
    options: {
      num_ctx: 50024
    }
  };

  // console.log(payload)

  const response = await fetch('http://localhost:11434/api/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  const result = await response.json();
  const summary = result.message.content;

  return summary;
}

const f1 = async (systemContent) => {

  const response = await fetch('http://localhost:11434/api/generate', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ 
      model: MODEL, 
      prompt: `Fasse den den Hauptartikel auf der folgenden Seite zusammen. Starte deine Antwort nicht mit der Artikel. Kommentiere nur den Hauptartikel. Antworte journalistisch. ${systemContent}`, 
      stream: false 
    }),
  });

  if (response.status != 200) {
    // exit 1
    console.log("Call ollama failed:", response);
    process.exit(1);
  }

  const output = await response.json();
  const summary = output.response;

  return summary;
}

let systemContent = '';
process.stdin.setEncoding('utf8');

process.stdin.on('data', function(chunk) {
  systemContent += chunk;
});

process.stdin.on('end', async () => {
    // systemContent = data.trim().replace(/[\n\r\t]/g, '').replace(/"/g, ' ').replace(/'/g, ' ');

    const summary = await f1(systemContent);

    console.log(summary);
});

