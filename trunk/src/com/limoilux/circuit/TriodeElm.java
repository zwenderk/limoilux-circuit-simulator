
package com.limoilux.circuit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

class TriodeElm extends CircuitElm
{
	double mu, kg1;
	double curcountp, curcountc, curcountg, currentp, currentg, currentc;
	final double gridCurrentR = 6000;

	public TriodeElm(int xx, int yy)
	{
		super(xx, yy);
		this.mu = 93;
		this.kg1 = 680;
		this.setup();
	}

	public TriodeElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.mu = new Double(st.nextToken()).doubleValue();
		this.kg1 = new Double(st.nextToken()).doubleValue();
		this.setup();
	}

	void setup()
	{
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
		this.curcount = 0;
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.mu + " " + this.kg1;
	}

	@Override
	public int getDumpType()
	{
		return 173;
	}

	Point plate[], grid[], cath[], midgrid, midcath;
	int circler;

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.plate = this.newPointArray(4);
		this.grid = this.newPointArray(8);
		this.cath = this.newPointArray(4);
		this.grid[0] = this.point1;
		int nearw = 8;
		this.interpPoint(this.point1, this.point2, this.plate[1], 1, nearw);
		int farw = 32;
		this.interpPoint(this.point1, this.point2, this.plate[0], 1, farw);
		int platew = 18;
		this.interpPoint2(this.point2, this.plate[1], this.plate[2], this.plate[3], 1, platew);

		this.circler = 24;
		this.interpPoint(this.point1, this.point2, this.grid[1], (this.dn - this.circler) / this.dn, 0);
		int i;
		for (i = 0; i != 3; i++)
		{
			this.interpPoint(this.grid[1], this.point2, this.grid[2 + i * 2], (i * 3 + 1) / 4.5, 0);
			this.interpPoint(this.grid[1], this.point2, this.grid[3 + i * 2], (i * 3 + 2) / 4.5, 0);
		}
		this.midgrid = this.point2;

		int cathw = 16;
		this.midcath = this.interpPoint(this.point1, this.point2, 1, -nearw);
		this.interpPoint2(this.point2, this.plate[1], this.cath[1], this.cath[2], -1, cathw);
		this.interpPoint(this.point2, this.plate[1], this.cath[3], -1.2, -cathw);
		this.interpPoint(this.point2, this.plate[1], this.cath[0], -farw / (double) nearw, cathw);
	}

	@Override
	public void draw(Graphics g)
	{
		g.setColor(Color.gray);
		CircuitElm.drawThickCircle(g, this.point2.x, this.point2.y, this.circler);
		this.setBbox(this.point1, this.plate[0], 16);
		this.adjustBbox(this.cath[0].x, this.cath[1].y, this.point2.x + this.circler, this.point2.y + this.circler);
		this.setPowerColor(g, true);
		// draw plate
		this.setVoltageColor(g, this.volts[0]);
		CircuitElm.drawThickLine(g, this.plate[0], this.plate[1]);
		CircuitElm.drawThickLine(g, this.plate[2], this.plate[3]);
		// draw grid
		this.setVoltageColor(g, this.volts[1]);
		int i;
		for (i = 0; i != 8; i += 2)
		{
			CircuitElm.drawThickLine(g, this.grid[i], this.grid[i + 1]);
		}
		// draw cathode
		this.setVoltageColor(g, this.volts[2]);
		for (i = 0; i != 3; i++)
		{
			CircuitElm.drawThickLine(g, this.cath[i], this.cath[i + 1]);
		}
		// draw dots
		this.curcountp = this.updateDotCount(this.currentp, this.curcountp);
		this.curcountc = this.updateDotCount(this.currentc, this.curcountc);
		this.curcountg = this.updateDotCount(this.currentg, this.curcountg);
		if (CircuitElm.cirSim.dragElm != this)
		{
			this.drawDots(g, this.plate[0], this.midgrid, this.curcountp);
			this.drawDots(g, this.midgrid, this.midcath, this.curcountc);
			this.drawDots(g, this.midcath, this.cath[1], this.curcountc + 8);
			this.drawDots(g, this.cath[1], this.cath[0], this.curcountc + 8);
			this.drawDots(g, this.point1, this.midgrid, this.curcountg);
		}
		this.drawPosts(g);
	}

	@Override
	Point getPost(int n)
	{
		return n == 0 ? this.plate[0] : n == 1 ? this.grid[0] : this.cath[0];
	}

	@Override
	public int getPostCount()
	{
		return 3;
	}

	@Override
	double getPower()
	{
		return (this.volts[0] - this.volts[2]) * this.current;
	}

	double lastv0, lastv1, lastv2;

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
		int grid = 1;
		int cath = 2;
		int plate = 0;
		double vgk = vs[grid] - vs[cath];
		double vpk = vs[plate] - vs[cath];
		if (Math.abs(this.lastv0 - vs[0]) > .01 || Math.abs(this.lastv1 - vs[1]) > .01
				|| Math.abs(this.lastv2 - vs[2]) > .01)
		{
			CircuitElm.cirSim.converged = false;
		}
		this.lastv0 = vs[0];
		this.lastv1 = vs[1];
		this.lastv2 = vs[2];
		double ids = 0;
		double gm = 0;
		double Gds = 0;
		double ival = vgk + vpk / this.mu;
		this.currentg = 0;
		if (vgk > .01)
		{
			CircuitElm.cirSim.stampResistor(this.nodes[grid], this.nodes[cath], this.gridCurrentR);
			this.currentg = vgk / this.gridCurrentR;
		}
		if (ival < 0)
		{
			// should be all zero, but that causes a singular matrix,
			// so instead we treat it as a large resistor
			Gds = 1e-8;
			ids = vpk * Gds;
		}
		else
		{
			ids = Math.pow(ival, 1.5) / this.kg1;
			double q = 1.5 * Math.sqrt(ival) / this.kg1;
			// gm = dids/dgk;
			// Gds = dids/dpk;
			Gds = q;
			gm = q / this.mu;
		}
		this.currentp = ids;
		this.currentc = ids + this.currentg;
		double rs = -ids + Gds * vpk + gm * vgk;
		CircuitElm.cirSim.stampMatrix(this.nodes[plate], this.nodes[plate], Gds);
		CircuitElm.cirSim.stampMatrix(this.nodes[plate], this.nodes[cath], -Gds - gm);
		CircuitElm.cirSim.stampMatrix(this.nodes[plate], this.nodes[grid], gm);

		CircuitElm.cirSim.stampMatrix(this.nodes[cath], this.nodes[plate], -Gds);
		CircuitElm.cirSim.stampMatrix(this.nodes[cath], this.nodes[cath], Gds + gm);
		CircuitElm.cirSim.stampMatrix(this.nodes[cath], this.nodes[grid], -gm);

		CircuitElm.cirSim.stampRightSide(this.nodes[plate], rs);
		CircuitElm.cirSim.stampRightSide(this.nodes[cath], -rs);
	}

	@Override
	void stamp()
	{
		CircuitElm.cirSim.stampNonLinear(this.nodes[0]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[1]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[2]);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "triode";
		double vbc = this.volts[0] - this.volts[1];
		double vbe = this.volts[0] - this.volts[2];
		double vce = this.volts[1] - this.volts[2];
		arr[1] = "Vbe = " + CircuitElm.getVoltageText(vbe);
		arr[2] = "Vbc = " + CircuitElm.getVoltageText(vbc);
		arr[3] = "Vce = " + CircuitElm.getVoltageText(vce);
	}

	// grid not connected to other terminals
	@Override
	boolean getConnection(int n1, int n2)
	{
		return !(n1 == 1 || n2 == 1);
	}
}
