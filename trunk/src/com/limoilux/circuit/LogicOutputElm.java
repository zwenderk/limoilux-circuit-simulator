
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Font;
import java.awt.Graphics;
import java.util.StringTokenizer;

class LogicOutputElm extends CircuitElm
{
	final int FLAG_TERNARY = 1;
	final int FLAG_NUMERIC = 2;
	final int FLAG_PULLDOWN = 4;
	double threshold;
	String value;

	public LogicOutputElm(int xx, int yy)
	{
		super(xx, yy);
		this.threshold = 2.5;
	}

	public LogicOutputElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		try
		{
			this.threshold = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
			this.threshold = 2.5;
		}
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.threshold;
	}

	@Override
	public int getDumpType()
	{
		return 'M';
	}

	@Override
	public int getPostCount()
	{
		return 1;
	}

	boolean isTernary()
	{
		return (this.flags & this.FLAG_TERNARY) != 0;
	}

	boolean isNumeric()
	{
		return (this.flags & (this.FLAG_TERNARY | this.FLAG_NUMERIC)) != 0;
	}

	boolean needsPullDown()
	{
		return (this.flags & this.FLAG_PULLDOWN) != 0;
	}

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.lead1 = this.interpPoint(this.point1, this.point2, 1 - 12 / this.dn);
	}

	@Override
	public void draw(Graphics g)
	{
		Font f = new Font("SansSerif", Font.BOLD, 20);
		g.setFont(f);
		// g.setColor(needsHighlight() ? selectColor : lightGrayColor);
		g.setColor(CircuitElm.lightGrayColor);
		String s = this.volts[0] < this.threshold ? "L" : "H";
		if (this.isTernary())
		{
			if (this.volts[0] > 3.75)
			{
				s = "2";
			}
			else if (this.volts[0] > 1.25)
			{
				s = "1";
			}
			else
			{
				s = "0";
			}
		}
		else if (this.isNumeric())
		{
			s = this.volts[0] < this.threshold ? "0" : "1";
		}
		this.value = s;
		this.setBbox(this.point1, this.lead1, 0);
		this.drawCenteredText(g, s, this.x2, this.y2, true);
		this.setVoltageColor(g, this.volts[0]);
		CircuitElm.drawThickLine(g, this.point1, this.lead1);
		this.drawPosts(g);
	}

	@Override
	void stamp()
	{
		if (this.needsPullDown())
		{
			CircuitElm.cirSim.stampResistor(this.nodes[0], 0, 1e6);
		}
	}

	@Override
	double getVoltageDiff()
	{
		return this.volts[0];
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "logic output";
		arr[1] = this.volts[0] < this.threshold ? "low" : "high";
		if (this.isNumeric())
		{
			arr[1] = this.value;
		}
		arr[2] = "V = " + CircuitElm.getVoltageText(this.volts[0]);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Threshold", this.threshold, 10, -10);
		}
		if (n == 1)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Current Required", this.needsPullDown());
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.threshold = ei.value;
		}
		if (n == 1)
		{
			if (ei.checkbox.getState())
			{
				this.flags = this.FLAG_PULLDOWN;
			}
			else
			{
				this.flags &= ~this.FLAG_PULLDOWN;
			}
		}
	}
}
