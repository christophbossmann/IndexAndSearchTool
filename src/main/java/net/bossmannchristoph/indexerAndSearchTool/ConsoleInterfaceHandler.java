package net.bossmannchristoph.indexerAndSearchTool;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.bossmannchristoph.indexerAndSearchTool.indexer.IndexerConsoleInterfaceHandler;
import net.bossmannchristoph.indexerAndSearchTool.searcher.SearcherConsoleInterfaceHandler;
import net.bossmannchristoph.other.ASCIIArtGenerator;

public class ConsoleInterfaceHandler implements IConsoleHandler {

	public static final Logger LOGGER = LogManager.getLogger(ConsoleInterfaceHandler.class.getName());

	public static final String MODE_CONSOLE = "console";
	public static final String INPUT_CHARSET = "inputcharset";
	public static final String OUTPUT_CHARSET = "outputcharset";

	public ExceptionHandler exceptionHandler;

	private String modeValue;
	private Map<String, IConsoleHandler> handlers;

	private Scanner sc;

	public static void main(String[] args) {
		new ConsoleInterfaceHandler().run(args);
	}
	
	public void run(String[] args) {

		try {
			LOGGER.log(Level.INFO, "Indexer and Search Tool:\n" + getWelcomeScreen() 
			+ "\nCreated by CHRISTOPH BOSSMANN\nUsing Apache Lucene and Apache Tika\n");
			exceptionHandler = new ExceptionHandler();
			
			LOGGER.log(Level.INFO,"defaultCharset: " + Charset.defaultCharset());
			
			handlers = initAndReturnHandlers();
			Map<String, String> resultingArgs = parseArguments(args);
			logArguments(resultingArgs);
			execute(resultingArgs);
		} catch (Exception e) {
			exceptionHandler.handle(e);
		}
	}

	@Override
	public void execute(Map<String, String> args) {
		exceptionHandler = new ExceptionHandler();
		try {
			initScanner(args.getOrDefault(INPUT_CHARSET, "UTF-8"));
			Map<String, IConsoleHandler> handlers = initAndReturnHandlers();
			askForParams(args);

			modeValue = args.get(MODE);
			if (modeValue == null) {
				LOGGER.error("No mode is provided to application. Use -m or --mode as parameter to provide the mode!");
			} else {
				IConsoleHandler handler = handlers.get(modeValue);
				if (handler == null) {
					LOGGER.error("Provided mode is invalid, possible modes: 'index', 'search'.");
				} else {
					handler.execute(args);
				}
			}
			sc.close();
		} catch (Exception e) {
			exceptionHandler.handle(e);
		}
	}

	public Map<String, String> parseArguments(String[] args) throws ParseException, FileNotFoundException {

		Map<String, String> resultingArgs = new HashMap<String, String>();

		Options options = new Options();
		for (Map.Entry<String, IConsoleHandler> e : handlers.entrySet()) {
			Option[] optionsArray = e.getValue().getOptions();
			for (int i = 0; i < optionsArray.length; ++i) {
				Option o = optionsArray[i];
				options.addOption(o);
			}
		}
		
		logHelp(options);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine line;
		line = parser.parse(options, args, true);

		for (Map.Entry<String, IConsoleHandler> e : handlers.entrySet()) {
			Option[] optionsArray = e.getValue().getOptions();
			for (int i = 0; i < optionsArray.length; ++i) {
				Option o = optionsArray[i];
				if (line.hasOption(o.getArgName())) {
					resultingArgs.put(o.getArgName(), line.getOptionValue(o.getArgName()));
				}
			}
		}
		return resultingArgs;
	}
	
	private void logArguments(Map<String, String> resultingArgs) {
		StringBuilder sb = new StringBuilder();
		sb.append("Resulting arguments:\n");
		for (Map.Entry<String, String> e : resultingArgs.entrySet())
		{
		  sb.append(e.getKey());
		  sb.append(": ");
		  sb.append(e.getValue());
		  sb.append("\n");
		}
		LOGGER.log(Level.INFO, sb.toString());
	}

	@Override
	public Option[] getOptions() {
		Option modeOpt = Option.builder("m").longOpt("mode").argName("mode").hasArg().required(false)
				.desc("mode of application, either search or index").build();
		Option inputCharsetOpt = Option.builder("ics").longOpt(INPUT_CHARSET).argName(INPUT_CHARSET).hasArg().required(false)
				.desc("set the input charset for inputs via console").build();
		Option outputCharsetOpt = Option.builder("ocs").longOpt(OUTPUT_CHARSET).argName(OUTPUT_CHARSET).hasArg().required(false)
				.desc("set the output charset for outputs to file").build();
		return new Option[] { modeOpt, inputCharsetOpt, outputCharsetOpt };
	}

	private void askForParams(Map<String, String> args) {
		if (args.get(MODE) == null) {
			System.out.println("Enter mode: ");
			args.put(MODE, sc.nextLine());
		}
	}

	private Map<String, IConsoleHandler> initAndReturnHandlers() {
		Map<String, IConsoleHandler> handlers = new HashedMap<>();
		handlers.put(ConsoleInterfaceHandler.MODE_CONSOLE, new ConsoleInterfaceHandler());
		handlers.put(SearcherConsoleInterfaceHandler.MODE_SEARCH, new SearcherConsoleInterfaceHandler(sc));
		handlers.put(IndexerConsoleInterfaceHandler.MODE_INDEX, new IndexerConsoleInterfaceHandler(sc));
		return handlers;
	}

	@Override
	public String getMode() {
		return MODE_CONSOLE;
	}
	
	private void initScanner(String charset) {
		if(charset == null) {
			sc = new Scanner(System.in);
		}
		else {
			sc = new Scanner(System.in, charset);
		}
	}
	
	public String getWelcomeScreen() {
		try {
			ASCIIArtGenerator asciiArtGenerator = new ASCIIArtGenerator();
			String s1 = asciiArtGenerator.printTextArt("Indexer and", 10);
			String s2 = asciiArtGenerator.printTextArt("Search Tool", 10);
			
			return s1 + "\n\n" + s2;
		}
		catch(Exception e) {
			throw new TechnicalException(e);
		}                                                            
	}
	
	private void logHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		pw.append("Help: \n");
		formatter.printHelp(pw, 80, "App", "", options, 0, 0, "");
		pw.flush();
		LOGGER.log(Level.INFO, new String(baos.toByteArray()));
	}

}
