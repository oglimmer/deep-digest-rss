from openai import OpenAI
import config
import sys
from loguru import logger
import tiktoken

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
                        "Erzeuge JSON, die Antwort muss folgende Attribute enthalten: "
                        "\"summary\": die eigentliche Zusammenfassung. "
                        "\"advertising\": true oder false, ob es sich um eine Werbung handelt. "
                        "\"tags\": ein Array von Strings mit bis zu 3 Tags die du im Artikel identifiziert hast. "
                        "\"timely\": true wenn der Artikel ein aktuelles, kürzlich eingetretenes Ereignis behandelt, false wenn es sich um einen zeitlosen Hintergrundartikel, Meinungsbeitrag oder Ratgeber ohne aktuellen Bezug handelt. "
                        "\"impact_scope\": die geografische oder gesellschaftliche Reichweite der Auswirkungen, einer von: "
                        "\"global\" (weltweite Relevanz), \"international\" (mehrere Länder, Außenpolitik), \"europa\" (europäischer Rahmen), "
                        "\"deutschland\" (primär relevant für Deutschland), \"regional\" (eine bestimmte Region oder Stadt), \"branche\" (branchenspezifisch, geringe breitere Wirkung). "
                        "Gültige Tags sind "
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
        response_format={
            "type": "json_schema",
            "json_schema": {
                "name": "news_summary",
                "strict": True,
                "schema": {
                    "type": "object",
                    "properties": {
                        "summary": {"type": "string"},
                        "tags": {
                            "type": "array",
                            "items": {"type": "string"}
                        },
                        "advertising": {"type": "boolean"},
                        "timely": {"type": "boolean"},
                        "impact_scope": {
                            "type": "string",
                            "enum": ["global", "international", "europa", "deutschland", "regional", "branche"]
                        }
                    },
                    "required": ["summary", "tags", "advertising", "timely", "impact_scope"],
                    "additionalProperties": False
                }
            }
        },
    )
    summary = response.choices[0].message.content
    return summary


def generate_summary(content):
    if config.GENERATION_ENGINE == "chatgpt":
        generate_summary_result = chatgpt(content)
    else:
        raise Exception(f"Invalid generation engine specified: {config.GENERATION_ENGINE}")
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
