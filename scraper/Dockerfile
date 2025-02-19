FROM node:latest

RUN apt-get update && apt-get install -y jq virtualenv

WORKDIR /app

COPY . .

RUN virtualenv ./ && . ./bin/activate && pip3 install -r requirements.txt

RUN npm install

CMD ["./run.sh"]
