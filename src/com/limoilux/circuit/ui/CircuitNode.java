
package com.limoilux.circuit.ui;

import java.util.Vector;

public class CircuitNode
{

	public final Vector<CircuitNodeLink> links;

	public int x;
    public int y;
	
	public boolean internal;
	
	public CircuitNode()
	{
		this.links = new Vector<CircuitNodeLink> ();
	}
}
