package com.limoilux.circuit;
class ACRailElm extends RailElm
{
	public ACRailElm(int xx, int yy)
	{
		super(xx, yy, WF_AC);
	}

	Class getDumpClass()
	{
		return RailElm.class;
	}
}