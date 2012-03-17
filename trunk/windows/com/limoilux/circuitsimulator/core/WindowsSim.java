
package com.limoilux.circuitsimulator.core;

import com.limoilux.util.App;


public class WindowsSim extends CircuitSimulator
{
	@Override
	protected void configForOs()
	{
		this.ctrlMetaKey = "Ctrl";
	}

	
	public static void main(String args[])
	{
		CircuitSimulator circuitSimulator = null;


			App.printDebugMsg("Platform is Windows");
			circuitSimulator = new WindowsSim();
		

		circuitSimulator.start();
	}
}
