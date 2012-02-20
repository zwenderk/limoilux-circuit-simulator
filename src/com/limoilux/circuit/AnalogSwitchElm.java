
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

class AnalogSwitchElm extends CircuitElm
{
	final int FLAG_INVERT = 1;
	double resistance, r_on, r_off;

	public AnalogSwitchElm(int xx, int yy)
	{
		super(xx, yy);
		this.r_on = 20;
		this.r_off = 1e10;
	}

	public AnalogSwitchElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.r_on = 20;
		this.r_off = 1e10;
		try
		{
			this.r_on = new Double(st.nextToken()).doubleValue();
			this.r_off = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
		}

	}

	@Override
	String dump()
	{
		return super.dump() + " " + this.r_on + " " + this.r_off;
	}

	@Override
	int getDumpType()
	{
		return 159;
	}

	boolean open;

	Point ps, point3, lead3;

	@Override
	void setPoints()
	{
		super.setPoints();
		this.calcLeads(32);
		this.ps = new Point();
		int openhs = 16;
		this.point3 = this.interpPoint(this.point1, this.point2, .5, -openhs);
		this.lead3 = this.interpPoint(this.point1, this.point2, .5, -openhs / 2);
	}

	@Override
	void draw(Graphics g)
	{
		int openhs = 16;
		int hs = this.open ? openhs : 0;
		this.setBbox(this.point1, this.point2, openhs);

		this.draw2Leads(g);

		g.setColor(CircuitElm.lightGrayColor);
		this.interpPoint(this.lead1, this.lead2, this.ps, 1, hs);
		CircuitElm.drawThickLine(g, this.lead1, this.ps);

		this.setVoltageColor(g, this.volts[2]);
		CircuitElm.drawThickLine(g, this.point3, this.lead3);

		if (!this.open)
		{
			this.doDots(g);
		}
		this.drawPosts(g);
	}

	@Override
	void calculateCurrent()
	{
		this.current = (this.volts[0] - this.volts[1]) / this.resistance;
	}

	// we need this to be able to change the matrix for each step
	@Override
	boolean nonLinear()
	{
		return true;
	}

	@Override
	void stamp()
	{
		CircuitElm.cirSim.stampNonLinear(this.nodes[0]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[1]);
	}

	@Override
	void doStep()
	{
		this.open = this.volts[2] < 2.5;
		if ((this.flags & this.FLAG_INVERT) != 0)
		{
			this.open = !this.open;
		}
		this.resistance = this.open ? this.r_off : this.r_on;
		CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[1], this.resistance);
	}

	@Override
	void drag(int xx, int yy)
	{
		xx = CircuitElm.cirSim.snapGrid(xx);
		yy = CircuitElm.cirSim.snapGrid(yy);
		if (CircuitElm.abs(this.x - xx) < CircuitElm.abs(this.y - yy))
		{
			xx = this.x;
		}
		else
		{
			yy = this.y;
		}
		int q1 = CircuitElm.abs(this.x - xx) + CircuitElm.abs(this.y - yy);
		int q2 = q1 / 2 % CircuitElm.cirSim.gridSize;
		if (q2 != 0)
		{
			return;
		}
		this.x2 = xx;
		this.y2 = yy;
		this.setPoints();
	}

	@Override
	int getPostCount()
	{
		return 3;
	}

	@Override
	Point getPost(int n)
	{
		return n == 0 ? this.point1 : n == 1 ? this.point2 : this.point3;
	}

	@Override
	void getInfo(String arr[])
	{
		arr[0] = "analog switch";
		arr[1] = this.open ? "open" : "closed";
		arr[2] = "Vd = " + CircuitElm.getVoltageDText(this.getVoltageDiff());
		arr[3] = "I = " + CircuitElm.getCurrentDText(this.getCurrent());
		arr[4] = "Vc = " + CircuitElm.getVoltageText(this.volts[2]);
	}

	// we have to just assume current will flow either way, even though that
	// might cause singular matrix errors
	@Override
	boolean getConnection(int n1, int n2)
	{
		if (n1 == 2 || n2 == 2)
		{
			return false;
		}
		return true;
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Normally closed", (this.flags & this.FLAG_INVERT) != 0);
			return ei;
		}
		if (n == 1)
		{
			return new EditInfo("On Resistance (ohms)", this.r_on, 0, 0);
		}
		if (n == 2)
		{
			return new EditInfo("Off Resistance (ohms)", this.r_off, 0, 0);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.flags = ei.checkbox.getState() ? this.flags | this.FLAG_INVERT : this.flags & ~this.FLAG_INVERT;
		}
		if (n == 1 && ei.value > 0)
		{
			this.r_on = ei.value;
		}
		if (n == 2 && ei.value > 0)
		{
			this.r_off = ei.value;
		}
	}
}
