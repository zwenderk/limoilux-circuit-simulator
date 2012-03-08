
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class DCVoltageElm extends VoltageElm
{
	public DCVoltageElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_DC);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{

		return VoltageElm.class;
	}
}
