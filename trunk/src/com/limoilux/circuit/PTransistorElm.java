
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class PTransistorElm extends TransistorElm
{
	public PTransistorElm(int xx, int yy)
	{
		super(xx, yy, true);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return TransistorElm.class;
	}
}
