
package com.limoilux.circuit;

class NTransistorElm extends TransistorElm
{
	public NTransistorElm(int xx, int yy)
	{
		super(xx, yy, false);
	}

	@Override
	public Class getDumpClass()
	{
		return TransistorElm.class;
	}
}
