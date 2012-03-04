
package com.limoilux.circuitsimulator.circuit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuitsimulator.scope.Scope;

public class CircuitManager
{
	public final Circuit circuit;
	public final CircuitPane circuitPanel;
	
	public final Class<?> dumpTypes[];

	public CircuitManager( CircuitPane circuitPanel)
	{
		this.circuit = new Circuit();
		this.circuitPanel = circuitPanel;
		
		
		// Init the dumpTypes
		this.dumpTypes = new Class[300];

		// these characters are reserved
		this.dumpTypes['o'] = Scope.class;
		this.dumpTypes['h'] = Scope.class;
		this.dumpTypes['$'] = Scope.class;
		this.dumpTypes['%'] = Scope.class;
		this.dumpTypes['?'] = Scope.class;
		this.dumpTypes['B'] = Scope.class;
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
