
package com.limoilux.circuit.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;

import javax.swing.JPanel;

import com.limoilux.circuit.core.CirSim;
import com.limoilux.circuit.techno.CircuitAnalysisException;

public class CircuitPane extends JPanel
{
	private static final long serialVersionUID = -3418969740606491502L;

	@Deprecated
	private final CirSim cirSim;

	public CircuitPane(CirSim cirSim)
	{
		this.cirSim = cirSim;

		this.setBackground(Color.BLACK);

	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		try
		{
			this.cirSim.updateCircuit(g);
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
