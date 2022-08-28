package net.bossmannchristoph.indexerAndSearchTool;

import java.io.PrintStream;

public class TwinWriter {

	private PrintStream p1;
	private PrintStream p2;
	
	public TwinWriter(PrintStream p1, PrintStream p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public void println(String s) {
		p1.println(s);
		p2.println(s);
	}
	public void print(String s) {
		p1.print(s);
		p2.print(s);
	}
}
