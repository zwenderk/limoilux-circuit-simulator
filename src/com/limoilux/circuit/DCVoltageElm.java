
package com.limoilux.circuit;

public class DCVoltageElm extends VoltageElm
{
	public DCVoltageElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_DC);
	}

	@Override
	public Class getDumpClass()
	{

		return VoltageElm.class;
	}
}
