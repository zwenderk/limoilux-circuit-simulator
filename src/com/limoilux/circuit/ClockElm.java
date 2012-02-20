
package com.limoilux.circuit;

public class ClockElm extends RailElm
{
	public ClockElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_SQUARE);
		this.maxVoltage = 2.5;
		this.bias = 2.5;
		this.frequency = 100;
		this.flags |= this.FLAG_CLOCK;
	}

	@Override
	public Class getDumpClass()
	{
		return RailElm.class;
	}
}
