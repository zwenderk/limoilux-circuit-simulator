package com.limoilux.circuit;
import java.util.Vector;

class CircuitNode
{
	int x, y;
	Vector links;
	boolean internal;

	CircuitNode()
	{
		this.links = new Vector();
	}
}
