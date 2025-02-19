import sys
import signal
import requests
import config

# Global variable for tracking the feed item being processed
last_item_in_process = None

def set_status():
    """Resets the processing status of the current feed item."""
    global last_item_in_process
    if last_item_in_process:
        try:
            requests.patch(
                f"{config.URL}/api/v1/feed-item-to-process/{last_item_in_process}",
                json={"processState": "NEW"},
                headers={"Content-Type": "application/json"},
                auth=(config.USERNAME, config.PASSWORD)
            )
            print(f"Reset process state for feed item {last_item_in_process}", flush=True)
        except Exception as e:
            print(f"Error in set_status: {e}", flush=True)
        last_item_in_process = None


def sigint_handler(signum, frame):
    set_status()
    sys.exit(1)


def init():
    signal.signal(signal.SIGINT, sigint_handler)
