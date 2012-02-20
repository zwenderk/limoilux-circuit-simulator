
package com.limoilux.circuit;

import java.util.StringTokenizer;

class CounterElm extends ChipElm
{
	final int FLAG_ENABLE = 2;

	public CounterElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public CounterElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	boolean needsBits()
	{
		return true;
	}

	@Override
	public String getChipName()
	{
		return "Counter";
	}

	@Override
	void setupPins()
	{
		this.sizeX = 2;
		this.sizeY = this.bits > 2 ? this.bits : 2;
		this.pins = new Pin[this.getPostCount()];
		this.pins[0] = new Pin(0, this.SIDE_W, "");
		this.pins[0].clock = true;
		this.pins[1] = new Pin(this.sizeY - 1, this.SIDE_W, "R");
		this.pins[1].bubble = true;
		int i;
		for (i = 0; i != this.bits; i++)
		{
			int ii = i + 2;
			this.pins[ii] = new Pin(i, this.SIDE_E, "Q" + (this.bits - i - 1));
			this.pins[ii].output = this.pins[ii].state = true;
		}
		if (this.hasEnable())
		{
			this.pins[this.bits + 2] = new Pin(this.sizeY - 2, this.SIDE_W, "En");
		}
		this.allocNodes();
	}

	@Override
	int getPostCount()
	{
		if (this.hasEnable())
		{
			return this.bits + 3;
		}
		return this.bits + 2;
	}

	boolean hasEnable()
	{
		return (this.flags & this.FLAG_ENABLE) != 0;
	}

	@Override
	int getVoltageSourceCount()
	{
		return this.bits;
	}

	@Override
	void execute()
	{
		boolean en = true;
		if (this.hasEnable())
		{
			en = this.pins[this.bits + 2].value;
		}
		if (this.pins[0].value && !this.lastClock && en)
		{
			int i;
			for (i = this.bits - 1; i >= 0; i--)
			{
				int ii = i + 2;
				if (!this.pins[ii].value)
				{
					this.pins[ii].value = true;
					break;
				}
				this.pins[ii].value = false;
			}
		}
		if (!this.pins[1].value)
		{
			int i;
			for (i = 0; i != this.bits; i++)
			{
				this.pins[i + 2].value = false;
			}
		}
		this.lastClock = this.pins[0].value;
	}

	@Override
	public int getDumpType()
	{
		return 164;
	}
}
