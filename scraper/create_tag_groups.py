
import sys
import json
import requests
from openai import OpenAI
import config
from loguru import logger

def create_tag_groups():
    # GET tag groups from /api/v1/tag-group/raw
    raw_url = f"{config.URL}/api/v1/tag-group/raw"
    response = requests.get(raw_url, auth=(config.USERNAME, config.PASSWORD))
    if response.status_code != 200:
        logger.error(f"An unexpected HTTP status code was returned from {raw_url}: {response.status_code}")
        sys.exit(1)

    tag_groups = response.json()  # Assuming this returns a list of tags
    context = "'" + "','".join(tag_groups) + "'"

    # Build the question with the context and fixed categories
    question = (
        f"Given the following tags: {context}, please group them into  the following categories "
        '{ "Technology": [ "Softwareentwicklung", "Algorithmen", "Datenanalyse", "IT-Sicherheit", '
        '"Künstliche Intelligenz", "Elektromobilität", "Technologie", "Innovation", "Cyberkriminalität" ], '
        '"Environment": [ "Klimaschutz", "Umwelt", "Nachhaltigkeit", "Energie" ], '
        '"Politics": [ "Politik", "Gesellschaft", "Demokratie", "Migration", "Konflikte", "Recht", "Sicherheit" ], '
        '"Economy": [ "Wirtschaft", "Verkehr", "Handel", "Infrastruktur" ], '
        '"Science": [ "Astronomie", "Forschung", "Weltraum", "Wissenschaft" ], '
        '"Culture": [ "Kunst", "Kultur", "Medien", "Bildung", "Geschichte", "Literatur", "Film", "Musik" ], '
        '"Health": [ "Gesundheit" ], '
        '"Sports": [ "Fußball", "Sport" ] }. '
        "Keep the categories and put all tags into an appropriate category. "
        "Return JSON with category name as attribute and for each category the top 15 tags grouped into it as a string array."
    )

    client = OpenAI(
        api_key=config.API_KEY,
    )
    response = client.chat.completions.create(
        model=config.MODEL,
        messages=[
            {
                "role": "user",
                "content": question,
            }
        ],
        stream=False,
        response_format={"type": "json_object"},
    )
    gen_result = response.choices[0].message.content

    logger.info(f"Generated tags: {gen_result}")

    # PATCH the /api/v1/tag-group endpoint with the processed tags
    patch_url = f"{config.URL}/api/v1/tag-group"
    try:
        tags_json = json.loads(gen_result)
    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse gen_result as JSON: {e}")
        sys.exit(1)

    patch_payload = {"tags": tags_json}
    r_patch = requests.patch(patch_url, auth=(config.USERNAME, config.PASSWORD), json=patch_payload)
    if r_patch.status_code not in (200, 304):
        logger.error(f"An unexpected HTTP status code was returned from {patch_url}: {r_patch.status_code}")
        sys.exit(1)

    logger.info(f"Patch response status: {r_patch.status_code}")

