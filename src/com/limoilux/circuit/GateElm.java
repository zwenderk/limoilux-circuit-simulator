
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.limoilux.circuit.core.CircuitElm;
import com.limoilux.circuit.core.CoreUtil;
import com.limoilux.circuit.ui.DrawUtil;
import com.limoilux.circuit.ui.EditInfo;

public abstract class GateElm extends CircuitElm
{
	public static final int FLAG_SMALL = 1;
	public int inputCount = 2;
	public boolean lastOutput;
	public int gsize, gwidth, gwidth2, gheight, hs2;

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
		this.setSize((f & GateElm.FLAG_SMALL) != 0 ? 1 : 2);
	}

	public boolean isInverting()
	{
		return false;
	}

	public void setSize(int s)
	{
		this.gsize = s;
		this.gwidth = 7 * s;
		this.gwidth2 = 14 * s;
		this.gheight = 8 * s;
		this.flags = s == 1 ? GateElm.FLAG_SMALL : 0;
	}

	Point inPosts[], inGates[];
	int ww;

	Polygon gatePoly;
	Point pcircle, linePoints[];

	public boolean getInput(int x)
	{
		return this.volts[x] > 2.5;
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.inputCount + " " + this.volts[this.inputCount];
	}

	@Override
	public void setPoints()
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
			this.inPosts[i] = CoreUtil.interpPoint(this.point1, this.point2, 0, hs * i0);
			this.inGates[i] = CoreUtil.interpPoint(this.lead1, this.lead2, 0, hs * i0);
			this.volts[i] = this.lastOutput ^ this.isInverting() ? 5 : 0;
		}
		this.hs2 = this.gwidth * (this.inputCount / 2 + 1);
		this.setBbox(this.point1, this.point2, this.hs2);
	}

	@Override
	public void draw(Graphics g)
	{
		int i;
		for (i = 0; i != this.inputCount; i++)
		{
			this.setVoltageColor(g, this.volts[i]);
			DrawUtil.drawThickLine(g, this.inPosts[i], this.inGates[i]);
		}
		this.setVoltageColor(g, this.volts[this.inputCount]);
		DrawUtil.drawThickLine(g, this.lead2, this.point2);
		g.setColor(this.needsHighlight() ? CircuitElm.selectColor : CircuitElm.lightGrayColor);
		DrawUtil.drawThickPolygon(g, this.gatePoly);
		if (this.linePoints != null)
		{
			for (i = 0; i != this.linePoints.length - 1; i++)
			{
				DrawUtil.drawThickLine(g, this.linePoints[i], this.linePoints[i + 1]);
			}
		}
		if (this.isInverting())
		{
			DrawUtil.drawThickCircle(g, this.pcircle.x, this.pcircle.y, 3);
		}
		this.curcount = CircuitElm.updateDotCount(this.current, this.curcount);
		DrawUtil.drawDots(g, this.lead2, this.point2, this.curcount);
		this.drawPosts(g);
	}

	@Override
	public int getPostCount()
	{
		return this.inputCount + 1;
	}

	@Override
	public Point getPost(int n)
	{
		if (n == this.inputCount)
		{
			return this.point2;
		}
		return this.inPosts[n];
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = this.getGateName();
		arr[1] = "Vout = " + CircuitElm.getVoltageText(this.volts[this.inputCount]);
		arr[2] = "Iout = " + CircuitElm.getCurrentText(this.getCurrent());
	}

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.circuit.stampVoltageSource(0, this.nodes[this.inputCount], this.voltSource);
	}

	@Override
	public void doStep()
	{
		int i;
		boolean f = this.calcFunction();
		if (this.isInverting())
		{
			f = !f;
		}
		this.lastOutput = f;
		double res = f ? 5 : 0;
		CircuitElm.cirSim.circuit.updateVoltageSource(0, this.nodes[this.inputCount], this.voltSource, res);
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
	public boolean getConnection(int n1, int n2)
	{
		return false;
	}

	@Override
	public boolean hasGroundConnection(int n1)
	{
		return n1 == this.inputCount;
	}

	public abstract boolean calcFunction();

	public abstract String getGateName();
}
