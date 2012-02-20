
package com.limoilux.circuit.core;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Random;

import com.limoilux.circuit.CirSim;
import com.limoilux.circuit.CircuitElm;

public class CoreUtil
{
	private static final Random RANDOM_GENERATOR = new Random();

	private CoreUtil()
	{

	}

	public static int getRandomInt(int max)
	{
		return CoreUtil.RANDOM_GENERATOR.nextInt(0 + 1) % Math.abs(max);
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

	public static int distanceSq(int x1, int y1, int x2, int y2)
	{
		x2 -= x1;
		y2 -= y1;
		return x2 * x2 + y2 * y2;
	}

	public static double distance(Point p1, Point p2)
	{
		double x = p1.x - p2.x;
		double y = p1.y - p2.y;
		return Math.sqrt(x * x + y * y);
	}

	public static int sign(int x)
	{
		return x < 0 ? -1 : x == 0 ? 0 : 1;
	}

	public static void interpPoint(Point a, Point b, Point c, double f)
	{
		int xpd = b.x - a.x;
		int ypd = b.y - a.y;
		/*
		 * double q = (a.x*(1-f)+b.x*f+.48); System.out.println(q + " " + (int)
		 * q);
		 */
		c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + .48);
		c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + .48);
	}

	public static Point interpPoint(Point a, Point b, double f)
	{
		Point p = new Point();
		CoreUtil.interpPoint(a, b, p, f);
		return p;
	}

	public static void interpPoint(Point a, Point b, Point c, double f, double g)
	{
		int xpd = b.x - a.x;
		int ypd = b.y - a.y;
		int gx = b.y - a.y;
		int gy = a.x - b.x;
		g /= Math.sqrt(gx * gx + gy * gy);
		c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + g * gx + .48);
		c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + g * gy + .48);
	}

	public static Point interpPoint(Point a, Point b, double f, double g)
	{
		Point p = new Point();
		CoreUtil.interpPoint(a, b, p, f, g);
		return p;
	}

	public static void interpPoint2(Point a, Point b, Point c, Point d, double f, double g)
	{
		int xpd = b.x - a.x;
		int ypd = b.y - a.y;
		int gx = b.y - a.y;
		int gy = a.x - b.x;
		g /= Math.sqrt(gx * gx + gy * gy);
		c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + g * gx + .48);
		c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + g * gy + .48);
		d.x = (int) Math.floor(a.x * (1 - f) + b.x * f - g * gx + .48);
		d.y = (int) Math.floor(a.y * (1 - f) + b.y * f - g * gy + .48);
	}

	public static Point[] newPointArray(int n)
	{
		Point a[] = new Point[n];
		while (n > 0)
		{
			a[--n] = new Point();
		}
		return a;
	}

	public static Polygon createPolygon(Point a, Point b, Point c)
	{
		Polygon p = new Polygon();
		p.addPoint(a.x, a.y);
		p.addPoint(b.x, b.y);
		p.addPoint(c.x, c.y);
		return p;

	}

	public static Polygon createPolygon(Point a, Point b, Point c, Point d)
	{
		Polygon p = new Polygon();
		p.addPoint(a.x, a.y);
		p.addPoint(b.x, b.y);
		p.addPoint(c.x, c.y);
		p.addPoint(d.x, d.y);
		return p;
	}

	public static Polygon createPolygon(Point a[])
	{
		Polygon p = new Polygon();
		int i;
		for (i = 0; i != a.length; i++)
		{
			p.addPoint(a[i].x, a[i].y);
		}
		return p;
	}
	

	public static String getVoltageDText(double v)
	{
		return CoreUtil.getUnitText(Math.abs(v), "V");
	}

	public static String getVoltageText(double v)
	{
		return CoreUtil.getUnitText(v, "V");
	}
	

	public static String getUnitText(double v, String u)
	{
		double va = Math.abs(v);
		if (va < 1e-14)
		{
			return "0 " + u;
		}
		if (va < 1e-9)
		{
			return CircuitElm.showFormat.format(v * 1e12) + " p" + u;
		}
		if (va < 1e-6)
		{
			return CircuitElm.showFormat.format(v * 1e9) + " n" + u;
		}
		if (va < 1e-3)
		{
			return CircuitElm.showFormat.format(v * 1e6) + " " + CirSim.muString + u;
		}
		if (va < 1)
		{
			return CircuitElm.showFormat.format(v * 1e3) + " m" + u;
		}
		if (va < 1e3)
		{
			return CircuitElm.showFormat.format(v) + " " + u;
		}
		if (va < 1e6)
		{
			return CircuitElm.showFormat.format(v * 1e-3) + " k" + u;
		}
		if (va < 1e9)
		{
			return CircuitElm.showFormat.format(v * 1e-6) + " M" + u;
		}
		return CircuitElm.showFormat.format(v * 1e-9) + " G" + u;
	}

	
	public static String getShortUnitText(double v, String u)
	{
		double va = Math.abs(v);
		if (va < 1e-13)
		{
			return null;
		}
		if (va < 1e-9)
		{
			return CircuitElm.shortFormat.format(v * 1e12) + "p" + u;
		}
		if (va < 1e-6)
		{
			return CircuitElm.shortFormat.format(v * 1e9) + "n" + u;
		}
		if (va < 1e-3)
		{
			return CircuitElm.shortFormat.format(v * 1e6) + CirSim.muString + u;
		}
		if (va < 1)
		{
			return CircuitElm.shortFormat.format(v * 1e3) + "m" + u;
		}
		if (va < 1e3)
		{
			return CircuitElm.shortFormat.format(v) + u;
		}
		if (va < 1e6)
		{
			return CircuitElm.shortFormat.format(v * 1e-3) + "k" + u;
		}
		if (va < 1e9)
		{
			return CircuitElm.shortFormat.format(v * 1e-6) + "M" + u;
		}
		return CircuitElm.shortFormat.format(v * 1e-9) + "G" + u;
	}
	
	public static String getCurrentText(double i)
	{
		return CoreUtil.getUnitText(i, "A");
	}

	public static String getCurrentDText(double i)
	{
		return CoreUtil.getUnitText(Math.abs(i), "A");
	}
	
	public static double updateDotCount(double cur, double cc)
	{
		if (CircuitElm.cirSim.stoppedCheck.getState())
		{
			return cc;
		}
		double cadd = cur * CircuitElm.currentMult;
		/*
		 * if (cur != 0 && cadd <= .05 && cadd >= -.05) cadd = (cadd < 0) ? -.05
		 * : .05;
		 */
		cadd %= 8;
		/*
		 * if (cadd > 8) cadd = 8; if (cadd < -8) cadd = -8;
		 */
		return cc + cadd;
	}
	
	public static boolean comparePair(int x1, int x2, int y1, int y2)
	{
		return x1 == y1 && x2 == y2 || x1 == y2 && x2 == y1;
	}
}
