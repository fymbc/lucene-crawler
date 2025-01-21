import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String seedUrl = "https://example.com";
        int maxLinks = 10;

        // Initialize the Indexer and Crawler
        try (Indexer indexer = new Indexer("./index")) {
            WebCrawler crawler = new WebCrawler(indexer, maxLinks);
            crawler.crawl(seedUrl);
        } catch (IOException e) {
            System.out.println("Error crawling.");
        }
    }
}
