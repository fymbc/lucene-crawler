import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.MMapDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Indexer {

    private final IndexWriter indexWriter;

    public Indexer(String dirPath) throws IOException {
        MMapDirectory indexDirectory = new MMapDirectory(Paths.get(dirPath));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        this.indexWriter = new IndexWriter(indexDirectory, config);
    }
}
