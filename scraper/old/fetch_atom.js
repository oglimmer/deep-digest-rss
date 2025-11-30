import fetch from 'node-fetch';
import { Parser } from 'xml2js';
import dotenv from 'dotenv';

dotenv.config();
const { URL, USERNAME, PASSWORD } = process.env;

const ATOM_FEED_URL = process.argv[2]
const FEED_ID = process.argv[3]

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

function convertXmlToUnifiedDataStructure(parsedFeed, refIds, unifiedEntries) {
  if (parsedFeed.feed && parsedFeed.feed.entry) {
    const entries = parsedFeed.feed.entry;

    for (const entry of entries) {
      const id = entry.id[0];
      const link = entry.link[0].$.href;
      const title = entry.title[0]['_'];

      unifiedEntries[id] = { link, title };
      refIds.push(id);
    }
  } else if (parsedFeed.rss && parsedFeed.rss.channel) {
    for (const channel of parsedFeed.rss.channel) {
      const items = channel.item;

      for (const item of items) {
        const id = item.guid[0]['_'] ? item.guid[0]['_'] : item.guid[0];
        const link = item.link[0];
        const title = item.title[0];

        unifiedEntries[id] = { link, title };
        refIds.push(id);
      }
    }
  }
}

async function filterExistingEntries(refIds) {
  const response = await fetch(URL + '/api/v1/feed-item-to-process/filter', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Basic ' + Buffer.from(USERNAME + ':' + PASSWORD).toString('base64')
    },
    body: JSON.stringify({ refIds: refIds })
  });

  if (!response.ok) {
    console.log("Call filter failed:", response.status, response.statusText);
    console.log(await response.text());
    process.exit(1);
  }

  const responseData = await response.json();
  // console.log('POST request response:', responseData);
  return responseData;
}

async function postFeedItem(refId, entry) {
  const response = await fetch(URL + '/api/v1/feed-item-to-process', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Basic ' + Buffer.from(USERNAME + ':' + PASSWORD).toString('base64')
    },
    body: JSON.stringify({
      feedId: FEED_ID,
      refId: refId,
      url: entry.link,
      title: entry.title
    })
  });

  if (!response.ok) {
    console.log("Call create feed-item failed:", response.status, response.statusText);
    console.log(await response.text());
    process.exit(1);
  }

  // const responseData = await response.json();
  // console.log('POST request response:', responseData);
}


async function processAtomFeed() {
  try {
    const xml = await fetchAtomFeed(ATOM_FEED_URL);
    const parsedFeed = await parseAtomFeed(xml);

    const refIds = [];
    const unifiedEntries = {};
    convertXmlToUnifiedDataStructure(parsedFeed, refIds, unifiedEntries);

    const filteredRefIds = await filterExistingEntries(refIds);

    for (const refId of filteredRefIds) {
      await postFeedItem(refId, unifiedEntries[refId]);
    }

    const currentDateTime = new Date().toLocaleString();
    if (filteredRefIds.length > 0) {
      console.log('Done processing the Atom feed [%s] Added %d new entries. [%s]', ATOM_FEED_URL, filteredRefIds.length, currentDateTime);
    }
  } catch (err) {
    console.error(`Error: ${err.message}`);
  }
}

processAtomFeed();
