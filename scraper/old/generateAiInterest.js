import fetch from 'node-fetch';
import dotenv from 'dotenv';

dotenv.config();
const { MODEL, API_KEY, GENERATION_ENGINE, USERNAME, PASSWORD, URL } = process.env;

/*
 * retrieve news from the server
 */
// {
//   id: 11664,
//   feedId: 7,
//   createdOn: '2025-02-16T10:48:13.410010Z',
//   url: 'xxx',
//   text: 'xxx',
//   title: 'xxx',
//   advertising: false,
//   tags: [ 'Gesellschaft', 'Konflikte', 'Sicherheit' ],
//   voted: false
// }
const retrieveNews = async (feedItemToProcessId) => {
  const responseFetchNews = await fetch(URL + `/api/v1/news/by-ref/${feedItemToProcessId}`, {
    method: 'GET',
    headers: {
      'Authorization': 'Basic ' + Buffer.from(USERNAME + ':' + PASSWORD).toString('base64')
    }
  });
  if (responseFetchNews.status != 200) {
    console.error('Call to news-api failed. ', responseFetchNews);
    process.exit(1);
  }
  const news = await responseFetchNews.json();
  return news;
};


/*
 * retrieve the relevant top news from the server
 */
// returns array of strings (headlines)
const retrieveRelevantTopHeadlines = async (userId) => {
  const responseFetchTopHeadlines = await fetch(URL + `/api/v1/user/${userId}/voted-news?hours=100&max=50`, {
    method: 'GET',
    headers: {
      'Authorization': 'Basic ' + Buffer.from(USERNAME + ':' + PASSWORD).toString('base64')
    }
  });
  if (responseFetchTopHeadlines.status != 200) {
    console.error('Call to news-api failed. ', responseFetchTopHeadlines);
    process.exit(1);
  }
  const topHeadlines = await responseFetchTopHeadlines.json();
  return topHeadlines;
};



/*
 * send the relevant flag to the server
 * 
 */
const pushRelevanceFlag = async (id) => {
  const responseFetchTopHeadlines = await fetch(URL + `/api/v1/news/${id}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Basic ' + Buffer.from(USERNAME + ':' + PASSWORD).toString('base64')
    },
    body: JSON.stringify({
      tagsToAdd: ['Interessant'],
    })
  });
  if (responseFetchTopHeadlines.status != 200) {
    console.error('Call to news-api failed. ', responseFetchTopHeadlines);
    process.exit(1);
  }
  const topHeadlines = await responseFetchTopHeadlines.json();
  return topHeadlines;
};


/*
 * ask the AI if this is relevant for the user or not
 */
const askAiRelevanceFlag = async (text, title, topHeadlines) => {
  const payload = {
    model: MODEL,
    messages: [
      {
        role: "system",
        content: "Erzeuge JSON, die antwort muss im attribut relevance mit einem float zwischen 0 und 1 stehen, wobei 0 für absolut nicht relevant und 1 für perfekt relevant steht. Hier die Liste der interessanten Nachrichten: " + topHeadlines.join(" "),
      },
      {
        role: "user",
        content: "Bewerte wie hoch die folgende Nachricht interessant ist " + title + ". " + text,
      },
    ],
    response_format: {
      type: "json_object"
    },
  };

  try {
    const response = await fetch("https://api.openai.com/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${API_KEY}`,
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      console.error("Call chatgpt failed:", response.status, response.statusText);
      console.error(await response.text());
      process.exit(1);
    }

    const result = await response.json();
    const summary = result.choices[0].message.content;

    return JSON.parse(summary);
  } catch (error) {
    console.error("Error fetching chatgpt response:", error);
    throw error;
  }
};

const feedItemToProcessId = process.argv[2];

if (feedItemToProcessId === 'get-news') {
  const news = await retrieveNews(process.argv[3]);
  console.log(news);
  process.exit(0);
}

if (feedItemToProcessId === 'dont-push') {
  const news = await retrieveNews(process.argv[3]);
  console.log(news.title);
  console.log(news.text);
  const topHeadlines = await retrieveRelevantTopHeadlines('1');
  console.log(topHeadlines);
  const relevanceFlag = await askAiRelevanceFlag(news.text, news.title, topHeadlines);
  console.log(relevanceFlag);
  process.exit(0);
}

const news = await retrieveNews(feedItemToProcessId);
console.log(">>>>>>>>Processing for intereset: ", news.id);
console.log(`------${news.title}------`);
console.log(news.text);
const topHeadlines = await retrieveRelevantTopHeadlines('1');
// console.log(topHeadlines);
const relevanceFlag = await askAiRelevanceFlag(news.text, news.title, topHeadlines);
console.log(`<<<<<<<<Detected relevance: ${relevanceFlag.relevance}`);
if (relevanceFlag.relevance > 0.5) {
  await pushRelevanceFlag(news.id);
}
