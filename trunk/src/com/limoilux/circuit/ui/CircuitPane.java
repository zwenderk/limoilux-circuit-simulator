
package com.limoilux.circuit.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Panel;

import javax.swing.JPanel;

import com.limoilux.circuit.core.CirSim;

public class CircuitPane extends Panel
{
	private static final long serialVersionUID = -3418969740606491502L;

	@Deprecated
	private final CirSim cirSim;

	public CircuitPane(CirSim cirSim)
	{
		this.cirSim = cirSim;

		this.setBackground(Color.BLACK);
		this.setPreferredSize(new Dimension(555, 555));
	}

	@Override
	public void update(Graphics g)
	{
		super.update(g);
		this.cirSim.updateCircuit(g);

	}
}
