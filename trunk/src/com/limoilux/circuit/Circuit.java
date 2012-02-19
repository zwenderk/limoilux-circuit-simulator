
package com.limoilux.circuit;

// Circuit.java (c) 2005,2008 by Paul Falstad, www.falstad.com

import java.awt.*;
import java.applet.Applet;
import java.awt.event.*;

public class Circuit extends Applet implements ComponentListener
{
	private static CirSim circuitSim;
	private ComponentListener localListener;
	boolean started = false;
	
	public Circuit()
	{
		this.localListener = new localListener();
	}

	void destroyFrame()
	{
		if (Circuit.circuitSim != null)
		{
			Circuit.circuitSim.dispose();
		}
		
		Circuit.circuitSim = null;
		
		this.repaint();
	}

	@Override
	public void init()
	{
		this.addComponentListener(this);
	}


	void showFrame()
	{
		if (Circuit.circuitSim == null)
		{
			this.started = true;
			Circuit.circuitSim = new CirSim(this, null);
			Circuit.circuitSim.init();
			this.repaint();
		}
	}

	public void toggleSwitch(int x)
	{
		Circuit.circuitSim.toggleSwitch(x);
	}

	@Override
	public void paint(Graphics g)
	{
		String message = "Applet is open in a separate window.";
		
		if (!this.started)
		{
			message = "Applet is starting.";
		}
		else if (circuitSim == null)
		{
			message = "Applet is finished.";
		}
		else if (Circuit.circuitSim.useFrame)
		{
			Circuit.circuitSim.triggerShow();
		}

		g.drawString(message, 10, 30);
	}

	@Override
	public void componentHidden(ComponentEvent e)
	{
	}

	@Override
	public void componentMoved(ComponentEvent e)
	{
	}

	@Override
	public void componentShown(ComponentEvent e)
	{
		showFrame();
	}

	@Override
	public void componentResized(ComponentEvent e)
	{
		if (Circuit.circuitSim != null)
			Circuit.circuitSim.componentResized(e);
	}

	@Override
	public void destroy()
	{
		if (Circuit.circuitSim != null)
		{
			Circuit.circuitSim.dispose();
		}

		Circuit.circuitSim = null;
		this.repaint();
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public void componentShown(ComponentEvent e)
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	public static void main(String args[])
	{
		Circuit.circuitSim = new CirSim(null, null);
		Circuit.circuitSim.init();
	}
}
