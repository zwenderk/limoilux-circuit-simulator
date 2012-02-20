
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

class DiodeElm extends CircuitElm
{
	Diode diode;
	static final int FLAG_FWDROP = 1;
	final double defaultdrop = .805904783;
	double fwdrop, zvoltage;

	public DiodeElm(int xx, int yy)
	{
		super(xx, yy);
		this.diode = new Diode(CircuitElm.cirSim);
		this.fwdrop = this.defaultdrop;
		this.zvoltage = 0;
		this.setup();
	}

	public DiodeElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.diode = new Diode(CircuitElm.cirSim);
		this.fwdrop = this.defaultdrop;
		this.zvoltage = 0;
		if ((f & DiodeElm.FLAG_FWDROP) > 0)
		{
			try
			{
				this.fwdrop = new Double(st.nextToken()).doubleValue();
			}
			catch (Exception e)
			{
			}
		}
		this.setup();
	}

	@Override
	boolean nonLinear()
	{
		return true;
	}

	public void setup()
	{
		this.diode.setup(this.fwdrop, this.zvoltage);
	}

	@Override
	public int getDumpType()
	{
		return 'd';
	}

	@Override
	public String dump()
	{
		this.flags |= DiodeElm.FLAG_FWDROP;
		return super.dump() + " " + this.fwdrop;
	}

	final int hs = 8;
	Polygon poly;
	Point cathode[];

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.calcLeads(16);
		this.cathode = this.newPointArray(2);
		Point pa[] = this.newPointArray(2);
		this.interpPoint2(this.lead1, this.lead2, pa[0], pa[1], 0, this.hs);
		this.interpPoint2(this.lead1, this.lead2, this.cathode[0], this.cathode[1], 1, this.hs);
		this.poly = this.createPolygon(pa[0], pa[1], this.lead2);
	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDiode(g);
		this.doDots(g);
		this.drawPosts(g);
	}

	@Override
	public void reset()
	{
		this.diode.reset();
		this.volts[0] = this.volts[1] = this.curcount = 0;
	}

	void drawDiode(Graphics g)
	{
		this.setBbox(this.point1, this.point2, this.hs);

		double v1 = this.volts[0];
		double v2 = this.volts[1];

		this.draw2Leads(g);

		// draw arrow thingy
		this.setPowerColor(g, true);
		this.setVoltageColor(g, v1);
		g.fillPolygon(this.poly);

		// draw thing arrow is pointing to
		this.setVoltageColor(g, v2);
		CircuitElm.drawThickLine(g, this.cathode[0], this.cathode[1]);
	}

	@Override
	public void stamp()
	{
		this.diode.stamp(this.nodes[0], this.nodes[1]);
	}

	@Override
	public void doStep()
	{
		this.diode.doStep(this.volts[0] - this.volts[1]);
	}

	@Override
	public void calculateCurrent()
	{
		this.current = this.diode.calculateCurrent(this.volts[0] - this.volts[1]);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "diode";
		arr[1] = "I = " + CircuitElm.getCurrentText(this.getCurrent());
		arr[2] = "Vd = " + CircuitElm.getVoltageText(this.getVoltageDiff());
		arr[3] = "P = " + CircuitElm.getUnitText(this.getPower(), "W");
		arr[4] = "Vf = " + CircuitElm.getVoltageText(this.fwdrop);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Fwd Voltage @ 1A", this.fwdrop, 10, 1000);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		this.fwdrop = ei.value;
		this.setup();
	}

	@Override
	public boolean needsShortcut()
	{
		return this.getClass() == DiodeElm.class;
	}
}
