
from readability import Document
from lxml.html import fromstring
import sys
import re
from bs4 import BeautifulSoup, NavigableString, Tag


def limit_size(html):
    MAX_SIZE = 100 * 1024  # 100 KB in bytes
    buffer_size = len(html)
    if buffer_size < MAX_SIZE:
        return html
    else:
        words_to_remove = ['der', 'die', 'das', 'dem', 'den', 'des', 'dessen', 'deren']
        regex = re.compile(r'\b(' + '|'.join(words_to_remove) + r')\b', flags=re.IGNORECASE)
        cleaned_string = regex.sub('', html)
        cleaned_string = re.sub(r'\s+', ' ', cleaned_string).strip()
        if len(cleaned_string) > MAX_SIZE:
            output = cleaned_string[:MAX_SIZE]
        else:
            output = cleaned_string
        return output

def extract_text_nodes(node):
    text_content = ""
    if isinstance(node, NavigableString):
        # If it's a text node, append its stripped text
        stripped = str(node).strip()
        if stripped:
            text_content += stripped
    elif isinstance(node, Tag):
        tag_name = node.name.lower()
        # Skip <style> and <script> elements
        if tag_name not in ('style', 'script'):
            for child in node.children:
                text_content += extract_text_nodes(child) + " "
            # Add a newline for specific tags
            if tag_name in ('div', 'p', 'li'):
                text_content += "\n"
    return text_content

def html2txt(html):
    # Parse HTML using BeautifulSoup
    soup = BeautifulSoup(html, "html.parser")
    # If there is a <body>, process it; otherwise process the whole document
    root = soup.body if soup.body is not None else soup
    extracted_text = extract_text_nodes(root)
    # Clean up the text similarly to the JS regex replacements
    extracted_text = re.sub(r'\n{2,}', '\n', extracted_text)
    extracted_text = re.sub(r' {2,}', ' ', extracted_text)
    extracted_text = re.sub(r'^\s*[\r\n]', '', extracted_text, flags=re.MULTILINE)
    # Split into lines and remove duplicate lines while preserving order
    lines = extracted_text.split('\n')
    seen = set()
    unique_lines = []
    for line in lines:
        line = line.strip()
        if line and line not in seen:
            seen.add(line)
            unique_lines.append(line)
    # Output the result
    html = "\n".join(unique_lines)
    return html


def process(html):
    # arc90 readability - not working
    # doc = Document(html)
    # summary_html = doc.summary()
    # tree = fromstring(summary_html)
    # content_text = tree.text_content().strip()
    # return content_text
    html = html2txt(html)
    return limit_size(html)


def main() -> int:
    html = sys.stdin.read()
    if not html:
        return 1

    print(process(html), flush=True)

    return 0

if __name__ == '__main__':
    sys.exit(main())
