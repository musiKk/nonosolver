/*
 * Copyright (c) 2009, Werner Hahn
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ONANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.musikk.nonosolver.janko;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses the nonograms to be found on <a
 * href="http://www.janko.at/Raetsel/Nonogramme">janko.at</a>.
 * 
 * @author Werner Hahn
 * @version 0.1
 * 
 */
public class JankoGrabber {

	/**
	 * The base URL of the nonograms in string format. %number% is to be
	 * replaced with the number of the nonogram.
	 */
	private static final String BASE_URL_STRING = "http://www.janko.at/Raetsel/Nonogramme/%number%.a.htm";

	/**
	 * The time in milliseconds between each nonogram gets downloaded. This
	 * prevents hammering the web server.
	 */
	private static final int WAITING_TIME = 500;

	/**
	 * Converts a nonogram number to a URL.
	 * 
	 * @param number
	 *            the number of the nonogram
	 * @return the URL of the site where the nonogram can be found
	 */
	private static URL getSiteUrl(int number) {
		String numberString = Integer.toString(number);
		if (number < 10) {
			numberString = "0" + numberString;
		}
		if (number < 100) {
			numberString = "0" + numberString;
		}
		try {
			return new URL(BASE_URL_STRING.replace("%number%", numberString));
		} catch (MalformedURLException e) {
			throw new RuntimeException("can't happen");
		}
	}

	/**
	 * Downloads the site content the nonogram is on and extracts the strings
	 * containing the actual nonogram information.
	 * 
	 * @param siteUrl
	 *            the URL of the site where the nonogram can be found
	 * @return a List of strings with the nonogram information. Each string
	 *         represents a row of the nonogram with 'x'es for marked cells and
	 *         '-'s for empty cells.
	 * @throws IOException
	 */
	private static List<String> extractNonoStrings(URL siteUrl)
			throws IOException {

		List<String> nonoStrings = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				siteUrl.openStream()));
		boolean foundNonoStrings = false;
		boolean readFurther = true;
		Pattern p = Pattern
				.compile(".*?param name=\"s\\d+\" value=\"(.*?)\".*");
		while (readFurther) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			Matcher m = p.matcher(line);
			if (m.find()) {
				foundNonoStrings = true;
				nonoStrings.add(m.group(1).replace(" ", ""));
			} else {
				if (foundNonoStrings) {
					readFurther = false;
				}
			}
		}

		return nonoStrings;
	}

	/**
	 * This method takes the raw nonogram information and converts the rows to
	 * the format used for the nonogram solver.
	 * 
	 * @param nonoStrings
	 *            a list of rows with 'x'es for marked cells and '-'s for empty
	 *            cells
	 * @return the row information suitable for use by the nonogram solver
	 */
	private static List<String> extractRowStrings(List<String> nonoStrings) {

		List<String> rowStrings = new ArrayList<String>();

		for (String nonoString : nonoStrings) {
			String[] blocks = nonoString.split("-+?");
			String fileLine = "";
			for (String block : blocks) {
				if (block.length() == 0) {
					continue;
				}
				if (!fileLine.equals("")) {
					fileLine += " ";
				}
				fileLine += Integer.toString(block.length());
			}
			rowStrings.add(fileLine);
		}

		return rowStrings;
	}

	/**
	 * This method takes the raw nonogram information and converts the columns
	 * to the format used for the nonogram solver.
	 * 
	 * @param nonoStrings
	 *            a list of rows with 'x'es for marked cells and '-'s for empty
	 *            cells
	 * @return the column information suitable for use by the nonogram solver
	 */
	private static List<String> extractColStrings(List<String> nonoStrings) {

		List<String> colStrings = new ArrayList<String>();

		int width = nonoStrings.get(0).length();

		for (int i = 0; i < width; i++) {
			int currentBlockLength = 0;
			String fileLine = "";
			for (String nonoString : nonoStrings) {
				char c = nonoString.charAt(i);
				if (c == 'x') {
					currentBlockLength++;
				} else {
					if (currentBlockLength > 0) {
						if (!fileLine.isEmpty()) {
							fileLine += " ";
						}
						fileLine += Integer.toString(currentBlockLength);
						currentBlockLength = 0;
					}
				}
			}
			if (currentBlockLength > 0) {
				fileLine += (fileLine.isEmpty() ? "" : " ")
						+ Integer.toString(currentBlockLength);
			}
			colStrings.add(fileLine);
		}

		return colStrings;
	}

	/**
	 * This method takes the raw nonogram information and converts it to the
	 * format used for the nonogram solver.
	 * 
	 * @param nonoStrings
	 *            a list of rows with 'x'es for marked cells and '-'s for empty
	 *            cells
	 * @return the information suitable for use by the nonogram solver
	 */
	private static List<String> convertToFileFormat(List<String> nonoStrings) {

		List<String> fileFormat = new ArrayList<String>();

		List<String> colLines = extractColStrings(nonoStrings);
		fileFormat.add("col " + Integer.toString(colLines.size()));
		fileFormat.addAll(colLines);

		List<String> rowLines = extractRowStrings(nonoStrings);
		fileFormat.add("row " + Integer.toString(rowLines.size()));
		fileFormat.addAll(rowLines);

		return fileFormat;
	}

	/**
	 * This method takes the number of the nonogram, downloads and parses the
	 * belonging HTML code and converts it to the format suitable for use by the
	 * nonogram solver.
	 * 
	 * @param number
	 *            the number of the nonogram
	 * @return a list of lines representing a file for use by the nonogram
	 *         solver
	 * @throws IOException
	 */
	public static List<String> grab(int number) throws IOException {

		URL siteUrl = getSiteUrl(number);
		List<String> nonoStrings = extractNonoStrings(siteUrl);
		if (nonoStrings.size() == 0) {
			return null;
		}
		for (String nonoString : nonoStrings) {
			System.out.println(nonoString);
		}

		return convertToFileFormat(nonoStrings);
	}

	public static void main(String[] args) throws Exception {

		File outFolder = new File("nonograms");

		for (int i = 1; i <= 1100; i++) {
			File outFile = new File(outFolder, "nonogram."
					+ Integer.toString(i));
			List<String> fileLines = grab(i);
			if (fileLines == null) {
				continue;
			}
			Writer writer = new BufferedWriter(new FileWriter(outFile));
			for (String line : fileLines) {
				writer.write(line);
				writer.write("\n");
			}
			writer.flush();
			writer.close();
			Thread.sleep(WAITING_TIME);
		}

	}

}
