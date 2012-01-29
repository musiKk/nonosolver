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
package com.github.musikk.nonosolver.util;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.github.musikk.minisat4j.Variable;

/**
 * Utility class to create an image from a nonogram solution.
 * 
 * @author Werner Hahn
 * @version 0.1
 * 
 */
public class ImageCreator {

	/**
	 * The default cell size.
	 */
	private final static int DEFAULT_CELL_SIZE = 3;

	/**
	 * Colors a cell in the image with the given Color.
	 * 
	 * @param bi
	 *            the image
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @param cellSize
	 *            the size of the cell
	 * @param c
	 *            the color for the cell
	 */
	private static void colorCell(BufferedImage bi, int x, int y, int cellSize,
			Color c) {

		int left = x * cellSize;
		int right = left + cellSize;
		int up = y * cellSize;
		int down = up + cellSize;
		for (int cellX = left; cellX < right; cellX++) {
			for (int cellY = up; cellY < down; cellY++) {
				bi.setRGB(cellX, cellY, c.getRGB());
			}
		}

	}

	/**
	 * Creates a BufferedImage based on the given variables.
	 * 
	 * @param variables
	 *            the variables that represent a nonogram solution
	 * @param cellSize
	 *            the size of a cell
	 * @return the image
	 */
	public static BufferedImage createImage(Variable[][] variables, int cellSize) {

		int height = variables[0].length;
		int width = variables.length;
		BufferedImage bi = new BufferedImage(width * cellSize, height
				* cellSize, BufferedImage.TYPE_3BYTE_BGR);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Color c = variables[x][y].getResult() ? Color.BLACK
						: Color.WHITE;
				colorCell(bi, x, y, cellSize, c);
			}
		}

		return bi;

	}

	/**
	 * Creates a BufferedImage based on the given variables. Uses the default
	 * cell size.
	 * 
	 * @param variables
	 *            the variables that represent a nonogram solution
	 * @return the image
	 */
	public static BufferedImage createImage(Variable[][] variables) {
		return createImage(variables, DEFAULT_CELL_SIZE);
	}

}
