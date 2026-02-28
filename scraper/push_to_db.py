import time
import requests
import config
from loguru import logger

def push_to_db(feed_id, item_id, parsed_content, max_retries=3):
    payload = {
        'feedId': feed_id,
        'originalFeedItemId': item_id,
        'text': parsed_content.get('summary'),
        'tags': parsed_content.get('tags'),
        'advertising': parsed_content.get('advertising')
    }

    for attempt in range(1, max_retries + 1):
        response = requests.post(
            f"{config.URL}/api/v1/news",
            json=payload,
            auth=(config.USERNAME, config.PASSWORD),
            headers={'Content-Type': 'application/json'}
        )
        try:
            response.raise_for_status()
            return
        except requests.HTTPError:
            if response.status_code >= 500 and attempt < max_retries:
                wait = attempt * 10
                logger.warning(f"Call to news-api failed (attempt {attempt}/{max_retries}). {response.status_code}: {response.text}. Retrying in {wait}s...")
                time.sleep(wait)
            else:
                raise RuntimeError(f"Call to news-api failed. {response.status_code}: {response.text}")
