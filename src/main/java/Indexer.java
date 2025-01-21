import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.MMapDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class Indexer implements AutoCloseable {

    private final IndexWriter indexWriter;

    public Indexer(String dirPath) throws IOException {

        File directory = new File(dirPath);
        if (!directory.exists()) {
            System.out.println("Directory does not exist at: " + directory);
            boolean status = directory.mkdirs();
            if (!status) {
                throw new IOException("Failed to create index directory at: " + dirPath);
            }
        }

        System.out.println(directory.getAbsolutePath());
        MMapDirectory indexDirectory = new MMapDirectory(Paths.get(dirPath));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        this.indexWriter = new IndexWriter(indexDirectory, config);
    }

    public void indexPage(String url, String content) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("url", url, Field.Store.YES)); // url
        doc.add(new TextField("content", content, Field.Store.YES)); // content
        indexWriter.addDocument(doc);
        System.out.println("Indexed: " + url);
    }

    public void close() throws IOException {
        indexWriter.close();
    }
}
