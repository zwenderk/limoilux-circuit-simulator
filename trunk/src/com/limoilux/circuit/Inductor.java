
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.core.CirSim;

class Inductor
{
	public static final int FLAG_BACK_EULER = 2;
	int nodes[];
	int flags;
	CirSim sim;

	double inductance;
	double compResistance, current;
	double curSourceValue;

	Inductor(CirSim s)
	{
		this.sim = s;
		this.nodes = new int[2];
	}

	void setup(double ic, double cr, int f)
	{
		this.inductance = ic;
		this.current = cr;
		this.flags = f;
	}

	boolean isTrapezoidal()
	{
		return (this.flags & Inductor.FLAG_BACK_EULER) == 0;
	}

	void reset()
	{
		this.current = 0;
	}

	public void stamp(int n0, int n1)
	{
		// inductor companion model using trapezoidal or backward euler
		// approximations (Norton equivalent) consists of a current
		// source in parallel with a resistor. Trapezoidal is more
		// accurate than backward euler but can cause oscillatory behavior.
		// The oscillation is a real problem in circuits with switches.
		this.nodes[0] = n0;
		this.nodes[1] = n1;
		if (this.isTrapezoidal())
		{
			this.compResistance = 2 * this.inductance / this.sim.timer.timeStep;
		}
		else
		{
			// backward euler
			this.compResistance = this.inductance / this.sim.timer.timeStep;
		}
		this.sim.circuit.stampResistor(this.nodes[0], this.nodes[1], this.compResistance);
		this.sim.circuit.stampRightSide(this.nodes[0]);
		this.sim.circuit.stampRightSide(this.nodes[1]);
	}

	boolean nonLinear()
	{
		return false;
	}

	void startIteration(double voltdiff)
	{
		if (this.isTrapezoidal())
		{
			this.curSourceValue = voltdiff / this.compResistance + this.current;
		}
		else
		{
			// backward euler
			this.curSourceValue = this.current;
		}
	}

	double calculateCurrent(double voltdiff)
	{
		// we check compResistance because this might get called
		// before stamp(), which sets compResistance, causing
		// infinite current
		if (this.compResistance > 0)
		{
			this.current = voltdiff / this.compResistance + this.curSourceValue;
		}
		return this.current;
	}

	void doStep(double voltdiff)
	{
		this.sim.stampCurrentSource(this.nodes[0], this.nodes[1], this.curSourceValue);
	}
}
