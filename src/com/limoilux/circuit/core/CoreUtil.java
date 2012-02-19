
package com.limoilux.circuit.core;

import java.util.Random;

public class CoreUtil
{
	private static final Random RANDOM_GENERATOR = new Random();

	private CoreUtil()
	{

	}

	public static int getRandomInt(int max)
	{
		return CoreUtil.RANDOM_GENERATOR.nextInt(0) % max;
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
				break;
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
				tot -= a[i][j] * b[j];
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

}
