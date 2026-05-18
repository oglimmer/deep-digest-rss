import os
import sys
import shutil
from loguru import logger

# Optionally use python-dotenv to load .env files
try:
    from dotenv import load_dotenv
except ImportError:
    load_dotenv = None


# Load environment variables from .env if NO_ENV_FILE is not set
if not os.environ.get("NO_ENV_FILE"):
    env_source = os.path.join("..", ".env")
    if os.path.exists(env_source):
        shutil.copy(env_source, ".env")
    if os.path.exists(".env") and load_dotenv:
        load_dotenv(".env")

# Retrieve required environment variables
try:
    URL = os.environ["URL"]
    MODEL = os.environ.get("MODEL")
    API_KEY = os.environ.get("API_KEY")
    GENERATION_ENGINE = os.environ.get("GENERATION_ENGINE")
except KeyError as e:
    logger.error(f"Missing environment variable: {e}")
    sys.exit(1)

# Auth: prefer X-API-Key when SCRAPER_API_KEY is set. Fall back to basic auth
# via USERNAME/PASSWORD only during the rollout window. Basic-auth support will
# be removed in a future release.
SCRAPER_API_KEY = os.environ.get("SCRAPER_API_KEY")
USERNAME = os.environ.get("USERNAME")
PASSWORD = os.environ.get("PASSWORD")

if SCRAPER_API_KEY:
    AUTH_HEADERS = {"X-API-Key": SCRAPER_API_KEY}
    AUTH_BASIC = None
elif USERNAME and PASSWORD:
    logger.warning(
        "Using deprecated basic auth (USERNAME/PASSWORD). Set SCRAPER_API_KEY to switch to X-API-Key auth."
    )
    AUTH_HEADERS = {}
    AUTH_BASIC = (USERNAME, PASSWORD)
else:
    logger.error("No auth configured. Set SCRAPER_API_KEY (preferred) or USERNAME+PASSWORD.")
    sys.exit(1)
