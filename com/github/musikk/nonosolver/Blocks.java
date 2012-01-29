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
package com.github.musikk.nonosolver;

/**
 * This class represents the information a nonogram provides on a single row or
 * column.
 * 
 * @author Werner Hahn
 * @version 0.1
 * 
 */
public class Blocks {

	/**
	 * The numbers on a row or column denoting the lengths of the blocks.
	 */
	private final int[] blocks;

	/**
	 * The number of blocks in a row or column.
	 */
	private final int numberOfBlocks;

	/**
	 * Creates a new instance of this class with the given blocks.
	 * 
	 * @param blocks
	 */
	public Blocks(int[] blocks) {
		this.blocks = blocks.clone();
		this.numberOfBlocks = blocks.length;
	}

	/**
	 * Returns the blocks containing the numbers denoting the lenghts of the
	 * blocks on a row or column of this Blocks instance.
	 * 
	 * @return the blocks
	 */
	public int[] getBlocks() {
		return this.blocks.clone();
	}

	/**
	 * Returns the number of blocks in a row or column of this Blocks instance.
	 * 
	 * @return
	 */
	public int getNumberOfBlocks() {
		return numberOfBlocks;
	}

	/**
	 * Creates a Blocks object from a String. The String contains numbers
	 * seperated by whitespace that denote the size of the blocks of a row or
	 * column.
	 * 
	 * @param blockString
	 *            the string containing the block information
	 * @return the Blocks object
	 */
	public static Blocks createBlockInfo(String blockString) {

		String[] splittedBlocks = blockString.split("\\s");

		int[] blocks = new int[splittedBlocks.length];

		for (int i = 0; i < splittedBlocks.length; i++) {
			String splittedBlock = splittedBlocks[i];
			if (splittedBlock.isEmpty()) {
				blocks[i] = 0;
			} else {
				blocks[i] = Integer.parseInt(splittedBlock);
			}
		}

		Blocks bi = new Blocks(blocks);
		return bi;

	}

	/**
	 * Returns an array of Blocks[] with each element created via
	 * <code>createBlockInfo()</code> with the elements of the given string
	 * array.
	 * 
	 * @param blockStrings
	 *            the strings for all rows or columns of a nonogram
	 * @return the Blocks array
	 */
	public static Blocks[] createBlockInfos(String[] blockStrings) {

		Blocks[] bis = new Blocks[blockStrings.length];
		for (int i = 0; i < bis.length; i++) {
			bis[i] = Blocks.createBlockInfo(blockStrings[i]);
		}
		return bis;

	}

}
