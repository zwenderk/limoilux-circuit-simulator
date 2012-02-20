
package com.limoilux.circuit.ui;

import java.util.Vector;

public class CircuitNode
{
	private final Vector<CircuitNodeLink> links;

	public int x;
	public int y;

	public boolean internal;

	public CircuitNode()
	{
		this.links = new Vector<CircuitNodeLink>();
	}

	public int getSize()
	{
		return links.size();
	}

	public CircuitNodeLink elementAt(int n)
	{
		return this.links.elementAt(n);
	}

	public void addElement(CircuitNodeLink nodeLink)
	{
		this.links.addElement(nodeLink);
	}
}
