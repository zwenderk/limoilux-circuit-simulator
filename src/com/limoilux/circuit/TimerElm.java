
package com.limoilux.circuit;

import java.util.StringTokenizer;

class TimerElm extends ChipElm
{
	final int FLAG_RESET = 2;
	final int N_DIS = 0;
	final int N_TRIG = 1;
	final int N_THRES = 2;
	final int N_VIN = 3;
	final int N_CTL = 4;
	final int N_OUT = 5;
	final int N_RST = 6;

	@Override
	int getDefaultFlags()
	{
		return this.FLAG_RESET;
	}

	public TimerElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public TimerElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	public String getChipName()
	{
		return "555 Timer";
	}

	@Override
	public void setupPins()
	{
		this.sizeX = 3;
		this.sizeY = 5;
		this.pins = new Pin[7];
		this.pins[this.N_DIS] = new Pin(1, this.SIDE_W, "dis");
		this.pins[this.N_TRIG] = new Pin(3, this.SIDE_W, "tr");
		this.pins[this.N_TRIG].lineOver = true;
		this.pins[this.N_THRES] = new Pin(4, this.SIDE_W, "th");
		this.pins[this.N_VIN] = new Pin(1, this.SIDE_N, "Vin");
		this.pins[this.N_CTL] = new Pin(1, this.SIDE_S, "ctl");
		this.pins[this.N_OUT] = new Pin(2, this.SIDE_E, "out");
		this.pins[this.N_OUT].output = this.pins[this.N_OUT].state = true;
		this.pins[this.N_RST] = new Pin(1, this.SIDE_E, "rst");
	}

	@Override
	public boolean nonLinear()
	{
		return true;
	}

	boolean hasReset()
	{
		return (this.flags & this.FLAG_RESET) != 0;
	}

	@Override
	public void stamp()
	{
		// stamp voltage divider to put ctl pin at 2/3 V
		CircuitElm.cirSim.stampResistor(this.nodes[this.N_VIN], this.nodes[this.N_CTL], 5000);
		CircuitElm.cirSim.stampResistor(this.nodes[this.N_CTL], 0, 10000);
		// output pin
		CircuitElm.cirSim.stampVoltageSource(0, this.nodes[this.N_OUT], this.pins[this.N_OUT].voltSource);
		// discharge pin
		CircuitElm.cirSim.stampNonLinear(this.nodes[this.N_DIS]);
	}

	@Override
	public void calculateCurrent()
	{
		// need current for V, discharge, control; output current is
		// calculated for us, and other pins have no current
		this.pins[this.N_VIN].current = (this.volts[this.N_CTL] - this.volts[this.N_VIN]) / 5000;
		this.pins[this.N_CTL].current = -this.volts[this.N_CTL] / 10000 - this.pins[this.N_VIN].current;
		this.pins[this.N_DIS].current = !this.out && !this.setOut ? -this.volts[this.N_DIS] / 10 : 0;
	}

	boolean setOut, out;

	@Override
	public void startIteration()
	{
		this.out = this.volts[this.N_OUT] > this.volts[this.N_VIN] / 2;
		this.setOut = false;
		// check comparators
		if (this.volts[this.N_CTL] / 2 > this.volts[this.N_TRIG])
		{
			this.setOut = this.out = true;
		}
		if (this.volts[this.N_THRES] > this.volts[this.N_CTL] || this.hasReset() && this.volts[this.N_RST] < .7)
		{
			this.out = false;
		}
	}

	@Override
	public void doStep()
	{
		// if output is low, discharge pin 0. we use a small
		// resistor because it's easier, and sometimes people tie
		// the discharge pin to the trigger and threshold pins.
		// We check setOut to properly emulate the case where
		// trigger is low and threshold is high.
		if (!this.out && !this.setOut)
		{
			CircuitElm.cirSim.stampResistor(this.nodes[this.N_DIS], 0, 10);
		}
		// output
		CircuitElm.cirSim.updateVoltageSource(0, this.nodes[this.N_OUT], this.pins[this.N_OUT].voltSource,
				this.out ? this.volts[this.N_VIN] : 0);
	}

	@Override
	public int getPostCount()
	{
		return this.hasReset() ? 7 : 6;
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	public int getDumpType()
	{
		return 165;
	}
}
