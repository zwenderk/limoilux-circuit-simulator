
package com.limoilux.circuit;

import java.util.StringTokenizer;

public class XorGateElm extends OrGateElm
{
	public XorGateElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public XorGateElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	public String getGateName()
	{
		return "XOR gate";
	}

	@Override
	public boolean calcFunction()
	{
		int i;
		boolean f = false;
		for (i = 0; i != this.inputCount; i++)
		{
			f ^= this.getInput(i);
		}
		return f;
	}

	@Override
	public int getElementId()
	{
		return 154;
	}
}
