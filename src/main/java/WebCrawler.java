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
}
