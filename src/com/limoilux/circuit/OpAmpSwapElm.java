package com.limoilux.circuit;
class OpAmpSwapElm extends OpAmpElm
{
	public OpAmpSwapElm(int xx, int yy)
	{
		super(xx, yy);
		this.flags |= this.FLAG_SWAP;
	}

	@Override
	public Class getDumpClass()
	{
		return OpAmpElm.class;
	}
}
