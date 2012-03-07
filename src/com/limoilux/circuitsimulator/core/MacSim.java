package com.limoilux.circuitsimulator.core;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.AppEventListener;
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
			System.out.println(e);
			System.out.println(r);

			r.performQuit();
		}
		
	}
	
}
