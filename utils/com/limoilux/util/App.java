
package com.limoilux.util;

/**
 * En une class de structure abstrate pour une application.
 * 
 * @author David Bernard
 *
 */
public abstract class App
{
	public static final double JAVA_VERSION = App.getJavaVersion();
	public static final boolean OS_MAC = App.isMac();
	public static final boolean OS_WINDOWS = !App.isMac();

	public App()
	{

	}

	/**
	 * L'interface graphique devient visible ici et l'application part.
	 */
	protected abstract void start();

	/**
	 * Est utilisé pour quitter l'application
	 */
	protected abstract void exit();

	/**
	 * Permet de faire certaines configuration au début de la vie de l'application. Devrait être appeller avant start()
	 */
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
