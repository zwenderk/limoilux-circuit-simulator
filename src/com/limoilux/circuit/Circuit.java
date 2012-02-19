
package com.limoilux.circuit;

// Circuit.java (c) 2005,2008 by Paul Falstad, www.falstad.com

import java.awt.*;
import java.applet.Applet;
import java.awt.event.*;

public class Circuit extends Applet implements ComponentListener
{
	static CirSim circuitSim;
	boolean started = false;

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

	public static void main(String args[])
	{
		Circuit.circuitSim = new CirSim(null);
		Circuit.circuitSim.init();
	}

	void showFrame()
	{
		if (circuitSim == null)
		{
			this.started = true;
			Circuit.circuitSim = new CirSim(this);
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
}
