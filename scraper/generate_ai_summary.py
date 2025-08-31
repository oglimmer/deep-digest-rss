from openai import OpenAI
import config
from ollama import Options, Client, ChatResponse
import sys
from loguru import logger
import tiktoken

def ollama(content):
    options = Options()
    options.num_ctx = 50024
    client = Client()
    response: ChatResponse = client.chat(
        model=config.MODEL,
        messages=[
            {
                "role": "system",
                "content": (
                    "You are a news article summarization assistant. Your task is to read the provided news article and produce a profound summary that covers all relevant aspects, including any problems, conclusions, or critical points discussed in the article. Please output your answer strictly as a valid JSON object with a key \"summary\", also create up to 3 tags from the list of available tags. For example:"
                    "{"
                      "\"summary\": \"Your comprehensive summary here.\""
                      "\"advertising\": \"Your assessment if this is an advertisement or not goes here as true or false\""
                      "\"tags\": [\"Softwareentwicklung\", \"Algorithmen\", \"Datenanalyse\", \"IT-Sicherheit\", \"Künstliche Intelligenz\", \"Elektromobilität\", \"Klimaschutz\", \"Migration\", \"Politik\", \"Gesellschaft\", \"Wirtschaft\", \"Fußball\", \"Sport\", \"Astronomie\", \"Forschung\", \"Gesundheit\", \"Cyberkriminalität\", \"Sicherheit\", \"Innovation\", \"Technologie\", \"Medien\", \"Kunst\", \"Kultur\", \"Bildung\", \"Geschichte\", \"Konflikte\", \"Umwelt\", \"Nachhaltigkeit\", \"Weltraum\", \"Infrastruktur\", \"Verkehr\", \"Recht\", \"Demokratie\", \"Handel\", \"Energie\", \"Musik\", \"Film\", \"Literatur\", \"Wissenschaft\"]"
                    "}"
                    "Ensure that no extra text or formatting is included outside of the JSON structure. Please provide your answer in the German language."
                )
            },
            {
                "role": "user",
                "content": (
                    "Please analyze the following news article and produce a profound summary covering all relevant aspects, problems, or conclusions."
                    "Text: " + content
                )
            }
        ],
        options=options,
        stream=False,
        format={
            "$schema": "http://json-schema.org/draft-07/schema#",
            "type": "object",
            "properties": {
                "summary": {"type": "string"},
                "tags": {
                    "type": "array",
                    "items": {"type": "string"}
                },
                "advertising": {"type": "boolean"}
            },
            "required": ["summary", "tags", "advertising"]
        },
    )
    return response.message.content

def shorten_string(s):
    cut_length = int(len(s) * 0.1)  # Calculate 10% of the string length
    return s[:-cut_length] if cut_length > 0 else s  # Remove from the end


def count_tokens(full_prompt, max_size = 118000):
    enc = tiktoken.encoding_for_model(config.MODEL)
    token_count = len(enc.encode(full_prompt))
    if token_count > max_size:
        logger.warning(f"Token count {token_count} exceeds {max_size}")
        return False
    logger.info(f"Token count {token_count}")
    return True

def chatgpt(content):
    while not count_tokens(content):
        content = shorten_string(content)
    client = OpenAI(
        api_key=config.API_KEY,
    )
    response = client.chat.completions.create(
        model=config.MODEL,
        messages=[
            {
                "role": "system",
                "content": (
                        "Erzeuge JSON, die antwort muss im attribut summary die eigentliche Zusammenfassung enthalten, "
                        "zusätzlich entählt das attribut advertising mit true oder false ob es sich um eine Werbung handelt und "
                        "das Attribut tags ist ein Arary von Strings mit Tags die du im Artikel identifiziert hast. Gültige Tags sind "
                        "'Softwareentwicklung', 'Algorithmen', 'Datenanalyse', 'IT-Sicherheit', 'Künstliche Intelligenz', "
                        "'Elektromobilität', 'Klimaschutz', 'Migration', 'Politik', 'Gesellschaft', 'Wirtschaft', 'Fußball', 'Sport', "
                        "'Astronomie', 'Forschung', 'Gesundheit', 'Cyberkriminalität', 'Sicherheit', 'Innovation', 'Technologie', "
                        "'Medien', 'Kunst', 'Kultur', 'Bildung', 'Geschichte', 'Konflikte', 'Umwelt', 'Nachhaltigkeit', 'Weltraum', "
                        "'Infrastruktur', 'Verkehr', 'Recht', 'Demokratie', 'Handel', 'Energie', 'Musik', 'Film', 'Literatur', "
                        "'Wissenschaft'. Dies ist der Inhalt einer vollständigen HTML-Seite, die deine Kenntnisse definiert: " + content
                )
            },
            {
                "role": "user",
                "content": (
                    "Fasse den Hauptartikel auf der Seite zusammen. Starte deine Antwort nicht mit der Artikel. "
                    "Kommentiere nur den Hauptartikel. Antworte journalistisch."
                )
            }
        ],
        stream=False,
        response_format={"type": "json_object"},
    )
    summary = response.choices[0].message.content
    return summary


def generate_summary(content):
    if config.GENERATION_ENGINE == "chatgpt":
        generate_summary_result = chatgpt(content)
    elif config.GENERATION_ENGINE == "ollama":
        generate_summary_result = ollama(content)
    else:
        raise Exception("Invalid generation engine specified")
    logger.info(f"Generated summary from {len(content)} to {len(generate_summary_result)}: {generate_summary_result}")
    return generate_summary_result

def main() -> int:
    html = sys.stdin.read()
    if not html:
        return 1

    print(generate_summary(html), flush=True)

    return 0

if __name__ == '__main__':
    sys.exit(main())
