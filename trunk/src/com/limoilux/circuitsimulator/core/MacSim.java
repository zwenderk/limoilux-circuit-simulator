package com.limoilux.circuitsimulator.core;

public class MacSim extends CircuitSimulator
{

	@Override
	protected void configForOs()
	{
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		this.ctrlMetaKey = "\u2318";
	}
}
