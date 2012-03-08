
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.limoilux.circuitsimulator.circuit.CircuitElm;
import com.limoilux.circuitsimulator.core.CoreUtil;
import com.limoilux.circuitsimulator.ui.DrawUtil;

public class JfetElm extends MosfetElm
{

	public Polygon gatePoly;
	public Polygon arrowPoly;
	public Point gatePt;

	public JfetElm(int xx, int yy, boolean pnpflag)
	{
		super(xx, yy, pnpflag);
		this.noDiagonal = true;
	}

	public JfetElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
		this.noDiagonal = true;
	}

	@Override
	public void draw(Graphics g)
	{
		this.setBbox(this.point1, this.point2, this.hs);
		this.setVoltageColor(g, this.volts[1]);
		DrawUtil.drawThickLine(g, this.src[0], this.src[1]);
		DrawUtil.drawThickLine(g, this.src[1], this.src[2]);
		this.setVoltageColor(g, this.volts[2]);
		DrawUtil.drawThickLine(g, this.drn[0], this.drn[1]);
		DrawUtil.drawThickLine(g, this.drn[1], this.drn[2]);
		this.setVoltageColor(g, this.volts[0]);
		DrawUtil.drawThickLine(g, this.point1, this.gatePt);
		g.fillPolygon(this.arrowPoly);
		this.setPowerColor(g, true);
		g.fillPolygon(this.gatePoly);
		this.curcount = CircuitElm.updateDotCount(-this.ids, this.curcount);
		if (this.curcount != 0)
		{
			DrawUtil.drawDots(g, this.src[0], this.src[1], this.curcount);
			DrawUtil.drawDots(g, this.src[1], this.src[2], this.curcount + 8);
			DrawUtil.drawDots(g, this.drn[0], this.drn[1], -this.curcount);
			DrawUtil.drawDots(g, this.drn[1], this.drn[2], -(this.curcount + 8));
		}
		this.drawPosts(g);
	}

	@Override
	public void setPoints()
	{
		super.setPoints();

		// find the coordinates of the various points we need to draw
		// the JFET.
		int hs2 = this.hs * this.dsign;
		this.src = CoreUtil.newPointArray(3);
		this.drn = CoreUtil.newPointArray(3);
		CoreUtil.interpPoint2(this.point1, this.point2, this.src[0], this.drn[0], 1, hs2);
		CoreUtil.interpPoint2(this.point1, this.point2, this.src[1], this.drn[1], 1, hs2 / 2);
		CoreUtil.interpPoint2(this.point1, this.point2, this.src[2], this.drn[2], 1 - 10 / this.longueur, hs2 / 2);

		this.gatePt = CoreUtil.interpPoint(this.point1, this.point2, 1 - 14 / this.longueur);

		Point ra[] = CoreUtil.newPointArray(4);
		CoreUtil.interpPoint2(this.point1, this.point2, ra[0], ra[1], 1 - 13 / this.longueur, this.hs);
		CoreUtil.interpPoint2(this.point1, this.point2, ra[2], ra[3], 1 - 10 / this.longueur, this.hs);
		this.gatePoly = CircuitElm.createPolygon(ra[0], ra[1], ra[3], ra[2]);
		if (this.pnp == -1)
		{
			Point x = CoreUtil.interpPoint(this.gatePt, this.point1, 18 / this.longueur);
			this.arrowPoly = CircuitElm.calcArrow(this.gatePt, x, 8, 3);
		}
		else
		{
			this.arrowPoly = CircuitElm.calcArrow(this.point1, this.gatePt, 8, 3);
		}
	}

	@Override
	public int getElementId()
	{
		return 'j';
	}

	// these values are taken from Hayes+Horowitz p155
	@Override
	public double getDefaultThreshold()
	{
		return -4;
	}

	@Override
	public double getBeta()
	{
		return .00125;
	}

	@Override
	public void getInfo(String arr[])
	{
		this.getFetInfo(arr, "JFET");
	}
}
