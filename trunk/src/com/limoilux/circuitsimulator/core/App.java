

package com.limoilux.circuitsimulator.core;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public abstract class App
{
	public static final double JAVA_VERSION = App.getJavaVersion();
	public static final boolean OS_MAC = App.isMac();
	public static final boolean OS_WINDOWS = !App.isMac();
	
	public App()
	{
	}
	
	protected abstract void start();

	protected abstract void exit();

	protected abstract void configForOs();
	

	
	private static double getJavaVersion()
	{
		return new Double(System.getProperty("java.class.version")).doubleValue();
	}

	private static boolean isMac()
	{
		return System.getProperty("mrj.version") != null;
	}
}
