
import { JSDOM } from 'jsdom';

// https://github.com/jsdom/jsdom/issues/3236
console.error = (message, ...optionalParams) => {
    if (message.includes('Could not parse CSS stylesheet')) {
        return;
    }
    originalConsoleError(message, ...optionalParams);
};

let systemContent = '';
process.stdin.setEncoding('utf8');

process.stdin.on('data', function(chunk) {
  systemContent += chunk;
});

process.stdin.on('end', async () => {
    const dom = new JSDOM(systemContent);
    const document = dom.window.document;

    function extractTextNodes(node) {
        let textContent = "";

        if (node.nodeType === 3) { // Node.TEXT_NODE
            // If it's a text node, append its text content
            textContent += node.textContent.trim();
        } else if (node.nodeType === 1) { // Node.ELEMENT_NODE
            // If it's an element node and not a <style> or <script>, recursively check its children
            const tagName = node.tagName.toLowerCase();
            if (tagName !== 'style' && tagName !== 'script') {
                node.childNodes.forEach(childNode => {
                    textContent += extractTextNodes(childNode) + " ";
                });
                if (tagName === 'div' || tagName === 'p' || tagName === 'li') {
                    textContent += '\n';
                }
            }
        }

        return textContent.replace(/\n{2,}/g, '\n').replace(/ {2,}/g, ' ').replace(/^\s*[\r\n]/gm, '');
    }

    const lines = extractTextNodes(document.body).split('\n');
    const seenLines = {};
    const uniqueLines = lines.filter(line => !seenLines[line] && (seenLines[line] = true));

    console.log(uniqueLines.join('\n'));
});
