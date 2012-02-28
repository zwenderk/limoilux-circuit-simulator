
package com.limoilux.circuit.techno;

import com.limoilux.circuit.core.CoreUtil;
import com.limoilux.circuit.ui.RowInfo;

public class Matrix
{
	public double[][] circuitMatrix;
	public double[][] originalMatrix;
	public double[] origRightSide;
	public double[] circuitRightSide;

	public int[] circuitPermute;

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

	public void init(int matrixSize)
	{
		this.circuitMatrix = new double[matrixSize][matrixSize];
		this.circuitRightSide = new double[matrixSize];
		this.originalMatrix = new double[matrixSize][matrixSize];
		this.origRightSide = new double[matrixSize];

		this.circuitMatrixSize = matrixSize;
		this.circuitMatrixFullSize = matrixSize;

		this.circuitRowInfo = new RowInfo[matrixSize];
		this.circuitPermute = new int[matrixSize];

		for (int i = 0; i < matrixSize; i++)
		{
			this.circuitRowInfo[i] = new RowInfo();
		}
	}

	/**
	 * Solves the set of n linear equations using a LU factorization previously
	 * performed by lu_factor. On input, b[0..n-1] is the right hand side of the
	 * equations, and on output, contains the solution.
	 **/
	public static void luSolve(double a[][], int n, int ipvt[], double b[])
	{
		int i;

		// find first nonzero b element
		for (i = 0; i != n; i++)
		{
			int row = ipvt[i];

			double swap = b[row];
			b[row] = b[i];
			b[i] = swap;
			if (swap != 0)
			{
				break;
			}
		}

		int bi = i++;
		for (; i < n; i++)
		{
			int row = ipvt[i];
			int j;
			double tot = b[row];

			b[row] = b[i];
			// forward substitution using the lower triangular matrix
			for (j = bi; j < i; j++)
			{
				tot -= a[i][j] * b[j];
			}
			b[i] = tot;
		}
		for (i = n - 1; i >= 0; i--)
		{
			double tot = b[i];

			// back-substitution using the upper triangular matrix
			int j;

			for (j = i + 1; j != n; j++)
			{
				tot -= a[i][j] * b[j];
			}

			b[i] = tot / a[i][i];
		}
	}

	/**
	 * factors a matrix into upper and lower triangular matrices by // gaussian
	 * elimination. On entry, a[0..n-1][0..n-1] is the // matrix to be factored.
	 * ipvt[] returns an integer vector of pivot indices, used in the lu_solve()
	 * routine.
	 **/
	public static boolean luFactor(double a[][], int n, int ipvt[])
	{
		double scaleFactors[];
		int i, j, k;

		scaleFactors = new double[n];

		// divide each row by its largest element, keeping track of the
		// scaling factors
		for (i = 0; i != n; i++)
		{
			double largest = 0;
			for (j = 0; j != n; j++)
			{
				double x = Math.abs(a[i][j]);
				if (x > largest)
				{
					largest = x;
				}
			}
			// if all zeros, it's a singular matrix
			if (largest == 0)
			{
				return false;
			}
			scaleFactors[i] = 1.0 / largest;
		}

		// use Crout's method; loop through the columns
		for (j = 0; j != n; j++)
		{

			// calculate upper triangular elements for this column
			for (i = 0; i != j; i++)
			{
				double q = a[i][j];
				for (k = 0; k != i; k++)
				{
					q -= a[i][k] * a[k][j];
				}
				a[i][j] = q;
			}

			// calculate lower triangular elements for this column
			double largest = 0;
			int largestRow = -1;
			for (i = j; i != n; i++)
			{
				double q = a[i][j];
				for (k = 0; k != j; k++)
				{
					q -= a[i][k] * a[k][j];
				}
				a[i][j] = q;
				double x = Math.abs(q);
				if (x >= largest)
				{
					largest = x;
					largestRow = i;
				}
			}

			// pivoting
			if (j != largestRow)
			{
				double x;
				for (k = 0; k != n; k++)
				{
					x = a[largestRow][k];
					a[largestRow][k] = a[j][k];
					a[j][k] = x;
				}
				scaleFactors[largestRow] = scaleFactors[j];
			}

			// keep track of row interchanges
			ipvt[j] = largestRow;

			// avoid zeros
			if (a[j][j] == 0.0)
			{
				a[j][j] = 1e-18;
			}

			if (j != n - 1)
			{
				double mult = 1.0 / a[j][j];
				for (i = j + 1; i != n; i++)
				{
					a[i][j] *= mult;
				}
			}
		}
		return true;
	}

	// TODO Trouver nom significatif
	public boolean doLuFactor()
	{
		return Matrix.luFactor(this.circuitMatrix, this.circuitMatrixSize, this.circuitPermute);
	}

	public int simplifyMatrix(int newsize, int matrixSize)
	{
		double newmatx[][] = new double[newsize][newsize];
		double newrs[] = new double[newsize];
		int ii = 0;

		for (int i = 0; i != matrixSize; i++)
		{
			RowInfo rri = this.circuitRowInfo[i];
			if (rri.dropRow)
			{
				rri.mapRow = -1;
				continue;
			}
			newrs[ii] = this.circuitRightSide[i];
			rri.mapRow = ii;
			// System.out.println("Row " + i + " maps to " + ii);

			for (int j = 0; j != matrixSize; j++)
			{
				RowInfo ri = this.circuitRowInfo[j];
				if (ri.type == RowInfo.ROW_CONST)
				{
					newrs[ii] -= ri.value * this.circuitMatrix[i][j];
				}
				else
				{
					newmatx[ii][ri.mapCol] += this.circuitMatrix[i][j];
				}
			}
			ii++;
		}

		this.circuitMatrix = newmatx;
		this.circuitRightSide = newrs;

		matrixSize = newsize;
		this.circuitMatrixSize = newsize;

		for (int i = 0; i < matrixSize; i++)
		{
			this.origRightSide[i] = this.circuitRightSide[i];
		}

		return matrixSize;
	}
}
