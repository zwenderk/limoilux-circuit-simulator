
package com.limoilux.circuit.ui;

import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JFrame;

public class CircuitFrame extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2501086205361457967L;
	private CircuitPane circuitPanel;

	public CircuitFrame(CircuitPane circuitPanel)
	{
		super();

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());

		this.setSize(860, 640);

		this.circuitPanel = circuitPanel;
	}

	@Override
	public void setTitle(String title)
	{
		super.setTitle("Limoilux Circuit Simulator v1.1 - " + title);
	}

	@Override
	public void paint(Graphics g)
	{
		super.paint(g);

		this.circuitPanel.repaint();
	}
}
