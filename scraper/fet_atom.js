import fetch from 'node-fetch';
import { Parser } from 'xml2js';
import { promises as fs } from 'fs';
import fsBase from 'fs';
import path from 'path';

const ATOM_FEED_URL = 'https://www.heise.de/rss/heise-atom.xml'; // Replace with your Atom feed URL

async function fetchAtomFeed(url) {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Failed to fetch the Atom feed: ${response.statusText}`);
  }
  return response.text();
}

async function parseAtomFeed(xml) {
  const parser = new Parser();
  return new Promise((resolve, reject) => {
    parser.parseString(xml, (err, result) => {
      if (err) {
        reject(err);
      } else {
        resolve(result);
      }
    });
  });
}

async function getSavedIds(filePath) {
  try {
    const data = await fs.readFile(filePath, 'utf-8');
    return new Set(data.split('\n').filter(Boolean));
  } catch (err) {
    if (err.code === 'ENOENT') {
      return new Set(); // File doesn't exist, return empty set
    }
    throw err;
  }
}

async function saveIdAndUrl(id, url, title) {
  await fs.appendFile('ids.txt', `${id}\n`);
  await fs.appendFile('url.txt', `${id} ${url} ${title}\n`);
}

async function processAtomFeed() {
  try {
    const xml = await fetchAtomFeed(ATOM_FEED_URL);
    const parsedFeed = await parseAtomFeed(xml);

    const entries = parsedFeed.feed.entry || [];
    const savedIds = await getSavedIds(path.resolve('ids.txt'));

    let count = 0;

    for (const entry of entries) {
      const id = entry.id[0];
      const link = entry.link[0].$.href;
      const title = entry.title[0];

      if (!savedIds.has(id)) {
        await saveIdAndUrl(id, link, title['_']);
        count++;
      }
    }

    console.log('Done processing the Atom feed. Added %d new entries.', count);
  } catch (err) {
    console.error(`Error: ${err.message}`);
  }
}

function renameFileIfExists() {
  if (fsBase.existsSync('url.txt')) {
      let number = 1;
      let newFilePath;
      do {
          newFilePath = `url.txt.${number}`;
          number++;
      } while (fsBase.existsSync(newFilePath));
      fsBase.renameSync('url.txt', newFilePath);
      console.log(`Renamed 'url.txt' to '${newFilePath}'`);
  }
}

renameFileIfExists();
processAtomFeed();
