
package com.limoilux.circuit.ui;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Panel;

import com.limoilux.circuit.core.CirSim;

public class CircuitPane extends Panel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3418969740606491502L;

	@Deprecated
	private final CirSim cirSim;

	public CircuitPane(CirSim cirSim)
	{
		this.cirSim = cirSim;
		
		this.setPreferredSize(new Dimension(350, 450));
	}

	@Override
	public void update(Graphics g)
	{
		this.cirSim.updateCircuit(g);
	}

	@Override
	public void paintComponents(Graphics g)
	{
		super.paintComponents(g);
		
		this.cirSim.updateCircuit(g);
	}
}
