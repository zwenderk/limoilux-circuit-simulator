
package com.limoilux.circuit;

import java.util.StringTokenizer;

class DACElm extends ChipElm
{
	public DACElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public DACElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	String getChipName()
	{
		return "DAC";
	}

	@Override
	boolean needsBits()
	{
		return true;
	}

	@Override
	void setupPins()
	{
		this.sizeX = 2;
		this.sizeY = this.bits > 2 ? this.bits : 2;
		this.pins = new Pin[this.getPostCount()];
		int i;
		for (i = 0; i != this.bits; i++)
		{
			this.pins[i] = new Pin(this.bits - 1 - i, this.SIDE_W, "D" + i);
		}
		this.pins[this.bits] = new Pin(0, this.SIDE_E, "O");
		this.pins[this.bits].output = true;
		this.pins[this.bits + 1] = new Pin(this.sizeY - 1, this.SIDE_E, "V+");
		this.allocNodes();
	}

	@Override
	void doStep()
	{
		int ival = 0;
		int i;
		for (i = 0; i != this.bits; i++)
		{
			if (this.volts[i] > 2.5)
			{
				ival |= 1 << i;
			}
		}
		int ivalmax = (1 << this.bits) - 1;
		double v = ival * this.volts[this.bits + 1] / ivalmax;
		CircuitElm.cirSim.updateVoltageSource(0, this.nodes[this.bits], this.pins[this.bits].voltSource, v);
	}

	@Override
	int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	int getPostCount()
	{
		return this.bits + 2;
	}

	@Override
	int getDumpType()
	{
		return 166;
	}
}
