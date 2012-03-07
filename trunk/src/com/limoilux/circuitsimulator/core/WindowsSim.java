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
	

}
