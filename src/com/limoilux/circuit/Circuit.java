
package com.limoilux.circuit;

// Circuit.java (c) 2005,2008 by Paul Falstad, www.falstad.com

import java.awt.*;
import java.applet.Applet;
import java.awt.event.*;

public class Circuit extends Applet implements ComponentListener
{
	static CirSim ogf;
	boolean started = false;

	void destroyFrame()
	{
		if (Circuit.ogf != null)
			Circuit.ogf.dispose();
		Circuit.ogf = null;
		this.repaint();
	}

	@Override
	public void init()
	{
		this.addComponentListener(this);
	}

	public static void main(String args[])
	{
		ogf = new CirSim(null);
		ogf.init();
	}

	void showFrame()
	{
		if (ogf == null)
		{
			this.started = true;
			Circuit.ogf = new CirSim(this);
			Circuit.ogf.init();
			this.repaint();
		}
	}

	public void toggleSwitch(int x)
	{
		Circuit.ogf.toggleSwitch(x);
	}

	@Override
	public void paint(Graphics g)
	{
		String message = "Applet is open in a separate window.";
		
		if (!this.started)
		{
			message = "Applet is starting.";
		}
		else if (ogf == null)
		{
			message = "Applet is finished.";
		}
		else if (Circuit.ogf.useFrame)
		{
			Circuit.ogf.triggerShow();
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
		if (Circuit.ogf != null)
			Circuit.ogf.componentResized(e);
	}

	@Override
	public void destroy()
	{
		if (Circuit.ogf != null)
		{
			Circuit.ogf.dispose();
		}

		Circuit.ogf = null;
		this.repaint();
	}
}
