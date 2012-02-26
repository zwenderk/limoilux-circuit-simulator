
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Graphics;
import java.util.StringTokenizer;

import com.limoilux.circuit.techno.CircuitAnalysisException;
import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuit.ui.EditInfo;

public class InductorElm extends CircuitElm
{
	Inductor ind;
	public double inductance;

	public InductorElm(int xx, int yy)
	{
		super(xx, yy);
		this.ind = new Inductor(CircuitElm.cirSim);
		this.inductance = 1;
		this.ind.setup(this.inductance, this.current, this.flags);
	}

	public InductorElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.ind = new Inductor(CircuitElm.cirSim);
		this.inductance = new Double(st.nextToken()).doubleValue();
		this.current = new Double(st.nextToken()).doubleValue();
		this.ind.setup(this.inductance, this.current, this.flags);
	}

	@Override
	public int getDumpType()
	{
		return 'l';
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.inductance + " " + this.current;
	}

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.calcLeads(32);
	}

	@Override
	public void draw(Graphics g)
	{
		double v1 = this.volts[0];
		double v2 = this.volts[1];
		int i;
		int hs = 8;
		this.setBbox(this.point1, this.point2, hs);
		this.draw2Leads(g);
		this.setPowerColor(g, false);
		this.drawCoil(g, 8, this.lead1, this.lead2, v1, v2);
		if (CircuitElm.cirSim.showValuesCheckItem.getState())
		{
			String s = CircuitElm.getShortUnitText(this.inductance, "H");
			this.drawValues(g, s, hs);
		}
		this.doDots(g);
		this.drawPosts(g);
	}

	@Override
	public void reset()
	{
		this.current = this.volts[0] = this.volts[1] = this.curcount = 0;
		this.ind.reset();
	}

	@Override
	public void stamp()
	{
		this.ind.stamp(this.nodes[0], this.nodes[1]);
	}

	@Override
	public void startIteration()
	{
		this.ind.startIteration(this.volts[0] - this.volts[1]);
	}

	@Override
	public boolean nonLinear()
	{
		return this.ind.nonLinear();
	}

	@Override
	public void calculateCurrent()
	{
		double voltdiff = this.volts[0] - this.volts[1];
		this.current = this.ind.calculateCurrent(voltdiff);
	}

	@Override
	public void doStep() throws CircuitAnalysisException
	{
		double voltdiff = this.volts[0] - this.volts[1];
		this.ind.doStep(voltdiff);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "inductor";
		this.getBasicInfo(arr);
		arr[3] = "L = " + CircuitElm.getUnitText(this.inductance, "H");
		arr[4] = "P = " + CircuitElm.getUnitText(this.getPower(), "W");
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Inductance (H)", this.inductance, 0, 0);
		}
		if (n == 1)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Trapezoidal Approximation", this.ind.isTrapezoidal());
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
			if (ei.checkbox.getState())
			{
				this.flags &= ~Inductor.FLAG_BACK_EULER;
			}
			else
			{
				this.flags |= Inductor.FLAG_BACK_EULER;
			}
		}
		this.ind.setup(this.inductance, this.current, this.flags);
	}
}
