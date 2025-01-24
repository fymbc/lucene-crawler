import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.MMapDirectory;

import java.io.IOException;

public class Indexer implements AutoCloseable {

    private final IndexWriter indexWriter;

    public Indexer(MMapDirectory directory) throws IOException {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        this.indexWriter = new IndexWriter(directory, config);
    }

    public void indexPage(String url, String title, String content) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("url", url, Field.Store.YES)); // url
        doc.add(new TextField("title", title != null ? title : "No Title", Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES)); // content
        this.indexWriter.addDocument(doc);
        this.indexWriter.commit();
    }

    public void close() throws IOException {
        this.indexWriter.close();
    }
}
