
package com.limoilux.circuit;

public class PJfetElm extends JfetElm
{
	public PJfetElm(int xx, int yy)
	{
		super(xx, yy, true);
	}

	@Override
	public Class getDumpClass()
	{
		return JfetElm.class;
	}
}
