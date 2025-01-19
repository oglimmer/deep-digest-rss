import fetch from 'node-fetch';
import dotenv from 'dotenv';

dotenv.config();
const { MODEL, CHATGPT_API_KEY, GENERATION_ENGINE } = process.env;

const chatgpt = async (systemContent) => {
  const payload = {
    model: "gpt-4o-mini", // Replace with your desired model, e.g., "gpt-4" or "gpt-3.5-turbo"
    messages: [
      {
        role: "system",
        content: "Dies ist der Inhalt einer vollständigen HTML-Seite, die deine Kenntnisse definiert: " + systemContent,
      },
      {
        role: "user",
        content: "Fasse den Hauptartikel auf der Seite zusammen. Starte deine Antwort nicht mit der Artikel. Kommentiere nur den Hauptartikel. Antworte journalistisch.",
      },
    ],
  };

  try {
    const response = await fetch("https://api.openai.com/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${CHATGPT_API_KEY}`,
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      console.log("Call chatgpt failed:", response.status, response.statusText);
      console.log(await response.text());
      process.exit(1);
    }

    const result = await response.json();
    const summary = result.choices[0].message.content;

    return summary;
  } catch (error) {
    console.error("Error fetching ChatGPT response:", error);
    throw error;
  }
};


const ollamaHighMem = async (systemContent) => {
  
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
  if (response.status != 200) {
    // exit 1
    console.log("Call ollama failed:", response);
    process.exit(1);
  }

  const result = await response.json();
  const summary = result.message.content;

  return summary;
}

const ollamaLowMem = async (systemContent) => {

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

    switch (GENERATION_ENGINE) {
      case 'ollama-low':
        console.log(await ollamaLowMem(systemContent));
        break;
      case 'chatgpt':
        console.log(await chatgpt(systemContent));
        break;
      case 'ollama-high':
        console.log(await ollamaHighMem(systemContent));
        break;
      default:
        console.log("Invalid generation engine specified");
        process.exit(1);
    }

});

