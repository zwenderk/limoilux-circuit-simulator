
package com.limoilux.circuitsimulator.core;

import java.awt.Point;
import java.awt.Polygon;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

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
			return CircuitElm.showFormat.format(v * 1e6) + " " + CircuitSimulator.muString + u;
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
			return CircuitElm.shortFormat.format(v * 1e6) + CircuitSimulator.muString + u;
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

	public static double updateDotCount(double cur, double cc, double currentMultiplier)
	{
		if (!CircuitElm.cirSim.activityManager.isPlaying())
		{
			return cc;
		}
		double cadd = cur * currentMultiplier;
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

	public static ByteArrayOutputStream readUrlData(URL url) throws IOException
	{
		Object o = url.getContent();
		FilterInputStream fis = (FilterInputStream) o;
		ByteArrayOutputStream ba = new ByteArrayOutputStream(fis.available());
		byte[] bytes = null;
		int blen = 1024;
		int len;

		bytes = new byte[blen];

		while (true)
		{
			len = fis.read(bytes);
			if (len <= 0)
			{
				break;
			}
			ba.write(bytes, 0, len);
		}
		return ba;
	}

	public static URL getCodeBase()
	{
		URL out = null;
		File f = null;

		try
		{
			f = new File(".");
			out = new URL("file:" + f.getCanonicalPath() + "/");
		}
		catch (MalformedURLException e)
		{
		}
		catch (IOException e)
		{
		}

		return out;
	}


}
