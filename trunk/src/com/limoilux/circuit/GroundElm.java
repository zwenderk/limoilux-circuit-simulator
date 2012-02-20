package com.limoilux.circuit;
import java.awt.Graphics;
import java.util.StringTokenizer;

class GroundElm extends CircuitElm
{
	public GroundElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public GroundElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
	}

	@Override
	int getDumpType()
	{
		return 'g';
	}

	@Override
	int getPostCount()
	{
		return 1;
	}

	@Override
	void draw(Graphics g)
	{
		this.setVoltageColor(g, 0);
		CircuitElm.drawThickLine(g, this.point1, this.point2);
		int i;
		for (i = 0; i != 3; i++)
		{
			int a = 10 - i * 4;
			int b = i * 5; // -10;
			this.interpPoint2(this.point1, this.point2, CircuitElm.ps1, CircuitElm.ps2, 1 + b / this.dn, a);
			CircuitElm.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
		}
		this.doDots(g);
		this.interpPoint(this.point1, this.point2, CircuitElm.ps2, 1 + 11. / this.dn);
		this.setBbox(this.point1, CircuitElm.ps2, 11);
		this.drawPost(g, this.x, this.y, this.nodes[0]);
	}

	@Override
	void setCurrent(int x, double c)
	{
		this.current = -c;
	}

	@Override
	void stamp()
	{
		CircuitElm.cirSim.stampVoltageSource(0, this.nodes[0], this.voltSource, 0);
	}

	@Override
	double getVoltageDiff()
	{
		return 0;
	}

	@Override
	int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	void getInfo(String arr[])
	{
		arr[0] = "ground";
		arr[1] = "I = " + CircuitElm.getCurrentText(this.getCurrent());
	}

	@Override
	boolean hasGroundConnection(int n1)
	{
		return true;
	}

	@Override
	boolean needsShortcut()
	{
		return true;
	}
}
