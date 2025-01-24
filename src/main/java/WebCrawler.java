import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import org.apache.lucene.store.MMapDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class WebCrawler {
    private final int maxLinks;
    private final Indexer indexer;
    private final HashSet<String> visited = new HashSet<>();
    private final Queue<String> urlQueue = new LinkedList<>();
    private final Reporter reporter;
    private final MMapDirectory directory;
    private final Logger logger;

    public WebCrawler(Indexer indexer, int maxLinks, Reporter reporter, MMapDirectory directory, Logger logger) {
        this.maxLinks = maxLinks;
        this.indexer = indexer;
        this.reporter = reporter;
        this.directory = directory;
        this.logger = logger;
    }

    public void crawl(String seedUrl) {
        this.urlQueue.add(seedUrl);

        while (!this.urlQueue.isEmpty() && this.visited.size() < this.maxLinks) {
            String current = this.urlQueue.poll();
            this.logger.info("Crawling: " + current);
            if (this.visited.contains(current)) {
                this.logger.info("Skipped: " + current);
                this.reporter.incrementPagesSkipped();
                continue; // skip visited URL
            }

            this.visited.add(current);

            try {
                String pageHTML = fetchContent(current);
                if (pageHTML != null) {
                    Document doc = Jsoup.parse(pageHTML);
                    String title = doc.title();
                    String body = doc.body().text();
                    this.indexer.indexPage(current, title, body);

                    this.reporter.update(this.directory);

                    processLinks(current, pageHTML);
                    this.logger.info("Indexed: " + current);
                }
            } catch (IOException | ParseException e) {
                this.logger.warning("Failed to fetch/process URL: " + current);
            }
        }
        this.reporter.logFinalStatistics();
    }

    public String fetchContent(String url) throws IOException, ParseException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);

            try (var response = httpClient.execute(request)) {
                if (response.getCode() == 200) {
                    return EntityUtils.toString(response.getEntity());
                }
            }
        }
        return null;
    }

    private void processLinks(String baseUrl, String html) {
        Document document = Jsoup.parse(html, baseUrl);
        Elements links = document.select("a[href]");
        for (Element link : links) {
            String absUrl = link.absUrl("href");
            if (!this.visited.contains(absUrl)) {
                this.urlQueue.add(absUrl);
            }
        }
    }

    public Queue<String> getUrlQueue() {
        return this.urlQueue;
    }

}
