package net.bossmannchristoph.indexerAndSearchTool.indexer;

import net.bossmannchristoph.indexerAndSearchTool.ExceptionHandler;
import net.bossmannchristoph.indexerAndSearchTool.TechnicalException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import net.bossmannchristoph.indexerAndSearchTool.Utilities;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.NullPrintStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.Tika;

public class LuceneIndexerWithTika {

	private static final String FILEMAP_FILE_NAME = "fileMap.dat";
	private static final String FILEMAP_FILE_NAME_2 = "fileMap2.dat";

	private static final int NOT_DELETE = 0;
	private static final int DELETE_DUE_TO_USER = 1;
	private static final int DELETE_DUE_TO_FILETYPE = 2;
	
	public static final int MESSAGE_CREATED = 0;
	public static final int MESSAGE_MODIFIED = 1;
	public static final int MESSAGE_NOT_CHANGED = 2;
	public static final int MESSAGE_TIME_LIMIT_REACHED = 3;
	public static final int MESSAGE_EMPTY_FILE_IGNORED = 4;
	public static final int MESSAGE_IGNORED_FILETYPE = 5;
	public static final int MESSAGE_DELETED_IGNORED_FILETYPE = 6;
	public static final int MESSAGE_DELETED_USER = 7;
	private static final Logger LOGGER = LogManager.getLogger(LuceneIndexerWithTika.class.getName());
	public static final Level INFO = Level.INFO;
	private Collection<String> indexedFileTypes;
	private Tika tika;
	private Path docDir;
	private Path indexLuceneDir;
	private Path indexDir;
	private IndexWriter writer;
	private Map<String, Long> fileMap;
	private List<String> existingFiles;
	//private TwinWriter out;
	private PrintStream fileOutputPrintStream;

	private ExecutorService executor;

	public LuceneIndexerWithTika() {
	}

	public static void main(String[] args) {
		// Input folder
		String docsPath = "C:\\Users\\chris\\dev\\IndexAndSearchTool\\documents\\input";
		// Output folder
		String indexPath = "C:\\Users\\chris\\dev\\IndexAndSearchTool\\documents\\index";
		// Output folder
		String outputPath = "C:\\Users\\chris\\dev\\IndexAndSearchTool\\documents\\output";

		String types = IndexerConsoleInterfaceHandler.defaultIndexFileTypes;

		singlePerform(docsPath, indexPath, outputPath, types);

	}

	public static void singlePerform(String docsPath, String indexPath, String outputPath, String indexedFileTypes) {
		ExceptionHandler exceptionHandler = new ExceptionHandler();

		try {
			LuceneIndexerWithTika luceneIndexerWithTika = new LuceneIndexerWithTika();
			luceneIndexerWithTika.prepareOutputAndLogging(outputPath);
			luceneIndexerWithTika.prepare(docsPath, indexPath, indexedFileTypes);
			luceneIndexerWithTika.indexDocs();
			luceneIndexerWithTika.close();
		} catch (Exception e) {
			exceptionHandler.handle(e);
		}

	}

	public void prepareOutputAndLogging(String outputPath) throws IOException {
		if(outputPath == null || outputPath.isEmpty()) {
			LOGGER.warn("No OutputPath provided, output to file is disabled!!");
			fileOutputPrintStream = new NullPrintStream();
			return;
		}
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
		File resultOutFile = Paths.get(outputPath, "indexer_" + timeStamp + ".out").toFile();
		resultOutFile.getParentFile().mkdirs();
		resultOutFile.createNewFile();
		 fileOutputPrintStream = new PrintStream(new FileOutputStream(resultOutFile), true, Charset.forName("UTF-8"));
	}

	@SuppressWarnings("unchecked")
	public void prepare(String docsPath, String indexPath, String indexedFileTypes)
			throws IOException, ClassNotFoundException {
		LOGGER.log(INFO, "preparing started!");
		tika = new Tika();
		tika.setMaxStringLength(-1);
		// Input Path Variable
		docDir = Paths.get(docsPath);
		indexDir = Paths.get(indexPath);
		this.indexedFileTypes = Arrays.asList(indexedFileTypes.split(","));
		indexLuceneDir = Paths.get(indexDir.toString(), "lucene");
		indexLuceneDir.toFile().mkdirs();
		Directory dir = FSDirectory.open(indexLuceneDir);

		// analyzer with the default stop words
		Analyzer analyzer = new StandardAnalyzer();

		// IndexWriter Configuration
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

		// IndexWriter writes new index files to the directory
		writer = new IndexWriter(dir, iwc);

		File fileMapFile = Paths.get(indexDir.toString(), FILEMAP_FILE_NAME).toFile();
		File f2 = Paths.get(indexDir.toString(), FILEMAP_FILE_NAME_2).toFile();

		if (f2.exists() && !fileMapFile.exists()) {
			f2.renameTo(fileMapFile);
		}

		if (!fileMapFile.exists()) {
			fileMap = new HashMap<>();
		} else {
			ObjectInputStream oi = new ObjectInputStream(
					Files.newInputStream(fileMapFile.toPath(), StandardOpenOption.READ));
			fileMap = (HashMap<String, Long>) oi.readObject();
		}
		LOGGER.log(INFO, "preparing finished!");
	}

	public void indexDocs() throws IOException {
		executor = Executors.newSingleThreadExecutor();
		LOGGER.log(INFO, "Indexing started ...");
		try {
			existingFiles = new LinkedList<>();
			if (Files.isDirectory(docDir)) {
				// Iterate directory
				Files.walkFileTree(docDir, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
						indexDoc(file, attrs.lastModifiedTime().toMillis());
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path path, IOException ioe) {
						try {
							return super.postVisitDirectory(path, ioe);
						} catch (FileSystemException fse) {
							LOGGER.log(Level.WARN, "FileSystemException at indexDocs, is directory, postVisitDirectory: "
									+ fse.getMessage(), fse);
							return FileVisitResult.SKIP_SUBTREE;
						} catch (IOException ioe2) {
							LOGGER.log(Level.WARN, "FileSystemException at indexDocs, is directory, postVisitDirectory: "
									+ ioe2.getMessage(), ioe2);
							return FileVisitResult.CONTINUE;
						}
					}
				});
			} else {
				// Index this file
				indexDoc(docDir, Files.getLastModifiedTime(docDir).toMillis());

			}
			LOGGER.log(INFO, "Indexing successfully finished, finalization processes ongoing...");
			deleteNotExistingFilesFromIndex();
			writeFileMapToFile();
			executor.shutdownNow();
			LOGGER.log(INFO, "Overall Indexing process successfully finished!");
		}
		catch(Exception e) {
			executor.shutdownNow();
			throw new TechnicalException(e);
		}
	}

	public void deleteIndex() throws IOException {
		writer.deleteAll();
	}

	public void close() throws IOException {
		writer.close();
	}

	private void writeFileMapToFile() throws IOException {
		LOGGER.log(Level.INFO, "writing file map to file started ...");
		File f = Paths.get(indexDir.toString(), FILEMAP_FILE_NAME).toFile();
		File f2 = Paths.get(indexDir.toString(), FILEMAP_FILE_NAME_2).toFile();
		if (f.exists()) {
			f.delete();
		}
		FileOutputStream fileOut;
		if (!f.createNewFile()) {
			f2.createNewFile();
			fileOut = new FileOutputStream(f2);
		} else {
			fileOut = new FileOutputStream(f);
		}
		ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
		objectOut.writeObject(fileMap);
		objectOut.close();
		LOGGER.log(Level.INFO, "writing file map to file finished!");
	}

	private void indexDoc(Path file, long lastModified) {
		String indexedFileString = Utilities.getDocPath(docDir, file.toFile()).toString().toLowerCase();
		IndexDocTask t = IndexDocTask.getTaskInstance(indexedFileTypes, fileOutputPrintStream, tika, fileMap,
				writer);
		t.init(file, lastModified, indexedFileString);
		Future<?> future = executor.submit(t);
		try {
			future.get(25, TimeUnit.SECONDS);
		} catch (TimeoutException te) {
			future.cancel(true);
			printIndexTypeMessage(MESSAGE_TIME_LIMIT_REACHED, indexedFileString,
					fileOutputPrintStream);
		}
		catch(ExecutionException | InterruptedException e) {
			throw new TechnicalException(e);
		}
		existingFiles.add(indexedFileString);
	}
	
	public static void printIndexTypeMessage(int type, String file, PrintStream out) {
		String typeString = "";
		switch(type) {
			case MESSAGE_CREATED:
				typeString = "CREATED -> INDEXING";
				break;
			case MESSAGE_MODIFIED:
				typeString = "UPDATED -> RE-INDEXING";
				break;
			case MESSAGE_NOT_CHANGED:
				typeString = "NOT CHANGED";
				break;
			case MESSAGE_EMPTY_FILE_IGNORED:
				typeString = "EMPTY FILE IGNORED";
				break;
			case MESSAGE_IGNORED_FILETYPE:
				typeString = "IGNORED FILETYPE";
				break;
			case MESSAGE_TIME_LIMIT_REACHED:
				typeString = "TIME LIMIT REACHED";
				break;
			case MESSAGE_DELETED_USER:
				typeString = "FILE DELETED BY USER, DELETED FROM INDEX";
				break;
			case MESSAGE_DELETED_IGNORED_FILETYPE:
				typeString = "IGNORED FILETYPE, DELETED FROM INDEX";
				break;
			default:
				throw new IllegalStateException("Not supported index type: " + type);			
		}
		String s = typeString + ": '" + file + "'";
		LOGGER.log(Level.INFO, s);
		out.println(s);
	}

	private void deleteNotExistingFilesFromIndex() throws IOException {
		LOGGER.log(Level.INFO,
				"Clearing index started: deletion of files from index started that do not exist anymore ...");
		// Map<String, Integer> toBeDeleted = new HashMap<>();
		// toBeDeleted.addAll(fileMap.keySet());
		Map<String, Integer> toBeDeleted = fileMap.keySet().stream()
				.collect(Collectors.toMap(v -> v, v -> DELETE_DUE_TO_USER));

		for (String file : existingFiles) {
			String fileExtension = FilenameUtils.getExtension(file);
			if (!indexedFileTypes.contains(fileExtension)) {
				toBeDeleted.replace(file, DELETE_DUE_TO_FILETYPE);
			} else {
				toBeDeleted.replace(file, NOT_DELETE);
			}
		}
		for (Map.Entry<String, Integer> e : toBeDeleted.entrySet()) {
			switch (e.getValue()) {
			case NOT_DELETE:
				break;
			case DELETE_DUE_TO_USER:
				writer.deleteDocuments(new Term("path", e.getKey()));
				fileMap.remove(e.getKey());
				printIndexTypeMessage(MESSAGE_DELETED_USER, e.getKey(), fileOutputPrintStream);
				break;
			case DELETE_DUE_TO_FILETYPE:
				writer.deleteDocuments(new Term("path", e.getKey()));
				fileMap.remove(e.getKey());
				printIndexTypeMessage(MESSAGE_DELETED_IGNORED_FILETYPE, e.getKey(), fileOutputPrintStream);
				break;
			default:
				throw new IllegalStateException("Type of delete not supported: " + e.getValue());
			}
		}
		LOGGER.log(Level.INFO, "Clearing index finished!");
	}

}
