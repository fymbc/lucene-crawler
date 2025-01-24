import org.apache.lucene.store.MMapDirectory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

        if (args.length != 3) {
            System.out.println("Usage: java -jar WebCrawler.jar <seedUrl> <maxLinks> <queryString>");
            return;
        }

        String[] arguments = null;
        try {
            arguments = validate(args);
        } catch (IOException e) {
            System.out.println("Invalid parameters: " + e.getMessage());
            return;
        }

        String seedUrl = arguments[0];
        int maxLinks = Integer.parseInt(arguments[1]);
        String queryString = arguments[2];

        String dirPath = "./index";

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        try {
            File directory = new File(dirPath);
            File logs = new File("./logs");
            if (!directory.exists()) {
                System.out.println("Directory does not exist at: " + directory);
                boolean status = directory.mkdirs();
                if (!status) {
                    throw new IOException("Failed to create index directory at: " + dirPath);
                }
            }
            if (!logs.exists()) {
                System.out.println("Logs does not exist at: " + logs);
                boolean status = logs.mkdirs();
                if (!status) {
                    throw new IOException("Failed to create logs directory at: " + logs);
                }
            }

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

            Searcher searcher = new Searcher(indexDirectory);
            searcher.search(queryString);
            searcher.close();

            reporter.close();
            scheduler.shutdownNow();

        } catch (IOException e) {
            System.out.println("Error crawling.");
        }

    }

    public static String[] validate(String[] args) throws IOException {

        String url = args[0];
        String links = args[1];
        String queryString = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        try {
            new URL(url);
            int maxLinks = Integer.parseInt(links);
            if (maxLinks <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (MalformedURLException e) {
            System.out.println("Invalid seed URL");
            throw new IOException(e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid maxLinks");
            throw new IOException(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("maxLinks must be a positive integer.");
            throw new IOException(e.getMessage());
        }
        return new String[] { url, links , queryString };
    }
}
