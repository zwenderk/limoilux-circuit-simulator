
package com.limoilux.circuit;

import java.awt.Graphics;
import java.util.StringTokenizer;

class VCOElm extends ChipElm
{
	public VCOElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public VCOElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	String getChipName()
	{
		return "VCO";
	}

	@Override
	void setupPins()
	{
		this.sizeX = 2;
		this.sizeY = 4;
		this.pins = new Pin[6];
		this.pins[0] = new Pin(0, this.SIDE_W, "Vi");
		this.pins[1] = new Pin(3, this.SIDE_W, "Vo");
		this.pins[1].output = true;
		this.pins[2] = new Pin(0, this.SIDE_E, "C");
		this.pins[3] = new Pin(1, this.SIDE_E, "C");
		this.pins[4] = new Pin(2, this.SIDE_E, "R1");
		this.pins[4].output = true;
		this.pins[5] = new Pin(3, this.SIDE_E, "R2");
		this.pins[5].output = true;
	}

	@Override
	boolean nonLinear()
	{
		return true;
	}

	@Override
	void stamp()
	{
		// output pin
		CircuitElm.cirSim.stampVoltageSource(0, this.nodes[1], this.pins[1].voltSource);
		// attach Vi to R1 pin so its current is proportional to Vi
		CircuitElm.cirSim.stampVoltageSource(this.nodes[0], this.nodes[4], this.pins[4].voltSource, 0);
		// attach 5V to R2 pin so we get a current going
		CircuitElm.cirSim.stampVoltageSource(0, this.nodes[5], this.pins[5].voltSource, 5);
		// put resistor across cap pins to give current somewhere to go
		// in case cap is not connected
		CircuitElm.cirSim.stampResistor(this.nodes[2], this.nodes[3], this.cResistance);
		CircuitElm.cirSim.stampNonLinear(this.nodes[2]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[3]);
	}

	final double cResistance = 1e6;
	double cCurrent;
	int cDir;

	@Override
	public void doStep()
	{
		double vc = this.volts[3] - this.volts[2];
		double vo = this.volts[1];
		int dir = vo < 2.5 ? 1 : -1;
		// switch direction of current through cap as we oscillate
		if (vo < 2.5 && vc > 4.5)
		{
			vo = 5;
			dir = -1;
		}
		if (vo > 2.5 && vc < .5)
		{
			vo = 0;
			dir = 1;
		}

		// generate output voltage
		CircuitElm.cirSim.updateVoltageSource(0, this.nodes[1], this.pins[1].voltSource, vo);
		// now we set the current through the cap to be equal to the
		// current through R1 and R2, so we can measure the voltage
		// across the cap
		int cur1 = CircuitElm.cirSim.nodeList.size() + this.pins[4].voltSource;
		int cur2 = CircuitElm.cirSim.nodeList.size() + this.pins[5].voltSource;
		CircuitElm.cirSim.stampMatrix(this.nodes[2], cur1, dir);
		CircuitElm.cirSim.stampMatrix(this.nodes[2], cur2, dir);
		CircuitElm.cirSim.stampMatrix(this.nodes[3], cur1, -dir);
		CircuitElm.cirSim.stampMatrix(this.nodes[3], cur2, -dir);
		this.cDir = dir;
	}

	// can't do this in calculateCurrent() because it's called before
	// we get pins[4].current and pins[5].current, which we need
	void computeCurrent()
	{
		if (this.cResistance == 0)
		{
			return;
		}
		double c = this.cDir * (this.pins[4].current + this.pins[5].current) + (this.volts[3] - this.volts[2])
				/ this.cResistance;
		this.pins[2].current = -c;
		this.pins[3].current = c;
		this.pins[0].current = -this.pins[4].current;
	}

	@Override
	public void draw(Graphics g)
	{
		this.computeCurrent();
		this.drawChip(g);
	}

	@Override
	int getPostCount()
	{
		return 6;
	}

	@Override
	int getVoltageSourceCount()
	{
		return 3;
	}

	@Override
	public int getDumpType()
	{
		return 158;
	}
}
