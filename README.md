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

## start

Have the DB ready

```bash
cd news-frontend
npm i
npm run dev

cd news-backend
./mvnw spring-boot:run

# the scraper needs will access the REST API, so wait for it to be started
# YOU HAVE TO CHANGE THE REST API ENDPOINT IN scraper/generateAiSummaryAndPushToDB.js !!!
cd scraper
# see readme.md in scraper dir for how to start
```

Access http://localhost:5173/ for the UI

http://localhost:8080 for the REST API (there are no OpenAPI docs yet)
