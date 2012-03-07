package com.limoilux.circuitsimulator.core;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class WindowsSim extends CircuitSimulator
{
	@Override
	protected void configForOs()
	{
		this.ctrlMetaKey = "Ctrl";
	}
	
	private class MyWindowLister implements WindowListener
	{
		@Override
		public void windowActivated(WindowEvent e)
		{

		}

		@Override
		public void windowClosed(WindowEvent e)
		{
			System.out.println("closed");
		}

		@Override
		public void windowClosing(WindowEvent e)
		{
			System.out.println("closing");
		}

		@Override
		public void windowDeactivated(WindowEvent e)
		{
		}

		@Override
		public void windowDeiconified(WindowEvent e)
		{
		}

		@Override
		public void windowIconified(WindowEvent e)
		{
		}

		@Override
		public void windowOpened(WindowEvent arg0)
		{

		}
	}
}
