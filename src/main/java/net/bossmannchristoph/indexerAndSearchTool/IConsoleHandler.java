package net.bossmannchristoph.indexerAndSearchTool;

import java.util.Map;

import org.apache.commons.cli.Option;

public interface IConsoleHandler {

	public static final String MODE = "mode";
	
	Option[] getOptions();

	void execute(Map<String, String> args);
	
	String getMode();

}
