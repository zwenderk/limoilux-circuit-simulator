
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class PushSwitchElm extends SwitchElm
{
	public PushSwitchElm(int xx, int yy)
	{
		super(xx, yy, true);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return SwitchElm.class;
	}
}
