package com.limoilux.circuit;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

class TappedTransformerElm extends CircuitElm
{
	double inductance, ratio;
	Point ptEnds[], ptCoil[], ptCore[];
	double current[], curcount[];

	public TappedTransformerElm(int xx, int yy)
	{
		super(xx, yy);
		this.inductance = 4;
		this.ratio = 1;
		this.noDiagonal = true;
		this.current = new double[4];
		this.curcount = new double[4];
	}

	public TappedTransformerElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.inductance = new Double(st.nextToken()).doubleValue();
		this.ratio = new Double(st.nextToken()).doubleValue();
		this.current = new double[4];
		this.curcount = new double[4];
		this.current[0] = new Double(st.nextToken()).doubleValue();
		this.current[1] = new Double(st.nextToken()).doubleValue();
		try
		{
			this.current[2] = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
		}
		this.noDiagonal = true;
	}

	@Override
	int getDumpType()
	{
		return 169;
	}

	@Override
	String dump()
	{
		return super.dump() + " " + this.inductance + " " + this.ratio + " " + this.current[0] + " " + this.current[1] + " " + this.current[2];
	}

	@Override
	void draw(Graphics g)
	{
		int i;
		for (i = 0; i != 5; i++)
		{
			this.setVoltageColor(g, this.volts[i]);
			CircuitElm.drawThickLine(g, this.ptEnds[i], this.ptCoil[i]);
		}
		for (i = 0; i != 4; i++)
		{
			if (i == 1)
			{
				continue;
			}
			this.setPowerColor(g, this.current[i] * (this.volts[i] - this.volts[i + 1]));
			this.drawCoil(g, i > 1 ? -6 : 6, this.ptCoil[i], this.ptCoil[i + 1], this.volts[i], this.volts[i + 1]);
		}
		g.setColor(this.needsHighlight() ? CircuitElm.selectColor : CircuitElm.lightGrayColor);
		for (i = 0; i != 4; i += 2)
		{
			CircuitElm.drawThickLine(g, this.ptCore[i], this.ptCore[i + 1]);
		}
		// calc current of tap wire
		this.current[3] = this.current[1] - this.current[2];
		for (i = 0; i != 4; i++)
		{
			this.curcount[i] = this.updateDotCount(this.current[i], this.curcount[i]);
		}

		// primary dots
		this.drawDots(g, this.ptEnds[0], this.ptCoil[0], this.curcount[0]);
		this.drawDots(g, this.ptCoil[0], this.ptCoil[1], this.curcount[0]);
		this.drawDots(g, this.ptCoil[1], this.ptEnds[1], this.curcount[0]);

		// secondary dots
		this.drawDots(g, this.ptEnds[2], this.ptCoil[2], this.curcount[1]);
		this.drawDots(g, this.ptCoil[2], this.ptCoil[3], this.curcount[1]);
		this.drawDots(g, this.ptCoil[3], this.ptEnds[3], this.curcount[3]);
		this.drawDots(g, this.ptCoil[3], this.ptCoil[4], this.curcount[2]);
		this.drawDots(g, this.ptCoil[4], this.ptEnds[4], this.curcount[2]);

		this.drawPosts(g);
		this.setBbox(this.ptEnds[0], this.ptEnds[4], 0);
	}

	@Override
	void setPoints()
	{
		super.setPoints();
		int hs = 32;
		this.ptEnds = this.newPointArray(5);
		this.ptCoil = this.newPointArray(5);
		this.ptCore = this.newPointArray(4);
		this.ptEnds[0] = this.point1;
		this.ptEnds[2] = this.point2;
		this.interpPoint(this.point1, this.point2, this.ptEnds[1], 0, -hs * 2);
		this.interpPoint(this.point1, this.point2, this.ptEnds[3], 1, -hs);
		this.interpPoint(this.point1, this.point2, this.ptEnds[4], 1, -hs * 2);
		double ce = .5 - 12 / this.dn;
		double cd = .5 - 2 / this.dn;
		int i;
		this.interpPoint(this.ptEnds[0], this.ptEnds[2], this.ptCoil[0], ce);
		this.interpPoint(this.ptEnds[0], this.ptEnds[2], this.ptCoil[1], ce, -hs * 2);
		this.interpPoint(this.ptEnds[0], this.ptEnds[2], this.ptCoil[2], 1 - ce);
		this.interpPoint(this.ptEnds[0], this.ptEnds[2], this.ptCoil[3], 1 - ce, -hs);
		this.interpPoint(this.ptEnds[0], this.ptEnds[2], this.ptCoil[4], 1 - ce, -hs * 2);
		for (i = 0; i != 2; i++)
		{
			int b = -hs * i * 2;
			this.interpPoint(this.ptEnds[0], this.ptEnds[2], this.ptCore[i], cd, b);
			this.interpPoint(this.ptEnds[0], this.ptEnds[2], this.ptCore[i + 2], 1 - cd, b);
		}
	}

	@Override
	Point getPost(int n)
	{
		return this.ptEnds[n];
	}

	@Override
	int getPostCount()
	{
		return 5;
	}

	@Override
	void reset()
	{
		this.current[0] = this.current[1] = this.volts[0] = this.volts[1] = this.volts[2] = this.volts[3] = this.curcount[0] = this.curcount[1] = 0;
	}

	double a[];

	@Override
	void stamp()
	{
		// equations for transformer:
		// v1 = L1 di1/dt + M1 di2/dt + M1 di3/dt
		// v2 = M1 di1/dt + L2 di2/dt + M2 di3/dt
		// v3 = M1 di1/dt + M2 di2/dt + L2 di3/dt
		// we invert that to get:
		// di1/dt = a1 v1 + a2 v2 + a3 v3
		// di2/dt = a4 v1 + a5 v2 + a6 v3
		// di3/dt = a7 v1 + a8 v2 + a9 v3
		// integrate di1/dt using trapezoidal approx and we get:
		// i1(t2) = i1(t1) + dt/2 (i1(t1) + i1(t2))
		// = i1(t1) + a1 dt/2 v1(t1)+a2 dt/2 v2(t1)+a3 dt/2 v3(t3) +
		// a1 dt/2 v1(t2)+a2 dt/2 v2(t2)+a3 dt/2 v3(t3)
		// the norton equivalent of this for i1 is:
		// a. current source, I = i1(t1) + a1 dt/2 v1(t1) + a2 dt/2 v2(t1)
		// + a3 dt/2 v3(t1)
		// b. resistor, G = a1 dt/2
		// c. current source controlled by voltage v2, G = a2 dt/2
		// d. current source controlled by voltage v3, G = a3 dt/2
		// and similarly for i2
		//
		// first winding goes from node 0 to 1, second is from 2 to 3 to 4
		double l1 = this.inductance;
		// second winding is split in half, so each part has half the turns;
		// we square the 1/2 to divide by 4
		double l2 = this.inductance * this.ratio * this.ratio / 4;
		double cc = .99;
		// double m1 = .999*Math.sqrt(l1*l2);
		// mutual inductance between two halves of the second winding
		// is equal to self-inductance of either half (slightly less
		// because the coupling is not perfect)
		// double m2 = .999*l2;
		this.a = new double[9];
		// load pre-inverted matrix
		this.a[0] = (1 + cc) / (l1 * (1 + cc - 2 * cc * cc));
		this.a[1] = this.a[2] = this.a[3] = this.a[6] = 2 * cc / ((2 * cc * cc - cc - 1) * this.inductance * this.ratio);
		this.a[4] = this.a[8] = -4 * (1 + cc) / ((2 * cc * cc - cc - 1) * l1 * this.ratio * this.ratio);
		this.a[5] = this.a[7] = 4 * cc / ((2 * cc * cc - cc - 1) * l1 * this.ratio * this.ratio);
		int i;
		for (i = 0; i != 9; i++)
		{
			this.a[i] *= CircuitElm.sim.timeStep / 2;
		}
		CircuitElm.sim.stampConductance(this.nodes[0], this.nodes[1], this.a[0]);
		CircuitElm.sim.stampVCCurrentSource(this.nodes[0], this.nodes[1], this.nodes[2], this.nodes[3], this.a[1]);
		CircuitElm.sim.stampVCCurrentSource(this.nodes[0], this.nodes[1], this.nodes[3], this.nodes[4], this.a[2]);

		CircuitElm.sim.stampVCCurrentSource(this.nodes[2], this.nodes[3], this.nodes[0], this.nodes[1], this.a[3]);
		CircuitElm.sim.stampConductance(this.nodes[2], this.nodes[3], this.a[4]);
		CircuitElm.sim.stampVCCurrentSource(this.nodes[2], this.nodes[3], this.nodes[3], this.nodes[4], this.a[5]);

		CircuitElm.sim.stampVCCurrentSource(this.nodes[3], this.nodes[4], this.nodes[0], this.nodes[1], this.a[6]);
		CircuitElm.sim.stampVCCurrentSource(this.nodes[3], this.nodes[4], this.nodes[2], this.nodes[3], this.a[7]);
		CircuitElm.sim.stampConductance(this.nodes[3], this.nodes[4], this.a[8]);

		for (i = 0; i != 5; i++)
		{
			CircuitElm.sim.stampRightSide(this.nodes[i]);
		}
		this.voltdiff = new double[3];
		this.curSourceValue = new double[3];
	}

	@Override
	void startIteration()
	{
		this.voltdiff[0] = this.volts[0] - this.volts[1];
		this.voltdiff[1] = this.volts[2] - this.volts[3];
		this.voltdiff[2] = this.volts[3] - this.volts[4];
		int i, j;
		for (i = 0; i != 3; i++)
		{
			this.curSourceValue[i] = this.current[i];
			for (j = 0; j != 3; j++)
			{
				this.curSourceValue[i] += this.a[i * 3 + j] * this.voltdiff[j];
			}
		}
	}

	double curSourceValue[], voltdiff[];

	@Override
	void doStep()
	{
		CircuitElm.sim.stampCurrentSource(this.nodes[0], this.nodes[1], this.curSourceValue[0]);
		CircuitElm.sim.stampCurrentSource(this.nodes[2], this.nodes[3], this.curSourceValue[1]);
		CircuitElm.sim.stampCurrentSource(this.nodes[3], this.nodes[4], this.curSourceValue[2]);
	}

	@Override
	void calculateCurrent()
	{
		this.voltdiff[0] = this.volts[0] - this.volts[1];
		this.voltdiff[1] = this.volts[2] - this.volts[3];
		this.voltdiff[2] = this.volts[3] - this.volts[4];
		int i, j;
		for (i = 0; i != 3; i++)
		{
			this.current[i] = this.curSourceValue[i];
			for (j = 0; j != 3; j++)
			{
				this.current[i] += this.a[i * 3 + j] * this.voltdiff[j];
			}
		}
	}

	@Override
	void getInfo(String arr[])
	{
		arr[0] = "transformer";
		arr[1] = "L = " + CircuitElm.getUnitText(this.inductance, "H");
		arr[2] = "Ratio = " + this.ratio;
		// arr[3] = "I1 = " + getCurrentText(current1);
		arr[3] = "Vd1 = " + CircuitElm.getVoltageText(this.volts[0] - this.volts[2]);
		// arr[5] = "I2 = " + getCurrentText(current2);
		arr[4] = "Vd2 = " + CircuitElm.getVoltageText(this.volts[1] - this.volts[3]);
	}

	@Override
	boolean getConnection(int n1, int n2)
	{
		if (this.comparePair(n1, n2, 0, 1))
		{
			return true;
		}
		if (this.comparePair(n1, n2, 2, 3))
		{
			return true;
		}
		if (this.comparePair(n1, n2, 3, 4))
		{
			return true;
		}
		if (this.comparePair(n1, n2, 2, 4))
		{
			return true;
		}
		return false;
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Primary Inductance (H)", this.inductance, .01, 5);
		}
		if (n == 1)
		{
			return new EditInfo("Ratio", this.ratio, 1, 10).setDimensionless();
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.inductance = ei.value;
		}
		if (n == 1)
		{
			this.ratio = ei.value;
		}
	}
}
