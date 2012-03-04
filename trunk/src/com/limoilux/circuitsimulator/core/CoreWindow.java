
package com.limoilux.circuitsimulator.core;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class CoreWindow extends JFrame
{
	private static final long serialVersionUID = 2501086205361457967L;

	public CoreWindow()
	{
		super();

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());

		this.setSize(860, 640);
	}

	@Override
	public void setTitle(String title)
	{
		super.setTitle("Limoilux Circuit Simulator v1.1 - " + title);
	}
}
