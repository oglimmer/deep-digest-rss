#!/bin/bash

set -eu

# Check if the file url.txt exists
if [[ ! -f "url.txt" ]]; then
  echo "Error: url.txt file not found!"
  exit 1
fi

# Read each line in url.txt
while IFS=' ' read -r id url title
do
  if ! $(grep -Fxq "$id" pushed_to_db.txt); then
    if [[ -n "$id" && -n "$url" ]]; then
      echo "********************************************************"

      echo "Fetching URL: $url with ID: $id"

      curl -s "$url" > page.html

      # Convert HTML to text based on the operating system
      if [[ "$OSTYPE" == "darwin"* ]]; then
        textutil -convert txt page.html
      else
        html2text page.html > page.txt
      fi      

      cat page.txt | node generateAiSummaryAndPushToDB.js "$id" "$url" "$title"

      echo "$id" >> pushed_to_db.txt
    fi
  fi
done < "url.txt"
