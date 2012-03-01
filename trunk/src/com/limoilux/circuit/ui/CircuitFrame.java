package com.limoilux.circuit.ui;

import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JFrame;

public class CircuitFrame extends JFrame
{
	private CircuitPane circuitPanel;
	
	public CircuitFrame(CircuitPane circuitPanel)
	{
		super();
		

		this.setLayout(new BorderLayout());
		this.circuitPanel = circuitPanel;

		this.setSize(860, 640);
		
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
