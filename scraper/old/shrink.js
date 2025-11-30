const MAX_SIZE = 100 * 1024; // 100 KB in bytes

process.stdin.setEncoding('utf8');

let data = '';

// Collect data from standard input
process.stdin.on('data', chunk => {
    data += chunk;
});

process.stdin.on('end', () => {
    const bufferSize = Buffer.byteLength(data, 'utf8');

    if (bufferSize < MAX_SIZE) {
        // If the total size is less than 100k, write it to stdout
        process.stdout.write(data);
    } else {

        const wordsToRemove = ['der', 'die', 'das', 'dem', 'den', 'des', 'dessen', 'deren'];
        // Create a regular expression that matches any of the words, with word boundaries
        const regex = new RegExp(`\\b(${wordsToRemove.join('|')})\\b`, 'gi');
        // Replace the words with an empty string
        const cleanedString = data.replace(regex, '').replace(/\s+/g, ' ').trim();

        const maxLength = MAX_SIZE;
        let output = '';
        if (cleanedString.length > maxLength) {
            output = cleanedString.slice(0, maxLength);
        } else {
            output = cleanedString;
        }

        process.stdout.write(output);
    }
});

process.stdin.on('error', (err) => {
    console.error('Error reading stdin:', err);
});

process.stdin.resume();
