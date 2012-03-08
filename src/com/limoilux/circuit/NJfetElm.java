
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.circuit.CircuitElm;

public class NJfetElm extends JfetElm
{
	public NJfetElm(int xx, int yy)
	{
		super(xx, yy, false);
	}

	@Override
	public Class<? extends CircuitElm> getDumpClass()
	{
		return JfetElm.class;
	}
}
