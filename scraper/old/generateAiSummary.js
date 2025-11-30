import fetch from 'node-fetch';
import dotenv from 'dotenv';

dotenv.config();
const { MODEL, API_KEY, GENERATION_ENGINE } = process.env;

const chatgpt = async (systemContent) => {
  const payload = {
    model: MODEL, // Replace with your desired model, e.g., "gpt-4" or "gpt-3.5-turbo"
    messages: [
      {
        role: "system",
        content: "Erzeuge JSON, die antwort muss im attribut summary die eigentliche Zusammenfassung enthalten, zusätzlich entählt das attribut advertising mit true oder false ob es sich um eine Werbung handelt und das Attribut tags ist ein Arary von Strings mit Tags die du im Artikel identifiziert hast. Gültige Tags sind 'Softwareentwicklung', 'Algorithmen', 'Datenanalyse', 'IT-Sicherheit', 'Künstliche Intelligenz', 'Elektromobilität', 'Klimaschutz', 'Migration', 'Politik', 'Gesellschaft', 'Wirtschaft', 'Fußball', 'Sport', 'Astronomie', 'Forschung', 'Gesundheit', 'Cyberkriminalität', 'Sicherheit', 'Innovation', 'Technologie', 'Medien', 'Kunst', 'Kultur', 'Bildung', 'Geschichte', 'Konflikte', 'Umwelt', 'Nachhaltigkeit', 'Weltraum', 'Infrastruktur', 'Verkehr', 'Recht', 'Demokratie', 'Handel', 'Energie', 'Musik', 'Film', 'Literatur', 'Wissenschaft'. Dies ist der Inhalt einer vollständigen HTML-Seite, die deine Kenntnisse definiert: " + systemContent,
      },
      {
        role: "user",
        content: "Fasse den Hauptartikel auf der Seite zusammen. Starte deine Antwort nicht mit der Artikel. Kommentiere nur den Hauptartikel. Antworte journalistisch.",
      },
    ],
    response_format: {
      type: "json_object"
    },
  };

  try {
    const response = await fetch("https://api.openai.com/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${API_KEY}`,
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      console.error("Call chatgpt failed:", response.status, response.statusText);
      console.error(await response.text());
      process.exit(1);
    }

    const result = await response.json();
    const summary = result.choices[0].message.content;

    return summary;
  } catch (error) {
    console.error("Error fetching chatgpt response:", error);
    throw error;
  }
};


const deepseek = async (systemContent) => {
  const payload = {
    model: MODEL,
    messages: [
      {
        role: "system",
        content: "Erzeuge JSON, die antwort muss im attribut summary die eigentliche Zusammenfassung enthalten, zusätzlich entählt das attribut advertising mit true oder false ob es sich um eine Werbung handelt und das Attribut tags ist ein Arary von Strings mit Tags die du im Artikel identifiziert hast. Gültige Tags sind 'Softwareentwicklung', 'Algorithmen', 'Datenanalyse', 'IT-Sicherheit', 'Künstliche Intelligenz', 'Elektromobilität', 'Klimaschutz', 'Migration', 'Politik', 'Gesellschaft', 'Wirtschaft', 'Fußball', 'Sport', 'Astronomie', 'Forschung', 'Gesundheit', 'Cyberkriminalität', 'Sicherheit', 'Innovation', 'Technologie', 'Medien', 'Kunst', 'Kultur', 'Bildung', 'Geschichte', 'Konflikte', 'Umwelt', 'Nachhaltigkeit', 'Weltraum', 'Infrastruktur', 'Verkehr', 'Recht', 'Demokratie', 'Handel', 'Energie', 'Musik', 'Film', 'Literatur', 'Wissenschaft'. Dies ist der Inhalt einer vollständigen HTML-Seite, die deine Kenntnisse definiert: " + systemContent,
      },
      {
        role: "user",
        content: "Fasse den Hauptartikel auf der Seite zusammen. Starte deine Antwort nicht mit der Artikel. Kommentiere nur den Hauptartikel. Antworte journalistisch.",
      },
    ],
    response_format: {
      type: "json_object"
    },
    max_tokens: 6000,
    presence_penalty: 1.0,
    stream: false,
  };

  // console.error(payload);

  try {
    const response = await fetch("https://api.deepseek.com/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${API_KEY}`,
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      console.error("Call deepseek failed:", response.status, response.statusText);
      console.error(await response.text());
      process.exit(1);
    }

    const resultBody = await response.text();
    if (!resultBody.trim()) {
      console.error("Empty news response from deepseek");
      process.exit(1);
    }
    const result = JSON.parse(resultBody);
    const summary = result.choices[0].message.content;

    return summary;
  } catch (error) {
    console.error("Error fetching deepseek response:", error);
    throw error;
  }
};

const anthropic = async (systemContent) => {
  const payload = {
    model: MODEL,
    messages: [
      {
        role: "user",
        content: "Erzeuge JSON, die antwort muss im attribut summary die eigentliche Zusammenfassung enthalten, zusätzlich entählt das attribut advertising mit " +
          " true oder false ob es sich um eine Werbung handelt und das Attribut tags ist ein Arary von Strings mit Tags die du im Artikel identifiziert hast. " +
          "Gültige Tags sind 'Softwareentwicklung', 'Algorithmen', 'Datenanalyse', 'IT-Sicherheit', 'Künstliche Intelligenz', 'Elektromobilität', 'Klimaschutz', " +
          "'Migration', 'Politik', 'Gesellschaft', 'Wirtschaft', 'Fußball', 'Sport', 'Astronomie', 'Forschung', 'Gesundheit', 'Cyberkriminalität', 'Sicherheit', " +
          "'Innovation', 'Technologie', 'Medien', 'Kunst', 'Kultur', 'Bildung', 'Geschichte', 'Konflikte', 'Umwelt', 'Nachhaltigkeit', 'Weltraum', 'Infrastruktur', " +
          "'Verkehr', 'Recht', 'Demokratie', 'Handel', 'Energie', 'Musik', 'Film', 'Literatur', 'Wissenschaft'. <content>" + systemContent + "</content>" +
          "Fasse den content auf der Seite zusammen. Starte deine Antwort nicht mit der Artikel. Kommentiere nur den Hauptartikel. Antworte journalistisch.",
      },
    ],
    "max_tokens": 8000
  };

  // console.error(payload);

  try {
    const response = await fetch("https://api.anthropic.com/v1/messages", {
      method: "POST",
      headers: {
        "x-api-key": API_KEY,
        "anthropic-version": "2023-06-01",
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      console.error("Call anthropics failed:", response.status, response.statusText);
      console.error(await response.text());
      process.exit(1);
    }

    const resultBody = await response.text();
    if (!resultBody.trim()) {
      console.error("Empty news response from anthropics");
      process.exit(1);
    }
    const result = JSON.parse(resultBody);
    const summary = result.content[0].text;

    return summary;
  } catch (error) {
    console.error("Error fetching anthropics response:", error);
    throw error;
  }
};

const ollamaHighMem = async (systemContent) => {

  const payload = {
    model: MODEL,
    messages: [
      {
        role: "system",
        content: "Erzeuge JSON, die antwort muss im attribut summary die eigentliche Zusammenfassung enthalten, zusätzlich entählt das attribut advertising mit true oder false ob es sich um eine Werbung handelt und das Attribut tags ist ein Arary von Strings mit Tags die du im Artikel identifiziert hast. Dies ist der Inhalt einer vollständigen HTML-Seite, die deine Kenntnisse definiert: " + systemContent,
      },
      {
        role: "user",
        content: "Fasse den Hauptartikel auf der Seite zusammen. Starte deine Antwort nicht mit der Artikel. Kommentiere nur den Hauptartikel. Antworte journalistisch.",
      },
    ],
    stream: false,
    options: {
      num_ctx: 50024
    },
    "format": {
      "type": "object",
      "properties": {
        "summary": {
          "type": "string"
        },
        "tags": {
          "type": "array",
          "items": {
            "type": "string"
          }
        },
        "advertising": {
          "type": "boolean"
        }
      },
      "required": [
        "summary",
        "tags",
        "advertising"
      ]
    }
  };

  // console.error(payload)

  const response = await fetch('http://localhost:11434/api/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });
  if (response.status != 200) {
    // exit 1
    console.error("Call ollama failed:", response);
    process.exit(1);
  }

  const result = await response.json();
  const summary = JSON.parse(result.message.content);

  return summary;
}

const ollamaHighMemTags = async (systemContent) => {

  const payload = {
    model: MODEL,
    messages: [
      {
        role: 'system',
        content: 'Dies ist der Inhalt einer vollständigen HTML-Seite, die deine Kenntnisse definiert: ' + systemContent,
      },
      {
        role: 'user',
        content: "Wähle 5 der relevantesten tags aus dieser Liste: 'Softwareentwicklung', 'Algorithmen', 'Datenanalyse', 'IT-Sicherheit', 'Künstliche Intelligenz', 'Elektromobilität', 'Klimaschutz', 'Migration', 'Politik', 'Gesellschaft', 'Wirtschaft', 'Fußball', 'Sport', 'Astronomie', 'Forschung', 'Gesundheit', 'Cyberkriminalität', 'Sicherheit', 'Innovation', 'Technologie', 'Medien', 'Kunst', 'Kultur', 'Bildung', 'Geschichte', 'Konflikte', 'Umwelt', 'Nachhaltigkeit', 'Weltraum', 'Infrastruktur', 'Verkehr', 'Recht', 'Demokratie', 'Handel', 'Energie', 'Musik', 'Film', 'Literatur', 'Wissenschaft'.",
      }
    ],
    stream: false,
    options: {
      num_ctx: 50024
    },
    "format": {
      "type": "object",
      "properties": {
        "tags": {
          "type": "array",
          "items": {
            "type": "string"
          }
        },
      },
      "required": [
        "tags"
      ]
    }
  };

  // console.error(payload)

  const response = await fetch('http://localhost:11434/api/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });
  if (response.status != 200) {
    // exit 1
    console.error("Call ollama failed:", response);
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
    console.error("Call ollama failed:", response);
    process.exit(1);
  }

  const output = await response.json();
  const summary = output.response;

  return summary;
}

const command = process.argv[2];

let systemContent = '';
process.stdin.setEncoding('utf8');

process.stdin.on('data', function(chunk) {
  systemContent += chunk;
});

process.stdin.on('end', async () => {
    // systemContent = data.trim().replace(/[\n\r\t]/g, '').replace(/"/g, ' ').replace(/'/g, ' ');

    if (!command || command === 'text') {
      switch (GENERATION_ENGINE) {
        case 'ollama-low':
          console.log(await ollamaLowMem(systemContent));
          break;
        case 'chatgpt':
          console.log(await chatgpt(systemContent));
          break;
        case 'deepseek':
          console.log(await deepseek(systemContent));
          break;
        case 'anthropic':
          console.log(await anthropic(systemContent));
          break;
        case 'ollama-high':
          console.log(JSON.stringify(await ollamaHighMem(systemContent)));
          break;
        default:
          console.log("Invalid generation engine specified");
          process.exit(1);
      }
    } else if (command === 'tags') {
      switch (GENERATION_ENGINE) {
        case 'ollama-high':
          console.log(await ollamaHighMemTags(systemContent));
          break;
        default:
          console.log("Invalid generation engine specified");
          process.exit(1);
      }
    }

});
