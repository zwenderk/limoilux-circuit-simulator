
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.limoilux.circuitsimulator.circuit.CircuitAnalysisException;
import com.limoilux.circuitsimulator.circuit.CircuitElm;
import com.limoilux.circuitsimulator.core.Configs;
import com.limoilux.circuitsimulator.core.CoreUtil;
import com.limoilux.circuitsimulator.ui.DrawUtil;
import com.limoilux.circuitsimulator.ui.EditInfo;

public class InverterElm extends CircuitElm
{
	double slewRate; // V/ns

	public InverterElm(int xx, int yy)
	{
		super(xx, yy);
		this.noDiagonal = true;
		this.slewRate = .5;
	}

	public InverterElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.noDiagonal = true;
		try
		{
			this.slewRate = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
			this.slewRate = .5;
		}
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.slewRate;
	}

	@Override
	public int getElementId()
	{
		return 'I';
	}

	@Override
	public void draw(Graphics g)
	{
		this.drawPosts(g);
		this.draw2Leads(g);
		g.setColor(this.needsHighlight() ? CircuitElm.SELECT_COLOR : CircuitElm.LIGHT_GRAY_COLOR);
		DrawUtil.drawThickPolygon(g, this.gatePoly);
		DrawUtil.drawThickCircle(g, this.pcircle.x, this.pcircle.y, 3);
		this.curcount = CoreUtil.updateDotCount(this.current, this.curcount, CircuitElm.cirSim.currentMultiplier);
		DrawUtil.drawDots(g, this.lead2, this.point2, this.curcount);
	}

	Polygon gatePoly;
	Point pcircle;

	@Override
	public void setPoints()
	{
		super.setPoints();
		int hs = 16;
		int ww = 16;
		if (ww > this.dn / 2)
		{
			ww = (int) (this.dn / 2);
		}
		this.lead1 = CoreUtil.interpPoint(this.point1, this.point2, .5 - ww / this.dn);
		this.lead2 = CoreUtil.interpPoint(this.point1, this.point2, .5 + (ww + 2) / this.dn);
		this.pcircle = CoreUtil.interpPoint(this.point1, this.point2, .5 + (ww - 2) / this.dn);
		Point triPoints[] = CoreUtil.newPointArray(3);
		CoreUtil.interpPoint2(this.lead1, this.lead2, triPoints[0], triPoints[1], 0, hs);
		triPoints[2] = CoreUtil.interpPoint(this.point1, this.point2, .5 + (ww - 5) / this.dn);
		this.gatePoly = CoreUtil.createPolygon(triPoints);
		this.setBbox(this.point1, this.point2, hs);
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.circuit.stampVoltageSource(0, this.nodes[1], this.voltSource);
	}

	@Override
	public void doStep() throws CircuitAnalysisException
	{
		double v0 = this.volts[1];
		double out = this.volts[0] > 2.5 ? 0 : 5;
		double maxStep = this.slewRate * Configs.TIME_STEP * 1e9;
		out = Math.max(Math.min(v0 + maxStep, out), v0 - maxStep);
		CircuitElm.cirSim.circuit.updateVoltageSource(0, this.nodes[1], this.voltSource, out);
	}

	@Override
	public double getVoltageDiff()
	{
		return this.volts[0];
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "inverter";
		arr[1] = "Vi = " + CircuitElm.getVoltageText(this.volts[0]);
		arr[2] = "Vo = " + CircuitElm.getVoltageText(this.volts[1]);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Slew Rate (V/ns)", this.slewRate, 0, 0);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		this.slewRate = ei.value;
	}

	// there is no current path through the inverter input, but there
	// is an indirect path through the output to ground.
	@Override
	public boolean getConnection(int n1, int n2)
	{
		return false;
	}

	@Override
	public boolean hasGroundConnection(int n1)
	{
		return n1 == 1;
	}
}
