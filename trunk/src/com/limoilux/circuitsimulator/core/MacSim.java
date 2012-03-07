package com.limoilux.circuitsimulator.core;

import com.apple.eawt.AppEvent.AppHiddenEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.AppHiddenListener;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
public class MacSim extends CircuitSimulator
{
	public MacSim()
	{
		super();
		
		Application app = Application.getApplication();
		
		app.setQuitHandler(new MyQuitHandler());
		
		app.addAppEventListener(new MyAppHiddenListener());
		app.setDefaultMenuBar(this.cirFrame.getJMenuBar());
		

	}
	@Override
	protected void configForOs()
	{
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		this.ctrlMetaKey = "\u2318";
	}
	
	private class MyQuitHandler implements QuitHandler
	{
		@Override
		public void handleQuitRequestWith(QuitEvent e, QuitResponse r)
		{
			MacSim.this.exit();
		}
	}
	
	private class MyAppHiddenListener implements AppHiddenListener
	{
		@Override
		public void appHidden(AppHiddenEvent e)
		{
			MacSim.this.stopRepaint();
		}

		@Override
		public void appUnhidden(AppHiddenEvent e)
		{
			MacSim.this.startRepaint();
		}
	}
}
