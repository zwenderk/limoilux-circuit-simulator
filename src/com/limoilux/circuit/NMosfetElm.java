
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class NMosfetElm extends MosfetElm
{
	public NMosfetElm(int xx, int yy)
	{
		super(xx, yy, false);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return MosfetElm.class;
	}
}
