# data extractor and AI generator

## requirements

Ollama needs to run on the host. Model `llama3.1:8b` needs to be available.

It needs 32 GB RAM available for Ollama.

## usage

```bash
npm i
./run.sh
```

## data files description

### pushed_to_db.txt

Created by `downloadAndGenerateAiSummary.sh`. Contains all ids which have been pushed to the DB. 

Log only.

### fetched_from_rss.txt

Created by `fetch_atom.js`. Contains all RSS ids which have been converted into a url.txt and thus should be processed again.

Critical state file.

### url.txt

Created by `fetch_atom.js`. Contains URLs to be processed by `downloadAndGenerateAiSummary.sh`. Will be removed after.

Temporary processing file.

### url.txt.*

Created by `fetch_atom.js`. Indicates that this url.txt has been processed.

Log only.

### page.html & page.txt

`page.html` is the downloaded file and `page.txt` the html page after going through a to text converter.

Temporary processing file.
