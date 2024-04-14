package net.bossmannchristoph.indexerAndSearchTool.searcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.bossmannchristoph.indexerAndSearchTool.indexer.LuceneIndexerWithTika;
import org.apache.commons.io.output.NullPrintStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import net.bossmannchristoph.indexerAndSearchTool.ExceptionHandler;

public class LuceneSearcher {

	private IndexSearcher searcher;
	private String indexDir;
	private PrintStream fileOutputPrintStream;

	private static final Logger LOGGER = LogManager.getLogger(LuceneSearcher.class.getName());
	
	public static void main(String[] args) {
				//Index path
				String indexPath = "C:\\Users\\chris\\dev\\IndexAndSearchTool\\documents\\index";
				// Output folder
				String outputPath = "C:\\Users\\chris\\dev\\IndexAndSearchTool\\documents\\output";
				//Search string
				String searchString = "*";
				String searchPath = "a to h";
				int numberOfResults = 10;
				singlePerform(indexPath, outputPath, searchString, searchPath, numberOfResults);
	}
	
	public static void singlePerform(String indexPath, String outputPath, String searchString, String searchPath,
									 int numberOfResults) {
		ExceptionHandler exceptionHandler = new ExceptionHandler();
		try {
			LuceneSearcher luceneSearcher = new LuceneSearcher();
			luceneSearcher.prepareOutput(outputPath);
			luceneSearcher.init(indexPath);
			SearchResults searchResults = luceneSearcher.search(searchString, searchPath, numberOfResults);
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
	
	public void prepareOutput(String outputPath) throws IOException {
		if(outputPath == null || outputPath.isEmpty()) {
			LOGGER.warn("No OutputPath provided, output to file is disabled!!");
			fileOutputPrintStream = new NullPrintStream();
			return;
		}
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
		File resultOutFile = Paths.get(outputPath, "searcher_" + timeStamp + ".out").toFile();
		resultOutFile.getParentFile().mkdirs();
		resultOutFile.createNewFile();
		fileOutputPrintStream = new PrintStream(new FileOutputStream(resultOutFile), true, StandardCharsets.UTF_8);
	}

	public SearchResults search(String searchString, String searchPath, int numberOfResults) throws IOException, ParseException {
		// Search indexed contents using search term
		TopDocs foundDocs;
		Query query;
		QueryBuilder queryBuilder = new QueryBuilder();

		if(searchString.isEmpty() || searchString.equals("*")) {
			queryBuilder.addAllDocsQuery();
		}
		else {
			queryBuilder.addQuery(searchString);
		}

		if(searchPath != null && !searchPath.isEmpty()) {
			queryBuilder.addPathFilter(searchPath);
		}

		query = queryBuilder.build();
		foundDocs = searcher.search(query, numberOfResults);

		// Total found documents
		SearchResults searchResults = new SearchResults();
		searchResults.setTotalResults((int)foundDocs.totalHits.value);
		searchResults.setUsedQuery(query.toString());
		
		// Let's print out the path of files which have searched term
		for (ScoreDoc sd : foundDocs.scoreDocs) {
			Document d = searcher.doc(sd.doc);
			searchResults.getSearchResults().add(new SearchResult(sd, d));
		}
		return searchResults;
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
		searchResults.prettyPrint(fileOutputPrintStream);
		searchResults.prettyPrint(System.out);
	}

}
