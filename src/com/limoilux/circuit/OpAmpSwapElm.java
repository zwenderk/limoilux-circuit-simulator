
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class OpAmpSwapElm extends OpAmpElm
{
	public OpAmpSwapElm(int xx, int yy)
	{
		super(xx, yy);
		this.flags |= this.FLAG_SWAP;
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return OpAmpElm.class;
	}
}
