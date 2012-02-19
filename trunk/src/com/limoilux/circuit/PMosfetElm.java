package com.limoilux.circuit;
class PMosfetElm extends MosfetElm
{
	public PMosfetElm(int xx, int yy)
	{
		super(xx, yy, true);
	}

	public Class getDumpClass()
	{
		return MosfetElm.class;
	}
}
