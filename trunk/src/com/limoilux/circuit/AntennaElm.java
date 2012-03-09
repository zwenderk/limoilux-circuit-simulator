
package com.limoilux.circuit;

import java.util.StringTokenizer;

import com.limoilux.circuitsimulator.circuit.CircuitAnalysisException;
import com.limoilux.circuitsimulator.circuit.CircuitElm;
import com.limoilux.circuitsimulator.core.Configs;

public class AntennaElm extends RailElm
{
	private double fmphase;

	public AntennaElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_DC);
	}

	public AntennaElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
		this.waveform = VoltageElm.WF_DC;
	}


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
		double time = CircuitElm.cirSim.timer.time;

		this.fmphase += 2 * Math.PI * (2200 + Math.sin(2 * Math.PI * time * 13) * 100) * Configs.timeStep;

		double fm = 3 * Math.sin(this.fmphase);

		double voltage = 0;

		voltage = 0;
		voltage += Math.sin(2 * Math.PI * time * 3000) * (1.3 + Math.sin(2 * Math.PI * time * 12)) * 3;
		voltage += Math.sin(2 * Math.PI * time * 2710) * (1.3 + Math.sin(2 * Math.PI * time * 13)) * 3;
		voltage += Math.sin(2 * Math.PI * time * 2433) * (1.3 + Math.sin(2 * Math.PI * time * 14)) * 3;
		voltage += fm;

		return voltage;
	}

	@Override
	public int getElementId()
	{
		return 'A';
	}
}
