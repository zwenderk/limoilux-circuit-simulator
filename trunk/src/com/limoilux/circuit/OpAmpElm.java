
package com.limoilux.circuit;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.limoilux.circuit.core.CoreUtil;
import com.limoilux.circuit.techno.CircuitAnalysisException;
import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuit.ui.EditInfo;

public class OpAmpElm extends CircuitElm
{
	int opsize, opheight, opwidth, opaddtext;
	double maxOut, minOut, gain, gbw;
	boolean reset;
	final int FLAG_SWAP = 1;
	final int FLAG_SMALL = 2;
	final int FLAG_LOWGAIN = 4;

	public OpAmpElm(int xx, int yy)
	{
		super(xx, yy);
		this.noDiagonal = true;
		this.maxOut = 15;
		this.minOut = -15;
		this.gbw = 1e6;
		this.setSize(CircuitElm.cirSim.smallGridCheckItem.getState() ? 1 : 2);
		this.setGain();
	}

	public OpAmpElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.maxOut = 15;
		this.minOut = -15;
		// GBW has no effect in this version of the simulator, but we
		// retain it to keep the file format the same
		this.gbw = 1e6;
		try
		{
			this.maxOut = new Double(st.nextToken()).doubleValue();
			this.minOut = new Double(st.nextToken()).doubleValue();
			this.gbw = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
		}
		this.noDiagonal = true;
		this.setSize((f & this.FLAG_SMALL) != 0 ? 1 : 2);
		this.setGain();
	}

	void setGain()
	{
		// gain of 100000 breaks e-amp-dfdx.txt
		// gain was 1000, but it broke amp-schmitt.txt
		this.gain = (this.flags & this.FLAG_LOWGAIN) != 0 ? 1000 : 100000;

	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.maxOut + " " + this.minOut + " " + this.gbw;
	}

	@Override
	public boolean nonLinear()
	{
		return true;
	}

	@Override
	public void draw(Graphics g)
	{
		this.setBbox(this.point1, this.point2, this.opheight * 2);
		this.setVoltageColor(g, this.volts[0]);
		CircuitElm.drawThickLine(g, this.in1p[0], this.in1p[1]);
		this.setVoltageColor(g, this.volts[1]);
		CircuitElm.drawThickLine(g, this.in2p[0], this.in2p[1]);
		g.setColor(this.needsHighlight() ? CircuitElm.SELECT_COLOR : CircuitElm.LIGHT_GRAY_COLOR);
		this.setPowerColor(g, true);
		CircuitElm.drawThickPolygon(g, this.triangle);
		g.setFont(this.plusFont);
		this.drawCenteredText(g, "-", this.textp[0].x, this.textp[0].y - 2, true);
		this.drawCenteredText(g, "+", this.textp[1].x, this.textp[1].y, true);
		this.setVoltageColor(g, this.volts[2]);
		CircuitElm.drawThickLine(g, this.lead2, this.point2);
		this.curcount = CircuitElm.updateDotCount(this.current, this.curcount);
		CircuitElm.drawDots(g, this.point2, this.lead2, this.curcount);
		this.drawPosts(g);
	}

	@Override
	public double getPower()
	{
		return this.volts[2] * this.current;
	}

	Point in1p[], in2p[], textp[];
	Polygon triangle;
	Font plusFont;

	void setSize(int s)
	{
		this.opsize = s;
		this.opheight = 8 * s;
		this.opwidth = 13 * s;
		this.flags = this.flags & ~this.FLAG_SMALL | (s == 1 ? this.FLAG_SMALL : 0);
	}

	@Override
	public void setPoints()
	{
		super.setPoints();
		if (this.dn > 150 && this == CircuitElm.cirSim.dragElm)
		{
			this.setSize(2);
		}
		int ww = this.opwidth;
		if (ww > this.dn / 2)
		{
			ww = (int) (this.dn / 2);
		}
		this.calcLeads(ww * 2);
		int hs = this.opheight * this.dsign;
		if ((this.flags & this.FLAG_SWAP) != 0)
		{
			hs = -hs;
		}
		this.in1p = CircuitElm.newPointArray(2);
		this.in2p = CircuitElm.newPointArray(2);
		this.textp = CircuitElm.newPointArray(2);
		CircuitElm.interpPoint2(this.point1, this.point2, this.in1p[0], this.in2p[0], 0, hs);
		CircuitElm.interpPoint2(this.lead1, this.lead2, this.in1p[1], this.in2p[1], 0, hs);
		CircuitElm.interpPoint2(this.lead1, this.lead2, this.textp[0], this.textp[1], .2, hs);
		Point tris[] = CircuitElm.newPointArray(2);
		CircuitElm.interpPoint2(this.lead1, this.lead2, tris[0], tris[1], 0, hs * 2);
		this.triangle = CircuitElm.createPolygon(tris[0], tris[1], this.lead2);
		this.plusFont = new Font("SansSerif", 0, this.opsize == 2 ? 14 : 10);
	}

	@Override
	public int getPostCount()
	{
		return 3;
	}

	@Override
	public Point getPost(int n)
	{
		return n == 0 ? this.in1p[0] : n == 1 ? this.in2p[0] : this.point2;
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "op-amp";
		arr[1] = "V+ = " + CoreUtil.getVoltageText(this.volts[1]);
		arr[2] = "V- = " + CoreUtil.getVoltageText(this.volts[0]);
		// sometimes the voltage goes slightly outside range, to make
		// convergence easier. so we hide that here.
		double vo = Math.max(Math.min(this.volts[2], this.maxOut), this.minOut);
		arr[3] = "Vout = " + CoreUtil.getVoltageText(vo);
		arr[4] = "Iout = " + CoreUtil.getCurrentText(this.getCurrent());
		arr[5] = "range = " + CoreUtil.getVoltageText(this.minOut) + " to " + CircuitElm.getVoltageText(this.maxOut);
	}

	double lastvd;

	@Override
	public void stamp()
	{
		int vn = CircuitElm.cirSim.circuit.getNodeCount() + this.voltSource;
		CircuitElm.cirSim.circuit.stampNonLinear(vn);
		CircuitElm.cirSim.circuit.stampMatrix(this.nodes[2], vn, 1);
	}

	@Override
	public void doStep() throws CircuitAnalysisException
	{
		double vd = this.volts[1] - this.volts[0];
		if (Math.abs(this.lastvd - vd) > .1)
		{
			CircuitElm.cirSim.circuit.converged = false;
		}
		else if (this.volts[2] > this.maxOut + .1 || this.volts[2] < this.minOut - .1)
		{
			CircuitElm.cirSim.circuit.converged = false;
		}
		double x = 0;
		int vn = CircuitElm.cirSim.circuit.getNodeCount() + this.voltSource;
		double dx = 0;
		if (vd >= this.maxOut / this.gain && (this.lastvd >= 0 || CoreUtil.getRandomInt(4) == 1))
		{
			dx = 1e-4;
			x = this.maxOut - dx * this.maxOut / this.gain;
		}
		else if (vd <= this.minOut / this.gain && (this.lastvd <= 0 || CoreUtil.getRandomInt(4) == 1))
		{
			dx = 1e-4;
			x = this.minOut - dx * this.minOut / this.gain;
		}
		else
		{
			dx = this.gain;
			// System.out.println("opamp " + vd + " " + volts[2] + " " + dx +
			// " " +
			// x + " " + lastvd + " " + sim.converged);
		}

		// newton-raphson
		CircuitElm.cirSim.circuit.stampMatrix(vn, this.nodes[0], dx);
		CircuitElm.cirSim.circuit.stampMatrix(vn, this.nodes[1], -dx);
		CircuitElm.cirSim.circuit.stampMatrix(vn, this.nodes[2], 1);
		CircuitElm.cirSim.circuit.stampRightSide(vn, x);

		this.lastvd = vd;
		/*
		 * if (sim.converged) System.out.println((volts[1]-volts[0]) + " " +
		 * volts[2] + " " + initvd);
		 */
	}

	// there is no current path through the op-amp inputs, but there
	// is an indirect path through the output to ground.
	@Override
	public boolean getConnection(int n1, int n2)
	{
		return false;
	}

	@Override
	public boolean hasGroundConnection(int n1)
	{
		return n1 == 2;
	}

	@Override
	public double getVoltageDiff()
	{
		return this.volts[2] - this.volts[1];
	}

	@Override
	public int getDumpType()
	{
		return 'a';
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Max Output (V)", this.maxOut, 1, 20);
		}
		if (n == 1)
		{
			return new EditInfo("Min Output (V)", this.minOut, -20, 0);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.maxOut = ei.value;
		}
		if (n == 1)
		{
			this.minOut = ei.value;
		}
	}
}
