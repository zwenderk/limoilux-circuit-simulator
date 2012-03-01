
package com.limoilux.circuit.techno.matrix;


import com.limoilux.circuit.techno.CircuitAnalysisException;

public class MatrixManager
{
	public double[][] matrix;
	public double[][] originalMatrix;
	public double[] originalRightSide;
	public double[] rightSide;

	public int[] circuitPermute;

	public int size;
	public int fullSize;

	public MatrixRowInfo[] circuitRowInfo;

	public void clear()
	{
		this.matrix = null;
	}

	public boolean matrixIsNull()
	{
		return this.matrix == null;
	}

	public boolean matrixIsInfiniteOrNAN()
	{
		double x;
		for (int j = 0; j != this.size; j++)
		{
			for (int i = 0; i != this.size; i++)
			{
				x = this.matrix[i][j];
				
				if (Double.isNaN(x) || Double.isInfinite(x))
				{
					return true;
				}
			}
		}

		return false;
	}

	public double getRightSide(int i)
	{
		return this.rightSide[i];
	}

	public void origRightToRight()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.rightSide[i] = this.originalRightSide[i];
		}
	}

	public String matrixToString()
	{
		String out = "";
		for (int j = 0; j != this.size; j++)
		{
			for (int i = 0; i != this.size; i++)
			{
				out += this.matrix[j][i] + ",";
			}

			out += "  " + this.rightSide[j] + "\n";
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
		for (int i = 0; i < this.size; i++)
		{
			for (int j = 0; j < this.size; j++)
			{
				this.matrix[i][j] = this.originalMatrix[i][j];
			}
		}
	}

	public void recopyMatrixToOrginal(int matrixSize)
	{
		for (int i = 0; i < matrixSize; i++)
		{
			for (int j = 0; j < matrixSize; j++)
			{
				this.originalMatrix[i][j] = this.matrix[i][j];
			}
		}
	}

	public void init(int matrixSize)
	{
		this.matrix = new double[matrixSize][matrixSize];
		this.rightSide = new double[matrixSize];
		this.originalMatrix = new double[matrixSize][matrixSize];
		this.originalRightSide = new double[matrixSize];

		this.size = matrixSize;
		this.fullSize = matrixSize;

		this.circuitRowInfo = new MatrixRowInfo[matrixSize];
		this.circuitPermute = new int[matrixSize];

		for (int i = 0; i < matrixSize; i++)
		{
			this.circuitRowInfo[i] = new MatrixRowInfo();
		}
	}

	// TODO Trouver nom significatif
	public boolean doLowUpFactor()
	{
		return MatrixUtil.lowUpFactor(this.matrix, this.circuitPermute);
	}

	public void doLowUpSolve()
	{
		MatrixUtil.lowUpSolve(this.matrix, this.size, this.circuitPermute, this.rightSide);
	}

	public int simplifyMatrix(int newsize, int matrixSize)
	{
		double newmatx[][] = new double[newsize][newsize];
		double newrs[] = new double[newsize];
		int ii = 0;

		for (int i = 0; i != matrixSize; i++)
		{
			MatrixRowInfo rri = this.circuitRowInfo[i];
			if (rri.dropRow)
			{
				rri.mapRow = -1;
				continue;
			}
			newrs[ii] = this.rightSide[i];
			rri.mapRow = ii;
			// System.out.println("Row " + i + " maps to " + ii);

			for (int j = 0; j != matrixSize; j++)
			{
				MatrixRowInfo ri = this.circuitRowInfo[j];
				if (ri.type == MatrixRowInfo.ROW_CONST)
				{
					newrs[ii] -= ri.value * this.matrix[i][j];
				}
				else
				{
					newmatx[ii][ri.mapCol] += this.matrix[i][j];
				}
			}
			ii++;
		}

		this.matrix = newmatx;
		this.rightSide = newrs;

		matrixSize = newsize;
		this.size = newsize;

		for (int i = 0; i < matrixSize; i++)
		{
			this.originalRightSide[i] = this.rightSide[i];
		}

		return matrixSize;
	}

	/**
	 * Utilité inconnue
	 * 
	 * @param matrixSize
	 */
	public void manageRowInfo(int matrixSize)
	{
		for (int i = 0; i != matrixSize; i++)
		{
			MatrixRowInfo elt = this.circuitRowInfo[i];
			if (elt.type == MatrixRowInfo.ROW_EQUAL)
			{
				MatrixRowInfo e2 = this.circuitRowInfo[elt.nodeEq];

				if (e2.type == MatrixRowInfo.ROW_CONST)
				{
					// if something is equal to a const, it's a const
					elt.type = e2.type;
					elt.value = e2.value;
					elt.mapCol = -1;
					// System.out.println(i + " = [late]const " + elt.value);
				}
				else
				{
					elt.mapCol = e2.mapCol;
					// System.out.println(i + " maps to: " + e2.mapCol);
				}
			}
		}
	}

	public void simplifyMatrixForSpeed(int matrixSize) throws CircuitAnalysisException
	{

		// simplify the matrix; this speeds things up quite a bit
		for (int i = 0; i != matrixSize; i++)
		{
			int qm = -1, qp = -1;
			double qv = 0;
			MatrixRowInfo re = this.circuitRowInfo[i];
			/*
			 * System.out.println("row " + i + " " + re.lsChanges + " " +
			 * re.rsChanges + " " + re.dropRow);
			 */
			if (re.leftSideChanges || re.dropRow || re.rightSideChanges)
			{
				continue;
			}
			double rsadd = 0;

			// look for rows that can be removed
			int j;
			for (j = 0; j != matrixSize; j++)
			{
				double q = this.matrix[i][j];

				if (this.circuitRowInfo[j].type == MatrixRowInfo.ROW_CONST)
				{
					// keep a running total of const values that have been
					// removed already
					rsadd -= this.circuitRowInfo[j].value * q;
					continue;
				}

				if (q == 0)
				{
					continue;
				}

				if (qp == -1)
				{
					qp = j;
					qv = q;
					continue;
				}

				if (qm == -1 && q == -qv)
				{
					qm = j;
					continue;
				}
				break;
			}
			// System.out.println("line " + i + " " + qp + " " + qm + " " + j);
			/*
			 * if (qp != -1 && circuitRowInfo[qp].lsChanges) {
			 * System.out.println("lschanges"); continue; } if (qm != -1 &&
			 * circuitRowInfo[qm].lsChanges) { System.out.println("lschanges");
			 * continue; }
			 */
			if (j == matrixSize)
			{
				if (qp == -1)
				{
					throw new CircuitAnalysisException("Matrix error");
				}

				MatrixRowInfo elt = this.circuitRowInfo[qp];
				if (qm == -1)
				{
					// we found a row with only one nonzero entry; that value
					// is a constant
					int k;

					for (k = 0; elt.type == MatrixRowInfo.ROW_EQUAL && k < 100; k++)
					{
						// follow the chain
						/*
						 * System.out.println("following equal chain from " + i
						 * + " " + qp + " to " + elt.nodeEq);
						 */
						qp = elt.nodeEq;
						elt = this.circuitRowInfo[qp];
					}

					if (elt.type == MatrixRowInfo.ROW_EQUAL)
					{
						// break equal chains
						// System.out.println("Break equal chain");
						elt.type = MatrixRowInfo.ROW_NORMAL;
						continue;
					}

					if (elt.type != MatrixRowInfo.ROW_NORMAL)
					{
						System.out.println("type already " + elt.type + " for " + qp + "!");
						continue;
					}

					elt.type = MatrixRowInfo.ROW_CONST;
					elt.value = (this.rightSide[i] + rsadd) / qv;
					this.circuitRowInfo[i].dropRow = true;
					// System.out.println(qp + " * " + qv + " = const " +
					// elt.value);
					i = -1; // start over from scratch
				}
				else if (this.rightSide[i] + rsadd == 0)
				{
					// we found a row with only two nonzero entries, and one
					// is the negative of the other; the values are equal
					if (elt.type != MatrixRowInfo.ROW_NORMAL)
					{
						// System.out.println("swapping");
						int qq = qm;
						qm = qp;
						qp = qq;
						elt = this.circuitRowInfo[qp];
						if (elt.type != MatrixRowInfo.ROW_NORMAL)
						{
							// we should follow the chain here, but this
							// hardly ever happens so it's not worth worrying
							// about
							System.out.println("swap failed");
							continue;
						}
					}
					elt.type = MatrixRowInfo.ROW_EQUAL;
					elt.nodeEq = qm;
					this.circuitRowInfo[i].dropRow = true;
					// System.out.println(qp + " = " + qm);
				}
			}
		}
	}
}
