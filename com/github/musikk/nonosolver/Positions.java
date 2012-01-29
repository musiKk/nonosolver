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

import java.util.ArrayList;
import java.util.List;

import com.github.musikk.nonosolver.util.ArrayUtils;

/**
 * Immutable class that organizes a collection of {@link Position Positions}
 * that represents all possible positions a {@link Blocks Block} with a given
 * width can create.
 * 
 * @author Werner Hahn
 * @version 0.1
 * 
 */
public class Positions {

	/**
	 * The length of the associated row or column.
	 */
	private final int width;

	/**
	 * The block info for this row or column.
	 */
	private final Blocks blockInfo;

	/**
	 * All possible positions that can be created from the given block info and
	 * width.
	 */
	private final List<Position> positions;

	/**
	 * Creates a new instance of this class and calculates all possible
	 * positions based on the given block info and width.
	 * 
	 * @param blockInfo
	 *            the block info for the row or column
	 * @param width
	 *            the length of the row or column
	 */
	public Positions(Blocks blockInfo, int width) {
		this.width = width;
		this.blockInfo = blockInfo;
		this.positions = new ArrayList<Position>();
		this.computePositions();
	}

	/**
	 * Returns all possible positions that have a mark in the field specified by
	 * the parameter.
	 * 
	 * @param field
	 *            zero based position of the mark in question
	 * @return
	 */
	public List<Position> getPositions(int field) {
		List<Position> positions = new ArrayList<Position>();

		for (Position position : this.positions) {
			if (position.hasMark(field)) {
				positions.add(position);
			}
		}

		return positions;
	}

	/**
	 * Computes all possible positions.
	 */
	private void computePositions() {

		if (!verifyLength()) {
			throw new IllegalArgumentException("row too wide for width "
					+ this.width);
		}

		if (blockInfo.getNumberOfBlocks() == 0) {
			return;
		}

		shiftPosition(new int[blockInfo.getNumberOfBlocks()], 0);

	}

	private void shiftPosition(int[] positions, int currentPosition) {

		while (positions[0] < (width - ArrayUtils.sum(blockInfo.getBlocks()) + blockInfo
				.getNumberOfBlocks())) {

			if (currentPosition >= blockInfo.getNumberOfBlocks()) {
				return;
			}

			if (currentPosition == 0 && positions[currentPosition] == 0) {
				positions[currentPosition] = 0;
			} else {
				if (positions[currentPosition] == 0) {
					positions[currentPosition] = blockInfo.getBlocks()[currentPosition - 1]
							+ positions[currentPosition - 1] + 1;
				}
			}

			shiftPosition(positions, currentPosition + 1);
			if (currentPosition == blockInfo.getNumberOfBlocks() - 1) {
				this.positions.add(new Position(positions.clone(), blockInfo,
						width));
			}

			int rightSum = 0;
			for (int i = currentPosition; i < blockInfo.getNumberOfBlocks(); i++) {
				rightSum += blockInfo.getBlocks()[i];
				if (i != currentPosition) {
					rightSum++;
				}
			}
			if (width - positions[currentPosition] > rightSum) {

				positions[currentPosition]++;
				for (int i = currentPosition + 1; i < blockInfo
						.getNumberOfBlocks(); i++) {
					positions[i] = 0;
				}
			} else {
				return;
			}
		}

	}

	/**
	 * Returns all possible positions.
	 * 
	 * @return all possible positions
	 */
	public List<Position> getPositions() {
		return new ArrayList<Position>(positions);
	}

	/**
	 * Checks whether the length of this row or column is sufficient for the
	 * given block info.
	 * 
	 * @return
	 */
	private boolean verifyLength() {
		int[] blocks = this.blockInfo.getBlocks();
		if ((ArrayUtils.sum(blocks) + blocks.length - 1) > width) {
			return false;
		}

		return true;
	}

}
