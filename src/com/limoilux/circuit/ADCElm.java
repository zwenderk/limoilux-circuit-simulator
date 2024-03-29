
package com.limoilux.circuit;

import java.util.StringTokenizer;

public class ADCElm extends ChipElm
{
	public ADCElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public ADCElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	public String getChipName()
	{
		return "ADC";
	}

	@Override
	public boolean needsBits()
	{
		return true;
	}

	@Override
	public void setupPins()
	{
		this.sizeX = 2;
		this.sizeY = this.bits > 2 ? this.bits : 2;
		this.pins = new Pin[this.getPostCount()];
		int i;
		for (i = 0; i != this.bits; i++)
		{
			this.pins[i] = new Pin(this.bits - 1 - i, this.SIDE_E, "D" + i);
			this.pins[i].output = true;
		}
		this.pins[this.bits] = new Pin(0, this.SIDE_W, "In");
		this.pins[this.bits + 1] = new Pin(this.sizeY - 1, this.SIDE_W, "V+");
		this.allocNodes();
	}

	@Override
	public void execute()
	{
		int imax = (1 << this.bits) - 1;
		// if we round, the half-flash doesn't work
		double val = imax * this.volts[this.bits] / this.volts[this.bits + 1]; // +
		// .5;
		int ival = (int) val;
		ival = Math.min(imax, Math.max(0, ival));
		int i;
		for (i = 0; i != this.bits; i++)
		{
			this.pins[i].value = (ival & 1 << i) != 0;
		}
	}

	@Override
	public int getVoltageSourceCount()
	{
		return this.bits;
	}

	@Override
	public int getPostCount()
	{
		return this.bits + 2;
	}

	@Override
	public int getElementId()
	{
		return 167;
	}
}
