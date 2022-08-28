package net.bossmannchristoph.indexerAndSearchTool;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExceptionHandler {

	public static Logger LOGGER = LogManager.getLogger(ExceptionHandler.class.getName());
	
	public void handle(Exception e) {
		getLogger().log(Level.FATAL, e.getMessage(), e);
	}
	
	public static Logger getLogger() {
		return LOGGER;
	}
}
