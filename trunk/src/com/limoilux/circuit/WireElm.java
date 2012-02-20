
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Graphics;
import java.util.StringTokenizer;

import com.limoilux.circuit.core.CircuitElm;

public class WireElm extends CircuitElm
{
	public WireElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public WireElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
	}

	static final int FLAG_SHOWCURRENT = 1;
	static final int FLAG_SHOWVOLTAGE = 2;

	@Override
	public void draw(Graphics g)
	{
		this.setVoltageColor(g, this.volts[0]);
		CircuitElm.drawThickLine(g, this.point1, this.point2);
		this.doDots(g);
		this.setBbox(this.point1, this.point2, 3);
		if (this.mustShowCurrent())
		{
			String s = CircuitElm.getShortUnitText(Math.abs(this.getCurrent()), "A");
			this.drawValues(g, s, 4);
		}
		else if (this.mustShowVoltage())
		{
			String s = CircuitElm.getShortUnitText(this.volts[0], "V");
			this.drawValues(g, s, 4);
		}
		this.drawPosts(g);
	}

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.stampVoltageSource(this.nodes[0], this.nodes[1], this.voltSource, 0);
	}

	boolean mustShowCurrent()
	{
		return (this.flags & WireElm.FLAG_SHOWCURRENT) != 0;
	}

	boolean mustShowVoltage()
	{
		return (this.flags & WireElm.FLAG_SHOWVOLTAGE) != 0;
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "wire";
		arr[1] = "I = " + CircuitElm.getCurrentDText(this.getCurrent());
		arr[2] = "V = " + CircuitElm.getVoltageText(this.volts[0]);
	}

	@Override
	public int getDumpType()
	{
		return 'w';
	}

	@Override
	public double getPower()
	{
		return 0;
	}

	@Override
	public double getVoltageDiff()
	{
		return this.volts[0];
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
			ei.checkbox = new Checkbox("Show Current", this.mustShowCurrent());
			return ei;
		}
		if (n == 1)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Show Voltage", this.mustShowVoltage());
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			if (ei.checkbox.getState())
			{
				this.flags = WireElm.FLAG_SHOWCURRENT;
			}
			else
			{
				this.flags &= ~WireElm.FLAG_SHOWCURRENT;
			}
		}
		if (n == 1)
		{
			if (ei.checkbox.getState())
			{
				this.flags = WireElm.FLAG_SHOWVOLTAGE;
			}
			else
			{
				this.flags &= ~WireElm.FLAG_SHOWVOLTAGE;
			}
		}
	}

	@Override
	public boolean needsShortcut()
	{
		return true;
	}
}
