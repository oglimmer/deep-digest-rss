import sys
import requests
import config

def push_to_db(feed_id, item_id, parsed_content):
    payload = {
        'feedId': feed_id,
        'originalFeedItemId': item_id,
        'text': parsed_content.get('summary'),
        'tags': parsed_content.get('tags'),
        'advertising': parsed_content.get('advertising')
    }

    response = requests.post(
        f"{config.URL}/api/v1/news",
        json=payload,
        auth=(config.USERNAME, config.PASSWORD),
        headers={'Content-Type': 'application/json'}
    )
    try:
        response.raise_for_status()
    except requests.HTTPError:
        sys.stderr.write(f"Call to news-api failed. {response.status_code}: {response.text}\n")
        sys.exit(1)

