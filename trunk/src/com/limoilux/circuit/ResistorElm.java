
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuit.ui.EditInfo;
import com.limoilux.circuitsimulator.core.CircuitSimulator;
import com.limoilux.circuitsimulator.core.Configs;

public class ResistorElm extends CircuitElm
{
	public double resistance;

	public ResistorElm(int xx, int yy)
	{
		super(xx, yy);
		this.resistance = 100;
	}

	public ResistorElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.resistance = new Double(st.nextToken()).doubleValue();
	}

	@Override
	public int getElementId()
	{
		return 'r';
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.resistance;
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
		int segments = 16;
		int i;
		int ox = 0;
		int hs = Configs.EURO_RESISTOR ? 6 : 8;
		double v1 = this.volts[0];
		double v2 = this.volts[1];
		this.setBbox(this.point1, this.point2, hs);
		this.draw2Leads(g);
		this.setPowerColor(g, true);
		double segf = 1. / segments;
		if (!Configs.EURO_RESISTOR)
		{
			// draw zigzag
			for (i = 0; i != segments; i++)
			{
				int nx = 0;
				switch (i & 3)
				{
				case 0:
					nx = 1;
					break;
				case 2:
					nx = -1;
					break;
				default:
					nx = 0;
					break;
				}
				double v = v1 + (v2 - v1) * i / segments;
				this.setVoltageColor(g, v);
				CircuitElm.interpPoint(this.lead1, this.lead2, CircuitElm.ps1, i * segf, hs * ox);
				CircuitElm.interpPoint(this.lead1, this.lead2, CircuitElm.ps2, (i + 1) * segf, hs * nx);
				CircuitElm.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
				ox = nx;
			}
		}
		else
		{
			// draw rectangle
			this.setVoltageColor(g, v1);
			CircuitElm.interpPoint2(this.lead1, this.lead2, CircuitElm.ps1, CircuitElm.ps2, 0, hs);
			CircuitElm.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
			for (i = 0; i != segments; i++)
			{
				double v = v1 + (v2 - v1) * i / segments;
				this.setVoltageColor(g, v);
				CircuitElm.interpPoint2(this.lead1, this.lead2, CircuitElm.ps1, CircuitElm.ps2, i * segf, hs);
				CircuitElm.interpPoint2(this.lead1, this.lead2, this.ps3, this.ps4, (i + 1) * segf, hs);
				CircuitElm.drawThickLine(g, CircuitElm.ps1, this.ps3);
				CircuitElm.drawThickLine(g, CircuitElm.ps2, this.ps4);
			}
			CircuitElm.interpPoint2(this.lead1, this.lead2, CircuitElm.ps1, CircuitElm.ps2, 1, hs);
			CircuitElm.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
		}
		if (CircuitElm.cirSim.menuMan.showValuesCheckItem.getState())
		{
			String s = CircuitElm.getShortUnitText(this.resistance, "");
			this.drawValues(g, s, hs);
		}
		this.doDots(g);
		this.drawPosts(g);
	}

	@Override
	public void calculateCurrent()
	{
		this.current = (this.volts[0] - this.volts[1]) / this.resistance;
		// System.out.print(this + " res current set to " + current + "\n");
	}

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.circuit.stampResistor(this.nodes[0], this.nodes[1], this.resistance);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "resistor";
		this.getBasicInfo(arr);
		arr[3] = "R = " + CircuitElm.getUnitText(this.resistance, CircuitSimulator.ohmString);
		arr[4] = "P = " + CircuitElm.getUnitText(this.getPower(), "W");
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		// ohmString doesn't work here on linux
		if (n == 0)
		{
			return new EditInfo("Resistance (ohms)", this.resistance, 0, 0);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (ei.value > 0)
		{
			this.resistance = ei.value;
		}
	}

	@Override
	public boolean needsShortcut()
	{
		return true;
	}
}
