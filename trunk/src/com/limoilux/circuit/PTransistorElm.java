package com.limoilux.circuit;
class PTransistorElm extends TransistorElm
{
	public PTransistorElm(int xx, int yy)
	{
		super(xx, yy, true);
	}

	@Override
	public Class getDumpClass()
	{
		return TransistorElm.class;
	}
}
