
package com.limoilux.circuit;

import java.util.StringTokenizer;

import com.limoilux.circuit.techno.CircuitAnalysisException;
import com.limoilux.circuit.techno.CircuitElm;

public class DACElm extends ChipElm
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
	public String getChipName()
	{
		return "DAC";
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
			this.pins[i] = new Pin(this.bits - 1 - i, this.SIDE_W, "D" + i);
		}
		this.pins[this.bits] = new Pin(0, this.SIDE_E, "O");
		this.pins[this.bits].output = true;
		this.pins[this.bits + 1] = new Pin(this.sizeY - 1, this.SIDE_E, "V+");
		this.allocNodes();
	}

	@Override
	public void doStep() throws CircuitAnalysisException
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
		CircuitElm.cirSim.circuit.updateVoltageSource(0, this.nodes[this.bits], this.pins[this.bits].voltSource, v);
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	public int getPostCount()
	{
		return this.bits + 2;
	}

	@Override
	public int getElementId()
	{
		return 166;
	}
}
