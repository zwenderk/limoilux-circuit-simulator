package com.limoilux.circuit;
// Circuit.java (c) 2005,2008 by Paul Falstad, www.falstad.com

import java.awt.*;
import java.applet.Applet;
import java.awt.event.*;

public class Circuit extends Applet implements ComponentListener
{
	static CirSim ogf;

	void destroyFrame()
	{
		if (ogf != null)
			ogf.dispose();
		ogf = null;
		repaint();
	}

	boolean started = false;

	@Override
	public void init()
	{
		addComponentListener(this);
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
			started = true;
			ogf = new CirSim(this);
			ogf.init();
			repaint();
		}
	}

	public void toggleSwitch(int x)
	{
		ogf.toggleSwitch(x);
	}

	@Override
	public void paint(Graphics g)
	{
		String s = "Applet is open in a separate window.";
		if (!started)
			s = "Applet is starting.";
		else if (ogf == null)
			s = "Applet is finished.";
		else if (ogf.useFrame)
			ogf.triggerShow();
		g.drawString(s, 10, 30);
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
		if (ogf != null)
			ogf.componentResized(e);
	}

	@Override
	public void destroy()
	{
		if (ogf != null)
			ogf.dispose();
		ogf = null;
		repaint();
	}
};
