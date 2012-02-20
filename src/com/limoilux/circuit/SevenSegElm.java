
package com.limoilux.circuit;

import java.awt.Color;
import java.awt.Graphics;
import java.util.StringTokenizer;

class SevenSegElm extends ChipElm
{
	public SevenSegElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public SevenSegElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	public String getChipName()
	{
		return "7-segment driver/display";
	}

	Color darkred;

	@Override
	public void setupPins()
	{
		this.darkred = new Color(30, 0, 0);
		this.sizeX = 4;
		this.sizeY = 4;
		this.pins = new Pin[7];
		this.pins[0] = new Pin(0, this.SIDE_W, "a");
		this.pins[1] = new Pin(1, this.SIDE_W, "b");
		this.pins[2] = new Pin(2, this.SIDE_W, "c");
		this.pins[3] = new Pin(3, this.SIDE_W, "d");
		this.pins[4] = new Pin(1, this.SIDE_S, "e");
		this.pins[5] = new Pin(2, this.SIDE_S, "f");
		this.pins[6] = new Pin(3, this.SIDE_S, "g");
	}

	@Override
	public void draw(Graphics g)
	{
		this.drawChip(g);
		g.setColor(Color.red);
		int xl = this.x + this.cspc * 5;
		int yl = this.y + this.cspc;
		this.setColor(g, 0);
		CircuitElm.drawThickLine(g, xl, yl, xl + this.cspc, yl);
		this.setColor(g, 1);
		CircuitElm.drawThickLine(g, xl + this.cspc, yl, xl + this.cspc, yl + this.cspc);
		this.setColor(g, 2);
		CircuitElm.drawThickLine(g, xl + this.cspc, yl + this.cspc, xl + this.cspc, yl + this.cspc2);
		this.setColor(g, 3);
		CircuitElm.drawThickLine(g, xl, yl + this.cspc2, xl + this.cspc, yl + this.cspc2);
		this.setColor(g, 4);
		CircuitElm.drawThickLine(g, xl, yl + this.cspc, xl, yl + this.cspc2);
		this.setColor(g, 5);
		CircuitElm.drawThickLine(g, xl, yl, xl, yl + this.cspc);
		this.setColor(g, 6);
		CircuitElm.drawThickLine(g, xl, yl + this.cspc, xl + this.cspc, yl + this.cspc);
	}

	void setColor(Graphics g, int p)
	{
		g.setColor(this.pins[p].value ? Color.red : CircuitElm.cirSim.printableCheckItem.getState() ? Color.white
				: this.darkred);
	}

	@Override
	public int getPostCount()
	{
		return 7;
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 0;
	}

	@Override
	public int getDumpType()
	{
		return 157;
	}
}
