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

import java.util.Arrays;

import com.github.musikk.minisat4j.Variable;

/**
 * Immutable class that represents a position of marks in a row or column.
 * 
 * @author Werner Hahn
 * @version 0.1
 * 
 */
public class Position {

	/**
	 * Every element of this array denotes the start of a new block.
	 */
	private final int[] positions;

	/**
	 * The associated block info for this row or column.
	 */
	private final Blocks blockInfo;

	/**
	 * The length of this row or column.
	 */
	private final int width;

	/**
	 * The Variable for the constraint solver that represents this position.
	 */
	private final Variable variable;

	/**
	 * A lazily computed array of marks. A true element represents a marked
	 * cell.
	 */
	private boolean[] marks;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param positions
	 *            the positions for this object
	 * @param blockInfo
	 *            the block info for this object
	 * @param width
	 *            the length of the row or column
	 */
	public Position(int[] positions, Blocks blockInfo, int width) {
		this.positions = positions;
		this.blockInfo = blockInfo;
		this.width = width;
		this.variable = Variable.getVariable();
	}

	/**
	 * Checks whether the given field (zero based) has a mark.
	 * 
	 * @param field
	 *            the field to check
	 * @return true if the field is marked, false otherwise
	 */
	public boolean hasMark(int field) {
		return getMarks()[field];
	}

	/**
	 * Returns the marks of this field. If this is the first invocation of the
	 * method, the field gets calculated first.
	 * 
	 * @return the field of marks.
	 */
	public boolean[] getMarks() {
		if (marks == null) {
			marks = calculateMarks();
		}
		return marks;
	}

	/**
	 * Calculates the marks based on the positions array.
	 * 
	 * @return the marks array
	 */
	private boolean[] calculateMarks() {
		boolean[] marks = new boolean[width];
		Arrays.fill(marks, false);
		for (int i = 0; i < positions.length; i++) {
			int pos = positions[i];
			int blockLength = blockInfo.getBlocks()[i];
			for (int j = pos; j < blockLength + pos; j++) {
				marks[j] = true;
			}
		}
		return marks;
	}

	/**
	 * Returns a basic string representation of this Position.
	 */
	@Override
	public String toString() {
		boolean[] marks = calculateMarks();
		String s = "";
		for (boolean mark : marks) {
			s += (mark ? "#" : " ");
		}
		return s;
	}

	/**
	 * Returns the Variable representing this Position.
	 * 
	 * @return the Variable representing this Position
	 */
	public Variable getVariable() {
		return variable;
	}

}
