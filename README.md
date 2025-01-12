# a news overview generator using AI

## dev setup

run this for the database

```
docker run -d --name mariadb \
       -e MARIADB_ROOT_PASSWORD=root \
       -e MARIADB_DATABASE=news_prod \
       -e MARIADB_USER=news \
       -e MARIADB_PASSWORD=news \
       -p 3306:3306 mariadb:latest
```
