
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

import com.limoilux.circuit.core.CircuitElm;
import com.limoilux.circuit.ui.DrawUtil;

public class AnalogSwitch2Elm extends AnalogSwitchElm
{
	public AnalogSwitch2Elm(int xx, int yy)
	{
		super(xx, yy);
	}

	public AnalogSwitch2Elm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	final int openhs = 16;
	Point swposts[], swpoles[], ctlPoint;

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.calcLeads(32);
		this.swposts = CircuitElm.newPointArray(2);
		this.swpoles = CircuitElm.newPointArray(2);
		CircuitElm.interpPoint2(this.lead1, this.lead2, this.swpoles[0], this.swpoles[1], 1, this.openhs);
		CircuitElm.interpPoint2(this.point1, this.point2, this.swposts[0], this.swposts[1], 1, this.openhs);
		this.ctlPoint = CircuitElm.interpPoint(this.point1, this.point2, .5, this.openhs);
	}

	@Override
	public int getPostCount()
	{
		return 4;
	}

	@Override
	public void draw(Graphics g)
	{
		this.setBbox(this.point1, this.point2, this.openhs);

		// draw first lead
		this.setVoltageColor(g, this.volts[0]);
		DrawUtil.drawThickLine(g, this.point1, this.lead1);

		// draw second lead
		this.setVoltageColor(g, this.volts[1]);
		DrawUtil.drawThickLine(g, this.swpoles[0], this.swposts[0]);

		// draw third lead
		this.setVoltageColor(g, this.volts[2]);
		DrawUtil.drawThickLine(g, this.swpoles[1], this.swposts[1]);

		// draw switch
		g.setColor(CircuitElm.lightGrayColor);
		int position = this.open ? 1 : 0;
		DrawUtil.drawThickLine(g, this.lead1, this.swpoles[position]);

		this.updateDotCount();
		DrawUtil.drawDots(g, this.point1, this.lead1, this.curcount);
		DrawUtil.drawDots(g, this.swpoles[position], this.swposts[position], this.curcount);
		this.drawPosts(g);
	}

	@Override
	public Point getPost(int n)
	{
		return n == 0 ? this.point1 : n == 3 ? this.ctlPoint : this.swposts[n - 1];
	}

	@Override
	public int getDumpType()
	{
		return 160;
	}

	@Override
	public void calculateCurrent()
	{
		if (this.open)
		{
			this.current = (this.volts[0] - this.volts[2]) / this.r_on;
		}
		else
		{
			this.current = (this.volts[0] - this.volts[1]) / this.r_on;
		}
	}

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.stampNonLinear(this.nodes[0]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[1]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[2]);
	}

	@Override
	public void doStep()
	{
		this.open = this.volts[3] < 2.5;
		if ((this.flags & this.FLAG_INVERT) != 0)
		{
			this.open = !this.open;
		}
		if (this.open)
		{
			CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[2], this.r_on);
			CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[1], this.r_off);
		}
		else
		{
			CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[1], this.r_on);
			CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[2], this.r_off);
		}
	}

	@Override
	public boolean getConnection(int n1, int n2)
	{
		if (n1 == 3 || n2 == 3)
		{
			return false;
		}
		return true;
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "analog switch (SPDT)";
		arr[1] = "I = " + CircuitElm.getCurrentDText(this.getCurrent());
	}
}
