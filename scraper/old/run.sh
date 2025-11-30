#!/bin/bash

set -eu

cd "$(dirname "$0")"

if [ -z "${NO_ENV_FILE:-}" ]; then
  if [ -f ../.env ]; then
    cp ../.env ./.env
  fi
  if [ -f .env ]; then
    source ./.env
  fi
fi

## fetch only
lastItemInProcess=
setStatus() {
  if [ -n "$lastItemInProcess" ]; then
    curl -s "${URL}/api/v1/feed-item-to-process/$lastItemInProcess" \
      -H "Content-Type: application/json" \
      -u "$USERNAME:$PASSWORD" -d '{"processState": "NEW"}' -X PATCH
    lastItemInProcess=
  fi
}
## fetch end

wait_until_http_status() {
  local max_attempts=10
  local sleep_time=5

  for ((attempt=0; attempt<max_attempts; attempt++))
  do
    if curl -s -w "%{http_code}" \
      -u "$USERNAME:$PASSWORD" \
      "${URL}/actuator/health" | grep 200 > /dev/null
    then
      echo "HTTP status code 200 received after $(($attempt+1)) attempts"
      return
    fi

    echo "Attempt $attempt/$max_attempts: Could not reach "${URL}/actuator/health". Retrying in $sleep_time seconds..."
    sleep $sleep_time # Wait before checking again
  done

  echo "Maximum attempts exceeded. Could not reach "${URL}/actuator/health""
}
wait_until_http_status

CMD=${1:-fetch}

if [ "$CMD" = "fetch" ]; then

  trap 'setStatus' SIGINT
  trap 'setStatus' ERR

  while true; do

    responseBodyAndStatus=$(curl -s -w "%{http_code}" "${URL}/api/v1/feed-item-to-process/next" \
      -H "Content-Type: application/json" \
      -u "$USERNAME:$PASSWORD")

    if [[ $responseBodyAndStatus == *200 ]]; then
        body=$(echo "$responseBodyAndStatus" | sed 's/...$//')

        id=$(echo "$body" | jq -r '.id')
        refId=$(echo "$body" | jq -r '.refId')
        url=$(echo "$body" | jq -r '.url')
        title=$(echo "$body" | jq -r '.title')
        feed_id=$(echo "$body" | jq -r '.feed.id')
        cookie=$(echo "$body" | jq -r '.feed.cookie')
        lastItemInProcess=$id

        trap 'setStatus' RETURN

        if [ "$cookie" = "null" ]; then
          cookie=""
        fi

        start_time=$(date +%s)
        # echo "********************************************************"

        echo "Fetching URL: $url for feed: $feed_id"

        if echo "$cookie" | tr '[:upper:]' '[:lower:]' | grep -q "^# netscape http cookie file"; then
            echo "$cookie" > cookies.txt
            curl -L -s "$url" \
              --compressed -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:134.0) Gecko/20100101 Firefox/134.0' \
              -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' -H 'Accept-Language: en-US,en;q=0.5' \
              -H 'Accept-Encoding: gzip, deflate, br, zstd' -H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1' \
              -H 'Sec-Fetch-Dest: document' -H 'Sec-Fetch-Mode: navigate' -H 'Sec-Fetch-Site: none' -H 'Sec-Fetch-User: ?1' \
              -H 'Priority: u=0, i' -H 'Pragma: no-cache' -H 'Cache-Control: no-cache' -H 'TE: trailers' \
              --cookie "cookies.txt" --cookie-jar "cookies-saved.txt" -o page.html

            cookie=$(cat cookies-saved.txt)
            json_data=$(jq -n --arg cookie "$cookie" '{"cookie": $cookie}')
            curl -s "${URL}/api/v1/feed/$feed_id" \
              -H "Content-Type: application/json" \
              -u "$USERNAME:$PASSWORD" -d "$json_data" \
              -X PATCH

        else
            curl -s "$url" \
              -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:134.0) Gecko/20100101 Firefox/134.0' \
              -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' \
              --cookie "$cookie" > page.html
        fi

        # Convert HTML to text based on the operating system
        if [[ "$OSTYPE" == "darwin"* ]]; then
          # textutil -convert txt page.html
          cat page.html | node html2txt.js | node shrink.js > page.txt
        else
          cat page.html | node html2txt.js | node shrink.js > page.txt
        fi

        cat page.txt | node generateAiSummary.js | node pushToDB.js $feed_id $id

        lastItemInProcess=

        node generateAiInterest.js $id

        end_time=$(date +%s)
        total_time=$((end_time - start_time))
        echo "Fetching, text generation and upload completed in $total_time seconds."
    elif [ "$responseBodyAndStatus" = "404" ]; then
      lastItemInProcess=

      responseAllFeeds=$(curl -s "${URL}/api/v1/feed" -H "Content-Type: application/json" -u "$USERNAME:$PASSWORD")
      # we need to remove the cookie from the response because it contains " and it will break the jq command when retrieving id/url
      echo "$responseAllFeeds" | jq -c 'del(.[].cookie) | .[]' | while read item; do
        id=$(echo "$item" | jq -r '.id')
        url=$(echo "$item" | jq -r '.url')
        node fetch_atom.js "$url" $id
      done

      hasNextItem=$(curl -s "${URL}/api/v1/feed-item-to-process/has-next" \
        -u "$USERNAME:$PASSWORD")

      if [ "$hasNextItem" = "0" ]; then
        # echo "No more items to process. Sleeping for 60 seconds."
        sleep 60
      fi
    else
      lastItemInProcess=
      echo "An unexpected HTTP status code was returned: $responseBodyAndStatus"
    fi
  done

elif [ "$CMD" = "taggroups" ]; then

  while true; do
    node createTagGroups.js
    sleep 600
  done

fi
