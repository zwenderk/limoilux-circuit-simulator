
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

class TransistorElm extends CircuitElm
{
	int pnp;
	double beta;
	double fgain;
	double gmin;
	final int FLAG_FLIP = 1;

	TransistorElm(int xx, int yy, boolean pnpflag)
	{
		super(xx, yy);
		this.pnp = pnpflag ? -1 : 1;
		this.beta = 100;
		this.setup();
	}

	public TransistorElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.pnp = new Integer(st.nextToken()).intValue();
		this.beta = 100;
		try
		{
			this.lastvbe = new Double(st.nextToken()).doubleValue();
			this.lastvbc = new Double(st.nextToken()).doubleValue();
			this.volts[0] = 0;
			this.volts[1] = -this.lastvbe;
			this.volts[2] = -this.lastvbc;
			this.beta = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
		}
		this.setup();
	}

	void setup()
	{
		this.vcrit = TransistorElm.vt * Math.log(TransistorElm.vt / (Math.sqrt(2) * TransistorElm.leakage));
		this.fgain = this.beta / (this.beta + 1);
		this.noDiagonal = true;
	}

	@Override
	boolean nonLinear()
	{
		return true;
	}

	@Override
	void reset()
	{
		this.volts[0] = this.volts[1] = this.volts[2] = 0;
		this.lastvbc = this.lastvbe = this.curcount_c = this.curcount_e = this.curcount_b = 0;
	}

	@Override
	public int getDumpType()
	{
		return 't';
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.pnp + " " + (this.volts[0] - this.volts[1]) + " "
				+ (this.volts[0] - this.volts[2]) + " " + this.beta;
	}

	double ic, ie, ib, curcount_c, curcount_e, curcount_b;
	Polygon rectPoly, arrowPoly;

	@Override
	public void draw(Graphics g)
	{
		this.setBbox(this.point1, this.point2, 16);
		this.setPowerColor(g, true);
		// draw collector
		this.setVoltageColor(g, this.volts[1]);
		CircuitElm.drawThickLine(g, this.coll[0], this.coll[1]);
		// draw emitter
		this.setVoltageColor(g, this.volts[2]);
		CircuitElm.drawThickLine(g, this.emit[0], this.emit[1]);
		// draw arrow
		g.setColor(CircuitElm.lightGrayColor);
		g.fillPolygon(this.arrowPoly);
		// draw base
		this.setVoltageColor(g, this.volts[0]);
		if (CircuitElm.cirSim.powerCheckItem.getState())
		{
			g.setColor(Color.gray);
		}
		CircuitElm.drawThickLine(g, this.point1, this.base);
		// draw dots
		this.curcount_b = this.updateDotCount(-this.ib, this.curcount_b);
		this.drawDots(g, this.base, this.point1, this.curcount_b);
		this.curcount_c = this.updateDotCount(-this.ic, this.curcount_c);
		this.drawDots(g, this.coll[1], this.coll[0], this.curcount_c);
		this.curcount_e = this.updateDotCount(-this.ie, this.curcount_e);
		this.drawDots(g, this.emit[1], this.emit[0], this.curcount_e);
		// draw base rectangle
		this.setVoltageColor(g, this.volts[0]);
		this.setPowerColor(g, true);
		g.fillPolygon(this.rectPoly);

		if ((this.needsHighlight() || CircuitElm.cirSim.dragElm == this) && this.dy == 0)
		{
			g.setColor(Color.white);
			g.setFont(CircuitElm.unitsFont);
			int ds = CircuitElm.sign(this.dx);
			g.drawString("B", this.base.x - 10 * ds, this.base.y - 5);
			g.drawString("C", this.coll[0].x - 3 + 9 * ds, this.coll[0].y + 4); // x+6
																				// if
			// ds=1,
			// -12
			// if -1
			g.drawString("E", this.emit[0].x - 3 + 9 * ds, this.emit[0].y + 4);
		}
		this.drawPosts(g);
	}

	@Override
	Point getPost(int n)
	{
		return n == 0 ? this.point1 : n == 1 ? this.coll[0] : this.emit[0];
	}

	@Override
	public int getPostCount()
	{
		return 3;
	}

	@Override
	double getPower()
	{
		return (this.volts[0] - this.volts[2]) * this.ib + (this.volts[1] - this.volts[2]) * this.ic;
	}

	Point rect[], coll[], emit[], base;

	@Override
	public void setPoints()
	{
		super.setPoints();
		int hs = 16;
		if ((this.flags & this.FLAG_FLIP) != 0)
		{
			this.dsign = -this.dsign;
		}
		int hs2 = hs * this.dsign * this.pnp;
		// calc collector, emitter posts
		this.coll = this.newPointArray(2);
		this.emit = this.newPointArray(2);
		this.interpPoint2(this.point1, this.point2, this.coll[0], this.emit[0], 1, hs2);
		// calc rectangle edges
		this.rect = this.newPointArray(4);
		this.interpPoint2(this.point1, this.point2, this.rect[0], this.rect[1], 1 - 16 / this.dn, hs);
		this.interpPoint2(this.point1, this.point2, this.rect[2], this.rect[3], 1 - 13 / this.dn, hs);
		// calc points where collector/emitter leads contact rectangle
		this.interpPoint2(this.point1, this.point2, this.coll[1], this.emit[1], 1 - 13 / this.dn, 6 * this.dsign
				* this.pnp);
		// calc point where base lead contacts rectangle
		this.base = new Point();
		this.interpPoint(this.point1, this.point2, this.base, 1 - 16 / this.dn);

		// rectangle
		this.rectPoly = this.createPolygon(this.rect[0], this.rect[2], this.rect[3], this.rect[1]);

		// arrow
		if (this.pnp == 1)
		{
			this.arrowPoly = this.calcArrow(this.emit[1], this.emit[0], 8, 4);
		}
		else
		{
			Point pt = this.interpPoint(this.point1, this.point2, 1 - 11 / this.dn, -5 * this.dsign * this.pnp);
			this.arrowPoly = this.calcArrow(this.emit[0], pt, 8, 4);
		}
	}

	static final double leakage = 1e-13; // 1e-6;
	static final double vt = .025;
	static final double vdcoef = 1 / TransistorElm.vt;
	static final double rgain = .5;
	double vcrit;
	double lastvbc, lastvbe;

	double limitStep(double vnew, double vold)
	{
		double arg;
		double oo = vnew;

		if (vnew > this.vcrit && Math.abs(vnew - vold) > TransistorElm.vt + TransistorElm.vt)
		{
			if (vold > 0)
			{
				arg = 1 + (vnew - vold) / TransistorElm.vt;
				if (arg > 0)
				{
					vnew = vold + TransistorElm.vt * Math.log(arg);
				}
				else
				{
					vnew = this.vcrit;
				}
			}
			else
			{
				vnew = TransistorElm.vt * Math.log(vnew / TransistorElm.vt);
			}
			CircuitElm.cirSim.converged = false;
			// System.out.println(vnew + " " + oo + " " + vold);
		}
		return vnew;
	}

	@Override
	void stamp()
	{
		CircuitElm.cirSim.stampNonLinear(this.nodes[0]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[1]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[2]);
	}

	@Override
	public void doStep()
	{
		double vbc = this.volts[0] - this.volts[1]; // typically negative
		double vbe = this.volts[0] - this.volts[2]; // typically positive
		if (Math.abs(vbc - this.lastvbc) > .01 || // .01
				Math.abs(vbe - this.lastvbe) > .01)
		{
			CircuitElm.cirSim.converged = false;
		}
		this.gmin = 0;
		if (CircuitElm.cirSim.subIterations > 100)
		{
			// if we have trouble converging, put a conductance in parallel with
			// all P-N junctions.
			// Gradually increase the conductance value for each iteration.
			this.gmin = Math.exp(-9 * Math.log(10) * (1 - CircuitElm.cirSim.subIterations / 3000.));
			if (this.gmin > .1)
			{
				this.gmin = .1;
			}
		}
		// System.out.print("T " + vbc + " " + vbe + "\n");
		vbc = this.pnp * this.limitStep(this.pnp * vbc, this.pnp * this.lastvbc);
		vbe = this.pnp * this.limitStep(this.pnp * vbe, this.pnp * this.lastvbe);
		this.lastvbc = vbc;
		this.lastvbe = vbe;
		double pcoef = TransistorElm.vdcoef * this.pnp;
		double expbc = Math.exp(vbc * pcoef);
		/*
		 * if (expbc > 1e13 || Double.isInfinite(expbc)) expbc = 1e13;
		 */
		double expbe = Math.exp(vbe * pcoef);
		if (expbe < 1)
		{
			expbe = 1;
		}
		/*
		 * if (expbe > 1e13 || Double.isInfinite(expbe)) expbe = 1e13;
		 */
		this.ie = this.pnp * TransistorElm.leakage * (-(expbe - 1) + TransistorElm.rgain * (expbc - 1));
		this.ic = this.pnp * TransistorElm.leakage * (this.fgain * (expbe - 1) - (expbc - 1));
		this.ib = -(this.ie + this.ic);
		// System.out.println("gain " + ic/ib);
		// System.out.print("T " + vbc + " " + vbe + " " + ie + " " + ic +
		// "\n");
		double gee = -TransistorElm.leakage * TransistorElm.vdcoef * expbe;
		double gec = TransistorElm.rgain * TransistorElm.leakage * TransistorElm.vdcoef * expbc;
		double gce = -gee * this.fgain;
		double gcc = -gec * (1 / TransistorElm.rgain);

		/*
		 * System.out.print("gee = " + gee + "\n"); System.out.print("gec = " +
		 * gec + "\n"); System.out.print("gce = " + gce + "\n");
		 * System.out.print("gcc = " + gcc + "\n");
		 * System.out.print("gce+gcc = " + (gce+gcc) + "\n");
		 * System.out.print("gee+gec = " + (gee+gec) + "\n");
		 */

		// stamps from page 302 of Pillage. Node 0 is the base,
		// node 1 the collector, node 2 the emitter. Also stamp
		// minimum conductance (gmin) between b,e and b,c
		CircuitElm.cirSim.stampMatrix(this.nodes[0], this.nodes[0], -gee - gec - gce - gcc + this.gmin * 2);
		CircuitElm.cirSim.stampMatrix(this.nodes[0], this.nodes[1], gec + gcc - this.gmin);
		CircuitElm.cirSim.stampMatrix(this.nodes[0], this.nodes[2], gee + gce - this.gmin);
		CircuitElm.cirSim.stampMatrix(this.nodes[1], this.nodes[0], gce + gcc - this.gmin);
		CircuitElm.cirSim.stampMatrix(this.nodes[1], this.nodes[1], -gcc + this.gmin);
		CircuitElm.cirSim.stampMatrix(this.nodes[1], this.nodes[2], -gce);
		CircuitElm.cirSim.stampMatrix(this.nodes[2], this.nodes[0], gee + gec - this.gmin);
		CircuitElm.cirSim.stampMatrix(this.nodes[2], this.nodes[1], -gec);
		CircuitElm.cirSim.stampMatrix(this.nodes[2], this.nodes[2], -gee + this.gmin);

		// we are solving for v(k+1), not delta v, so we use formula
		// 10.5.13, multiplying J by v(k)
		CircuitElm.cirSim.stampRightSide(this.nodes[0], -this.ib - (gec + gcc) * vbc - (gee + gce) * vbe);
		CircuitElm.cirSim.stampRightSide(this.nodes[1], -this.ic + gce * vbe + gcc * vbc);
		CircuitElm.cirSim.stampRightSide(this.nodes[2], -this.ie + gee * vbe + gec * vbc);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "transistor (" + (this.pnp == -1 ? "PNP)" : "NPN)") + " beta="
				+ CircuitElm.showFormat.format(this.beta);
		double vbc = this.volts[0] - this.volts[1];
		double vbe = this.volts[0] - this.volts[2];
		double vce = this.volts[1] - this.volts[2];
		if (vbc * this.pnp > .2)
		{
			arr[1] = vbe * this.pnp > .2 ? "saturation" : "reverse active";
		}
		else
		{
			arr[1] = vbe * this.pnp > .2 ? "fwd active" : "cutoff";
		}
		arr[2] = "Ic = " + CircuitElm.getCurrentText(this.ic);
		arr[3] = "Ib = " + CircuitElm.getCurrentText(this.ib);
		arr[4] = "Vbe = " + CircuitElm.getVoltageText(vbe);
		arr[5] = "Vbc = " + CircuitElm.getVoltageText(vbc);
		arr[6] = "Vce = " + CircuitElm.getVoltageText(vce);
	}

	@Override
	double getScopeValue(int x)
	{
		switch (x)
		{
		case Scope.VAL_IB:
			return this.ib;
		case Scope.VAL_IC:
			return this.ic;
		case Scope.VAL_IE:
			return this.ie;
		case Scope.VAL_VBE:
			return this.volts[0] - this.volts[2];
		case Scope.VAL_VBC:
			return this.volts[0] - this.volts[1];
		case Scope.VAL_VCE:
			return this.volts[1] - this.volts[2];
		}
		return 0;
	}

	@Override
	String getScopeUnits(int x)
	{
		switch (x)
		{
		case Scope.VAL_IB:
		case Scope.VAL_IC:
		case Scope.VAL_IE:
			return "A";
		default:
			return "V";
		}
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Beta/hFE", this.beta, 10, 1000).setDimensionless();
		}
		if (n == 1)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Swap E/C", (this.flags & this.FLAG_FLIP) != 0);
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.beta = ei.value;
			this.setup();
		}
		if (n == 1)
		{
			if (ei.checkbox.getState())
			{
				this.flags |= this.FLAG_FLIP;
			}
			else
			{
				this.flags &= ~this.FLAG_FLIP;
			}
			this.setPoints();
		}
	}

	@Override
	boolean canViewInScope()
	{
		return true;
	}
}
