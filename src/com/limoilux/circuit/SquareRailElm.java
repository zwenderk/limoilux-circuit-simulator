
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class SquareRailElm extends RailElm
{
	public SquareRailElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_SQUARE);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return RailElm.class;
	}
}
