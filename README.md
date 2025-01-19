# a news overview generator using AI

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

The scraper needs either Ollama or a ChatGPT API key.

When using ollama you can choose betwee a high memory setup which needs at least 32 GB RAM or a low memory setup, which runs with 8 GB of RAM.

Check the configuration in scrapter/.env.example

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

# the scraper needs access the REST API, so wait for it to be started
cd scraper
# copy .env.exmaple to .env
```

Access http://localhost:5173/ for the UI

http://localhost:8080 for the REST API (there are no OpenAPI docs yet)
