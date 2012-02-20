
package com.limoilux.circuit;

// stub implementation of DiacElm, based on SparkGapElm
// FIXME need to add DiacElm.java to srclist
// FIXME need to uncomment DiacElm line from CirSim.java

import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

class DiacElm extends CircuitElm
{
	double onresistance, offresistance, breakdown, holdcurrent;
	boolean state;

	public DiacElm(int xx, int yy)
	{
		super(xx, yy);
		// FIXME need to adjust defaults to make sense for diac
		this.offresistance = 1e9;
		this.onresistance = 1e3;
		this.breakdown = 1e3;
		this.holdcurrent = 0.001;
		this.state = false;
	}

	public DiacElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.onresistance = new Double(st.nextToken()).doubleValue();
		this.offresistance = new Double(st.nextToken()).doubleValue();
		this.breakdown = new Double(st.nextToken()).doubleValue();
		this.holdcurrent = new Double(st.nextToken()).doubleValue();
	}

	@Override
	boolean nonLinear()
	{
		return true;
	}

	@Override
	public int getDumpType()
	{
		return 185;
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.onresistance + " " + this.offresistance + " " + this.breakdown + " "
				+ this.holdcurrent;
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
		// FIXME need to draw Diac
		int i;
		double v1 = this.volts[0];
		double v2 = this.volts[1];
		this.setBbox(this.point1, this.point2, 6);
		this.draw2Leads(g);
		this.setPowerColor(g, true);
		this.doDots(g);
		this.drawPosts(g);
	}

	@Override
	void calculateCurrent()
	{
		double vd = this.volts[0] - this.volts[1];
		if (this.state)
		{
			this.current = vd / this.onresistance;
		}
		else
		{
			this.current = vd / this.offresistance;
		}
	}

	@Override
	public void startIteration()
	{
		double vd = this.volts[0] - this.volts[1];
		if (Math.abs(this.current) < this.holdcurrent)
		{
			this.state = false;
		}
		if (Math.abs(vd) > this.breakdown)
		{
			this.state = true;
			// System.out.print(this + " res current set to " + current + "\n");
		}
	}

	@Override
	public void doStep()
	{
		if (this.state)
		{
			CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[1], this.onresistance);
		}
		else
		{
			CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[1], this.offresistance);
		}
	}

	@Override
	void stamp()
	{
		CircuitElm.cirSim.stampNonLinear(this.nodes[0]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[1]);
	}

	@Override
	public void getInfo(String arr[])
	{
		// FIXME
		arr[0] = "spark gap";
		this.getBasicInfo(arr);
		arr[3] = this.state ? "on" : "off";
		arr[4] = "Ron = " + CircuitElm.getUnitText(this.onresistance, CircuitElm.cirSim.ohmString);
		arr[5] = "Roff = " + CircuitElm.getUnitText(this.offresistance, CircuitElm.cirSim.ohmString);
		arr[6] = "Vbrkdn = " + CircuitElm.getUnitText(this.breakdown, "V");
		arr[7] = "Ihold = " + CircuitElm.getUnitText(this.holdcurrent, "A");
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("On resistance (ohms)", this.onresistance, 0, 0);
		}
		if (n == 1)
		{
			return new EditInfo("Off resistance (ohms)", this.offresistance, 0, 0);
		}
		if (n == 2)
		{
			return new EditInfo("Breakdown voltage (volts)", this.breakdown, 0, 0);
		}
		if (n == 3)
		{
			return new EditInfo("Hold current (amps)", this.holdcurrent, 0, 0);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (ei.value > 0 && n == 0)
		{
			this.onresistance = ei.value;
		}
		if (ei.value > 0 && n == 1)
		{
			this.offresistance = ei.value;
		}
		if (ei.value > 0 && n == 2)
		{
			this.breakdown = ei.value;
		}
		if (ei.value > 0 && n == 3)
		{
			this.holdcurrent = ei.value;
		}
	}

	@Override
	public boolean needsShortcut()
	{
		return false;
	}
}
