package com.limoilux.circuit;
// stub PhotoResistorElm based on SparkGapElm
// FIXME need to uncomment PhotoResistorElm line from CirSim.java
// FIXME need to add PhotoResistorElm.java to srclist

import java.awt.Graphics;
import java.awt.Label;
import java.awt.Point;
import java.awt.Scrollbar;
import java.util.StringTokenizer;

class PhotoResistorElm extends CircuitElm
{
	double minresistance, maxresistance;
	double resistance;
	Scrollbar slider;
	Label label;

	public PhotoResistorElm(int xx, int yy)
	{
		super(xx, yy);
		this.maxresistance = 1e9;
		this.minresistance = 1e3;
		this.createSlider();
	}

	public PhotoResistorElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
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
		return 186;
	}

	@Override
	String dump()
	{
		return super.dump() + " " + this.minresistance + " " + this.maxresistance;
	}

	Point ps3, ps4;

	void createSlider()
	{
		CircuitElm.sim.mainContainer.add(this.label = new Label("Light Level", Label.CENTER));
		int value = 50;
		CircuitElm.sim.mainContainer.add(this.slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101));
		CircuitElm.sim.mainContainer.validate();
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
		CircuitElm.sim.mainContainer.remove(this.label);
		CircuitElm.sim.mainContainer.remove(this.slider);
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
		CircuitElm.sim.stampResistor(this.nodes[0], this.nodes[1], this.resistance);
	}

	@Override
	void stamp()
	{
		CircuitElm.sim.stampNonLinear(this.nodes[0]);
		CircuitElm.sim.stampNonLinear(this.nodes[1]);
	}

	@Override
	void getInfo(String arr[])
	{
		// FIXME
		arr[0] = "spark gap";
		this.getBasicInfo(arr);
		arr[3] = "R = " + CircuitElm.getUnitText(this.resistance, CircuitElm.sim.ohmString);
		arr[4] = "Ron = " + CircuitElm.getUnitText(this.minresistance, CircuitElm.sim.ohmString);
		arr[5] = "Roff = " + CircuitElm.getUnitText(this.maxresistance, CircuitElm.sim.ohmString);
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
