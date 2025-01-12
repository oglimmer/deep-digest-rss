#!/bin/bash

set -eu

# Check if the file url.txt exists
if [[ ! -f "url.txt" ]]; then
  echo "Error: url.txt file not found!"
  exit 1
fi

# Create done.txt if it doesn't exist
touch done.txt

# Read each line in url.txt
while IFS=' ' read -r id url title
do
  if ! $(grep -Fxq "$id" done.txt); then
    if [[ -n "$id" && -n "$url" ]]; then
      echo "********************************************************"
      echo "********************************************************"
      echo "********************************************************"

      echo "Fetching URL: $url with ID: $id"
      echo "********************************************************"
      # Use curl to fetch the URL
      curl -s "$url" > page.html

      # Convert HTML to text based on the operating system
      if [[ "$OSTYPE" == "darwin"* ]]; then
        textutil -convert txt page.html
      else
        html2text page.html > page.txt
      fi      

      cat page.txt | node main.js "$id" "$url" "$title"

      # Add the ID to done.txt
      echo "$id" >> done.txt
    fi
  fi
done < "url.txt"
