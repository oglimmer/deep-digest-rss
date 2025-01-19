#!/bin/bash

set -eu

source ./.env

while true; do

  responseBodyAndStatus=$(curl -s -w "%{http_code}" "$URL/api/v1/feed-item-to-process/next" \
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

      if [ "$cookie" = "null" ]; then
        cookie=""
      fi
      
      start_time=$(date +%s)
      echo "********************************************************"

      echo "Fetching URL: $url with refId: $refId"

      curl -s "$url" \
          -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:134.0) Gecko/20100101 Firefox/134.0' \
          -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' \
          --cookie "$cookie" > page.html

      # Convert HTML to text based on the operating system
      if [[ "$OSTYPE" == "darwin"* ]]; then
        # textutil -convert txt page.html
        cat page.html | node html2txt.js | node shrink.js > page.txt
      else
        cat page.html | node html2txt.js | node shrink.js > page.txt
      fi

      cat page.txt | node generateAiSummary.js | node pushToDB.js $feed_id $id

      end_time=$(date +%s)
      total_time=$((end_time - start_time))
      echo "Fetching, text generation and upload completed in $total_time seconds."
  elif [ "$responseBodyAndStatus" = "404" ]; then
      
      responseAllFeeds=$(curl -s "$URL/api/v1/feed" -H "Content-Type: application/json")
      # we need to remove the cookie from the response because it contains " and it will break the jq command when retrieving id/url
      echo "$responseAllFeeds" | jq -c 'del(.[].cookie) | .[]' | while read item; do
        id=$(echo "$item" | jq -r '.id')
        url=$(echo "$item" | jq -r '.url')
        node fetch_atom.js "$url" $id
      done

      echo "No more items to process. Sleeping for 60 seconds."
      sleep 60

  else
      echo "An unexpected HTTP status code was returned: $responseBodyAndStatus"
  fi
done
