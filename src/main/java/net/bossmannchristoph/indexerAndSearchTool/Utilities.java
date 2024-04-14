package net.bossmannchristoph.indexerAndSearchTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;

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

	public static Path getDocPath(Path rootPath, File f) {
		return rootPath.relativize(f.toPath());
	}
}
