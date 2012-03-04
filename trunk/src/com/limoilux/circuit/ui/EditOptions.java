
package com.limoilux.circuit.ui;

import com.limoilux.circuit.core.Editable;
import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuitsimulator.core.CirSim;

public class EditOptions implements Editable
{
	CirSim sim;

	public EditOptions(CirSim s)
	{
		this.sim = s;
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Time step size (s)", this.sim.timer.timeStep, 0, 0);
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
		if (n == 0 && ei.value > 0)
		{
			this.sim.timer.timeStep = ei.value;
		}
		if (n == 1 && ei.value > 0)
		{
			CircuitElm.voltageRange = ei.value;
		}
	}
};
