
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuit.ui.EditInfo;

public class CurrentElm extends CircuitElm
{
	public double currentValue;

	public CurrentElm(int xx, int yy)
	{
		super(xx, yy);
		this.currentValue = .01;
	}

	public CurrentElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		try
		{
			this.currentValue = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
			this.currentValue = .01;
		}
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.currentValue;
	}

	@Override
	public int getElementId()
	{
		return 'i';
	}

	Polygon arrow;
	Point ashaft1, ashaft2, center;

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.calcLeads(26);
		this.ashaft1 = CircuitElm.interpPoint(this.lead1, this.lead2, .25);
		this.ashaft2 = CircuitElm.interpPoint(this.lead1, this.lead2, .6);
		this.center = CircuitElm.interpPoint(this.lead1, this.lead2, .5);
		Point p2 = CircuitElm.interpPoint(this.lead1, this.lead2, .75);
		this.arrow = CircuitElm.calcArrow(this.center, p2, 4, 4);
	}

	@Override
	public void draw(Graphics g)
	{
		int cr = 12;
		this.draw2Leads(g);
		this.setVoltageColor(g, (this.volts[0] + this.volts[1]) / 2);
		this.setPowerColor(g, false);

		CircuitElm.drawThickCircle(g, this.center.x, this.center.y, cr);
		CircuitElm.drawThickLine(g, this.ashaft1, this.ashaft2);

		g.fillPolygon(this.arrow);
		this.setBbox(this.point1, this.point2, cr);
		this.doDots(g);
		if (CircuitElm.cirSim.menuMan.showValuesCheckItem.getState())
		{
			String s = CircuitElm.getShortUnitText(this.currentValue, "A");
			if (this.dx == 0 || this.dy == 0)
			{
				this.drawValues(g, s, cr);
			}
		}
		this.drawPosts(g);
	}

	@Override
	public void stamp()
	{
		this.current = this.currentValue;
		CircuitElm.cirSim.stampCurrentSource(this.nodes[0], this.nodes[1], this.current);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Current (A)", this.currentValue, 0, .1);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		this.currentValue = ei.value;
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "current source";
		this.getBasicInfo(arr);
	}

	@Override
	public double getVoltageDiff()
	{
		return this.volts[1] - this.volts[0];
	}
}
