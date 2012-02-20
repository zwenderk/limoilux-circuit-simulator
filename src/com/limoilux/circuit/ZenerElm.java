
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.limoilux.circuit.core.CoreUtil;
import com.limoilux.circuit.ui.DrawUtil;

// Zener code contributed by J. Mike Rollins
// http://www.camotruck.net/rollins/simulator.html
public class ZenerElm extends DiodeElm
{

	private static final double DEFAULT_ZVOLTAGE = 5.6;
	private static final int HS = 8;

	private Polygon poly;
	private Point cathode[];
	private Point wing[];

	public ZenerElm(int xx, int yy)
	{
		super(xx, yy);
		this.zvoltage = ZenerElm.DEFAULT_ZVOLTAGE;
		this.setup();
	}

	public ZenerElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
		this.zvoltage = new Double(st.nextToken()).doubleValue();
		this.setup();
	}

	@Override
	public void setup()
	{
		this.diode.leakage = 5e-6; // 1N4004 is 5.0 uAmp
		super.setup();
	}

	@Override
	public int getDumpType()
	{
		return 'z';
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.zvoltage;
	}

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.calcLeads(16);
		this.cathode = CoreUtil.newPointArray(2);
		this.wing = CoreUtil.newPointArray(2);
		Point pa[] = CoreUtil.newPointArray(2);
		CoreUtil.interpPoint2(this.lead1, this.lead2, pa[0], pa[1], 0, ZenerElm.HS);
		CoreUtil.interpPoint2(this.lead1, this.lead2, this.cathode[0], this.cathode[1], 1, ZenerElm.HS);
		CoreUtil.interpPoint(this.cathode[0], this.cathode[1], this.wing[0], -0.2, -ZenerElm.HS);
		CoreUtil.interpPoint(this.cathode[1], this.cathode[0], this.wing[1], -0.2, -ZenerElm.HS);
		this.poly = CircuitElm.createPolygon(pa[0], pa[1], this.lead2);
	}

	@Override
	public void draw(Graphics g)
	{
		this.setBbox(this.point1, this.point2, ZenerElm.HS);

		double v1 = this.volts[0];
		double v2 = this.volts[1];

		this.draw2Leads(g);

		// draw arrow thingy
		this.setPowerColor(g, true);
		this.setVoltageColor(g, v1);
		g.fillPolygon(this.poly);

		// draw thing arrow is pointing to
		this.setVoltageColor(g, v2);
		DrawUtil.drawThickLine(g, this.cathode[0], this.cathode[1]);

		// draw wings on cathode
		DrawUtil.drawThickLine(g, this.wing[0], this.cathode[0]);
		DrawUtil.drawThickLine(g, this.wing[1], this.cathode[1]);

		this.doDots(g);
		this.drawPosts(g);
	}

	@Override
	public void getInfo(String arr[])
	{
		super.getInfo(arr);
		arr[0] = "Zener diode";
		arr[5] = "Vz = " + CircuitElm.getVoltageText(this.zvoltage);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Fwd Voltage @ 1A", this.fwdrop, 10, 1000);
		}
		if (n == 1)
		{
			return new EditInfo("Zener Voltage @ 5mA", this.zvoltage, 1, 25);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.fwdrop = ei.value;
		}
		if (n == 1)
		{
			this.zvoltage = ei.value;
		}
		this.setup();
	}
}
