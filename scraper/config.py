import os
import sys
import shutil

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
    USERNAME = os.environ["USERNAME"]
    PASSWORD = os.environ["PASSWORD"]
    MODEL = os.environ.get("MODEL")
    API_KEY = os.environ.get("API_KEY")
    GENERATION_ENGINE = os.environ.get("GENERATION_ENGINE")
except KeyError as e:
    print(f"Missing environment variable: {e}")
    sys.exit(1)
