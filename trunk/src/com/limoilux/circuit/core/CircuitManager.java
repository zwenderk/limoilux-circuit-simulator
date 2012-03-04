
package com.limoilux.circuit.core;

import com.limoilux.circuit.techno.Circuit;
import com.limoilux.circuit.ui.CircuitPane;

public class CircuitManager
{
	public final Circuit circuit;
	public final CircuitPane circuitPanel;

	public CircuitManager(Circuit circuit, CircuitPane circuitPanel)
	{
		this.circuit = circuit;
		this.circuitPanel = circuitPanel;
	}

}
