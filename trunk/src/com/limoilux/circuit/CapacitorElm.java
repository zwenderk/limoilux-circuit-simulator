
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

class CapacitorElm extends CircuitElm
{
	double capacitance;
	double compResistance, voltdiff;
	Point plate1[], plate2[];
	public static final int FLAG_BACK_EULER = 2;

	public CapacitorElm(int xx, int yy)
	{
		super(xx, yy);
		this.capacitance = 1e-5;
	}

	public CapacitorElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.capacitance = new Double(st.nextToken()).doubleValue();
		this.voltdiff = new Double(st.nextToken()).doubleValue();
	}

	public boolean isTrapezoidal()
	{
		return (this.flags & CapacitorElm.FLAG_BACK_EULER) == 0;
	}

	@Override
	public void setNodeVoltage(int n, double c)
	{
		super.setNodeVoltage(n, c);
		this.voltdiff = this.volts[0] - this.volts[1];
	}

	@Override
	public void reset()
	{
		this.current = this.curcount = 0;
		// put small charge on caps when reset to start oscillators
		this.voltdiff = 1e-3;
	}

	@Override
	public int getDumpType()
	{
		return 'c';
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.capacitance + " " + this.voltdiff;
	}

	@Override
	public void setPoints()
	{
		super.setPoints();
		double f = (this.dn / 2 - 4) / this.dn;
		// calc leads
		this.lead1 = this.interpPoint(this.point1, this.point2, f);
		this.lead2 = this.interpPoint(this.point1, this.point2, 1 - f);
		// calc plates
		this.plate1 = this.newPointArray(2);
		this.plate2 = this.newPointArray(2);
		this.interpPoint2(this.point1, this.point2, this.plate1[0], this.plate1[1], f, 12);
		this.interpPoint2(this.point1, this.point2, this.plate2[0], this.plate2[1], 1 - f, 12);
	}

	@Override
	public void draw(Graphics g)
	{
		int hs = 12;
		this.setBbox(this.point1, this.point2, hs);

		// draw first lead and plate
		this.setVoltageColor(g, this.volts[0]);
		CircuitElm.drawThickLine(g, this.point1, this.lead1);
		this.setPowerColor(g, false);
		CircuitElm.drawThickLine(g, this.plate1[0], this.plate1[1]);
		if (CircuitElm.cirSim.powerCheckItem.getState())
		{
			g.setColor(Color.gray);
		}

		// draw second lead and plate
		this.setVoltageColor(g, this.volts[1]);
		CircuitElm.drawThickLine(g, this.point2, this.lead2);
		this.setPowerColor(g, false);
		CircuitElm.drawThickLine(g, this.plate2[0], this.plate2[1]);

		this.updateDotCount();
		if (CircuitElm.cirSim.dragElm != this)
		{
			this.drawDots(g, this.point1, this.lead1, this.curcount);
			this.drawDots(g, this.point2, this.lead2, -this.curcount);
		}
		this.drawPosts(g);
		if (CircuitElm.cirSim.showValuesCheckItem.getState())
		{
			String s = CircuitElm.getShortUnitText(this.capacitance, "F");
			this.drawValues(g, s, hs);
		}
	}

	@Override
	public void stamp()
	{
		// capacitor companion model using trapezoidal approximation
		// (Norton equivalent) consists of a current source in
		// parallel with a resistor. Trapezoidal is more accurate
		// than backward euler but can cause oscillatory behavior
		// if RC is small relative to the timestep.
		if (this.isTrapezoidal())
		{
			this.compResistance = CircuitElm.cirSim.timeStep / (2 * this.capacitance);
		}
		else
		{
			this.compResistance = CircuitElm.cirSim.timeStep / this.capacitance;
		}
		CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[1], this.compResistance);
		CircuitElm.cirSim.stampRightSide(this.nodes[0]);
		CircuitElm.cirSim.stampRightSide(this.nodes[1]);
	}

	@Override
	public void startIteration()
	{
		if (this.isTrapezoidal())
		{
			this.curSourceValue = -this.voltdiff / this.compResistance - this.current;
		}
		else
		{
			this.curSourceValue = -this.voltdiff / this.compResistance;
			// System.out.println("cap " + compResistance + " " + curSourceValue
			// +
			// " " + current + " " + voltdiff);
		}
	}

	@Override
	public void calculateCurrent()
	{
		double voltdiff = this.volts[0] - this.volts[1];
		// we check compResistance because this might get called
		// before stamp(), which sets compResistance, causing
		// infinite current
		if (this.compResistance > 0)
		{
			this.current = voltdiff / this.compResistance + this.curSourceValue;
		}
	}

	double curSourceValue;

	@Override
	public void doStep()
	{
		CircuitElm.cirSim.stampCurrentSource(this.nodes[0], this.nodes[1], this.curSourceValue);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "capacitor";
		this.getBasicInfo(arr);
		arr[3] = "C = " + CircuitElm.getUnitText(this.capacitance, "F");
		arr[4] = "P = " + CircuitElm.getUnitText(this.getPower(), "W");
		// double v = getVoltageDiff();
		// arr[4] = "U = " + getUnitText(.5*capacitance*v*v, "J");
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Capacitance (F)", this.capacitance, 0, 0);
		}
		if (n == 1)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Trapezoidal Approximation", this.isTrapezoidal());
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0 && ei.value > 0)
		{
			this.capacitance = ei.value;
		}
		if (n == 1)
		{
			if (ei.checkbox.getState())
			{
				this.flags &= ~CapacitorElm.FLAG_BACK_EULER;
			}
			else
			{
				this.flags |= CapacitorElm.FLAG_BACK_EULER;
			}
		}
	}

	@Override
	public boolean needsShortcut()
	{
		return true;
	}
}
