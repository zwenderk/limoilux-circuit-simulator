package com.limoilux.circuit.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class CircuitFrame extends JFrame
{
	private CircuitPane circuitPanel;
	
	public CircuitFrame(CircuitPane circuitPanel)
	{
		super("Limoilux Circuit Simulator v1.1");
		

		this.setLayout(new BorderLayout());
		this.circuitPanel = circuitPanel;

		this.setSize(860, 640);
		
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);

		this.circuitPanel.repaint();
	}
	
	public void add(Component c,Object i)
	{
		System.out.println(c);
		 super.add(c,i);
		
	}

	public Component add(Component c)
	{
		System.out.println(c);
		return super.add(c);
		
	}


}
