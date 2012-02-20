
package com.limoilux.circuit;

// stub ThermistorElm based on SparkGapElm
// FIXME need to uncomment ThermistorElm line from CirSim.java
// FIXME need to add ThermistorElm.java to srclist

import java.awt.Graphics;
import java.awt.Label;
import java.awt.Point;
import java.awt.Scrollbar;
import java.util.StringTokenizer;

class ThermistorElm extends CircuitElm
{
	double minresistance, maxresistance;
	double resistance;
	Scrollbar slider;
	Label label;

	public ThermistorElm(int xx, int yy)
	{
		super(xx, yy);
		this.maxresistance = 1e9;
		this.minresistance = 1e3;
		this.createSlider();
	}

	public ThermistorElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.minresistance = new Double(st.nextToken()).doubleValue();
		this.maxresistance = new Double(st.nextToken()).doubleValue();
		this.createSlider();
	}

	@Override
	boolean nonLinear()
	{
		return true;
	}

	@Override
	int getDumpType()
	{
		return 188;
	}

	@Override
	String dump()
	{
		return super.dump() + " " + this.minresistance + " " + this.maxresistance;
	}

	Point ps3, ps4;

	void createSlider()
	{
		CircuitElm.cirSim.mainContainer.add(this.label = new Label("Temperature", Label.CENTER));
		int value = 50;
		CircuitElm.cirSim.mainContainer.add(this.slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101));
		CircuitElm.cirSim.mainContainer.validate();
	}

	@Override
	void setPoints()
	{
		super.setPoints();
		this.calcLeads(32);
		this.ps3 = new Point();
		this.ps4 = new Point();
	}

	@Override
	void delete()
	{
		CircuitElm.cirSim.mainContainer.remove(this.label);
		CircuitElm.cirSim.mainContainer.remove(this.slider);
	}

	@Override
	void draw(Graphics g)
	{
		int i;
		double v1 = this.volts[0];
		double v2 = this.volts[1];
		this.setBbox(this.point1, this.point2, 6);
		this.draw2Leads(g);
		// FIXME need to draw properly, see ResistorElm.java
		this.setPowerColor(g, true);
		this.doDots(g);
		this.drawPosts(g);
	}

	@Override
	void calculateCurrent()
	{
		double vd = this.volts[0] - this.volts[1];
		this.current = vd / this.resistance;
	}

	@Override
	void startIteration()
	{
		double vd = this.volts[0] - this.volts[1];
		// FIXME set resistance as appropriate, using slider.getValue()
		this.resistance = this.minresistance;
		// System.out.print(this + " res current set to " + current + "\n");
	}

	@Override
	void doStep()
	{
		CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[1], this.resistance);
	}

	@Override
	void stamp()
	{
		CircuitElm.cirSim.stampNonLinear(this.nodes[0]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[1]);
	}

	@Override
	void getInfo(String arr[])
	{
		// FIXME
		arr[0] = "spark gap";
		this.getBasicInfo(arr);
		arr[3] = "R = " + CircuitElm.getUnitText(this.resistance, CircuitElm.cirSim.ohmString);
		arr[4] = "Ron = " + CircuitElm.getUnitText(this.minresistance, CircuitElm.cirSim.ohmString);
		arr[5] = "Roff = " + CircuitElm.getUnitText(this.maxresistance, CircuitElm.cirSim.ohmString);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		// ohmString doesn't work here on linux
		if (n == 0)
		{
			return new EditInfo("Min resistance (ohms)", this.minresistance, 0, 0);
		}
		if (n == 1)
		{
			return new EditInfo("Max resistance (ohms)", this.maxresistance, 0, 0);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (ei.value > 0 && n == 0)
		{
			this.minresistance = ei.value;
		}
		if (ei.value > 0 && n == 1)
		{
			this.maxresistance = ei.value;
		}
	}
}
