
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class ACRailElm extends RailElm
{
	public ACRailElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_AC);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return RailElm.class;
	}
}
