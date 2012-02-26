
package com.limoilux.circuit.ui;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;

import com.limoilux.circuit.core.CirSim;

public class CircuitCanvas extends Canvas
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3418969740606491502L;

	@Deprecated
	private final CirSim cirSim;

	public CircuitCanvas(CirSim cirSim)
	{
		this.cirSim = cirSim;
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(350, 450);
	}

	@Override
	public void update(Graphics g)
	{
		this.cirSim.updateCircuit(g);
	}

	@Override
	public void paint(Graphics g)
	{
		this.cirSim.updateCircuit(g);
	}
}
