
package com.limoilux.circuit;

import java.util.StringTokenizer;

import com.limoilux.circuit.core.CircuitElm;
import com.limoilux.circuit.techno.CircuitAnalysisException;

public class AntennaElm extends RailElm
{
	public AntennaElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_DC);
	}

	public AntennaElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
		this.waveform = VoltageElm.WF_DC;
	}

	double fmphase;

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.circuit.stampVoltageSource(0, this.nodes[0], this.voltSource);
	}

	@Override
	public void doStep() throws CircuitAnalysisException
	{
		CircuitElm.cirSim.circuit.updateVoltageSource(0, this.nodes[0], this.voltSource, this.getVoltage());
	}

	@Override
	double getVoltage()
	{
		this.fmphase += 2 * Math.PI * (2200 + Math.sin(2 * Math.PI * CircuitElm.cirSim.t * 13) * 100)
				* CircuitElm.cirSim.timeStep;
		double fm = 3 * Math.sin(this.fmphase);
		return Math.sin(2 * Math.PI * CircuitElm.cirSim.t * 3000)
				* (1.3 + Math.sin(2 * Math.PI * CircuitElm.cirSim.t * 12)) * 3
				+ Math.sin(2 * Math.PI* CircuitElm.cirSim.t * 2710)
				* (1.3 + Math.sin(2 * Math.PI * CircuitElm.cirSim.t * 13)) * 3
				+ Math.sin(2 * Math.PI * CircuitElm.cirSim.t * 2433)
				* (1.3 + Math.sin(2 *Math.PI * CircuitElm.cirSim.t * 14)) * 3 + fm;
	}

	@Override
	public int getDumpType()
	{
		return 'A';
	}
}
