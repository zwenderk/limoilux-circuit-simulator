
package com.limoilux.circuitsimulator.circuit;

import java.util.Vector;

public class CircuitNode
{
	private final Vector<CircuitNodeLink> links;
	private final boolean internal;

	public int x;
	public int y;

	public CircuitNode(boolean internal)
	{
		this.links = new Vector<CircuitNodeLink>();
		this.internal = internal;
	}

	public CircuitNode()
	{
		this(false);
	}

	public int getSize()
	{
		return this.links.size();
	}

	public CircuitNodeLink elementAt(int n)
	{
		return this.links.elementAt(n);
	}

	public void addElement(CircuitNodeLink nodeLink)
	{
		this.links.addElement(nodeLink);
	}

	public boolean isInternal()
	{
		return this.internal;
	}
}
