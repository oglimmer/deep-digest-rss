const express = require('express');
const cookieParser = require('cookie-parser');
const redis = require('redis');

const app = express();
app.use(cookieParser());

const redisConfigUrl = process.env.REDIS_URL || 'redis';

const client = redis.createClient({url: `redis://${redisConfigUrl}`});
client.on('error', err => console.log('Redis Client Error connecting to ' + redisConfigUrl, err));
client.connect();

const port = parseInt(process.env.SERVER_PORT || '3000');
const lifetimeInSecods = parseInt(process.env.LIFETIME || '7776000');

// Basic auth credentials (in real app, these should be in env vars or config)
const VALID_USERNAME = process.env.AUTH_USERNAME || 'guest';
const VALID_PASSWORD = process.env.AUTH_PASSWORD || 'guest';

// Helper function to parse basic auth header
const parseBasicAuth = (authHeader) => {
    if (!authHeader || !authHeader.startsWith('Basic ')) {
        return null;
    }
    
    const base64Credentials = authHeader.split(' ')[1];
    const credentials = Buffer.from(base64Credentials, 'base64').toString('utf8');
    const [username, password] = credentials.split(':');
    
    return { username, password };
};

app.get('/', async (req, res) => {
    // First check for valid cookie
    if (req.cookies && req.cookies.auth) {
        const reply = await client.get(req.cookies.auth);
        if (reply === 'exists') {
            res.status(200).send('200 OK');
            return;
        }
    }

    // If no valid cookie, check basic auth
    const authHeader = req.headers.authorization;
    const credentials = parseBasicAuth(authHeader);

    if (!credentials) {
        res.setHeader('WWW-Authenticate', 'Basic realm="Authentication Required"');
        res.status(401).send('Authentication required');
        return;
    }

    if (credentials.username === VALID_USERNAME && credentials.password === VALID_PASSWORD) {
        // Generate a new session token and store in Redis
        const sessionToken = Math.random().toString(36).substring(2);
        await client.set(sessionToken, 'exists', { EX: lifetimeInSecods });

        // Set the cookie
        res.cookie('auth', sessionToken, {
            httpOnly: true,
            secure: process.env.NODE_ENV === 'production',
            maxAge: lifetimeInSecods * 1000
        });

        res.status(200).send('200 OK');
    } else {
        res.setHeader('WWW-Authenticate', 'Basic realm="Authentication Required"');
        res.status(401).send('Invalid credentials');
    }
});

const serverAddress = process.env.SERVER_ADDRESS || 'localhost';

app.listen(port, serverAddress, () => {
    console.log(`Server is listening at http://${serverAddress}:${port}`);
});
