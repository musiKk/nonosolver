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

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.github.musikk.minisat4j.Clause;
import com.github.musikk.minisat4j.Solver;
import com.github.musikk.minisat4j.SolverResult;
import com.github.musikk.minisat4j.Variable;
import com.github.musikk.nonosolver.util.ImageCreator;
import com.github.musikk.minisat4j.util.Timer;

/**
 * This class solves a nonogram utilizing the minisat constraint solver.
 * 
 * @author Werner Hahn
 * @version 0.1
 * 
 */
public class NonoSolver {

	/**
	 * All possible positions for all columns.
	 */
	private Positions[] colPositions;
	/**
	 * All possible positions for all rows.
	 */
	private Positions[] rowPositions;

	/**
	 * The width of the nonogram.
	 */
	private final int width;
	/**
	 * The height of the nonogram.
	 */
	private final int height;

	/**
	 * The Solver instance used to solve the nonogram.
	 */
	private final Solver solver;

	/**
	 * The result of the Solver.
	 */
	private SolverResult solverResult;

	/**
	 * An array of Variables for all cells.
	 */
	private final Variable[][] cellVariables;

	/**
	 * The time it took to create the constraint system.
	 */
	private long constraintCreationTime;

	/**
	 * Creates a new nonogram solver for the nonogram described by the given
	 * file.
	 * 
	 * @param nonoFile
	 *            the nonogram
	 */
	public NonoSolver(File nonoFile) {

		BlockCreator bc = new BlockCreator(nonoFile);
		Blocks[] columns = bc.getCols();
		Blocks[] rows = bc.getRows();

		Positions[][] positions = calculatePositions(columns, rows);
		this.colPositions = positions[0];
		this.rowPositions = positions[1];
		this.width = columns.length;
		this.height = rows.length;
		this.solver = new Solver();

		cellVariables = new Variable[width][];
		for (int i = 0; i < width; i++) {
			cellVariables[i] = new Variable[height];
			for (int j = 0; j < height; j++) {
				cellVariables[i][j] = Variable.getVariable();
			}
		}
	}

	/**
	 * Associates the variable of a cell to all positions that the cell
	 * participates in. This method assumes that for the given positions this is
	 * the case. That way the boolean value of a cell gets set to true iff a
	 * position it participates in is part of the solution.
	 * 
	 * @param cellVar
	 *            the cell variable
	 * @param positions
	 *            all positions that contain the cell the variable is associated
	 *            with
	 * @return the constraints
	 */
	private static List<Clause> createCellConstraintsForCell(Variable cellVar,
			List<Position> positions) {

		List<Variable> positionVars = new ArrayList<Variable>(positions.size());
		for (Position p : positions) {
			positionVars.add(p.getVariable());
		}
		return Arrays.asList(Clause.equivalence(cellVar, positionVars));
	}

	private List<Clause> createCellConstraints() {

		List<Clause> cellConstraints = new ArrayList<Clause>();

		for (int col = 0; col < width; col++) {
			Positions possibleColumnPositions = this.colPositions[col];
			for (int row = 0; row < height; row++) {
				Positions possibleRowPositions = this.rowPositions[row];
				Variable cellVar = cellVariables[col][row];

				List<Position> colPositionsForThisCell = possibleColumnPositions
						.getPositions(row);
				List<Position> rowPositionsForThisCell = possibleRowPositions
						.getPositions(col);

				cellConstraints.addAll(createCellConstraintsForCell(cellVar,
						colPositionsForThisCell));
				cellConstraints.addAll(createCellConstraintsForCell(cellVar,
						rowPositionsForThisCell));
			}
		}

		return cellConstraints;
	}

	private static List<Clause> createPositionConstraints(Positions positions) {

		List<Variable> variables = new ArrayList<Variable>();
		for (Position p : positions.getPositions()) {
			variables.add(p.getVariable());
		}
		return Arrays.asList(Clause.onlyOne(variables
				.toArray(new Variable[] {})));
	}

	private List<Clause> createPositionConstraints() {

		List<Clause> posConstraints = new ArrayList<Clause>();

		for (Positions col : colPositions) {
			posConstraints.addAll(createPositionConstraints(col));
		}
		for (Positions row : rowPositions) {
			posConstraints.addAll(createPositionConstraints(row));
		}

		return posConstraints;
	}

	/**
	 * Solves the CNF for the nonogram.
	 * 
	 * @return true if the CNF is satisfiable, i.e. there is a solution, false
	 *         otherwise
	 */
	public boolean solve() {

		Timer constraintCreationTimer = Timer.startTimer();

		List<Clause> cellConstraints = createCellConstraints();
		solver.addClauses(cellConstraints);

		List<Clause> posConstraints = createPositionConstraints();
		solver.addClauses(posConstraints);

		constraintCreationTimer.stop();
		this.constraintCreationTime = constraintCreationTimer.getDuration();

		this.solverResult = solver.solve();

		return solverResult.isSatisfiable();

	}

	public Variable[][] getCellVariables() {
		return cellVariables.clone();
	}

	public SolverResult getSolverResult() {
		return solverResult;
	}

	public long getConstraintCreationTime() {
		return constraintCreationTime;
	}

	private static Positions[][] calculatePositions(Blocks[] columns,
			Blocks[] rows) {
		Positions[] colPositions = new Positions[columns.length];
		Positions[] rowPositions = new Positions[rows.length];

		for (int i = 0; i < colPositions.length; i++) {
			colPositions[i] = new Positions(columns[i], rows.length);
		}
		for (int i = 0; i < rowPositions.length; i++) {
			rowPositions[i] = new Positions(rows[i], columns.length);
		}

		return new Positions[][] { colPositions, rowPositions };
	}

	private static final String BASE_INPUT_FILE_NAME = "nonograms/nonogram.%number%";

	private static final String BASE_OUTPUT_FILE_NAME = System
			.getProperty("java.io.tmpdir")
			+ "/nonogram_solution%number%.png";

	public static void main(String[] args) {

		if (args.length < 1) {
			printUsage();
			return;
		}

		String inputFileName = args[0];
		File inputFile = new File(inputFileName);
		if (!inputFile.exists()) {
			inputFile = new File(BASE_INPUT_FILE_NAME.replace("%number%",
					inputFileName));
		}

		File outputFile = null;
		if (args.length >= 2) {
			outputFile = new File(args[1]);
		} else {
			int number = 0;
			boolean fileNameFound = false;
			while (!fileNameFound) {
				outputFile = new File(BASE_OUTPUT_FILE_NAME.replace("%number%",
						Integer.toString(number)));
				fileNameFound = !outputFile.exists();
				number++;
			}
		}

		NonoSolver ns = new NonoSolver(inputFile);

		boolean solved = ns.solve();
		System.err.println("constraint creation took "
				+ ns.getConstraintCreationTime() / 1000.0 + "s");
		System.err.println(ns.getSolverResult());
		if (!solved) {
			System.err.println("unsat");
			return;
		}

		Variable[][] cellVariables = ns.getCellVariables();
		for (int row = 0; row < cellVariables[0].length; row++) {
			for (int col = 0; col < cellVariables.length; col++) {
				Variable cellVar = cellVariables[col][row];
				System.err.print(cellVar.getResult() ? "#" : ".");
			}
			System.err.println();
		}

		BufferedImage bi = ImageCreator.createImage(ns.getCellVariables(), 10);
		try {
			ImageIO.write(bi, "png", outputFile);
		} catch (IOException e) {
			throw new RuntimeException("error writing image", e);
		}

	}

	private static void printUsage() {
		System.err.println("usage: nonosolver [input file]{1} [output file]?");
	}

	/**
	 * A helper class that converts the file format to the block information
	 * used by the nonogram solver.
	 * 
	 * @author werner
	 * 
	 */
	private static class BlockCreator {

		private final File nonoFile;

		private Blocks[] cols;

		private Blocks[] rows;

		public BlockCreator(File nonoFile) {
			this.nonoFile = nonoFile;
			calculateBlocks();
		}

		private void calculateBlocks() {

			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						nonoFile));

				cols = extractBlocks(reader, BlockType.COL);
				rows = extractBlocks(reader, BlockType.ROW);

				reader.close();
			} catch (FileNotFoundException e) {
				throw new RuntimeException("file '"
						+ nonoFile.getAbsolutePath() + "' not found");
			} catch (IOException e) {
				throw new RuntimeException("error reading file", e);
			}

		}

		private static Blocks[] extractBlocks(BufferedReader reader,
				BlockType blockType) throws IOException {

			String descriptor = reader.readLine();
			Pattern p = Pattern.compile(blockType + "\\s+(\\d+)");
			Matcher m = p.matcher(descriptor);
			if (!m.find()) {
				throw new RuntimeException("invalid file format, expected "
						+ blockType + " descriptor but got '" + descriptor
						+ "'");
			}

			int blockCount = Integer.parseInt(m.group(1));

			String[] blockStrings = new String[blockCount];
			for (int i = 0; i < blockCount; i++) {
				String blockLine = reader.readLine();
				blockStrings[i] = blockLine;
			}

			return Blocks.createBlockInfos(blockStrings);
		}

		public Blocks[] getCols() {
			return cols.clone();
		}

		public Blocks[] getRows() {
			return rows.clone();
		}

		private static enum BlockType {
			COL("col"), ROW("row");

			private final String keyWord;

			private BlockType(String keyWord) {
				this.keyWord = keyWord;
			}

			@Override
			public String toString() {
				return keyWord;
			}
		}

	}

}
