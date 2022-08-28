package net.bossmannchristoph.other;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.StringJoiner;

public class ScannerWrapper {
	
	public static void main(String[] args) throws IOException {
		System.out.println(args[2]);
		if(args.length > 1) {
			scanXbytes(Integer.valueOf(args[0]), args[1]);	
		}
		else {
			scanXbytes(Integer.valueOf(args[0]), null);	
		}
			
	}
	
	static void scanXbytes(int x, String inputCharset) throws IOException {
		System.out.println("defaultCharset: " + Charset.defaultCharset());
		String enter = "123äöü애애애양";
		System.out.println("Umlaute: " + enter);
		printBytes(enter.getBytes(), "Umlaute (int)", "Umlaute (hex)");			
		ByteArrayInputStream bais = new ByteArrayInputStream(enter.getBytes());
		InputStreamReader isr2 = new InputStreamReader(bais);	
		String s = readLineIntoString(isr2);
		System.out.println("line: " + s);
		
		System.out.println("read: ");
		Scanner sc;
		if(inputCharset == null) {
			sc = new Scanner(System.in);
		}
		else {
			sc = new Scanner(System.in, inputCharset);
		}
		String test = sc.nextLine();
		System.out.println("result: " + test);
		
		System.out.println("number of bytes to read: " + x);
		System.out.println("enter: ");
		
		StringJoiner sjInt = new StringJoiner(",");
		StringJoiner sjHex = new StringJoiner(",");
		int counter = 0;
		while(counter < x) {
			int b = System.in.read();
			sjInt.add(String.valueOf(b));
			sjHex.add(Integer.toHexString(b));
			
			++counter;
		}
		System.out.println("direct: ");
		System.out.println("read bytes (int): " + sjInt.toString());
		System.out.println("read bytes (hex): " + sjHex.toString());
		InputStreamReader isr = new InputStreamReader(System.in, "UTF-8");
		counter = 0;
		sjInt = new StringJoiner(",");
		sjHex = new StringJoiner(",");
		byte[] bytes = new byte[x];
		while(counter < x) {
			int b = isr.read();
			sjInt.add(String.valueOf(b));
			sjHex.add(Integer.toHexString(b));
			bytes[counter] = (byte)b;		
			++counter;
		}
		System.out.println("isr read: " + new String(bytes));
		System.out.println("read bytes (int): " + sjInt.toString());
		System.out.println("read bytes (hex): " + sjHex.toString());
		sc.close();
	}
	
	static void printBytes(byte[] bytes, String titleInt, String titleHex) {
		StringJoiner sjInt = new StringJoiner(",");
		StringJoiner sjHex = new StringJoiner(",");
		int counter = 0;
		int x = bytes.length;
		while(counter < x) {			
			sjInt.add(String.valueOf(bytes[counter] & 0xff));
			sjHex.add(Integer.toHexString(bytes[counter] & 0xff));	
			++counter;
		}
		System.out.println("plain: " + new String(bytes));
		System.out.println(titleInt + ": " + sjInt.toString());
		System.out.println(titleHex + ": " + sjHex.toString());	
	}
	
	public static byte[] readIntoBytes(InputStreamReader isr, int amount) throws IOException {
		byte[] bytes = new byte[amount];
		int counter = 0;
		while(counter < amount) {
			int b = isr.read();
			bytes[counter] = (byte)b;		
			++counter;
		}
		return bytes;
	}
	
	public static String readLineIntoString(InputStreamReader isr) throws IOException {
		BufferedReader br = new BufferedReader(isr);
		return br.readLine();
	}
	
	

}
