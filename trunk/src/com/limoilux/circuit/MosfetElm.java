
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

class MosfetElm extends CircuitElm
{
	int pnp;
	int FLAG_PNP = 1;
	int FLAG_SHOWVT = 2;
	int FLAG_DIGITAL = 4;
	double vt;

	MosfetElm(int xx, int yy, boolean pnpflag)
	{
		super(xx, yy);
		this.pnp = pnpflag ? -1 : 1;
		this.flags = pnpflag ? this.FLAG_PNP : 0;
		this.noDiagonal = true;
		this.vt = this.getDefaultThreshold();
	}

	public MosfetElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.pnp = (f & this.FLAG_PNP) != 0 ? -1 : 1;
		this.noDiagonal = true;
		this.vt = this.getDefaultThreshold();
		try
		{
			this.vt = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
		}
	}

	double getDefaultThreshold()
	{
		return 1.5;
	}

	double getBeta()
	{
		return .02;
	}

	@Override
	boolean nonLinear()
	{
		return true;
	}

	boolean drawDigital()
	{
		return (this.flags & this.FLAG_DIGITAL) != 0;
	}

	@Override
	public void reset()
	{
		this.lastv1 = this.lastv2 = this.volts[0] = this.volts[1] = this.volts[2] = this.curcount = 0;
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.vt;
	}

	@Override
	public int getDumpType()
	{
		return 'f';
	}

	final int hs = 16;

	@Override
	public void draw(Graphics g)
	{
		this.setBbox(this.point1, this.point2, this.hs);
		this.setVoltageColor(g, this.volts[1]);
		CircuitElm.drawThickLine(g, this.src[0], this.src[1]);
		this.setVoltageColor(g, this.volts[2]);
		CircuitElm.drawThickLine(g, this.drn[0], this.drn[1]);
		int segments = 6;
		int i;
		this.setPowerColor(g, true);
		double segf = 1. / segments;
		for (i = 0; i != segments; i++)
		{
			double v = this.volts[1] + (this.volts[2] - this.volts[1]) * i / segments;
			this.setVoltageColor(g, v);
			this.interpPoint(this.src[1], this.drn[1], CircuitElm.ps1, i * segf);
			this.interpPoint(this.src[1], this.drn[1], CircuitElm.ps2, (i + 1) * segf);
			CircuitElm.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
		}
		this.setVoltageColor(g, this.volts[1]);
		CircuitElm.drawThickLine(g, this.src[1], this.src[2]);
		this.setVoltageColor(g, this.volts[2]);
		CircuitElm.drawThickLine(g, this.drn[1], this.drn[2]);
		if (!this.drawDigital())
		{
			this.setVoltageColor(g, this.pnp == 1 ? this.volts[1] : this.volts[2]);
			g.fillPolygon(this.arrowPoly);
		}
		if (CircuitElm.cirSim.powerCheckItem.getState())
		{
			g.setColor(Color.gray);
		}
		this.setVoltageColor(g, this.volts[0]);
		CircuitElm.drawThickLine(g, this.point1, this.gate[1]);
		CircuitElm.drawThickLine(g, this.gate[0], this.gate[2]);
		if (this.drawDigital() && this.pnp == -1)
		{
			CircuitElm.drawThickCircle(g, this.pcircle.x, this.pcircle.y, this.pcircler);
		}
		if ((this.flags & this.FLAG_SHOWVT) != 0)
		{
			String s = "" + this.vt * this.pnp;
			g.setColor(CircuitElm.whiteColor);
			g.setFont(CircuitElm.unitsFont);
			this.drawCenteredText(g, s, this.x2 + 2, this.y2, false);
		}
		if ((this.needsHighlight() || CircuitElm.cirSim.dragElm == this) && this.dy == 0)
		{
			g.setColor(Color.white);
			g.setFont(CircuitElm.unitsFont);
			int ds = CircuitElm.sign(this.dx);
			g.drawString("G", this.gate[1].x - 10 * ds, this.gate[1].y - 5);
			g.drawString(this.pnp == -1 ? "D" : "S", this.src[0].x - 3 + 9 * ds, this.src[0].y + 4); // x+6
			// if
			// ds=1,
			// -12
			// if
			// -1
			g.drawString(this.pnp == -1 ? "S" : "D", this.drn[0].x - 3 + 9 * ds, this.drn[0].y + 4);
		}
		this.curcount = this.updateDotCount(-this.ids, this.curcount);
		this.drawDots(g, this.src[0], this.src[1], this.curcount);
		this.drawDots(g, this.src[1], this.drn[1], this.curcount);
		this.drawDots(g, this.drn[1], this.drn[0], this.curcount);
		this.drawPosts(g);
	}

	@Override
	Point getPost(int n)
	{
		return n == 0 ? this.point1 : n == 1 ? this.src[0] : this.drn[0];
	}

	@Override
	double getCurrent()
	{
		return this.ids;
	}

	@Override
	double getPower()
	{
		return this.ids * (this.volts[2] - this.volts[1]);
	}

	@Override
	public int getPostCount()
	{
		return 3;
	}

	int pcircler;
	Point src[], drn[], gate[], pcircle;
	Polygon arrowPoly;

	@Override
	public void setPoints()
	{
		super.setPoints();

		// find the coordinates of the various points we need to draw
		// the MOSFET.
		int hs2 = this.hs * this.dsign;
		this.src = this.newPointArray(3);
		this.drn = this.newPointArray(3);
		this.interpPoint2(this.point1, this.point2, this.src[0], this.drn[0], 1, -hs2);
		this.interpPoint2(this.point1, this.point2, this.src[1], this.drn[1], 1 - 22 / this.dn, -hs2);
		this.interpPoint2(this.point1, this.point2, this.src[2], this.drn[2], 1 - 22 / this.dn, -hs2 * 4 / 3);

		this.gate = this.newPointArray(3);
		this.interpPoint2(this.point1, this.point2, this.gate[0], this.gate[2], 1 - 28 / this.dn, hs2 / 2); // was
		// 1-20/dn
		this.interpPoint(this.gate[0], this.gate[2], this.gate[1], .5);

		if (!this.drawDigital())
		{
			if (this.pnp == 1)
			{
				this.arrowPoly = this.calcArrow(this.src[1], this.src[0], 10, 4);
			}
			else
			{
				this.arrowPoly = this.calcArrow(this.drn[0], this.drn[1], 12, 5);
			}
		}
		else if (this.pnp == -1)
		{
			this.interpPoint(this.point1, this.point2, this.gate[1], 1 - 36 / this.dn);
			int dist = this.dsign < 0 ? 32 : 31;
			this.pcircle = this.interpPoint(this.point1, this.point2, 1 - dist / this.dn);
			this.pcircler = 3;
		}
	}

	double lastv1, lastv2;
	double ids;
	int mode = 0;
	double gm = 0;

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.stampNonLinear(this.nodes[1]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[2]);
	}

	@Override
	public void doStep()
	{
		double vs[] = new double[3];
		vs[0] = this.volts[0];
		vs[1] = this.volts[1];
		vs[2] = this.volts[2];
		if (vs[1] > this.lastv1 + .5)
		{
			vs[1] = this.lastv1 + .5;
		}
		if (vs[1] < this.lastv1 - .5)
		{
			vs[1] = this.lastv1 - .5;
		}
		if (vs[2] > this.lastv2 + .5)
		{
			vs[2] = this.lastv2 + .5;
		}
		if (vs[2] < this.lastv2 - .5)
		{
			vs[2] = this.lastv2 - .5;
		}
		int source = 1;
		int drain = 2;
		if (this.pnp * vs[1] > this.pnp * vs[2])
		{
			source = 2;
			drain = 1;
		}
		int gate = 0;
		double vgs = vs[gate] - vs[source];
		double vds = vs[drain] - vs[source];
		if (Math.abs(this.lastv1 - vs[1]) > .01 || Math.abs(this.lastv2 - vs[2]) > .01)
		{
			CircuitElm.cirSim.converged = false;
		}
		this.lastv1 = vs[1];
		this.lastv2 = vs[2];
		double realvgs = vgs;
		double realvds = vds;
		vgs *= this.pnp;
		vds *= this.pnp;
		this.ids = 0;
		this.gm = 0;
		double Gds = 0;
		double beta = this.getBeta();
		if (vgs > .5 && this instanceof JfetElm)
		{
			CircuitElm.cirSim.stop("JFET is reverse biased!", this);
			return;
		}
		if (vgs < this.vt)
		{
			// should be all zero, but that causes a singular matrix,
			// so instead we treat it as a large resistor
			Gds = 1e-8;
			this.ids = vds * Gds;
			this.mode = 0;
		}
		else if (vds < vgs - this.vt)
		{
			// linear
			this.ids = beta * ((vgs - this.vt) * vds - vds * vds * .5);
			this.gm = beta * vds;
			Gds = beta * (vgs - vds - this.vt);
			this.mode = 1;
		}
		else
		{
			// saturation; Gds = 0
			this.gm = beta * (vgs - this.vt);
			// use very small Gds to avoid nonconvergence
			Gds = 1e-8;
			this.ids = .5 * beta * (vgs - this.vt) * (vgs - this.vt) + (vds - (vgs - this.vt)) * Gds;
			this.mode = 2;
		}
		double rs = -this.pnp * this.ids + Gds * realvds + this.gm * realvgs;
		// System.out.println("M " + vds + " " + vgs + " " + ids + " " + gm +
		// " "+ Gds + " " + volts[0] + " " + volts[1] + " " + volts[2] + " " +
		// source + " " + rs + " " + this);
		CircuitElm.cirSim.stampMatrix(this.nodes[drain], this.nodes[drain], Gds);
		CircuitElm.cirSim.stampMatrix(this.nodes[drain], this.nodes[source], -Gds - this.gm);
		CircuitElm.cirSim.stampMatrix(this.nodes[drain], this.nodes[gate], this.gm);

		CircuitElm.cirSim.stampMatrix(this.nodes[source], this.nodes[drain], -Gds);
		CircuitElm.cirSim.stampMatrix(this.nodes[source], this.nodes[source], Gds + this.gm);
		CircuitElm.cirSim.stampMatrix(this.nodes[source], this.nodes[gate], -this.gm);

		CircuitElm.cirSim.stampRightSide(this.nodes[drain], rs);
		CircuitElm.cirSim.stampRightSide(this.nodes[source], -rs);
		if (source == 2 && this.pnp == 1 || source == 1 && this.pnp == -1)
		{
			this.ids = -this.ids;
		}
	}

	void getFetInfo(String arr[], String n)
	{
		arr[0] = (this.pnp == -1 ? "p-" : "n-") + n;
		arr[0] += " (Vt = " + CircuitElm.getVoltageText(this.pnp * this.vt) + ")";
		arr[1] = (this.pnp == 1 ? "Ids = " : "Isd = ") + CircuitElm.getCurrentText(this.ids);
		arr[2] = "Vgs = " + CircuitElm.getVoltageText(this.volts[0] - this.volts[this.pnp == -1 ? 2 : 1]);
		arr[3] = (this.pnp == 1 ? "Vds = " : "Vsd = ") + CircuitElm.getVoltageText(this.volts[2] - this.volts[1]);
		arr[4] = this.mode == 0 ? "off" : this.mode == 1 ? "linear" : "saturation";
		arr[5] = "gm = " + CircuitElm.getUnitText(this.gm, "A/V");
	}

	@Override
	public void getInfo(String arr[])
	{
		this.getFetInfo(arr, "MOSFET");
	}

	@Override
	boolean canViewInScope()
	{
		return true;
	}

	@Override
	double getVoltageDiff()
	{
		return this.volts[2] - this.volts[1];
	}

	@Override
	boolean getConnection(int n1, int n2)
	{
		return !(n1 == 0 || n2 == 0);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Threshold Voltage", this.pnp * this.vt, .01, 5);
		}
		if (n == 1)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Digital Symbol", this.drawDigital());
			return ei;
		}

		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.vt = this.pnp * ei.value;
		}
		if (n == 1)
		{
			this.flags = ei.checkbox.getState() ? this.flags | this.FLAG_DIGITAL : this.flags & ~this.FLAG_DIGITAL;
			this.setPoints();
		}
	}
}