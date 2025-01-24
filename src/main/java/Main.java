import org.apache.lucene.store.MMapDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    private static final Logger crawlLogger = Logger.getLogger("crawlLogger");

    public static void main(String[] args) {
        String seedUrl = "https://example.com";
        int maxLinks = 20;
        String dirPath = "./index";
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        try {
            File directory = new File(dirPath);
            if (!directory.exists()) {
                System.out.println("Directory does not exist at: " + directory);
                boolean status = directory.mkdirs();
                if (!status) {
                    throw new IOException("Failed to create index directory at: " + dirPath);
                }
            }
            new File("/logs").mkdirs();

            FileHandler fileHandler = new FileHandler("logs/crawling.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            crawlLogger.addHandler(fileHandler);

            MMapDirectory indexDirectory = new MMapDirectory(Paths.get(dirPath));
            Indexer indexer = new Indexer(indexDirectory);
            Reporter reporter = new Reporter();
            WebCrawler crawler = new WebCrawler(indexer, maxLinks, reporter, indexDirectory, crawlLogger);

            scheduler.scheduleAtFixedRate(() -> {
                reporter.logRawStatistics(crawler.getUrlQueue());
            }, 0, 2, TimeUnit.SECONDS);

            crawler.crawl(seedUrl);
            indexer.close(); // close index before search

//            Searcher searcher = new Searcher(indexDirectory);
//            searcher.search("example");
//            searcher.close();

            reporter.close();
            scheduler.shutdownNow();

        } catch (IOException e) {
            System.out.println("Error crawling.");
        }

    }
}
