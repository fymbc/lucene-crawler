# Lucene Crawler
This is a toy web crawler that integrates with Apache Lucene for indexing, search and retrieval.

Designed as part of a project for GATech's CS4675.

## Usage
All user-adjustable parameters reside within ``Main.java``.
* `seedUrl`: Base/seed URL to start crawl from. Default URL is `https:/www.example.com`
* `maxLinks`: Number of pages to crawl. Default value is `10`
* `dirPath`: Location of indexed pages. Default location is `./index` (i.e. Upon startup, a directory will be created titled `index`)