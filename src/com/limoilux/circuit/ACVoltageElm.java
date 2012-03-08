
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class ACVoltageElm extends VoltageElm
{
	public ACVoltageElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_AC);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return VoltageElm.class;
	}
}
