
package com.limoilux.circuit.ui;

import com.limoilux.circuit.core.Editable;
import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuitsimulator.core.CircuitSimulator;
import com.limoilux.circuitsimulator.core.Configs;

public class EditOptions implements Editable
{
	CircuitSimulator sim;

	public EditOptions(CircuitSimulator s)
	{
		this.sim = s;
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Time step size (s)", Configs.timeStep, 0, 0);
		}
		if (n == 1)
		{
			return new EditInfo("Range for voltage color (V)", CircuitElm.voltageRange, 0, 0);
		}

		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{

	}
};
