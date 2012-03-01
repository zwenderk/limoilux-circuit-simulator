
package com.limoilux.circuit.techno.matrix;

/**
 * info about each row/column of the matrix for simplification purposes
 * 
 * @author Paul Falstad (2011 version)
 * @author David Bernard
 * 
 */
public class MatrixRowInfo
{
	/**
	 * ordinary value
	 */
	public static final int ROW_NORMAL = 0;
	/**
	 * value is constant
	 */
	public static final int ROW_CONST = 1;

	/**
	 * value is equal to another value
	 */
	public static final int ROW_EQUAL = 2;

	public int nodeEq;
	public int type;
	public int mapCol;
	public int mapRow;
	public double value;
	public boolean rightSideChanges; // row's right side changes
	public boolean leftSideChanges; // row's left side changes
	public boolean dropRow; // row is not needed in matrix

	public MatrixRowInfo()
	{
		this.type = MatrixRowInfo.ROW_NORMAL;
	}
}
