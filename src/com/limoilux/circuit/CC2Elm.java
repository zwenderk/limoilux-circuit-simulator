
package com.limoilux.circuit;

import java.awt.Graphics;
import java.util.StringTokenizer;

import com.limoilux.circuitsimulator.circuit.CircuitElm;
import com.limoilux.circuitsimulator.core.CoreUtil;

public class CC2Elm extends ChipElm
{
	double gain;

	public CC2Elm(int xx, int yy)
	{
		super(xx, yy);
		this.gain = 1;
	}

	public CC2Elm(int xx, int yy, int g)
	{
		super(xx, yy);
		this.gain = g;
	}

	public CC2Elm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
		this.gain = new Double(st.nextToken()).doubleValue();
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.gain;
	}

	@Override
	public String getChipName()
	{
		return "CC2";
	}

	@Override
	public void setupPins()
	{
		this.sizeX = 2;
		this.sizeY = 3;
		this.pins = new Pin[3];
		this.pins[0] = new Pin(0, this.SIDE_W, "X");
		this.pins[0].output = true;
		this.pins[1] = new Pin(2, this.SIDE_W, "Y");
		this.pins[2] = new Pin(1, this.SIDE_E, "Z");
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = this.gain == 1 ? "CCII+" : "CCII-";
		arr[1] = "X,Y = " + CoreUtil.getVoltageText(this.volts[0]);
		arr[2] = "Z = " + CoreUtil.getVoltageText(this.volts[2]);
		arr[3] = "I = " + CoreUtil.getCurrentText(this.pins[0].current);
	}

	// boolean nonLinear() { return true; }
	@Override
	public void stamp()
	{
		// X voltage = Y voltage
		CircuitElm.cirSim.circuit.stampVoltageSource(0, this.nodes[0], this.pins[0].voltSource);
		CircuitElm.cirSim.circuit.stampVCVS(0, this.nodes[1], 1, this.pins[0].voltSource);
		// Z current = gain * X current
		CircuitElm.cirSim.circuit.stampCCCS(0, this.nodes[2], this.pins[0].voltSource, this.gain);
	}

	@Override
	public void draw(Graphics g)
	{
		this.pins[2].current = this.pins[0].current * this.gain;
		this.drawChip(g);
	}

	@Override
	public int getPostCount()
	{
		return 3;
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	public int getElementId()
	{
		return 179;
	}
}
