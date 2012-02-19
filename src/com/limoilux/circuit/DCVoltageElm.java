package com.limoilux.circuit;
class DCVoltageElm extends VoltageElm
{
	public DCVoltageElm(int xx, int yy)
	{
		super(xx, yy, WF_DC);
	}

	public Class getDumpClass()
	{
		
		return VoltageElm.class;
	}
}
