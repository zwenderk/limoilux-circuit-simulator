
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

class MemristorElm extends CircuitElm
{
	double r_on, r_off, dopeWidth, totalWidth, mobility, resistance;

	public MemristorElm(int xx, int yy)
	{
		super(xx, yy);
		this.r_on = 100;
		this.r_off = 160 * this.r_on;
		this.dopeWidth = 0;
		this.totalWidth = 10e-9; // meters
		this.mobility = 1e-10; // m^2/sV
		this.resistance = 100;
	}

	public MemristorElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.r_on = new Double(st.nextToken()).doubleValue();
		this.r_off = new Double(st.nextToken()).doubleValue();
		this.dopeWidth = new Double(st.nextToken()).doubleValue();
		this.totalWidth = new Double(st.nextToken()).doubleValue();
		this.mobility = new Double(st.nextToken()).doubleValue();
		this.resistance = 100;
	}

	@Override
	public int getDumpType()
	{
		return 'm';
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.r_on + " " + this.r_off + " " + this.dopeWidth + " " + this.totalWidth + " "
				+ this.mobility;
	}

	Point ps3, ps4;

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.calcLeads(32);
		this.ps3 = new Point();
		this.ps4 = new Point();
	}

	@Override
	public void draw(Graphics g)
	{
		int segments = 6;
		int i;
		int ox = 0;
		double v1 = this.volts[0];
		double v2 = this.volts[1];
		int hs = 2 + (int) (8 * (1 - this.dopeWidth / this.totalWidth));
		this.setBbox(this.point1, this.point2, hs);
		this.draw2Leads(g);
		this.setPowerColor(g, true);
		double segf = 1. / segments;

		// draw zigzag
		for (i = 0; i <= segments; i++)
		{
			int nx = (i & 1) == 0 ? 1 : -1;
			if (i == segments)
			{
				nx = 0;
			}
			double v = v1 + (v2 - v1) * i / segments;
			this.setVoltageColor(g, v);
			CircuitElm.interpPoint(this.lead1, this.lead2, CircuitElm.ps1, i * segf, hs * ox);
			CircuitElm.interpPoint(this.lead1, this.lead2, CircuitElm.ps2, i * segf, hs * nx);
			CircuitElm.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
			if (i == segments)
			{
				break;
			}
			CircuitElm.interpPoint(this.lead1, this.lead2, CircuitElm.ps1, (i + 1) * segf, hs * nx);
			CircuitElm.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
			ox = nx;
		}

		this.doDots(g);
		this.drawPosts(g);
	}

	@Override
	public boolean nonLinear()
	{
		return true;
	}

	@Override
	public void calculateCurrent()
	{
		this.current = (this.volts[0] - this.volts[1]) / this.resistance;
	}

	@Override
	public void reset()
	{
		this.dopeWidth = 0;
	}

	@Override
	public void startIteration()
	{
		double wd = this.dopeWidth / this.totalWidth;
		this.dopeWidth += CircuitElm.cirSim.timeStep * this.mobility * this.r_on * this.current / this.totalWidth;
		if (this.dopeWidth < 0)
		{
			this.dopeWidth = 0;
		}
		if (this.dopeWidth > this.totalWidth)
		{
			this.dopeWidth = this.totalWidth;
		}
		this.resistance = this.r_on * wd + this.r_off * (1 - wd);
	}

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.stampNonLinear(this.nodes[0]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[1]);
	}

	@Override
	public void doStep()
	{
		CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[1], this.resistance);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "memristor";
		this.getBasicInfo(arr);
		arr[3] = "R = " + CircuitElm.getUnitText(this.resistance, CirSim.ohmString);
		arr[4] = "P = " + CircuitElm.getUnitText(this.getPower(), "W");
	}

	@Override
	double getScopeValue(int x)
	{
		return x == 2 ? this.resistance : x == 1 ? this.getPower() : this.getVoltageDiff();
	}

	@Override
	String getScopeUnits(int x)
	{
		return x == 2 ? CirSim.ohmString : x == 1 ? "W" : "V";
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Max Resistance (ohms)", this.r_on, 0, 0);
		}
		if (n == 1)
		{
			return new EditInfo("Min Resistance (ohms)", this.r_off, 0, 0);
		}
		if (n == 2)
		{
			return new EditInfo("Width of Doped Region (nm)", this.dopeWidth * 1e9, 0, 0);
		}
		if (n == 3)
		{
			return new EditInfo("Total Width (nm)", this.totalWidth * 1e9, 0, 0);
		}
		if (n == 4)
		{
			return new EditInfo("Mobility (um^2/(s*V))", this.mobility * 1e12, 0, 0);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.r_on = ei.value;
		}
		if (n == 1)
		{
			this.r_off = ei.value;
		}
		if (n == 2)
		{
			this.dopeWidth = ei.value * 1e-9;
		}
		if (n == 3)
		{
			this.totalWidth = ei.value * 1e-9;
		}
		if (n == 4)
		{
			this.mobility = ei.value * 1e-12;
		}
	}
}
