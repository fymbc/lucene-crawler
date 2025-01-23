import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;

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
    private final HashSet<String> keywords = new HashSet<>();

    public Reporter() {
        this.startTime = Instant.now();
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
                        if (keywords.add(keyword)) { // Add only unique keywords
                            currentKeywordCount++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating keywords: " + e.getMessage());
        }
        this.keywordsExtracted = keywords.size();
    }

    public void incrementDocsIndexed() {
        this.docsIndexed++;
    }

    public void logStatistics(Queue<String> urlQueue) {
        long elapsedTime = Duration.between(startTime, Instant.now()).toSeconds();
        double elapsedMinutes = elapsedTime / 60.0;

        double crawlSpeedPagesPerMinute = elapsedMinutes > 0 ? linksCrawled / elapsedMinutes : 0;
        double crawlSpeedKeywordsPerMinute = elapsedMinutes > 0 ? keywordsExtracted / elapsedMinutes : 0;
        double urlCrawledRatio = linksCrawled / (double) (linksCrawled + urlQueue.size());

        System.out.printf("Crawl Statistics:\n");
        System.out.printf("Total time elapsed: %d seconds\n", elapsedTime);
        System.out.printf("- Pages Crawled per Minute: %.2f\n", crawlSpeedPagesPerMinute);
        System.out.printf("- Keywords Extracted per Minute: %.2f\n", crawlSpeedKeywordsPerMinute);
        System.out.printf("- URLs Crawled/Queued Ratio: %.2f\n", urlCrawledRatio);
        System.out.printf("- Total Links Crawled: %d\n", linksCrawled);
        System.out.printf("- Total Keywords Extracted: %d\n", keywordsExtracted);
        System.out.printf("- Total Pages Indexed: %d\n", docsIndexed);
    }

    public void update(MMapDirectory directory) {
        incrementLinksCrawled();
        incrementDocsIndexed();
        updateKeywords(directory);
    }

}
