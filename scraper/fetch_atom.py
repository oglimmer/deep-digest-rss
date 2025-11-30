

import requests
import xmltodict
import config
from loguru import logger

def fetch_atom_feed(url):
    response = requests.get(url)
    if not response.ok:
        raise Exception(f"Failed to fetch the Atom feed: {response.reason}")
    return response.text

def parse_atom_feed(xml):
    try:
        return xmltodict.parse(xml)
    except Exception as e:
        raise Exception(f"Error parsing XML: {e}")

def normalize_entries(parsed_feed):
    """
    Process Atom or RSS feeds into a unified structure.
    Returns a list of reference IDs and a mapping of ref_id -> entry details.
    """
    ref_ids = []
    entries_dict = {}

    def add_entry(_entry, _entry_id):
        if not _entry_id:
            return
        ref_ids.append(_entry_id)
        # Process link field
        link_obj = _entry.get('link')
        if isinstance(link_obj, list):
            link = link_obj[0].get('@href')
        elif isinstance(link_obj, dict):
            link = link_obj.get('@href')
        else:
            link = link_obj

        # Process title field
        title = _entry.get('title', '')
        if isinstance(title, dict):
            title = title.get('#text', '')
        entries_dict[_entry_id] = {'link': link, 'title': title}

    if 'feed' in parsed_feed and 'entry' in parsed_feed['feed']:
        entries = parsed_feed['feed']['entry']
        if not isinstance(entries, list):
            entries = [entries]
        for entry in entries:
            add_entry(entry, entry.get('id'))
    elif 'rss' in parsed_feed and 'channel' in parsed_feed['rss']:
        channels = parsed_feed['rss']['channel']
        if not isinstance(channels, list):
            channels = [channels]
        for channel in channels:
            items = channel.get('item', [])
            if not isinstance(items, list):
                items = [items]
            for item in items:
                guid = item.get('guid')
                entry_id = guid.get('#text') if isinstance(guid, dict) else guid
                add_entry(item, entry_id)
    return ref_ids, entries_dict


def perform_request(method, url, payload):
    response = requests.request(method, url, auth=(config.USERNAME, config.PASSWORD), json=payload)
    if not response.ok:
        raise Exception(f"Request failed: {response.status_code} {response.reason}")
    return response.json()

def filter_existing_entries(ref_ids):
    api_url = f"{config.URL}/api/v1/feed-item-to-process/filter"
    return perform_request("post", api_url, {'refIds': ref_ids})

def post_feed_item(ref_id, entry, feed_id):
    api_url = f"{config.URL}/api/v1/feed-item-to-process"
    payload = {
        'feedId': feed_id,
        'refId': ref_id,
        'url': entry['link'],
        'title': entry['title']
    }
    perform_request("post", api_url, payload)

def process_atom_feed(atom_feed_url, feed_id):
    xml = fetch_atom_feed(atom_feed_url)
    parsed_feed = parse_atom_feed(xml)
    ref_ids, entries = normalize_entries(parsed_feed)
    new_entries = filter_existing_entries(ref_ids)
    for ref_id in new_entries:
        if ref_id in entries:
            post_feed_item(ref_id, entries[ref_id], feed_id)
    if new_entries:
        logger.info(f"Done processing the Atom feed [{atom_feed_url}]. Added {len(new_entries)} new entries.")
