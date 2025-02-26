
import os
import sys
import time
import json
import requests
import traceback
from loguru import logger

# needed to import modules from the same directory
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# Instead of launching subprocesses, import modules directly.
import shrink
import shrink_stub
import generate_ai_summary
import push_to_db
import generate_ai_interest
import create_tag_groups
import fetch_atom
import status_check
import config
import signal_handler
import download_page


signal_handler.init()

status_check.wait_until_http_status()

def fetch_command_main():
    # Request the next feed item to process
    response = requests.get(f"{config.URL}/api/v1/feed-item-to-process/next",
                            auth=(config.USERNAME, config.PASSWORD))

    if response.status_code == 200:
        try:
            data = response.json()
        except json.JSONDecodeError:
            logger.error(f"Failed to decode JSON response from /api/v1/feed-item-to-process/next: {response.text}")
            return
        logger.info(f"Processing next feed item {data}")
        item_id = data.get("id")
        feed = data.get("feed", {})
        feed_id = feed.get("id")
        cookie = feed.get("cookie")
        item_url = data.get("url")
        process_next_feed_item(item_id, feed_id, item_url, cookie)
    elif response.status_code == 404:
        logger.info("No feed-items to process. Will look for new rss items")
        signal_handler.last_item_in_process = None
        fetch_new_rss_items_from_origin()
        has_next_response = requests.get(f"{config.URL}/api/v1/feed-item-to-process/has-next",
                                         auth=(config.USERNAME, config.PASSWORD))
        if has_next_response.text.strip() == "0":
            time.sleep(60)
    else:
        signal_handler.last_item_in_process = None
        logger.error(f"An unexpected HTTP status code was returned from /api/v1/feed-item-to-process/next: {response.status_code}")

def process_next_feed_item(item_id, feed_id, item_url, cookie):
    signal_handler.last_item_in_process = item_id

    start_time = time.time()

    page_content = download_page.download(feed_id, item_url, cookie)

    # shrink_output = shrink.process(page_content)
    shrink_output = shrink_stub.shrink_stub(page_content)
    if not shrink_output:
        raise Exception("shrink.process failed")

    summary_output = generate_ai_summary.generate_summary(shrink_output)
    summary_data = json.loads(summary_output)

    push_to_db.push_to_db(feed_id, item_id, summary_data)
    signal_handler.last_item_in_process = None

    generate_ai_interest.generate_interest(item_id)
    end_time = time.time()
    total_time = int(end_time - start_time)
    logger.info(f"Fetching, text generation and upload completed in {total_time} seconds.")

def fetch_new_rss_items_from_origin():
    feeds_response = requests.get(f"{config.URL}/api/v1/feed",
                                  auth=(config.USERNAME, config.PASSWORD))
    if feeds_response.status_code == 200:
        feeds = feeds_response.json()
        for item in feeds:
            fid = item.get("id")
            furl = item.get("url")
            fetch_atom.process_atom_feed(furl, fid)
    else:
        logger.error(f"An unexpected HTTP status code was returned from /api/v1/feed: {feeds_response.status_code}")


def main() -> int:
    # Determine command (default: "fetch")
    cmd = sys.argv[1] if len(sys.argv) > 1 else "fetch"
    if cmd == "fetch":
        while True:
            try:
                fetch_command_main()
            except Exception as e:
                logger.error(f"Error: {e}")
                traceback.print_exc()
                signal_handler.set_status()
                continue
    elif cmd == "taggroups":
        while True:
            # Directly call the createTagGroups module.
            create_tag_groups.create_tag_groups()
            time.sleep(600)

    return 0

if __name__ == '__main__':
    sys.exit(main())
