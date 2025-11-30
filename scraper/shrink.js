import { JSDOM } from 'jsdom';
import { Readability } from '@mozilla/readability';

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
    const doc = new JSDOM(systemContent);
    const reader = new Readability(doc.window.document);
    const article = reader.parse();
    console.log(article.content);
});
