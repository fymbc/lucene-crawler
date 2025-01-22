import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class WebCrawler {
    private int maxLinks = 0;
    private final Indexer indexer;
    private final HashSet<String> visited = new HashSet<>();
    private final Queue<String> urlQueue = new LinkedList<>();

    public WebCrawler(Indexer indexer, int maxLinks) {
        this.maxLinks = maxLinks;
        this.indexer = indexer;
    }

    public void crawl(String seedUrl) {
        this.urlQueue.add(seedUrl);

        while (!this.urlQueue.isEmpty() && this.visited.size() < this.maxLinks) {
            String current = this.urlQueue.poll();
            if (this.visited.contains(current)) {
                continue; // skip visited URL
            }

            this.visited.add(current);
            System.out.println("Crawling: " + current);

            try {
                String pageHTML = fetchContent(current);
                if (pageHTML != null) {
                    Document doc = Jsoup.parse(pageHTML);
                    String title = doc.title();
                    String body = doc.body().text();
                    this.indexer.indexPage(current, title, body);
                    processLinks(current, pageHTML);
                }
            } catch (IOException | ParseException e) {
                System.err.println("Failed to fetch/process URL: " + current);
            }
        }
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

}
