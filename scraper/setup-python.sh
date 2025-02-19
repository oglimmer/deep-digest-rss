#!/bin/bash

set -eu

virtualenv --python=/opt/homebrew/bin/python3.11 ./
source ./bin/activate

pip3 install -r requirements.txt


