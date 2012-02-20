
package com.limoilux.circuit;

class NMosfetElm extends MosfetElm
{
	public NMosfetElm(int xx, int yy)
	{
		super(xx, yy, false);
	}

	@Override
	public Class getDumpClass()
	{
		return MosfetElm.class;
	}
}
