
package com.limoilux.circuit;

import java.util.StringTokenizer;

import com.limoilux.circuit.core.CircuitElm;
import com.limoilux.circuit.techno.CircuitAnalysisException;

public class PhaseCompElm extends ChipElm
{
	public PhaseCompElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public PhaseCompElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	public String getChipName()
	{
		return "phase comparator";
	}

	@Override
	public void setupPins()
	{
		this.sizeX = 2;
		this.sizeY = 2;
		this.pins = new Pin[3];
		this.pins[0] = new Pin(0, this.SIDE_W, "I1");
		this.pins[1] = new Pin(1, this.SIDE_W, "I2");
		this.pins[2] = new Pin(0, this.SIDE_E, "O");
		this.pins[2].output = true;
	}

	@Override
	public boolean nonLinear()
	{
		return true;
	}

	@Override
	public void stamp()
	{
		int vn = CircuitElm.cirSim.circuit.nodeList.size() + this.pins[2].voltSource;
		CircuitElm.cirSim.stampNonLinear(vn);
		CircuitElm.cirSim.stampNonLinear(0);
		CircuitElm.cirSim.stampNonLinear(this.nodes[2]);
	}

	boolean ff1, ff2;

	@Override
	public void doStep() throws CircuitAnalysisException
	{
		boolean v1 = this.volts[0] > 2.5;
		boolean v2 = this.volts[1] > 2.5;
		if (v1 && !this.pins[0].value)
		{
			this.ff1 = true;
		}
		if (v2 && !this.pins[1].value)
		{
			this.ff2 = true;
		}
		if (this.ff1 && this.ff2)
		{
			this.ff1 = this.ff2 = false;
		}
		double out = this.ff1 ? 5 : this.ff2 ? 0 : -1;
		// System.out.println(out + " " + v1 + " " + v2);
		if (out != -1)
		{
			CircuitElm.cirSim.circuit.stampVoltageSource(0, this.nodes[2], this.pins[2].voltSource, out);
		}
		else
		{
			// tie current through output pin to 0
			int vn = CircuitElm.cirSim.circuit.nodeList.size() + this.pins[2].voltSource;
			CircuitElm.cirSim.stampMatrix(vn, vn, 1);
		}
		this.pins[0].value = v1;
		this.pins[1].value = v2;
	}

	@Override
	public int getPostCount()
	{
		return 3;
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	public int getDumpType()
	{
		return 161;
	}
}
