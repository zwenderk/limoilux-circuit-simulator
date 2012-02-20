
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.util.StringTokenizer;

class DFlipFlopElm extends ChipElm
{
	final int FLAG_RESET = 2;

	boolean hasReset()
	{
		return (this.flags & this.FLAG_RESET) != 0;
	}

	public DFlipFlopElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public DFlipFlopElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
		this.pins[2].value = !this.pins[1].value;
	}

	@Override
	String getChipName()
	{
		return "D flip-flop";
	}

	@Override
	void setupPins()
	{
		this.sizeX = 2;
		this.sizeY = 3;
		this.pins = new Pin[this.getPostCount()];
		this.pins[0] = new Pin(0, this.SIDE_W, "D");
		this.pins[1] = new Pin(0, this.SIDE_E, "Q");
		this.pins[1].output = this.pins[1].state = true;
		this.pins[2] = new Pin(2, this.SIDE_E, "Q");
		this.pins[2].output = true;
		this.pins[2].lineOver = true;
		this.pins[3] = new Pin(1, this.SIDE_W, "");
		this.pins[3].clock = true;
		if (this.hasReset())
		{
			this.pins[4] = new Pin(2, this.SIDE_W, "R");
		}
	}

	@Override
	int getPostCount()
	{
		return this.hasReset() ? 5 : 4;
	}

	@Override
	int getVoltageSourceCount()
	{
		return 2;
	}

	@Override
	void execute()
	{
		if (this.pins[3].value && !this.lastClock)
		{
			this.pins[1].value = this.pins[0].value;
			this.pins[2].value = !this.pins[0].value;
		}
		if (this.pins.length > 4 && this.pins[4].value)
		{
			this.pins[1].value = false;
			this.pins[2].value = true;
		}
		this.lastClock = this.pins[3].value;
	}

	@Override
	public int getDumpType()
	{
		return 155;
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 2)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Reset Pin", this.hasReset());
			return ei;
		}
		return super.getEditInfo(n);
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 2)
		{
			if (ei.checkbox.getState())
			{
				this.flags |= this.FLAG_RESET;
			}
			else
			{
				this.flags &= ~this.FLAG_RESET;
			}
			this.setupPins();
			this.allocNodes();
			this.setPoints();
		}
		super.setEditValue(n, ei);
	}
}
