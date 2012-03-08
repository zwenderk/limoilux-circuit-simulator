
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class NTransistorElm extends TransistorElm
{
	public NTransistorElm(int xx, int yy)
	{
		super(xx, yy, false);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return TransistorElm.class;
	}
}
