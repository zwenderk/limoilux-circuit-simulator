
package com.limoilux.circuit.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

import com.limoilux.circuit.core.CircuitElm;

public class DrawUtil
{
	private DrawUtil()
	{
	}

	public static void drawThickLine(Graphics g, int x, int y, int x2, int y2)
	{
		g.drawLine(x, y, x2, y2);
		g.drawLine(x + 1, y, x2 + 1, y2);
		g.drawLine(x, y + 1, x2, y2 + 1);
		g.drawLine(x + 1, y + 1, x2 + 1, y2 + 1);
	}

	public static void drawThickLine(Graphics g, Point pa, Point pb)
	{
		g.drawLine(pa.x, pa.y, pb.x, pb.y);
		g.drawLine(pa.x + 1, pa.y, pb.x + 1, pb.y);
		g.drawLine(pa.x, pa.y + 1, pb.x, pb.y + 1);
		g.drawLine(pa.x + 1, pa.y + 1, pb.x + 1, pb.y + 1);
	}

	public static void drawThickPolygon(Graphics g, int xs[], int ys[], int c)
	{
		int i;
		for (i = 0; i != c - 1; i++)
		{
			DrawUtil.drawThickLine(g, xs[i], ys[i], xs[i + 1], ys[i + 1]);
		}
		DrawUtil.drawThickLine(g, xs[i], ys[i], xs[0], ys[0]);
	}

	public static void drawThickPolygon(Graphics g, Polygon p)
	{
		DrawUtil.drawThickPolygon(g, p.xpoints, p.ypoints, p.npoints);
	}

	public static void drawThickCircle(Graphics g, int cx, int cy, int ri)
	{
		int a;
		double m = Math.PI / 180;
		double r = ri * .98;
		for (a = 0; a != 360; a += 20)
		{
			double ax = Math.cos(a * m) * r + cx;
			double ay = Math.sin(a * m) * r + cy;
			double bx = Math.cos((a + 20) * m) * r + cx;
			double by = Math.sin((a + 20) * m) * r + cy;
			DrawUtil.drawThickLine(g, (int) ax, (int) ay, (int) bx, (int) by);
		}
	}

	public static void drawDots(Graphics g, Point pa, Point pb, double pos)
	{
		if (CircuitElm.cirSim.stoppedCheck.getState() || pos == 0 || !CircuitElm.cirSim.dotsCheckItem.getState())
		{
			return;
		}
		int dx = pb.x - pa.x;
		int dy = pb.y - pa.y;
		double dn = Math.sqrt(dx * dx + dy * dy);
		g.setColor(Color.yellow);
		int ds = 16;
		pos %= ds;
		if (pos < 0)
		{
			pos += ds;
		}
		double di = 0;
		for (di = pos; di < dn; di += ds)
		{
			int x0 = (int) (pa.x + di * dx / dn);
			int y0 = (int) (pa.y + di * dy / dn);
			g.fillRect(x0 - 1, y0 - 1, 4, 4);
		}
	}

	public static void drawPost(Graphics g, int x0, int y0)
	{
		g.setColor(CircuitElm.whiteColor);
		g.fillOval(x0 - 3, y0 - 3, 7, 7);
	}

	public static void setPowerColor(Graphics g, double w0)
	{
		w0 *= CircuitElm.powerMult;
		// System.out.println(w);
		double w = w0 < 0 ? -w0 : w0;
		if (w > 1)
		{
			w = 1;
		}
		int rg = 128 + (int) (w * 127);
		int b = (int) (128 * (1 - w));
		/*
		 * if (yellow) g.setColor(new Color(rg, rg, b)); else
		 */
		if (w0 > 0)
		{
			g.setColor(new Color(rg, b, b));
		}
		else
		{
			g.setColor(new Color(b, rg, b));
		}
	}

	public static void setConductanceColor(Graphics g, double w0)
	{
		w0 *= CircuitElm.powerMult;
		// System.out.println(w);
		double w = w0 < 0 ? -w0 : w0;
		if (w > 1)
		{
			w = 1;
		}
		int rg = (int) (w * 255);
		g.setColor(new Color(rg, rg, rg));
	}

}
