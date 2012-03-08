
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class PMosfetElm extends MosfetElm
{
	public PMosfetElm(int xx, int yy)
	{
		super(xx, yy, true);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return MosfetElm.class;
	}
}
