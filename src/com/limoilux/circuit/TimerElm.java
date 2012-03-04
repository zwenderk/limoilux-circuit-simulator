
package com.limoilux.circuit;

import java.util.StringTokenizer;

import com.limoilux.circuit.techno.CircuitAnalysisException;
import com.limoilux.circuit.techno.CircuitElm;

public class TimerElm extends ChipElm
{
	private static final int FLAG_RESET = 2;
	private static final int N_DIS = 0;
	private static final int N_TRIG = 1;
	private static final int N_THRES = 2;
	private static final int N_VIN = 3;
	private static final int N_CTL = 4;
	private static final int N_OUT = 5;
	private static final int N_RST = 6;

	private boolean setOut, out;

	@Override
	public int getDefaultFlags()
	{
		return TimerElm.FLAG_RESET;
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
		this.pins[TimerElm.N_DIS] = new Pin(1, this.SIDE_W, "dis");
		this.pins[TimerElm.N_TRIG] = new Pin(3, this.SIDE_W, "tr");
		this.pins[TimerElm.N_TRIG].lineOver = true;
		this.pins[TimerElm.N_THRES] = new Pin(4, this.SIDE_W, "th");
		this.pins[TimerElm.N_VIN] = new Pin(1, this.SIDE_N, "Vin");
		this.pins[TimerElm.N_CTL] = new Pin(1, this.SIDE_S, "ctl");
		this.pins[TimerElm.N_OUT] = new Pin(2, this.SIDE_E, "out");
		this.pins[TimerElm.N_OUT].output = this.pins[TimerElm.N_OUT].state = true;
		this.pins[TimerElm.N_RST] = new Pin(1, this.SIDE_E, "rst");
	}

	@Override
	public boolean nonLinear()
	{
		return true;
	}

	public boolean hasReset()
	{
		return (this.flags & TimerElm.FLAG_RESET) != 0;
	}

	@Override
	public void stamp()
	{
		// stamp voltage divider to put ctl pin at 2/3 V
		CircuitElm.cirSim.circuit.stampResistor(this.nodes[TimerElm.N_VIN], this.nodes[TimerElm.N_CTL], 5000);
		CircuitElm.cirSim.circuit.stampResistor(this.nodes[TimerElm.N_CTL], 0, 10000);
		// output pin
		CircuitElm.cirSim.circuit.stampVoltageSource(0, this.nodes[TimerElm.N_OUT],
				this.pins[TimerElm.N_OUT].voltSource);
		// discharge pin
		CircuitElm.cirSim.circuit.stampNonLinear(this.nodes[TimerElm.N_DIS]);
	}

	@Override
	public void calculateCurrent()
	{
		// need current for V, discharge, control; output current is
		// calculated for us, and other pins have no current
		this.pins[TimerElm.N_VIN].current = (this.volts[TimerElm.N_CTL] - this.volts[TimerElm.N_VIN]) / 5000;
		this.pins[TimerElm.N_CTL].current = -this.volts[TimerElm.N_CTL] / 10000 - this.pins[TimerElm.N_VIN].current;
		this.pins[TimerElm.N_DIS].current = !this.out && !this.setOut ? -this.volts[TimerElm.N_DIS] / 10 : 0;
	}

	@Override
	public void startIteration() throws CircuitAnalysisException
	{
		this.out = this.volts[TimerElm.N_OUT] > this.volts[TimerElm.N_VIN] / 2;
		this.setOut = false;
		// check comparators
		if (this.volts[TimerElm.N_CTL] / 2 > this.volts[TimerElm.N_TRIG])
		{
			this.setOut = this.out = true;
		}
		if (this.volts[TimerElm.N_THRES] > this.volts[TimerElm.N_CTL] || this.hasReset()
				&& this.volts[TimerElm.N_RST] < .7)
		{
			this.out = false;
		}
	}

	@Override
	public void doStep() throws CircuitAnalysisException
	{
		// if output is low, discharge pin 0. we use a small
		// resistor because it's easier, and sometimes people tie
		// the discharge pin to the trigger and threshold pins.
		// We check setOut to properly emulate the case where
		// trigger is low and threshold is high.
		if (!this.out && !this.setOut)
		{
			CircuitElm.cirSim.circuit.stampResistor(this.nodes[TimerElm.N_DIS], 0, 10);
		}
		// output
		CircuitElm.cirSim.circuit.updateVoltageSource(0, this.nodes[TimerElm.N_OUT],
				this.pins[TimerElm.N_OUT].voltSource, this.out ? this.volts[TimerElm.N_VIN] : 0);
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
	public int getElementId()
	{
		return 165;
	}
}
