
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

import com.limoilux.circuit.techno.CircuitAnalysisException;
import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuit.ui.DrawUtil;
import com.limoilux.circuit.ui.EditInfo;
import com.limoilux.circuitsimulator.core.CoreUtil;

public class TransformerElm extends CircuitElm
{
	double inductance, ratio, couplingCoef;
	Point ptEnds[], ptCoil[], ptCore[];
	double current[], curcount[];
	int width;
	public static final int FLAG_BACK_EULER = 2;

	public TransformerElm(int xx, int yy)
	{
		super(xx, yy);
		this.inductance = 4;
		this.ratio = 1;
		this.width = 32;
		this.noDiagonal = true;
		this.couplingCoef = .999;
		this.current = new double[2];
		this.curcount = new double[2];
	}

	public TransformerElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.width = CircuitElm.max(32, CircuitElm.abs(yb - ya));
		this.inductance = new Double(st.nextToken()).doubleValue();
		this.ratio = new Double(st.nextToken()).doubleValue();
		this.current = new double[2];
		this.curcount = new double[2];
		this.current[0] = new Double(st.nextToken()).doubleValue();
		this.current[1] = new Double(st.nextToken()).doubleValue();
		this.couplingCoef = .999;
		try
		{
			this.couplingCoef = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
		}
		this.noDiagonal = true;
	}

	@Override
	public void drag(int xx, int yy)
	{
		xx = CircuitElm.cirSim.snapGrid(xx);
		yy = CircuitElm.cirSim.snapGrid(yy);
		this.width = CircuitElm.max(32, CircuitElm.abs(yy - this.y));
		if (xx == this.x)
		{
			yy = this.y;
		}
		this.x2 = xx;
		this.y2 = yy;
		this.setPoints();
	}

	@Override
	public int getElementId()
	{
		return 'T';
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.inductance + " " + this.ratio + " " + this.current[0] + " " + this.current[1]
				+ " " + this.couplingCoef;
	}

	boolean isTrapezoidal()
	{
		return (this.flags & TransformerElm.FLAG_BACK_EULER) == 0;
	}

	@Override
	public void draw(Graphics g)
	{
		int i;
		for (i = 0; i != 4; i++)
		{
			this.setVoltageColor(g, this.volts[i]);
			CircuitElm.drawThickLine(g, this.ptEnds[i], this.ptCoil[i]);
		}
		for (i = 0; i != 2; i++)
		{
			CircuitElm.setPowerColor(g, this.current[i] * (this.volts[i] - this.volts[i + 2]));
			this.drawCoil(g, this.dsign * (i == 1 ? -6 : 6), this.ptCoil[i], this.ptCoil[i + 2], this.volts[i],
					this.volts[i + 2]);
		}
		g.setColor(this.needsHighlight() ? CircuitElm.SELECT_COLOR : CircuitElm.LIGHT_GRAY_COLOR);
		for (i = 0; i != 2; i++)
		{
			CircuitElm.drawThickLine(g, this.ptCore[i], this.ptCore[i + 2]);
			this.curcount[i] = CircuitElm.updateDotCount(this.current[i], this.curcount[i]);
		}
		for (i = 0; i != 2; i++)
		{
			DrawUtil.drawDots(g, this.ptEnds[i], this.ptCoil[i], this.curcount[i]);
			DrawUtil.drawDots(g, this.ptCoil[i], this.ptCoil[i + 2], this.curcount[i]);
			DrawUtil.drawDots(g, this.ptEnds[i + 2], this.ptCoil[i + 2], -this.curcount[i]);
		}

		this.drawPosts(g);
		this.setBbox(this.ptEnds[0], this.ptEnds[3], 0);
	}

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.point2.y = this.point1.y;
		this.ptEnds = CoreUtil.newPointArray(4);
		this.ptCoil = CoreUtil.newPointArray(4);
		this.ptCore = CoreUtil.newPointArray(4);
		this.ptEnds[0] = this.point1;
		this.ptEnds[1] = this.point2;
		CoreUtil.interpPoint(this.point1, this.point2, this.ptEnds[2], 0, -this.dsign * this.width);
		CoreUtil.interpPoint(this.point1, this.point2, this.ptEnds[3], 1, -this.dsign * this.width);
		double ce = .5 - 12 / this.dn;
		double cd = .5 - 2 / this.dn;
		int i;
		for (i = 0; i != 4; i += 2)
		{
			CoreUtil.interpPoint(this.ptEnds[i], this.ptEnds[i + 1], this.ptCoil[i], ce);
			CoreUtil.interpPoint(this.ptEnds[i], this.ptEnds[i + 1], this.ptCoil[i + 1], 1 - ce);
			CoreUtil.interpPoint(this.ptEnds[i], this.ptEnds[i + 1], this.ptCore[i], cd);
			CoreUtil.interpPoint(this.ptEnds[i], this.ptEnds[i + 1], this.ptCore[i + 1], 1 - cd);
		}
	}

	@Override
	public Point getPost(int n)
	{
		return this.ptEnds[n];
	}

	@Override
	public int getPostCount()
	{
		return 4;
	}

	@Override
	public void reset()
	{
		this.current[0] = this.current[1] = this.volts[0] = this.volts[1] = this.volts[2] = this.volts[3] = this.curcount[0] = this.curcount[1] = 0;
	}

	double a1, a2, a3, a4;

	@Override
	public void stamp()
	{
		// equations for transformer:
		// v1 = L1 di1/dt + M di2/dt
		// v2 = M di1/dt + L2 di2/dt
		// we invert that to get:
		// di1/dt = a1 v1 + a2 v2
		// di2/dt = a3 v1 + a4 v2
		// integrate di1/dt using trapezoidal approx and we get:
		// i1(t2) = i1(t1) + dt/2 (i1(t1) + i1(t2))
		// = i1(t1) + a1 dt/2 v1(t1) + a2 dt/2 v2(t1) +
		// a1 dt/2 v1(t2) + a2 dt/2 v2(t2)
		// the norton equivalent of this for i1 is:
		// a. current source, I = i1(t1) + a1 dt/2 v1(t1) + a2 dt/2 v2(t1)
		// b. resistor, G = a1 dt/2
		// c. current source controlled by voltage v2, G = a2 dt/2
		// and for i2:
		// a. current source, I = i2(t1) + a3 dt/2 v1(t1) + a4 dt/2 v2(t1)
		// b. resistor, G = a3 dt/2
		// c. current source controlled by voltage v2, G = a4 dt/2
		//
		// For backward euler,
		//
		// i1(t2) = i1(t1) + a1 dt v1(t2) + a2 dt v2(t2)
		//
		// So the current source value is just i1(t1) and we use
		// dt instead of dt/2 for the resistor and VCCS.
		//
		// first winding goes from node 0 to 2, second is from 1 to 3
		double l1 = this.inductance;
		double l2 = this.inductance * this.ratio * this.ratio;
		double m = this.couplingCoef * Math.sqrt(l1 * l2);
		// build inverted matrix
		double deti = 1 / (l1 * l2 - m * m);
		double ts = this.isTrapezoidal() ? CircuitElm.cirSim.timer.timeStep / 2 : CircuitElm.cirSim.timer.timeStep;
		this.a1 = l2 * deti * ts; // we multiply dt/2 into a1..a4 here
		this.a2 = -m * deti * ts;
		this.a3 = -m * deti * ts;
		this.a4 = l1 * deti * ts;
		CircuitElm.cirSim.circuit.stampConductance(this.nodes[0], this.nodes[2], this.a1);
		CircuitElm.cirSim.circuit.stampVCCurrentSource(this.nodes[0], this.nodes[2], this.nodes[1], this.nodes[3],
				this.a2);
		CircuitElm.cirSim.circuit.stampVCCurrentSource(this.nodes[1], this.nodes[3], this.nodes[0], this.nodes[2],
				this.a3);
		CircuitElm.cirSim.circuit.stampConductance(this.nodes[1], this.nodes[3], this.a4);
		CircuitElm.cirSim.circuit.stampRightSide(this.nodes[0]);
		CircuitElm.cirSim.circuit.stampRightSide(this.nodes[1]);
		CircuitElm.cirSim.circuit.stampRightSide(this.nodes[2]);
		CircuitElm.cirSim.circuit.stampRightSide(this.nodes[3]);
	}

	@Override
	public void startIteration() throws CircuitAnalysisException
	{
		double voltdiff1 = this.volts[0] - this.volts[2];
		double voltdiff2 = this.volts[1] - this.volts[3];
		if (this.isTrapezoidal())
		{
			this.curSourceValue1 = voltdiff1 * this.a1 + voltdiff2 * this.a2 + this.current[0];
			this.curSourceValue2 = voltdiff1 * this.a3 + voltdiff2 * this.a4 + this.current[1];
		}
		else
		{
			this.curSourceValue1 = this.current[0];
			this.curSourceValue2 = this.current[1];
		}
	}

	double curSourceValue1, curSourceValue2;

	@Override
	public void doStep() throws CircuitAnalysisException
	{
		CircuitElm.cirSim.stampCurrentSource(this.nodes[0], this.nodes[2], this.curSourceValue1);
		CircuitElm.cirSim.stampCurrentSource(this.nodes[1], this.nodes[3], this.curSourceValue2);
	}

	@Override
	public void calculateCurrent()
	{
		double voltdiff1 = this.volts[0] - this.volts[2];
		double voltdiff2 = this.volts[1] - this.volts[3];
		this.current[0] = voltdiff1 * this.a1 + voltdiff2 * this.a2 + this.curSourceValue1;
		this.current[1] = voltdiff1 * this.a3 + voltdiff2 * this.a4 + this.curSourceValue2;
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "transformer";
		arr[1] = "L = " + CircuitElm.getUnitText(this.inductance, "H");
		arr[2] = "Ratio = 1:" + this.ratio;
		arr[3] = "Vd1 = " + CircuitElm.getVoltageText(this.volts[0] - this.volts[2]);
		arr[4] = "Vd2 = " + CircuitElm.getVoltageText(this.volts[1] - this.volts[3]);
		arr[5] = "I1 = " + CircuitElm.getCurrentText(this.current[0]);
		arr[6] = "I2 = " + CircuitElm.getCurrentText(this.current[1]);
	}

	@Override
	public boolean getConnection(int n1, int n2)
	{
		if (CircuitElm.comparePair(n1, n2, 0, 2))
		{
			return true;
		}
		if (CircuitElm.comparePair(n1, n2, 1, 3))
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
		if (n == 2)
		{
			return new EditInfo("Coupling Coefficient", this.couplingCoef, 0, 1).setDimensionless();
		}
		if (n == 3)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Trapezoidal Approximation", this.isTrapezoidal());
			return ei;
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
		if (n == 2 && ei.value > 0 && ei.value < 1)
		{
			this.couplingCoef = ei.value;
		}
		if (n == 3)
		{
			if (ei.checkbox.getState())
			{
				this.flags &= ~Inductor.FLAG_BACK_EULER;
			}
			else
			{
				this.flags |= Inductor.FLAG_BACK_EULER;
			}
		}
	}
}
