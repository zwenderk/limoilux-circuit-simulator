package com.limoilux.circuitsimulator.core;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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

		

	}
	@Override
	protected void configForOs()
	{
		String name = null;
		
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name","CircuitSimulator");
		this.ctrlMetaKey = "\u2318";
		
		try
		{
			name = UIManager.getSystemLookAndFeelClassName();
			App.printDebugMsg("Look and feel name is " + name);
			UIManager.setLookAndFeel(name);
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Application app = Application.getApplication();
		
		app.setQuitHandler(new MyQuitHandler());
		
		app.addAppEventListener(new MyAppHiddenListener());
		app.setDefaultMenuBar(this.cirFrame.getJMenuBar());
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
