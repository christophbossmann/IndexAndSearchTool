package net.bossmannchristoph.indexerAndSearchTool.searcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import net.bossmannchristoph.indexerAndSearchTool.ExceptionHandler;
import net.bossmannchristoph.indexerAndSearchTool.TwinWriter;
import net.bossmannchristoph.indexerAndSearchTool.Utilities;

public class LuceneSearcher {

	private IndexSearcher searcher;
	private String indexDir;
	private TwinWriter out;
	
	public static void main(String[] args) {
				//Index path
				String indexPath = "C:\\Users\\Christoph\\Documents\\lucenetest\\index";
				// Output folder
				String outputPath = "C:\\Users\\Christoph\\Documents\\lucenetest\\output";
				//Search string
				String searchString = "die";
				int numberOfResults = 10;
				singlePerform(indexPath, outputPath, searchString, numberOfResults);
	}
	
	public static void singlePerform(String indexPath, String outputPath, String searchString, int numberOfResults) {
		ExceptionHandler exceptionHandler = new ExceptionHandler();
		try {
			LuceneSearcher luceneSearcher = new LuceneSearcher();
			luceneSearcher.prepareOutput(outputPath, "UTF-8");
			luceneSearcher.init(indexPath);
			SearchResults searchResults = luceneSearcher.search(searchString, numberOfResults);
			luceneSearcher.prettyPrint(searchResults);
			
		}
		catch(Exception e) {
			exceptionHandler.handle(e);
		}
		
	}
	public void init(String indexDir) throws IOException {
		this.indexDir = indexDir;
		// Create lucene searcher. It search over a single IndexReader.
		searcher = createSearcher();

	}
	
	public void prepareOutput(String outputPath, String outputCharset) throws IOException {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
		File resultOutFile = Paths.get(outputPath, "searcher_" + timeStamp + ".out").toFile();
		resultOutFile.getParentFile().mkdirs();
		resultOutFile.createNewFile();
		out = Utilities.getFileAndConsoleTwinWriter(resultOutFile, Charset.forName(outputCharset));
	}

	public SearchResults search(String searchString, int numberOfResults) throws IOException, ParseException {
		// Search indexed contents using search term
		TopDocs foundDocs = searchInContent(searchString, searcher, numberOfResults);
		// Total found documents
		SearchResults searchResults = new SearchResults();
		searchResults.setTotalResults(foundDocs.totalHits);
		
		// Let's print out the path of files which have searched term
		for (ScoreDoc sd : foundDocs.scoreDocs) {
			Document d = searcher.doc(sd.doc);
			searchResults.getSearchResults().add(new SearchResult(sd, d));
		}
		return searchResults;
	}

	private static TopDocs searchInContent(String textToFind, IndexSearcher searcher, int displayedNumberOfResults) throws IOException, ParseException {
		// Create search query
		QueryParser qp = new QueryParser("contents", new StandardAnalyzer());
		qp.setDefaultOperator(QueryParser.Operator.AND);
		Query query = qp.parse(textToFind);

		// search the index
		TopDocs hits = searcher.search(query, displayedNumberOfResults);
		return hits;
	}

	private IndexSearcher createSearcher() throws IOException {
		Directory dir = FSDirectory.open(Paths.get(indexDir, "lucene"));

		// It is an interface for accessing a point-in-time view of a lucene index
		IndexReader reader = DirectoryReader.open(dir);

		// Index searcher
		IndexSearcher searcher = new IndexSearcher(reader);
		return searcher;
	}
	
	public void prettyPrint(SearchResults searchResults)  {
		searchResults.prettyPrint(out);
	}

}
