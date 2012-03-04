
package com.limoilux.circuit;

import com.limoilux.circuitsimulator.core.CirSim;

public class Diode
{
	public int nodes[];
	@Deprecated
	public CirSim sim;

	public double leakage = 1e-14; // was 1e-9;
	double vt, vdcoef, fwdrop, zvoltage, zoffset;
	double lastvoltdiff;
	double vcrit;

	public Diode(CirSim s)
	{
		this.sim = s;
		this.nodes = new int[2];
	}

	public void setup(double fw, double zv)
	{
		this.fwdrop = fw;
		this.zvoltage = zv;
		this.vdcoef = Math.log(1 / this.leakage + 1) / this.fwdrop;
		this.vt = 1 / this.vdcoef;
		// critical voltage for limiting; current is vt/sqrt(2) at
		// this voltage
		this.vcrit = this.vt * Math.log(this.vt / (Math.sqrt(2) * this.leakage));
		if (this.zvoltage == 0)
		{
			this.zoffset = 0;
		}
		else
		{
			// calculate offset which will give us 5mA at zvoltage
			double i = -.005;
			this.zoffset = this.zvoltage - Math.log(-(1 + i / this.leakage)) / this.vdcoef;
		}
	}

	public void reset()
	{
		this.lastvoltdiff = 0;
	}

	public double limitStep(double vnew, double vold)
	{
		double arg;
		double oo = vnew;

		// check new voltage; has current changed by factor of e^2?
		if (vnew > this.vcrit && Math.abs(vnew - vold) > this.vt + this.vt)
		{
			if (vold > 0)
			{
				arg = 1 + (vnew - vold) / this.vt;
				if (arg > 0)
				{
					// adjust vnew so that the current is the same
					// as in linearized model from previous iteration.
					// current at vnew = old current * arg
					vnew = vold + this.vt * Math.log(arg);
					// current at v0 = 1uA
					double v0 = Math.log(1e-6 / this.leakage) * this.vt;
					vnew = Math.max(v0, vnew);
				}
				else
				{
					vnew = this.vcrit;
				}
			}
			else
			{
				// adjust vnew so that the current is the same
				// as in linearized model from previous iteration.
				// (1/vt = slope of load line)
				vnew = this.vt * Math.log(vnew / this.vt);
			}
			this.sim.circuit.converged = false;
			// System.out.println(vnew + " " + oo + " " + vold);
		}
		else if (vnew < 0 && this.zoffset != 0)
		{
			// for Zener breakdown, use the same logic but translate the values
			vnew = -vnew - this.zoffset;
			vold = -vold - this.zoffset;

			if (vnew > this.vcrit && Math.abs(vnew - vold) > this.vt + this.vt)
			{
				if (vold > 0)
				{
					arg = 1 + (vnew - vold) / this.vt;
					if (arg > 0)
					{
						vnew = vold + this.vt * Math.log(arg);
						double v0 = Math.log(1e-6 / this.leakage) * this.vt;
						vnew = Math.max(v0, vnew);
						// System.out.println(oo + " " + vnew);
					}
					else
					{
						vnew = this.vcrit;
					}
				}
				else
				{
					vnew = this.vt * Math.log(vnew / this.vt);
				}
				this.sim.circuit.converged = false;
			}
			vnew = -(vnew + this.zoffset);
		}
		return vnew;
	}

	public void stamp(int n0, int n1)
	{
		this.nodes[0] = n0;
		this.nodes[1] = n1;
		this.sim.circuit.stampNonLinear(this.nodes[0]);
		this.sim.circuit.stampNonLinear(this.nodes[1]);
	}

	public void doStep(double voltdiff)
	{
		// used to have .1 here, but needed .01 for peak detector
		if (Math.abs(voltdiff - this.lastvoltdiff) > .01)
		{
			this.sim.circuit.converged = false;
		}
		voltdiff = this.limitStep(voltdiff, this.lastvoltdiff);
		this.lastvoltdiff = voltdiff;

		if (voltdiff >= 0 || this.zvoltage == 0)
		{
			// regular diode or forward-biased zener
			double eval = Math.exp(voltdiff * this.vdcoef);
			// make diode linear with negative voltages; aids convergence
			if (voltdiff < 0)
			{
				eval = 1;
			}
			double geq = this.vdcoef * this.leakage * eval;
			double nc = (eval - 1) * this.leakage - geq * voltdiff;
			this.sim.circuit.stampConductance(this.nodes[0], this.nodes[1], geq);
			this.sim.circuit.stampCurrentSource(this.nodes[0], this.nodes[1], nc);
		}
		else
		{
			// Zener diode

			/*
			 * I(Vd) = Is * (exp[Vd*C] - exp[(-Vd-Vz)*C] - 1 )
			 * 
			 * geq is I'(Vd) nc is I(Vd) + I'(Vd)*(-Vd)
			 */

			double geq = this.leakage * this.vdcoef
					* (Math.exp(voltdiff * this.vdcoef) + Math.exp((-voltdiff - this.zoffset) * this.vdcoef));

			double nc = this.leakage
					* (Math.exp(voltdiff * this.vdcoef) - Math.exp((-voltdiff - this.zoffset) * this.vdcoef) - 1) + geq
					* -voltdiff;

			this.sim.circuit.stampConductance(this.nodes[0], this.nodes[1], geq);
			this.sim.circuit.stampCurrentSource(this.nodes[0], this.nodes[1], nc);
		}
	}

	public double calculateCurrent(double voltdiff)
	{
		if (voltdiff >= 0 || this.zvoltage == 0)
		{
			return this.leakage * (Math.exp(voltdiff * this.vdcoef) - 1);
		}
		return this.leakage
				* (Math.exp(voltdiff * this.vdcoef) - Math.exp((-voltdiff - this.zoffset) * this.vdcoef) - 1);
	}
}
