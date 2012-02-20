
package com.limoilux.circuit;

import java.util.Vector;

public class CircuitNode
{
	public int x, y;
	public Vector links;
	public boolean internal;

	public CircuitNode()
	{
		this.links = new Vector();
	}
}
