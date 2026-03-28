# deep-digest-rss - a news overview generator using AI pulling RSS

Copy .env.example to .env and add a ChatGPT API key. Then run `docker compose up --build`.

## setup

run this for the database

```bash
docker run -d --name mariadb \
       -e MARIADB_ROOT_PASSWORD=root \
       -e MARIADB_DATABASE=news_prod \
       -e MARIADB_USER=news \
       -e MARIADB_PASSWORD=news \
       -p 3306:3306 mariadb:latest
```
## requirements

The scraper needs a ChatGPT API key.

Check the configuration in scraper/.env.example

## usage

```bash
npm i
./run.sh
```


## start

Have the DB ready

```bash
cd news-frontend
npm i
npm run dev

cd news-backend
./mvnw spring-boot:run

cd scraper
```

Access http://localhost:5173/ for the UI

http://localhost:8080 for the REST API (there are no OpenAPI docs yet)
