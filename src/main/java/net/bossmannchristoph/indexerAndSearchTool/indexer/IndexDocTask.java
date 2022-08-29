package net.bossmannchristoph.indexerAndSearchTool.indexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.exception.ZeroByteFileException;

import net.bossmannchristoph.indexerAndSearchTool.TechnicalException;

public class IndexDocTask implements Runnable {

	public Collection<String> indexedFileTypes;
	public PrintStream out;
	public Tika tika;
	public Map<String, Long> fileMap;
	public IndexWriter writer;
	public List<String> existingFiles;

	public Path file;

	public static final Logger LOGGER = LogManager.getLogger(IndexDocTask.class.getName());

	private long lastModified;

	private IndexDocTask(Collection<String> indexedFileTypes, PrintStream out, Tika tika, Map<String, Long> fileMap,
			IndexWriter writer) {
		this.indexedFileTypes = indexedFileTypes;
		this.out = out;
		this.tika = tika;
		this.fileMap = fileMap;
		this.writer = writer;
	}

	public static IndexDocTask getTaskInstance(Collection<String> indexedFileTypes, PrintStream out, Tika tika,
			Map<String, Long> fileMap, IndexWriter writer) {
		return new IndexDocTask(indexedFileTypes, out, tika, fileMap, writer);
	}

	public void init(Path file, long lastModified) {
		this.file = file;
		this.lastModified = lastModified;
	}

	@Override
	public void run() {
		try {
			indexDoc();
		} catch (IOException ioe) {
			LOGGER.log(Level.WARN, "IOException at indexDocTask: " + ioe.getMessage(), ioe);
		} catch (TikaException te) {
			LOGGER.log(Level.WARN, "TikaException at indexDocTask: " + te.getMessage(), te);
		} catch (Exception e) {
			throw new TechnicalException(e);
		}
	}

	private void indexDoc() throws IOException, TikaException {
		
		Long lastModifiedInIndex = fileMap.get(file.toString());
		if (lastModifiedInIndex != null && lastModifiedInIndex >= lastModified) {
			LuceneIndexerWithTika.printIndexTypeMessage(LuceneIndexerWithTika.MESSAGE_NOT_CHANGED, file.toString(), out);
			return;
		}
		if (!indexedFileTypes.contains(FilenameUtils.getExtension(file.toString()))) {
			LuceneIndexerWithTika.printIndexTypeMessage(LuceneIndexerWithTika.MESSAGE_IGNORED_FILETYPE, file.toString(), out);
			return;
		}
		InputStream stream = Files.newInputStream(file);

		// Create lucene Document
		Document doc = new Document();

		try {
			// Updates a document by first deleting the document(s)
			// containing <code>term</code> and then adding the new
			// document. The delete and then add are atomic as seen
			// by a reader on the same index

			Long previousValue = fileMap.put(file.toString(), lastModified);
			if (previousValue == null) {
				LuceneIndexerWithTika.printIndexTypeMessage(LuceneIndexerWithTika.MESSAGE_CREATED, file.toString(), out);
			} else {
				LuceneIndexerWithTika.printIndexTypeMessage(LuceneIndexerWithTika.MESSAGE_MODIFIED, file.toString(), out);
			}

			String extractedContent = tika.parseToString(stream);
			doc.add(new StringField("path", file.toString(), Field.Store.YES));
			doc.add(new LongPoint("modified", lastModified));
			doc.add(new TextField("contents", extractedContent, Store.YES));

			writer.updateDocument(new Term("path", file.toString()), doc);

		} catch (ZeroByteFileException e) {
			LuceneIndexerWithTika.printIndexTypeMessage(LuceneIndexerWithTika.MESSAGE_EMPTY_FILE_IGNORED, file.toString(), out);
		}
	}

}
