#!/bin/bash

set -eu

while true; do

  # any RSS url not in fetch_atom.txt will be written to url.txt and directly added to fetch_atom.txt
  node fetch_atom.js

  if [[ -f "url.txt" ]]; then
    # Read each line in url.txt
    while IFS=' ' read -r id url title; do
      if ! $(grep -Fxq "$id" pushToDB.txt); then
        if [[ -n "$id" && -n "$url" ]]; then
          echo "********************************************************"

          echo "Fetching URL: $url with ID: $id"

          curl -s "$url" > page.html

          # Convert HTML to text based on the operating system
          if [[ "$OSTYPE" == "darwin"* ]]; then
            # textutil -convert txt page.html
            cat page.html | node html2txt.js | node shrink.js > page.txt
          else
            cat page.html | node html2txt.js | node shrink.js > page.txt
          fi

          cat page.txt | node generateAiSummary.js | node pushToDB.js "$id" "$url" "$title"

          echo "$id" >> pushToDB.txt
        fi
      fi
    done < "url.txt"
  fi

  sleep 60
done
