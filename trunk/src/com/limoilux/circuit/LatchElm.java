package com.limoilux.circuit;
import java.util.StringTokenizer;

class LatchElm extends ChipElm
{
	public LatchElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public LatchElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	String getChipName()
	{
		return "Latch";
	}

	@Override
	boolean needsBits()
	{
		return true;
	}

	int loadPin;

	@Override
	void setupPins()
	{
		this.sizeX = 2;
		this.sizeY = this.bits + 1;
		this.pins = new Pin[this.getPostCount()];
		int i;
		for (i = 0; i != this.bits; i++)
		{
			this.pins[i] = new Pin(this.bits - 1 - i, this.SIDE_W, "I" + i);
		}
		for (i = 0; i != this.bits; i++)
		{
			this.pins[i + this.bits] = new Pin(this.bits - 1 - i, this.SIDE_E, "O");
			this.pins[i + this.bits].output = true;
		}
		this.pins[this.loadPin = this.bits * 2] = new Pin(this.bits, this.SIDE_W, "Ld");
		this.allocNodes();
	}

	boolean lastLoad = false;

	@Override
	void execute()
	{
		int i;
		if (this.pins[this.loadPin].value && !this.lastLoad)
		{
			for (i = 0; i != this.bits; i++)
			{
				this.pins[i + this.bits].value = this.pins[i].value;
			}
		}
		this.lastLoad = this.pins[this.loadPin].value;
	}

	@Override
	int getVoltageSourceCount()
	{
		return this.bits;
	}

	@Override
	int getPostCount()
	{
		return this.bits * 2 + 1;
	}

	@Override
	int getDumpType()
	{
		return 168;
	}
}
