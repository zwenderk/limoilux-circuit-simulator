package com.limoilux.circuit;
import java.util.StringTokenizer;

class DecadeElm extends ChipElm
{
	public DecadeElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public DecadeElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	String getChipName()
	{
		return "decade counter";
	}

	@Override
	boolean needsBits()
	{
		return true;
	}

	@Override
	void setupPins()
	{
		this.sizeX = this.bits > 2 ? this.bits : 2;
		this.sizeY = 2;
		this.pins = new Pin[this.getPostCount()];
		this.pins[0] = new Pin(1, this.SIDE_W, "");
		this.pins[0].clock = true;
		this.pins[1] = new Pin(this.sizeX - 1, this.SIDE_S, "R");
		this.pins[1].bubble = true;
		int i;
		for (i = 0; i != this.bits; i++)
		{
			int ii = i + 2;
			this.pins[ii] = new Pin(i, this.SIDE_N, "Q" + i);
			this.pins[ii].output = this.pins[ii].state = true;
		}
		this.allocNodes();
	}

	@Override
	int getPostCount()
	{
		return this.bits + 2;
	}

	@Override
	int getVoltageSourceCount()
	{
		return this.bits;
	}

	@Override
	void execute()
	{
		int i;
		if (this.pins[0].value && !this.lastClock)
		{
			for (i = 0; i != this.bits; i++)
			{
				if (this.pins[i + 2].value)
				{
					break;
				}
			}
			if (i < this.bits)
			{
				this.pins[i++ + 2].value = false;
			}
			i %= this.bits;
			this.pins[i + 2].value = true;
		}
		if (!this.pins[1].value)
		{
			for (i = 1; i != this.bits; i++)
			{
				this.pins[i + 2].value = false;
			}
			this.pins[2].value = true;
		}
		this.lastClock = this.pins[0].value;
	}

	@Override
	int getDumpType()
	{
		return 163;
	}
}
