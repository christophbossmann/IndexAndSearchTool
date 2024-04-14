package net.bossmannchristoph.indexerAndSearchTool.searcher;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SearchResults {

	public SearchResults() {
		searchResults = new ArrayList<>();
	}
	private List<SearchResult> searchResults;
	private String usedQuery;
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
	
	public void prettyPrint(PrintStream ps) {
		ps.println("Used query :: " + usedQuery);
		ps.println("Total results :: " + totalResults);
		int i = 0;
		for(SearchResult searchResult : searchResults) {
			++i;
			ps.print(i + ": ");
			searchResult.prettyPrint(ps);
			ps.print("\n");
		}
	}

	public String getUsedQuery() {
		return usedQuery;
	}

	public void setUsedQuery(String usedQuery) {
		this.usedQuery = usedQuery;
	}
}
