#!/bin/bash

set -eu

docker build --build-arg API_URL=https://api-news.oglimmer.com  --tag registry.oglimmer.com/news-frontend:latest news-frontend
docker build --tag registry.oglimmer.com/news-backend:latest news-backend
docker build --tag registry.oglimmer.com/news-scraper:latest scraper
docker build --tag registry.oglimmer.com/auth-module:latest auth

docker push registry.oglimmer.com/news-frontend:latest
docker push registry.oglimmer.com/news-backend:latest
docker push registry.oglimmer.com/news-scraper:latest
docker push registry.oglimmer.com/auth-module:latest
