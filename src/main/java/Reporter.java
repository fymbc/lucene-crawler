import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Queue;

public class Reporter {

    private final Instant startTime;

    private int linksCrawled = 0;
    private int keywordsExtracted = 0;
    private int docsIndexed = 0;
    private int pagesSkipped = 0;
    private final HashSet<String> keywords = new HashSet<>();
    private FileWriter csvWriter;

    public Reporter() {
        this.startTime = Instant.now();

        try {
            this.csvWriter = new FileWriter("logs/crawling_statistics.csv");
            csvWriter.append("Timestamp,Pages Crawled,Keywords Extracted,URLs Crawled,URLs Queued,Pages Indexed,Pages Skipped\n");
        } catch (IOException e) {
            System.out.println("Error initializing CSV file: " + e.getMessage());
        }
    }

    public void incrementLinksCrawled() {
        this.linksCrawled++;
    }

    public void updateKeywords(MMapDirectory directory) {
        int currentKeywordCount = 0;
        try {
            DirectoryReader reader = DirectoryReader.open(directory);
            for (LeafReaderContext leaf : reader.leaves()) {
                Terms terms = leaf.reader().terms("content");
                if (terms != null) {
                    TermsEnum termsEnum = terms.iterator();
                    BytesRef term;
                    while ((term = termsEnum.next()) != null) {
                        String keyword = term.utf8ToString();
                        if (this.keywords.add(keyword)) { // add only unique keywords
                            currentKeywordCount++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating keywords: " + e.getMessage());
        }
        this.keywordsExtracted = this.keywords.size();
    }

    public void incrementDocsIndexed() {
        this.docsIndexed++;
    }

    public void incrementPagesSkipped() {
        this.pagesSkipped++;
    }

    public void logRawStatistics(Queue<String> urlQueue) {
        long elapsedTime = Duration.between(this.startTime, Instant.now()).toSeconds();

        try {
            this.csvWriter.append(String.format(
                    "%d,%d,%d,%d,%d,%d,%d\n",
                    elapsedTime, this.linksCrawled, this.keywordsExtracted, this.linksCrawled,
                    urlQueue.size(), this.docsIndexed, this.pagesSkipped));
            this.csvWriter.flush();
        } catch (IOException e) {
            System.out.println("Error writing to CSV file: " + e.getMessage());
        }
    }

    public void logFinalStatistics() {
        System.out.print("Crawl Summary:\n");
        System.out.printf("- Total Links Crawled: %d\n", linksCrawled);
        System.out.printf("- Total Pages Indexed: %d\n", docsIndexed);
        System.out.printf("- Total Keywords Extracted: %d\n", keywordsExtracted);
    }

    public void close() {
        try {
            if (csvWriter != null) {
                csvWriter.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing CSV writer: " + e.getMessage());
        }
    }

    public void update(MMapDirectory directory) {
        incrementLinksCrawled();
        incrementDocsIndexed();
        updateKeywords(directory);
    }

}
