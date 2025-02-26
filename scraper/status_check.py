
import time
import requests
import config
from loguru import logger

def wait_until_http_status():
    """Poll the health endpoint until HTTP 200 is received or max attempts are exceeded."""
    max_attempts = 10
    sleep_time = 5
    for attempt in range(max_attempts):
        try:
            response_x = requests.get(f"{config.URL}/actuator/health", auth=(config.USERNAME, config.PASSWORD))
            if response_x.status_code == 200:
                logger.info(f"HTTP status code 200 received after {attempt+1} attempts")
                return
        except Exception:
            pass
        logger.warning(f"Attempt {attempt+1}/{max_attempts}: Could not reach {config.URL}/actuator/health. Retrying in {sleep_time} seconds...")
        time.sleep(sleep_time)
    logger.error(f"Maximum attempts exceeded. Could not reach {config.URL}/actuator/health")
