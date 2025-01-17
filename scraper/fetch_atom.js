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
  await fs.appendFile('fetch_atom.txt', `${id}\n`);
  await fs.appendFile('url.txt', `${id} ${url} ${title}\n`);
}

async function processAtomFeed() {
  try {
    const xml = await fetchAtomFeed(ATOM_FEED_URL);
    const parsedFeed = await parseAtomFeed(xml);

    const entries = parsedFeed.feed.entry || [];
    const savedIds = await getSavedIds(path.resolve('fetch_atom.txt'));

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

// Function to read file content and parse it into an array of IDs
function getIdsFromFile(filePath, columnIndex) {
  const fileContent = fsBase.readFileSync(filePath, 'utf-8');
  const lines = fileContent.split('\n');
  const ids = lines.map(line => {
      const columns = line.split(' ');
      return columns[columnIndex]?.trim();
  }).filter(Boolean); // Remove empty or undefined values
  return ids;
}

// Function to check if all IDs from one list exist in another
function checkIdsExist(sourceIds, targetIds) {
  return sourceIds.every(id => targetIds.includes(id));
}

if (fsBase.existsSync('url.txt')) {
  // Paths to the files
  const urlFilePath = 'url.txt';
  const pushToDBFilePath = 'pushToDB.txt';

  // Extract IDs from the files
  const urlIds = getIdsFromFile(urlFilePath, 0); // Extract from column 0 of url.txt
  const pushToDBIds = getIdsFromFile(pushToDBFilePath, 0); // Extract all lines from pushToDB.txt

  // Check if all IDs in url.txt are present in pushToDB.txt
  const allIdsExist = checkIdsExist(urlIds, pushToDBIds);

  if (allIdsExist) {
    renameFileIfExists();
    processAtomFeed();  
  } else {
    console.log('Some IDs from url.txt are missing in pushToDB.txt, thus not processed yet. Skipping to fetch more URLs.');
  }
} else {
  processAtomFeed();
}
