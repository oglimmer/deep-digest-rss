
import time
import requests
import config

def wait_until_http_status():
    """Poll the health endpoint until HTTP 200 is received or max attempts are exceeded."""
    max_attempts = 10
    sleep_time = 5
    for attempt in range(max_attempts):
        try:
            response_x = requests.get(f"{config.URL}/actuator/health", auth=(config.USERNAME, config.PASSWORD))
            if response_x.status_code == 200:
                print(f"HTTP status code 200 received after {attempt+1} attempts", flush=True)
                return
        except Exception:
            pass
        print(f"Attempt {attempt+1}/{max_attempts}: Could not reach {config.URL}/actuator/health. Retrying in {sleep_time} seconds...", flush=True)
        time.sleep(sleep_time)
    print(f"Maximum attempts exceeded. Could not reach {config.URL}/actuator/health", flush=True)
