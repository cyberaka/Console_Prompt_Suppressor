/**
 * 
 */
package com.cyberaka.pps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cyberaka
 * 
 */
public class Main {

	private String instructionFile;
	private String promptToLook;
	private String valueToSubstitute;
	private List<String[]> instructionList;

	public Main(String pInstructionFile, String pPromptToLook,
			String pValueToSubstitute) {
		instructionFile = pInstructionFile;
		promptToLook = pPromptToLook;
		valueToSubstitute = pValueToSubstitute;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err
					.println("Usage:\ncom.cyberaka.pps.Main <instructions> <prompt> <value>");
			System.exit(-100);
		}

		Main main = new Main(args[0], args[1], args[2]);
		main.execute();
	}

	private void execute() {
		instructionList = readFile(instructionFile);
		try {
			for (String[] instructions : instructionList) {
				ProcessBuilder builder = new ProcessBuilder(instructions);
				builder.redirectErrorStream(true);
				Process process = builder.start();

				OutputStream stdIn = process.getOutputStream();
				InputStream stdOut = process.getInputStream();

				int value;
				byte[] bArr = new byte[1024 *2];
				int bytesRead = 0;
				while ((value = stdOut.read()) != -1) {
					if (value == 10 && bytesRead > 0) {
						String line = new String(bArr, 0, bytesRead - 1);
						System.out.println(line);
						if (line.contains(promptToLook)) {
			            	stdIn.write(valueToSubstitute.getBytes());
			            	stdIn.flush();
			            	System.out.println("\nFlushed Substitute..");
			            }
						bytesRead = 0;
						continue;
					}
					if (value == 10 || value == 13) { // Ignore
						continue;
					}
//					System.out.print(value + ",");
					bArr[bytesRead] = (byte) value;
					bytesRead++;
				}
		        
		        // Wait to get exit value
		        try {
		        	System.out.println("Waiting it out!");
		            int exitValue = process.waitFor();
		            System.out.println("\n\nExit Value is " + exitValue);
		        } catch (InterruptedException e) {
		            e.printStackTrace();
		            System.exit(-500);
		        }
		        stdIn.close();
		        stdOut.close();
		        process.destroy();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-400);
		}
	}

	public List<String[]> readFile(String filePath) {
		BufferedReader br = null;
		ArrayList<String[]> instructionList = new ArrayList<String[]>();
		try {

			String currentLine;

			br = new BufferedReader(new FileReader(filePath));

			while ((currentLine = br.readLine()) != null) {
//				System.out.println(currentLine);
				String instructions[] = currentLine.split(" ");
				instructionList.add(instructions);
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-200);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(-300);
			}
		}
		return instructionList;
	}
}
