import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.MMapDirectory;

import java.io.IOException;

public class Searcher implements AutoCloseable {
    private QueryParser parser;
    private IndexSearcher searcher;
    private DirectoryReader reader;
    private MMapDirectory directory;

    public Searcher(MMapDirectory directory) {
        try {
            this.directory = directory;
            this.reader = DirectoryReader.open(directory);
            this.searcher = new IndexSearcher(reader);
            StandardAnalyzer analyzer = new StandardAnalyzer();
            this.parser = new QueryParser("content", analyzer);

        } catch (IOException e) {
            System.out.println("Failed to open directory: " + e.getMessage());
        }
    }

    public void search(String queryString) {
        try {
            Query query = this.parser.parse(queryString);
            TopDocs results = this.searcher.search(query, 10);

            System.out.println("Results returned: " + results.totalHits.value);

            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document document = this.searcher.doc(scoreDoc.doc);
                System.out.println("URL: " + document.get("url"));
                System.out.println("Title: " + document.get("title"));
                System.out.println(document.get("content").substring(0,60) + "...");
                System.out.println("Score: " + scoreDoc.score);
                System.out.println("--");
            }
        } catch (ParseException e) {
            System.out.println("Error parsing query");
        } catch (IOException e) {
            System.out.println("Error searching query");
        }
    }
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
        if (directory != null) {
            directory.close();
        }
    }
}
