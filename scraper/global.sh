#!/bin/bash

set -eu

while true; do
  node fet_atom.js

  if [[ -f "url.txt" ]]; then
    ./conv.sh
  fi

  # Sleep for a specified duration before the next iteration (e.g., 60 seconds)
  sleep 60
done
