
package com.limoilux.circuitsimulator.circuit;

import java.awt.Color;
import java.awt.Graphics;

import com.limoilux.circuitsimulator.core.CircuitSimulator;

public class CircuitManager
{
	public final Circuit circuit;
	public final CircuitPane circuitPanel;
	public final DumpManager dumpMan;
	public final CircuitMouseManager mouseMan;

	public String stopMessage;
	public CircuitElm stopElm;

	public CircuitManager(CircuitPane circuitPanel)
	{

		this.circuit = new Circuit();

		this.mouseMan = new CircuitMouseManager(this.circuit);
		this.circuitPanel = circuitPanel;
		this.dumpMan = new DumpManager();

		this.circuitPanel.addMouseMotionListener(this.mouseMan);
		this.circuitPanel.addMouseListener(this.mouseMan);
	}

	public void repaint()
	{
		this.circuitPanel.repaint();
	}

	public void prepareRepaint() throws CircuitAnalysisException
	{
		// Analyser le circuit si nessaire.
		if (this.circuit.needAnalysis())
		{
			try
			{
				this.stopMessage = null;
				this.stopElm = null;

				this.circuit.analyzeCircuit();
			}
			catch (CircuitAnalysisException e)
			{
				throw e;
			}
			finally
			{
				this.circuit.setNeedAnalysis(false);
			}
		}
		
		if (CircuitSimulator.editDialog != null && CircuitSimulator.editDialog.elm instanceof CircuitElm)
		{
			this.mouseMan.mouseElm = (CircuitElm) CircuitSimulator.editDialog.elm;
		}

		if (this.mouseMan.mouseElm == null)
		{
			this.mouseMan.mouseElm = this.stopElm;
		}
	}

	public Circuit getCircuit()
	{
		return this.circuit;
	}

	public int findAndDrawBadNode(Graphics g)
	{
		int badnodes = 0;

		// find bad connections, nodes not connected to other elements which
		// intersect other elements' bounding boxes
		for (int i = 0; i != this.circuit.getNodeCount(); i++)
		{
			CircuitNode cn = this.circuit.getNodeAt(i);
			if (!cn.isInternal() && cn.getSize() == 1)
			{
				int bb = 0;
				CircuitNodeLink cnl = cn.elementAt(0);
				for (int j = 0; j != this.circuit.getElementCount(); j++)
				{
					if (cnl.elm != this.circuit.getElementAt(j)
							&& this.circuit.getElementAt(j).boundingBox.contains(cn.x, cn.y))
					{
						bb++;
					}
				}

				if (bb > 0)
				{
					g.setColor(Color.red);
					g.fillOval(cn.x - 3, cn.y - 3, 7, 7);
					badnodes++;
				}
			}
		}

		return badnodes;
	}

}
