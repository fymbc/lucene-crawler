# Lucene Crawler
This is a toy web crawler that integrates with Apache Lucene for indexing, search and retrieval.

Designed as part of a project for GATech's CS4675.

## Usage
Download the executable JAR file under releases into an empty folder.

Ensure you have Java JDK 17 installed.

Syntax: `java -jar NAME_OF_FILE.jar <seedUrl> <maxLinks> <queryString>`

Replace NAME_OF_FILE with the name you saved the JAR file as.
* `seedUrl`: Base/seed URL to start crawl from. Ensure it is a fully qualified URL, including `https://`
* `maxLinks`: Number of pages to crawl. Recommended value between 500-2000.
* `queryString`: Search query to process after crawling is complete.