package com.limoilux.circuit;

public class ACRailElm extends RailElm
{
	public ACRailElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_AC);
	}

	@Override
	public Class<RailElm> getDumpClass()
	{
		return RailElm.class;
	}
}
