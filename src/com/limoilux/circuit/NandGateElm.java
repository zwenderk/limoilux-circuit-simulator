
package com.limoilux.circuit;

import java.util.StringTokenizer;

public class NandGateElm extends AndGateElm
{
	public NandGateElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public NandGateElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	public boolean isInverting()
	{
		return true;
	}

	@Override
	public String getGateName()
	{
		return "NAND gate";
	}

	@Override
	public int getElementId()
	{
		return 151;
	}
}
