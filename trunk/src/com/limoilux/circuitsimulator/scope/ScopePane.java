
package com.limoilux.circuitsimulator.scope;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

public class ScopePane extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6532697895940366402L;

	public ScopePane()
	{
		this.setPreferredSize(new Dimension(0, 150));
		this.setBackground(Color.GREEN);
	}
}
