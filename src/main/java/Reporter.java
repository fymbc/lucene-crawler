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

    public int getLinksCrawled() {
        return this.linksCrawled;
    }

    public int getKeywordsExtracted() {
        return this.keywordsExtracted;
    }

    public int getDocsIndexed() {
        return this.docsIndexed;
    }

    public double crawlSpeedPerMinute() {
        long elapsedSeconds = Duration.between(this.startTime, Instant.now()).getSeconds();
        if (elapsedSeconds == 0) {
            return 0;
        }
        double minutes = elapsedSeconds / 60.0;
        return this.linksCrawled / minutes;
    }

    public double linksQueueRatio(Queue<String> queue) {
        return (double) this.linksCrawled / (this.linksCrawled + queue.size());
    }

    public long getElapsedTime() {
        return Duration.between(this.startTime, Instant.now()).getSeconds();
    }

    public HashSet<String> getKeywords() {
        return new HashSet<>(this.keywords); // Return a copy to preserve encapsulation
    }

    public void update(MMapDirectory directory) {
        incrementLinksCrawled();
        incrementDocsIndexed();
        updateKeywords(directory);
    }

}
