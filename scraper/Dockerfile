FROM node:latest

RUN apt-get update && apt-get install -y jq

WORKDIR /app

COPY . .

RUN npm install

CMD ["./run.sh"]
