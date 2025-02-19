
import os
import sys


import requests
import http.cookiejar

# needed to import modules from the same directory
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# Instead of launching subprocesses, import modules directly.
import config


def download(feed_id, item_url, cookie):

    # Normalize cookie value if necessary
    if cookie == "null" or cookie is None:
        cookie = ""

    print(f"Fetching URL: {item_url} for feed: {feed_id}", flush=True)
    # Prepare common headers
    headers = {
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:134.0) Gecko/20100101 Firefox/134.0',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'en-US,en;q=0.5',
        'Accept-Encoding': 'gzip, deflate, br, zstd',
        'Connection': 'keep-alive',
        'Upgrade-Insecure-Requests': '1',
        'Sec-Fetch-Dest': 'document',
        'Sec-Fetch-Mode': 'navigate',
        'Sec-Fetch-Site': 'none',
        'Sec-Fetch-User': '?1',
        'Priority': 'u=0, i',
        'Pragma': 'no-cache',
        'Cache-Control': 'no-cache',
        'TE': 'trailers'
    }

    if cookie.lower().startswith("# netscape http cookie file"):
        page_content = download_netscape_cookie(cookie, feed_id, headers, item_url)
    else:
        page_content = download_simple_cookie(cookie, headers, item_url)

    return page_content


def download_simple_cookie(cookie, headers, item_url):
    headers_with_cookie = headers.copy()
    if cookie:
        headers_with_cookie["Cookie"] = cookie
    r = requests.get(item_url, headers=headers_with_cookie)
    if r.status_code != 200:
        print(f"Failed to retrieve the page. Status code: {r.status_code}", flush=True)
        raise Exception(f"Failed to retrieve the page. Status code: {r.status_code}")
    page_content = r.text
    return page_content


def download_netscape_cookie(cookie, feed_id, headers, item_url):
    # Parse the Netscape cookie string into a cookie jar in memory.
    jar = http.cookiejar.MozillaCookieJar()
    for line in cookie.splitlines():
        if line and not line.startswith("#"):
            parts = line.split('\t')
            if len(parts) == 7:
                domain, flag, path, secure_val, expiration, name, value = parts
                secure = True if secure_val.upper() == "TRUE" else False
                expires = int(expiration) if expiration.isdigit() and int(expiration) != 0 else None
                jar.set_cookie(http.cookiejar.Cookie(
                    version=0, name=name, value=value,
                    port=None, port_specified=False,
                    domain=domain, domain_specified=bool(domain),
                    domain_initial_dot=domain.startswith('.'),
                    path=path, path_specified=True,
                    secure=secure,
                    expires=expires,
                    discard=False,
                    comment=None,
                    comment_url=None,
                    rest={'HttpOnly': "False"},
                    rfc2109=False
                ))
    session = requests.Session()
    session.cookies = jar
    r = session.get(item_url, headers=headers)
    page_content = r.text
    # Re-serialize the cookie jar into Netscape format in memory.
    new_cookie_lines = ["# Netscape HTTP Cookie File"]
    for ck in jar:
        secure_flag = "TRUE" if ck.secure else "FALSE"
        expires = str(int(ck.expires)) if ck.expires else "0"
        line = "\t".join([ck.domain, secure_flag, ck.path, secure_flag, expires, ck.name, ck.value])
        new_cookie_lines.append(line)
    new_cookie = "\n".join(new_cookie_lines)
    patch_url = f"{config.URL}/api/v1/feed/{feed_id}"
    requests.patch(
        patch_url,
        json={"cookie": new_cookie},
        auth=(config.USERNAME, config.PASSWORD)
    )
    return page_content

def main() -> int:
    url = sys.argv[1]
    feed_it = sys.argv[2] if len(sys.argv) > 2 else ""
    cookie = sys.argv[3] if len(sys.argv) > 3 else ""

    print(download(feed_it, url, cookie), flush=True)

    return 0

if __name__ == '__main__':
    sys.exit(main())
