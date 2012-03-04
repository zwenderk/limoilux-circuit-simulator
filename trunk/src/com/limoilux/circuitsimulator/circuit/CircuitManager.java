
package com.limoilux.circuitsimulator.circuit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.limoilux.circuit.techno.CircuitElm;

public class CircuitManager
{
	public final Circuit circuit;
	public final CircuitPane circuitPanel;

	public CircuitManager( CircuitPane circuitPanel)
	{
		this.circuit = new Circuit();
		this.circuitPanel = circuitPanel;
	}

	public void repaint()
	{
		this.circuitPanel.repaint();
	}

	public Circuit getCircuit()
	{
		return this.circuit;
	}
}
