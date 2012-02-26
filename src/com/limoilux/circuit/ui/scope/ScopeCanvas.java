
package com.limoilux.circuit.ui.scope;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;

import com.limoilux.circuit.core.CirSim;

public class ScopeCanvas extends Canvas
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2970241997274498066L;
	@Deprecated
	private final CirSim cirSim;

	public ScopeCanvas(CirSim cirSim)
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
