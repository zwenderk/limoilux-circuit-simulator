
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

abstract class GateElm extends CircuitElm
{
	final int FLAG_SMALL = 1;
	int inputCount = 2;
	boolean lastOutput;

	public GateElm(int xx, int yy)
	{
		super(xx, yy);
		this.noDiagonal = true;
		this.inputCount = 2;
		this.setSize(CircuitElm.cirSim.smallGridCheckItem.getState() ? 1 : 2);
	}

	public GateElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.inputCount = new Integer(st.nextToken()).intValue();
		this.lastOutput = new Double(st.nextToken()).doubleValue() > 2.5;
		this.noDiagonal = true;
		this.setSize((f & this.FLAG_SMALL) != 0 ? 1 : 2);
	}

	boolean isInverting()
	{
		return false;
	}

	int gsize, gwidth, gwidth2, gheight, hs2;

	void setSize(int s)
	{
		this.gsize = s;
		this.gwidth = 7 * s;
		this.gwidth2 = 14 * s;
		this.gheight = 8 * s;
		this.flags = s == 1 ? this.FLAG_SMALL : 0;
	}

	@Override
	String dump()
	{
		return super.dump() + " " + this.inputCount + " " + this.volts[this.inputCount];
	}

	Point inPosts[], inGates[];
	int ww;

	@Override
	void setPoints()
	{
		super.setPoints();
		if (this.dn > 150 && this == CircuitElm.cirSim.dragElm)
		{
			this.setSize(2);
		}
		int hs = this.gheight;
		int i;
		this.ww = this.gwidth2; // was 24
		if (this.ww > this.dn / 2)
		{
			this.ww = (int) (this.dn / 2);
		}
		if (this.isInverting() && this.ww + 8 > this.dn / 2)
		{
			this.ww = (int) (this.dn / 2 - 8);
		}
		this.calcLeads(this.ww * 2);
		this.inPosts = new Point[this.inputCount];
		this.inGates = new Point[this.inputCount];
		this.allocNodes();
		int i0 = -this.inputCount / 2;
		for (i = 0; i != this.inputCount; i++, i0++)
		{
			if (i0 == 0 && (this.inputCount & 1) == 0)
			{
				i0++;
			}
			this.inPosts[i] = this.interpPoint(this.point1, this.point2, 0, hs * i0);
			this.inGates[i] = this.interpPoint(this.lead1, this.lead2, 0, hs * i0);
			this.volts[i] = this.lastOutput ^ this.isInverting() ? 5 : 0;
		}
		this.hs2 = this.gwidth * (this.inputCount / 2 + 1);
		this.setBbox(this.point1, this.point2, this.hs2);
	}

	@Override
	void draw(Graphics g)
	{
		int i;
		for (i = 0; i != this.inputCount; i++)
		{
			this.setVoltageColor(g, this.volts[i]);
			CircuitElm.drawThickLine(g, this.inPosts[i], this.inGates[i]);
		}
		this.setVoltageColor(g, this.volts[this.inputCount]);
		CircuitElm.drawThickLine(g, this.lead2, this.point2);
		g.setColor(this.needsHighlight() ? CircuitElm.selectColor : CircuitElm.lightGrayColor);
		CircuitElm.drawThickPolygon(g, this.gatePoly);
		if (this.linePoints != null)
		{
			for (i = 0; i != this.linePoints.length - 1; i++)
			{
				CircuitElm.drawThickLine(g, this.linePoints[i], this.linePoints[i + 1]);
			}
		}
		if (this.isInverting())
		{
			CircuitElm.drawThickCircle(g, this.pcircle.x, this.pcircle.y, 3);
		}
		this.curcount = this.updateDotCount(this.current, this.curcount);
		this.drawDots(g, this.lead2, this.point2, this.curcount);
		this.drawPosts(g);
	}

	Polygon gatePoly;
	Point pcircle, linePoints[];

	@Override
	int getPostCount()
	{
		return this.inputCount + 1;
	}

	@Override
	Point getPost(int n)
	{
		if (n == this.inputCount)
		{
			return this.point2;
		}
		return this.inPosts[n];
	}

	@Override
	int getVoltageSourceCount()
	{
		return 1;
	}

	abstract String getGateName();

	@Override
	void getInfo(String arr[])
	{
		arr[0] = this.getGateName();
		arr[1] = "Vout = " + CircuitElm.getVoltageText(this.volts[this.inputCount]);
		arr[2] = "Iout = " + CircuitElm.getCurrentText(this.getCurrent());
	}

	@Override
	void stamp()
	{
		CircuitElm.cirSim.stampVoltageSource(0, this.nodes[this.inputCount], this.voltSource);
	}

	boolean getInput(int x)
	{
		return this.volts[x] > 2.5;
	}

	abstract boolean calcFunction();

	@Override
	void doStep()
	{
		int i;
		boolean f = this.calcFunction();
		if (this.isInverting())
		{
			f = !f;
		}
		this.lastOutput = f;
		double res = f ? 5 : 0;
		CircuitElm.cirSim.updateVoltageSource(0, this.nodes[this.inputCount], this.voltSource, res);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("# of Inputs", this.inputCount, 1, 8).setDimensionless();
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		this.inputCount = (int) ei.value;
		this.setPoints();
	}

	// there is no current path through the gate inputs, but there
	// is an indirect path through the output to ground.
	@Override
	boolean getConnection(int n1, int n2)
	{
		return false;
	}

	@Override
	boolean hasGroundConnection(int n1)
	{
		return n1 == this.inputCount;
	}
}
