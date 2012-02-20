
package com.limoilux.circuit;

import java.awt.Font;
import java.awt.Graphics;
import java.util.StringTokenizer;

class RailElm extends VoltageElm
{
	public RailElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_DC);
	}

	RailElm(int xx, int yy, int wf)
	{
		super(xx, yy, wf);
	}

	public RailElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	final int FLAG_CLOCK = 1;

	@Override
	public int getDumpType()
	{
		return 'R';
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
		this.lead1 = CircuitElm.interpPoint(this.point1, this.point2, 1 - this.circleSize / this.dn);
	}

	@Override
	public void draw(Graphics g)
	{
		this.setBbox(this.point1, this.point2, this.circleSize);
		this.setVoltageColor(g, this.volts[0]);
		CircuitElm.drawThickLine(g, this.point1, this.lead1);
		boolean clock = this.waveform == VoltageElm.WF_SQUARE && (this.flags & this.FLAG_CLOCK) != 0;
		if (this.waveform == VoltageElm.WF_DC || this.waveform == VoltageElm.WF_VAR || clock)
		{
			Font f = new Font("SansSerif", 0, 12);
			g.setFont(f);
			g.setColor(this.needsHighlight() ? CircuitElm.selectColor : CircuitElm.whiteColor);
			this.setPowerColor(g, false);
			double v = this.getVoltage();
			String s = CircuitElm.getShortUnitText(v, "V");
			if (Math.abs(v) < 1)
			{
				s = CircuitElm.showFormat.format(v) + "V";
			}
			if (this.getVoltage() > 0)
			{
				s = "+" + s;
			}
			if (this instanceof AntennaElm)
			{
				s = "Ant";
			}
			if (clock)
			{
				s = "CLK";
			}
			this.drawCenteredText(g, s, this.x2, this.y2, true);
		}
		else
		{
			this.drawWaveform(g, this.point2);
		}
		this.drawPosts(g);
		this.curcount = CircuitElm.updateDotCount(-this.current, this.curcount);
		if (CircuitElm.cirSim.dragElm != this)
		{
			CircuitElm.drawDots(g, this.point1, this.lead1, this.curcount);
		}
	}

	@Override
	public double getVoltageDiff()
	{
		return this.volts[0];
	}

	@Override
	public void stamp()
	{
		if (this.waveform == VoltageElm.WF_DC)
		{
			CircuitElm.cirSim.stampVoltageSource(0, this.nodes[0], this.voltSource, this.getVoltage());
		}
		else
		{
			CircuitElm.cirSim.stampVoltageSource(0, this.nodes[0], this.voltSource);
		}
	}

	@Override
	public void doStep()
	{
		if (this.waveform != VoltageElm.WF_DC)
		{
			CircuitElm.cirSim.updateVoltageSource(0, this.nodes[0], this.voltSource, this.getVoltage());
		}
	}

	@Override
	public boolean hasGroundConnection(int n1)
	{
		return true;
	}
}
