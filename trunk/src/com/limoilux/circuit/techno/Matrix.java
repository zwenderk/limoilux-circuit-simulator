package com.limoilux.circuit.techno;

import com.limoilux.circuit.ui.RowInfo;

public class Matrix
{
	public double[][] circuitMatrix;
	public double[][] originalMatrix;
	
	public double[] origRightSide;
	public double[] circuitRightSide;
	
	public int circuitMatrixSize;
	public int circuitMatrixFullSize;
	
	public RowInfo[] circuitRowInfo;
	
	public void clearMatrix()
	{
		this.circuitMatrix = null;
	}

	public boolean matrixIsNull()
	{
		return this.circuitMatrix == null;
	}
	
	public boolean matrixIsInfiniteOrNAN()
	{
		double x;
		for (int j = 0; j != this.circuitMatrixSize; j++)
		{
			for (int i = 0; i != this.circuitMatrixSize; i++)
			{
				x = this.circuitMatrix[i][j];
				if (Double.isNaN(x) || Double.isInfinite(x))
				{
					return true;
				}
			}
		}

		return false;
	}
	

	public String matrixToString()
	{
		String out = "";
		for (int j = 0; j != this.circuitMatrixSize; j++)
		{
			for (int i = 0; i != this.circuitMatrixSize; i++)
			{
				out += this.circuitMatrix[j][i] + ",";
			}
			
			out += "  " + this.circuitRightSide[j] + "\n";
		}

		out += "\n";
		return out;
	}
	

	/**
	 * ???? origMatrix to circuitMatrix
	 */
	public void recopyMatrix()
	{
		// TODO à optimiser
		for (int i = 0; i < this.circuitMatrixSize; i++)
		{
			for (int j = 0; j < this.circuitMatrixSize; j++)
			{
				this.circuitMatrix[i][j] = this.originalMatrix[i][j];
			}
		}
	}
	
	public void recopyMatrixToOrginal(int matrixSize)
	{
		for (int i = 0; i < matrixSize; i++)
		{
			for (int j = 0; j < matrixSize; j++)
			{
				this.originalMatrix[i][j] = this.circuitMatrix[i][j];
			}
		}
	}
}
