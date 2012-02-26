
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.limoilux.circuit.core.CircuitElm;
import com.limoilux.circuit.techno.CircuitAnalysisException;

public class TunnelDiodeElm extends CircuitElm
{
	public TunnelDiodeElm(int xx, int yy)
	{
		super(xx, yy);
		this.setup();
	}

	public TunnelDiodeElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.setup();
	}

	@Override
	public boolean nonLinear()
	{
		return true;
	}

	void setup()
	{
	}

	@Override
	public int getDumpType()
	{
		return 175;
	}

	final int hs = 8;
	Polygon poly;
	Point cathode[];

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.calcLeads(16);
		this.cathode = CircuitElm.newPointArray(4);
		Point pa[] = CircuitElm.newPointArray(2);
		CircuitElm.interpPoint2(this.lead1, this.lead2, pa[0], pa[1], 0, this.hs);
		CircuitElm.interpPoint2(this.lead1, this.lead2, this.cathode[0], this.cathode[1], 1, this.hs);
		CircuitElm.interpPoint2(this.lead1, this.lead2, this.cathode[2], this.cathode[3], .8, this.hs);
		this.poly = CircuitElm.createPolygon(pa[0], pa[1], this.lead2);
	}

	@Override
	public void draw(Graphics g)
	{
		this.setBbox(this.point1, this.point2, this.hs);

		double v1 = this.volts[0];
		double v2 = this.volts[1];

		this.draw2Leads(g);

		// draw arrow thingy
		this.setPowerColor(g, true);
		this.setVoltageColor(g, v1);
		g.fillPolygon(this.poly);

		// draw thing arrow is pointing to
		this.setVoltageColor(g, v2);
		CircuitElm.drawThickLine(g, this.cathode[0], this.cathode[1]);
		CircuitElm.drawThickLine(g, this.cathode[2], this.cathode[0]);
		CircuitElm.drawThickLine(g, this.cathode[3], this.cathode[1]);

		this.doDots(g);
		this.drawPosts(g);
	}

	@Override
	public void reset()
	{
		this.lastvoltdiff = this.volts[0] = this.volts[1] = this.curcount = 0;
	}

	double lastvoltdiff;

	double limitStep(double vnew, double vold)
	{
		// Prevent voltage changes of more than 1V when iterating. Wow, I
		// thought it would be
		// much harder than this to prevent convergence problems.
		if (vnew > vold + 1)
		{
			return vold + 1;
		}
		if (vnew < vold - 1)
		{
			return vold - 1;
		}
		return vnew;
	}

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.stampNonLinear(this.nodes[0]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[1]);
	}

	static final double pvp = .1;
	static final double pip = 4.7e-3;
	static final double pvv = .37;
	static final double pvt = .026;
	static final double pvpp = .525;
	static final double piv = 370e-6;

	@Override
	public void doStep() throws CircuitAnalysisException
	{
		double voltdiff = this.volts[0] - this.volts[1];
		if (Math.abs(voltdiff - this.lastvoltdiff) > .01)
		{
			CircuitElm.cirSim.circuit.converged = false;
		}
		// System.out.println(voltdiff + " " + lastvoltdiff + " " +
		// Math.abs(voltdiff-lastvoltdiff));
		voltdiff = this.limitStep(voltdiff, this.lastvoltdiff);
		this.lastvoltdiff = voltdiff;

		double i = TunnelDiodeElm.pip * Math.exp(-TunnelDiodeElm.pvpp / TunnelDiodeElm.pvt)
				* (Math.exp(voltdiff / TunnelDiodeElm.pvt) - 1) + TunnelDiodeElm.pip * (voltdiff / TunnelDiodeElm.pvp)
				* Math.exp(1 - voltdiff / TunnelDiodeElm.pvp) + TunnelDiodeElm.piv
				* Math.exp(voltdiff - TunnelDiodeElm.pvv);

		double geq = TunnelDiodeElm.pip * Math.exp(-TunnelDiodeElm.pvpp / TunnelDiodeElm.pvt)
				* Math.exp(voltdiff / TunnelDiodeElm.pvt) / TunnelDiodeElm.pvt + TunnelDiodeElm.pip
				* Math.exp(1 - voltdiff / TunnelDiodeElm.pvp) / TunnelDiodeElm.pvp
				- Math.exp(1 - voltdiff / TunnelDiodeElm.pvp) * TunnelDiodeElm.pip * voltdiff
				/ (TunnelDiodeElm.pvp * TunnelDiodeElm.pvp) + Math.exp(voltdiff - TunnelDiodeElm.pvv)
				* TunnelDiodeElm.piv;
		double nc = i - geq * voltdiff;
		CircuitElm.cirSim.circuit.stampConductance(this.nodes[0], this.nodes[1], geq);
		CircuitElm.cirSim.circuit.stampCurrentSource(this.nodes[0], this.nodes[1], nc);
	}

	@Override
	public void calculateCurrent()
	{
		double voltdiff = this.volts[0] - this.volts[1];
		this.current = TunnelDiodeElm.pip * Math.exp(-TunnelDiodeElm.pvpp / TunnelDiodeElm.pvt)
				* (Math.exp(voltdiff / TunnelDiodeElm.pvt) - 1) + TunnelDiodeElm.pip * (voltdiff / TunnelDiodeElm.pvp)
				* Math.exp(1 - voltdiff / TunnelDiodeElm.pvp) + TunnelDiodeElm.piv
				* Math.exp(voltdiff - TunnelDiodeElm.pvv);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "tunnel diode";
		arr[1] = "I = " + CircuitElm.getCurrentText(this.getCurrent());
		arr[2] = "Vd = " + CircuitElm.getVoltageText(this.getVoltageDiff());
		arr[3] = "P = " + CircuitElm.getUnitText(this.getPower(), "W");
	}
}
