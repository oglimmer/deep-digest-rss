import fetch from 'node-fetch';
import dotenv from 'dotenv';
import fs from 'fs';

if (!process.env.NO_ENV_FILE) {
    dotenv.config();
}
const { USERNAME, PASSWORD, URL, MODEL, API_KEY, GENERATION_ENGINE } = process.env;

try {
    const response = await fetch(URL + '/api/v1/tag-group/raw', {
        method: 'GET',
        headers: {
          'Authorization': 'Basic ' + Buffer.from(USERNAME + ':' + PASSWORD).toString('base64')
        }
    });

    if (response.status != 200) {
        console.error('Call to tag-group/raw failed. ', response);
        process.exit(1);
    }

    const tagGroups = await response.json();
    const context = "'" + tagGroups.join("','") + "'";

    // const question = "look at all the words. cluster them into groups of related words. identify the largest groups. show the top 10 most important groups only. no explanations. return JSON with group name as attribute name and for each the top 10 words grouped into it as a string array."
    const question = "Given the following tags: " + context + ', please group them into  the following categories { "Technology": [ "Softwareentwicklung", "Algorithmen", "Datenanalyse", "IT-Sicherheit", "Künstliche Intelligenz", "Elektromobilität", "Technologie", "Innovation", "Cyberkriminalität" ], "Environment": [ "Klimaschutz", "Umwelt", "Nachhaltigkeit", "Energie" ], "Politics": [ "Politik", "Gesellschaft", "Demokratie", "Migration", "Konflikte", "Recht", "Sicherheit" ], "Economy": [ "Wirtschaft", "Verkehr", "Handel", "Infrastruktur" ], "Science": [ "Astronomie", "Forschung", "Weltraum", "Wissenschaft" ], "Culture": [ "Kunst", "Kultur", "Medien", "Bildung", "Geschichte", "Literatur", "Film", "Musik" ], "Health": [ "Gesundheit" ], "Sports": [ "Fußball", "Sport" ] }. Keep the categories and put all tags into an appropriate category. Return JSON with category name as attribute and for each category the top 15 tags grouped into it as a string array.'

    let genResult;
    if (GENERATION_ENGINE === 'chatgpt') {

        const payload = {
            model: MODEL, // Replace with your desired model, e.g., "gpt-4" or "gpt-3.5-turbo"
            messages: [
                // {
                //     role: "system",
                //     content: "Erzeuge JSON, die antwort muss im attribut summary die eigentliche Zusammenfassung enthalten, zusätzlich entählt das attribut advertising mit true oder false ob es sich um eine Werbung handelt und das Attribut tags ist ein Arary von Strings mit Tags die du im Artikel identifiziert hast. Gültige Tags sind 'Softwareentwicklung', 'Algorithmen', 'Datenanalyse', 'IT-Sicherheit', 'Künstliche Intelligenz', 'Elektromobilität', 'Klimaschutz', 'Migration', 'Politik', 'Gesellschaft', 'Wirtschaft', 'Fußball', 'Sport', 'Astronomie', 'Forschung', 'Gesundheit', 'Cyberkriminalität', 'Sicherheit', 'Innovation', 'Technologie', 'Medien', 'Kunst', 'Kultur', 'Bildung', 'Geschichte', 'Konflikte', 'Umwelt', 'Nachhaltigkeit', 'Weltraum', 'Infrastruktur', 'Verkehr', 'Recht', 'Demokratie', 'Handel', 'Energie', 'Musik', 'Film', 'Literatur', 'Wissenschaft'. Dies ist der Inhalt einer vollständigen HTML-Seite, die deine Kenntnisse definiert: " + systemContent,
                // },
                {
                    role: "user",
                    content: question,
                },
            ],
            response_format: {
                type: "json_object"
            }
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
            genResult = result.choices[0].message.content;

        } catch (error) {
            console.error("Error fetching chatgpt response:", error);
            throw error;
        }
    } else if (GENERATION_ENGINE === 'anthropic') {

        const payload = {
            model: MODEL,
            messages: [
                {
                    role: "user",
                    content: question,
                },
            ],
            "max_tokens": 8000
        };

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
                console.error("Call anthropic failed:", response.status, response.statusText);
                console.error(await response.text());
                process.exit(1);
            }

            const result = await response.json();
            genResult = result.content[0].text;

        } catch (error) {
            console.error("Error fetching anthropic response:", error);
            throw error;
        }

    } else if (GENERATION_ENGINE === 'ollama-high') {

        const payload = {
            model: MODEL,
            messages: [
                {
                    role: "user",
                    content: question,
                },
            ],
            stream: false,
            options: {
                num_ctx: 50024
            },
            "format": "json"
        };

        try {
            const response = await fetch('http://localhost:11434/api/chat', {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                console.error("Call ollama failed:", response.status, response.statusText);
                console.error(await response.text());
                process.exit(1);
            }

            const result = await response.json();
            genResult = result.message.content;
        } catch (error) {
            console.error("Error fetching ollama response:", error);
            throw error;
        }

} else if (GENERATION_ENGINE === 'deepseek') {
          const payload = {
              model: MODEL,
              messages: [
                  // {
                  //     role: "system",
                  //     content: context,
                  // },
                  {
                      role: "user",
                      content: question,
                  },
              ],
              response_format: {
                  type: "json_object"
              },
              "stream": false
              // max_tokens: 8192
          };

          console.log(payload);
          // fs.writeFileSync('payload.json', JSON.stringify(payload));

          const responseGpt = await fetch("https://api.deepseek.com/chat/completions", {
              method: "POST",
              headers: {
                  "Content-Type": "application/json",
                  "Authorization": `Bearer ${API_KEY}`,
              },
              body: JSON.stringify(payload),
          });
          console.log("Deepseek response:");
          if (!responseGpt.ok) {
              console.log("Call deepseek failed:", responseGpt.status, responseGpt.statusText);
              console.log(await responseGpt.text());
              process.exit(1);
          }
          console.log(responseGpt.status, responseGpt.statusText);

          try {
              const reponseBody = await responseGpt.text();
              console.log(reponseBody);
              if (!reponseBody.trim()) {
                  console.error("Empty response from deepseek");
                  process.exit(1);
              }
              const result = JSON.parse(reponseBody);
              genResult = result.choices[0].message.content;
          } catch (error) {
              console.error(error);
              process.exit(2);
          }
    } else {
        console.log("Invalid generation engine specified");
        process.exit(1);
    }


    console.log(genResult);
    // fs.writeFileSync('gen-res.json', genResult);

    const responsePatch = await fetch(URL + '/api/v1/tag-group', {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Basic ' + Buffer.from(USERNAME + ':' + PASSWORD).toString('base64')
        },
        body: JSON.stringify({
            "tags": JSON.parse(genResult)
        }),
    });

    if (responsePatch.status != 200 && responsePatch.status != 304) {
      console.error('Call to tag-group failed. ', responsePatch);
      process.exit(1);
    }

    console.log(responsePatch.status);


  } catch (error) {
    console.error(error);
    process.exit(1);
  }
