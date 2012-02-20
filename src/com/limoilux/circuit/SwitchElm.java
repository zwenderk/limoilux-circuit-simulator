
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

import com.limoilux.circuit.core.CircuitElm;

public class SwitchElm extends CircuitElm
{
	public boolean momentary;
	// position 0 == closed, position 1 == open
	int position, posCount;

	public SwitchElm(int xx, int yy)
	{
		super(xx, yy);
		this.momentary = false;
		this.position = 0;
		this.posCount = 2;
	}

	SwitchElm(int xx, int yy, boolean mm)
	{
		super(xx, yy);
		this.position = mm ? 1 : 0;
		this.momentary = mm;
		this.posCount = 2;
	}

	public SwitchElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		String str = st.nextToken();
		if (str.compareTo("true") == 0)
		{
			this.position = this instanceof LogicInputElm ? 0 : 1;
		}
		else if (str.compareTo("false") == 0)
		{
			this.position = this instanceof LogicInputElm ? 1 : 0;
		}
		else
		{
			this.position = new Integer(str).intValue();
		}
		this.momentary = new Boolean(st.nextToken()).booleanValue();
		this.posCount = 2;
	}

	@Override
	public int getDumpType()
	{
		return 's';
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.position + " " + this.momentary;
	}

	Point ps, ps2;

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.calcLeads(32);
		this.ps = new Point();
		this.ps2 = new Point();
	}

	@Override
	public void draw(Graphics g)
	{
		int openhs = 16;
		int hs1 = this.position == 1 ? 0 : 2;
		int hs2 = this.position == 1 ? openhs : 2;
		this.setBbox(this.point1, this.point2, openhs);

		this.draw2Leads(g);

		if (this.position == 0)
		{
			this.doDots(g);
		}

		if (!this.needsHighlight())
		{
			g.setColor(CircuitElm.whiteColor);
		}
		CircuitElm.interpPoint(this.lead1, this.lead2, this.ps, 0, hs1);
		CircuitElm.interpPoint(this.lead1, this.lead2, this.ps2, 1, hs2);

		CircuitElm.drawThickLine(g, this.ps, this.ps2);
		this.drawPosts(g);
	}

	@Override
	public void calculateCurrent()
	{
		if (this.position == 1)
		{
			this.current = 0;
		}
	}

	@Override
	public void stamp()
	{
		if (this.position == 0)
		{
			CircuitElm.cirSim.stampVoltageSource(this.nodes[0], this.nodes[1], this.voltSource, 0);
		}
	}

	@Override
	public int getVoltageSourceCount()
	{
		return this.position == 1 ? 0 : 1;
	}

	public void mouseUp()
	{
		if (this.momentary)
		{
			this.toggle();
		}
	}

	public void toggle()
	{
		this.position++;
		if (this.position >= this.posCount)
		{
			this.position = 0;
		}
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = this.momentary ? "push switch (SPST)" : "switch (SPST)";
		if (this.position == 1)
		{
			arr[1] = "open";
			arr[2] = "Vd = " + CircuitElm.getVoltageDText(this.getVoltageDiff());
		}
		else
		{
			arr[1] = "closed";
			arr[2] = "V = " + CircuitElm.getVoltageText(this.volts[0]);
			arr[3] = "I = " + CircuitElm.getCurrentDText(this.getCurrent());
		}
	}

	@Override
	public boolean getConnection(int n1, int n2)
	{
		return this.position == 0;
	}

	@Override
	public boolean isWire()
	{
		return true;
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Momentary Switch", this.momentary);
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.momentary = ei.checkbox.getState();
		}
	}
}
