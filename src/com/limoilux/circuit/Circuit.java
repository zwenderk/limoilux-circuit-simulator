
package com.limoilux.circuit;

// Circuit.java (c) 2005,2008 by Paul Falstad, www.falstad.com

import java.awt.*;
import java.applet.Applet;
import java.awt.event.*;

public class Circuit
{
	private static CirSim circuitSim;

	private ComponentListener localListener;
	private boolean started = false;

	public Circuit()
	{
		Circuit.circuitSim = new CirSim(null);

		this.localListener = new localListener();
		this.showFrame();
	}

	void destroyFrame()
	{
		if (Circuit.circuitSim != null)
		{
			Circuit.circuitSim.dispose();
		}

		Circuit.circuitSim = null;

		Circuit.circuitSim.repaint();
	}

	
	void showFrame()
	{
		if (Circuit.circuitSim == null)
		{
			this.started = true;
			Circuit.circuitSim.repaint();
		}
	}

	public void toggleSwitch(int x)
	{
		Circuit.circuitSim.toggleSwitch(x);
	}



	
	private class localListener implements ComponentListener
	{
		@Override
		public void componentHidden(ComponentEvent e)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void componentMoved(ComponentEvent e)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void componentResized(ComponentEvent e)
		{
			if (Circuit.circuitSim != null)
				Circuit.circuitSim.componentResized(e);
		}

		@Override
		public void componentShown(ComponentEvent e)
		{
			showFrame();
		}
	}

	public static void main(String args[])
	{
		Circuit c = new Circuit();
;
	}
}
