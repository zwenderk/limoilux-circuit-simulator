
package com.limoilux.circuit;

public class PushSwitchElm extends SwitchElm
{
	public PushSwitchElm(int xx, int yy)
	{
		super(xx, yy, true);
	}

	@Override
	public Class getDumpClass()
	{
		return SwitchElm.class;
	}
}
