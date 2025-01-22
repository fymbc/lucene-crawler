import org.apache.lucene.store.MMapDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        String seedUrl = "https://example.com";
        int maxLinks = 10;
        String dirPath = "./index";

        try {
            File directory = new File(dirPath);
            if (!directory.exists()) {
                System.out.println("Directory does not exist at: " + directory);
                boolean status = directory.mkdirs();
                if (!status) {
                    throw new IOException("Failed to create index directory at: " + dirPath);
                }
            }

            MMapDirectory indexDirectory = new MMapDirectory(Paths.get(dirPath));
            Indexer indexer = new Indexer(indexDirectory);
            WebCrawler crawler = new WebCrawler(indexer, maxLinks);
            crawler.crawl(seedUrl);

            indexer.close();

            Searcher searcher = new Searcher(indexDirectory);
            searcher.search("example");

            searcher.close();

        } catch (IOException e) {
            System.out.println("Error crawling.");
        }

    }
}
