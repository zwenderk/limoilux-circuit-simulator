
package com.limoilux.circuit;

class SquareRailElm extends RailElm
{
	public SquareRailElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_SQUARE);
	}

	@Override
	public Class getDumpClass()
	{
		return RailElm.class;
	}
}
