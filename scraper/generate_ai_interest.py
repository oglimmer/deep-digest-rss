
import sys
import requests
import json
from openai import OpenAI
import config
from ollama import Options, Client, ChatResponse
from loguru import logger

def retrieve_news(feed_item_to_process_id):
    response = requests.get(f"{config.URL}/api/v1/news/by-ref/{feed_item_to_process_id}", auth=(config.USERNAME, config.PASSWORD))
    if response.status_code != 200:
        logger.error(f"Call to news-api failed. {response.status_code}: {response.text}")
        sys.exit(1)
    return response.json()


def retrieve_relevant_top_headlines(user_id):
    response = requests.get(f"{config.URL}/api/v1/user/{user_id}/voted-news?hours=100&max=50", auth=(config.USERNAME, config.PASSWORD))
    if response.status_code != 200:
        logger.error(f"Call to news-api failed. {response.status_code}: {response.text}")
        sys.exit(1)
    return response.json()


def push_relevance_flag(news_id):
    data = {"tagsToAdd": ["Interessant"]}
    response = requests.patch(f"{config.URL}/api/v1/news/{news_id}", auth=(config.USERNAME, config.PASSWORD), json=data)
    if response.status_code != 200:
        logger.error(f"Call to news-api failed. {response.status_code}: {response.text}")
        sys.exit(1)
    return response.json()

def ask_ai_relevance_flag(text, title, top_headlines):
    if config.GENERATION_ENGINE == "chatgpt":
        return ask_ai_relevance_flag_chatgpt(text, title, top_headlines)
    elif config.GENERATION_ENGINE == "ollama":
        return ask_ai_relevance_flag_ollama(text, title, top_headlines)
    else:
        raise Exception("Invalid generation engine specified")

def ask_ai_relevance_flag_ollama(text, title, top_headlines):
    options = Options()
    options.num_ctx = 50024
    client = Client()
    response: ChatResponse = client.chat(
        model=config.MODEL,
        messages=[
            {
                "role": "system",
                "content": (
                    "Erzeuge JSON, die antwort muss im attribut relevance mit einem float zwischen 0 und 1 stehen, "
                    "wobei 0 für absolut nicht relevant und 1 für perfekt relevant steht. Hier die Liste der interessanten Nachrichten: "
                    + " ".join(top_headlines)
                )
            },
            {
                "role": "user",
                "content": f"Bewerte wie hoch die folgende Nachricht interessant ist {title}. {text}"
            }
        ],
        options=options,
        stream=False,
        format={
            "$schema": "http://json-schema.org/draft-07/schema#",
            "type": "object",
            "properties": {
                "relevance": {"type": "number"}
            },
            "required": ["relevance"]
        },
    )
    return response.message.content

def ask_ai_relevance_flag_chatgpt(text, title, top_headlines):
    client = OpenAI(
        api_key=config.API_KEY,
    )
    response = client.chat.completions.create(
        model=config.MODEL,
        messages=[
            {
                "role": "system",
                "content": (
                    "Erzeuge JSON, die antwort muss im attribut relevance mit einem float zwischen 0 und 1 stehen, "
                    "wobei 0 für absolut nicht relevant und 1 für perfekt relevant steht. Hier die Liste der interessanten Nachrichten: "
                    + " ".join(top_headlines)
                )
            },
            {
                "role": "user",
                "content": f"Bewerte wie hoch die folgende Nachricht interessant ist {title}. {text}"
            }
        ],
        stream=False,
        response_format={"type": "json_object"},
    )
    summary = response.choices[0].message.content
    return summary


def generate_interest(feed_item_to_process_id):
    news = retrieve_news(feed_item_to_process_id)

    logger.info(f"Processing for interest: {news.get('id')}")
    logger.info(news.get('title'))
    logger.info(news.get("text"))

    top_headlines = retrieve_relevant_top_headlines("1")
    relevance_flag = ask_ai_relevance_flag(news.get("text"), news.get("title"), top_headlines)

    try:
        relevance_flag_json = json.loads(relevance_flag)
    except json.JSONDecodeError as e:
        logger.error("Failed to parse relevance_flag as JSON", e)
        sys.exit(1)

    logger.info(f"<<<<<<<<Detected relevance: {relevance_flag_json.get('relevance')}")

    if relevance_flag_json.get("relevance", 0) > 0.5:
        push_relevance_flag(news.get("id"))

