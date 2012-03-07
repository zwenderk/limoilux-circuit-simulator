
package com.limoilux.circuitsimulator.core;

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

	public static void printDebugMsg(String msg)
	{
		String label = "[" + Thread.currentThread().getName();
		label += " at " + System.currentTimeMillis() + "]";
		System.out.println(label + " " + msg);
	}

	private static double getJavaVersion()
	{
		return new Double(System.getProperty("java.class.version")).doubleValue();
	}

	private static boolean isMac()
	{
		return System.getProperty("mrj.version") != null;
	}
}
