
package com.limoilux.circuit.ui.scope;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;

import javax.swing.JPanel;

import com.limoilux.circuit.core.CirSim;

public class ScopePane extends Panel
{
	private static final long serialVersionUID = 6532697895940366402L;
	private final CirSim cirSim;
	public Image scopeImg;
	
	public ScopePane(CirSim cirSim)
	{
		this.cirSim = cirSim;
		
		this.setPreferredSize(new Dimension(0, 150));
		this.setBackground(Color.GREEN);
	}
	
	@Override
	public void update(Graphics g)
	{
		System.out.println("tot");
		super.update(g);
		this.cirSim.updateCircuit(this, this.scopeImg, g);
	}
}
