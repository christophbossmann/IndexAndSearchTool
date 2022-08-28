package net.bossmannchristoph.indexerAndSearchTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.io.output.TeeOutputStream;

public class Utilities {

	@Deprecated
	public static PrintStream getDualPrintStream(File f) throws FileNotFoundException, UnsupportedEncodingException {
	    FileOutputStream fileOutputStream = new FileOutputStream(f);
	    TeeOutputStream teeOutputStream = new TeeOutputStream(System.out, fileOutputStream);
	    PrintStream out = new PrintStream(teeOutputStream, true, System.getProperty("file.encoding"));
	    System.out.println("System encoding: " + System.getProperty("file.encoding"));
	    return out;
	}
	
	public static TwinWriter getFileAndConsoleTwinWriter(File f, Charset charset) throws FileNotFoundException {
		FileOutputStream fileOutputStream = new FileOutputStream(f);
		TwinWriter tw = new TwinWriter(System.out, new PrintStream(fileOutputStream, true, charset));
		return tw;
	}	
	
	public static void printFileNames() {
		File folder = new File(System.getProperty("user.dir"));
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
		    System.out.println("File " + listOfFiles[i].getName());
		  } else if (listOfFiles[i].isDirectory()) {
		    System.out.println("Directory " + listOfFiles[i].getName());
		  }
		}
	}
}
