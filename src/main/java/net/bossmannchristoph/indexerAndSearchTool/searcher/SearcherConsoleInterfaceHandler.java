package net.bossmannchristoph.indexerAndSearchTool.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.cli.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;

import net.bossmannchristoph.indexerAndSearchTool.ConsoleInterfaceHandler;
import net.bossmannchristoph.indexerAndSearchTool.IConsoleHandler;
import net.bossmannchristoph.indexerAndSearchTool.TechnicalException;

public class SearcherConsoleInterfaceHandler implements IConsoleHandler {


	public static Logger LOGGER = LogManager.getLogger(SearcherConsoleInterfaceHandler.class.getName());
	
	public static final String QUERY = "query";
	public static final String NUMBER_OF_RESULTS = "numberofresults";
	public static final String INDEXPATH = "indexpath";
	public static final String OUTPATH = "outpath";
	public static final String SEARCHPATH = "searchpath";
	public static final String MODE_SEARCH = "search";
	
	private LuceneSearcher luceneSearcher;
	private Scanner sc;
	
	public SearcherConsoleInterfaceHandler(Scanner sc) {
		luceneSearcher = new LuceneSearcher();
		this.sc = sc;
	}
	
	@Override
	public void execute(Map<String, String> args) {
		try {
			askForParams(args);
	    	luceneSearcher.prepareOutput(args.get(OUTPATH));
	    	luceneSearcher.init(args.get(INDEXPATH));
	    	SearchResults searchResults = luceneSearcher.search(args.get(QUERY), args.get(SEARCHPATH), Integer.valueOf(args.get(NUMBER_OF_RESULTS)));
	    	luceneSearcher.prettyPrint(searchResults);
		}
		catch(IOException | NumberFormatException | ParseException e) {
			throw new TechnicalException(e);
		}
		
	}
	
	private void askForParams(Map<String, String> args) {
		List<String> askedParams = new ArrayList<>();
		if(args.get(QUERY) == null) {			
			System.out.println("Enter search query: ");
			String param = sc.nextLine();
			args.put(QUERY, param);
			askedParams.add(QUERY + ": " + param);
		}
		if(args.get(SEARCHPATH) == null) {
			System.out.println("Enter search path (or leave empty): ");
			String param = sc.nextLine();
			args.put(SEARCHPATH, param);
			askedParams.add(SEARCHPATH + ": " + param);
		}
		if(args.get(NUMBER_OF_RESULTS) == null) {			
			System.out.println("Enter number of results: ");
			String param = sc.nextLine();
			args.put(NUMBER_OF_RESULTS, param);
			askedParams.add(NUMBER_OF_RESULTS + ": " + param);
		}
		if(args.get(INDEXPATH) == null) {
			System.out.println("Enter index path: ");
			String param = sc.nextLine();
			args.put(INDEXPATH, param);
			askedParams.add(INDEXPATH + ": " + param);
		}
		if(args.get(OUTPATH) == null) {
			System.out.println("Enter out path (or leave empty): ");
			String param = sc.nextLine();
			args.put(OUTPATH, param);
			askedParams.add(OUTPATH + ": " + param);
		}
		
		System.out.print("Asked params: ");
		for(String s : askedParams) {
			System.out.print(s + ", ");
		}
		System.out.println("");
	}
	
	@Override
	public Option[] getOptions() {
        Option numberOfResultsOpt = Option.builder("nor").longOpt("numberofresults").argName("numberofresults").hasArg().required(false)
				.desc("mode of application, either search or index").build();
        Option queryOpt = Option.builder("q").longOpt("query").argName("query").hasArg().required(false)
				.desc("path of docs to be indexed").build();
		Option searchpathOpt = Option.builder("sp").longOpt("searchpath").argName("searchpath").hasArg().required(false)
				.desc("path of docs to be indexed").build();
        Option indOpt = Option.builder("ind").longOpt("indexpath").argName("indexpath").hasArg().required(false)
				.desc("path where index of indexed docs shall be stored").build();
        Option outOpt = Option.builder("out").longOpt("outpath").argName("outpath").hasArg().required(false)
				.desc("path where the result of the index process shall be stored").build();
        queryOpt.setRequired(false);
		searchpathOpt.setRequired(false);
        indOpt.setRequired(false);
        outOpt.setRequired(false);
        numberOfResultsOpt.setRequired(false);
		return new Option[] {queryOpt, searchpathOpt, indOpt, outOpt, numberOfResultsOpt};
	}

	@Override
	public String getMode() {
		return MODE_SEARCH;
	}
}
