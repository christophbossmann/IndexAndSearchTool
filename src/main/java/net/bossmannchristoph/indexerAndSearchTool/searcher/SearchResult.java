package net.bossmannchristoph.indexerAndSearchTool.searcher;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

import java.io.PrintStream;

public class SearchResult {
	public SearchResult(ScoreDoc doc, Document d) {
		this.scoreDoc = doc;
		this.document = d;
	}

	private ScoreDoc scoreDoc;
	private Document document;
	
	public ScoreDoc getScoreDoc() {
		return scoreDoc;
	}
	public void setScoreDoc(ScoreDoc scoreDoc) {
		this.scoreDoc = scoreDoc;
	}
	public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
	
	public void prettyPrint(PrintStream ps) {
		ps.print(document.get("path") + ", score: " + scoreDoc.score);
	}
	
	
}
