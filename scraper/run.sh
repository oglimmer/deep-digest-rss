#!/bin/bash

set -eu

while true; do
  node fetch_atom.js

  if [[ -f "url.txt" ]]; then
    ./downloadAndGenerateAiSummary.sh
  fi

  sleep 60
done
