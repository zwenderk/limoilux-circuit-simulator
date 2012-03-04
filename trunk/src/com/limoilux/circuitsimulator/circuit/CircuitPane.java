
package com.limoilux.circuitsimulator.circuit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;

import javax.swing.JPanel;

import com.limoilux.circuit.techno.CircuitAnalysisException;
import com.limoilux.circuitsimulator.core.CirSim;

public class CircuitPane extends JPanel
{
	private static final long serialVersionUID = -3418969740606491502L;

	private final CirSim cirSim;
	public Image dbimage;

	public CircuitPane(CirSim cirSim)
	{
		this.cirSim = cirSim;

		this.setBackground(Color.BLACK);
		this.setForeground(Color.lightGray);

	}
	
	public void buildDBImage()
	{
		Dimension dim = this.getSize();
		this.dbimage = this.createImage(dim.width, dim.height);
	}


	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		try
		{
			this.cirSim.createCircuitImage(this.dbimage);
			
			g.drawImage(this.dbimage, 0, 0, this.cirSim.cirFrame);
		}

		catch (CircuitAnalysisException e)
		{
			this.cirSim.handleAnalysisException(e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			this.cirSim.circuit.setNeedAnalysis(true);
			
		}

	}
}
