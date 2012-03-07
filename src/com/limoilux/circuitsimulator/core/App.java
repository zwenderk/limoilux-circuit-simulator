
package com.limoilux.circuitsimulator.core;

public abstract class App
{
	public App()
	{

	}

	protected abstract void exit();

	protected abstract void configForOs();

	public static boolean isMac()
	{
		return System.getProperty("mrj.version") != null;
	}
}
