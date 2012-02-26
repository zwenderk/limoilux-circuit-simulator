
package com.limoilux.circuit.ui;

import com.limoilux.circuit.techno.CircuitElm;

public class CircuitNodeLink
{
	public final int num;
	public final CircuitElm elm;

	public CircuitNodeLink(int num, CircuitElm elm)
	{
		this.num = num;
		this.elm = elm;
	}
}