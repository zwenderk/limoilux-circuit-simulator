
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class PJfetElm extends JfetElm
{
	public PJfetElm(int xx, int yy)
	{
		super(xx, yy, true);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return JfetElm.class;
	}
}
