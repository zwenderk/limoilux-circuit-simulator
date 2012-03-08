
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class CC2NegElm extends CC2Elm
{
	public CC2NegElm(int xx, int yy)
	{
		super(xx, yy, -1);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return CC2Elm.class;
	}
}
