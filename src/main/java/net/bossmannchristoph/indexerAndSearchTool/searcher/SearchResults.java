package net.bossmannchristoph.indexerAndSearchTool.searcher;

import java.util.ArrayList;
import java.util.List;

import net.bossmannchristoph.indexerAndSearchTool.TwinWriter;

public class SearchResults {

	public SearchResults() {
		searchResults = new ArrayList<>();
	}
	private List<SearchResult> searchResults;
	private int totalResults;
	
	public List<SearchResult> getSearchResults() {
		return searchResults;
	}
	public void setSearchResults(List<SearchResult> searchResults) {
		this.searchResults = searchResults;
	}
	public int getTotalResults() {
		return totalResults;
	}
	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}
	
	public void prettyPrint(TwinWriter tw) {
		tw.println("Total results :: " + totalResults);
		int i = 0;
		for(SearchResult searchResult : searchResults) {
			++i;
			tw.print(i + ": ");
			searchResult.prettyPrint(tw);
			tw.print("\n");
		}
	}
	
}
