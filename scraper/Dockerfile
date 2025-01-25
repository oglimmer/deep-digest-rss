FROM node:latest

RUN apt-get update && apt-get install -y jq

WORKDIR /app

copy . .

RUN npm install

CMD ["./run.sh"]
