
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

import com.limoilux.circuitsimulator.circuit.CircuitElm;
import com.limoilux.circuitsimulator.ui.EditInfo;

public class OutputElm extends CircuitElm
{
	final int FLAG_VALUE = 1;

	public OutputElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public OutputElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
	}

	@Override
	public int getElementId()
	{
		return 'O';
	}

	@Override
	public int getPostCount()
	{
		return 1;
	}

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.lead1 = new Point();
	}

	@Override
	public void draw(Graphics g)
	{
		boolean selected = this.needsHighlight() || CircuitElm.cirSim.plotYElm == this;
		Font f = new Font("SansSerif", selected ? Font.BOLD : 0, 14);
		g.setFont(f);
		g.setColor(selected ? CircuitElm.SELECT_COLOR : CircuitElm.WHITE_COLOR);
		String s = (this.flags & this.FLAG_VALUE) != 0 ? CircuitElm.getVoltageText(this.volts[0]) : "out";
		FontMetrics fm = g.getFontMetrics();
		if (this == CircuitElm.cirSim.mouseMan.plotXElm)
		{
			s = "X";
		}
		if (this == CircuitElm.cirSim.plotYElm)
		{
			s = "Y";
		}
		CircuitElm.interpPoint(this.point1, this.point2, this.lead1, 1 - (fm.stringWidth(s) / 2 + 8) / this.dn);
		this.setBbox(this.point1, this.lead1, 0);
		this.drawCenteredText(g, s, this.x2, this.y2, true);
		this.setVoltageColor(g, this.volts[0]);
		if (selected)
		{
			g.setColor(CircuitElm.SELECT_COLOR);
		}
		CircuitElm.drawThickLine(g, this.point1, this.lead1);
		this.drawPosts(g);
	}

	@Override
	public double getVoltageDiff()
	{
		return this.volts[0];
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "output";
		arr[1] = "V = " + CircuitElm.getVoltageText(this.volts[0]);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Show Voltage", (this.flags & this.FLAG_VALUE) != 0);
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.flags = ei.checkbox.getState() ? this.flags | this.FLAG_VALUE : this.flags & ~this.FLAG_VALUE;
		}
	}
}
