package com.limoilux.circuit;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;

class CircuitCanvas extends Canvas
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3418969740606491502L;
	CirSim pg;

	CircuitCanvas(CirSim p)
	{
		this.pg = p;
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(300, 400);
	}

	@Override
	public void update(Graphics g)
	{
		this.pg.updateCircuit(g);
	}

	@Override
	public void paint(Graphics g)
	{
		this.pg.updateCircuit(g);
	}
};
