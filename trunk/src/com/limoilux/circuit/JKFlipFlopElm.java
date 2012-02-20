
package com.limoilux.circuit;

import java.util.StringTokenizer;

public class JKFlipFlopElm extends ChipElm
{
	public JKFlipFlopElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public JKFlipFlopElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
		this.pins[4].value = !this.pins[3].value;
	}

	@Override
	public String getChipName()
	{
		return "JK flip-flop";
	}

	@Override
	public void setupPins()
	{
		this.sizeX = 2;
		this.sizeY = 3;
		this.pins = new Pin[5];
		this.pins[0] = new Pin(0, this.SIDE_W, "J");
		this.pins[1] = new Pin(1, this.SIDE_W, "");
		this.pins[1].clock = true;
		this.pins[1].bubble = true;
		this.pins[2] = new Pin(2, this.SIDE_W, "K");
		this.pins[3] = new Pin(0, this.SIDE_E, "Q");
		this.pins[3].output = this.pins[3].state = true;
		this.pins[4] = new Pin(2, this.SIDE_E, "Q");
		this.pins[4].output = true;
		this.pins[4].lineOver = true;
	}

	@Override
	public int getPostCount()
	{
		return 5;
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 2;
	}

	@Override
	public void execute()
	{
		if (!this.pins[1].value && this.lastClock)
		{
			boolean q = this.pins[3].value;
			if (this.pins[0].value)
			{
				if (this.pins[2].value)
				{
					q = !q;
				}
				else
				{
					q = true;
				}
			}
			else if (this.pins[2].value)
			{
				q = false;
			}
			this.pins[3].value = q;
			this.pins[4].value = !q;
		}
		this.lastClock = this.pins[1].value;
	}

	@Override
	public int getDumpType()
	{
		return 156;
	}
}
