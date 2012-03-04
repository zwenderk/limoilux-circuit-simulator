
package com.limoilux.circuit;

import java.awt.Point;
import java.util.StringTokenizer;

import com.limoilux.circuit.techno.CircuitElm;

public class AndGateElm extends GateElm
{
	public AndGateElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public AndGateElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	public void setPoints()
	{
		super.setPoints();

		// 0=topleft, 1-10 = top curve, 11 = right, 12-21=bottom curve,
		// 22 = bottom left
		Point triPoints[] = CircuitElm.newPointArray(23);
		CircuitElm.interpPoint2(this.lead1, this.lead2, triPoints[0], triPoints[22], 0, this.hs2);
		int i;
		for (i = 0; i != 10; i++)
		{
			double a = i * .1;
			double b = Math.sqrt(1 - a * a);
			CircuitElm.interpPoint2(this.lead1, this.lead2, triPoints[i + 1], triPoints[21 - i], .5 + a / 2, b
					* this.hs2);
		}
		triPoints[11] = new Point(this.lead2);
		if (this.isInverting())
		{
			this.pcircle = CircuitElm.interpPoint(this.point1, this.point2, .5 + (this.ww + 4) / this.dn);
			this.lead2 = CircuitElm.interpPoint(this.point1, this.point2, .5 + (this.ww + 8) / this.dn);
		}
		this.gatePoly = CircuitElm.createPolygon(triPoints);
	}

	@Override
	public String getGateName()
	{
		return "AND gate";
	}

	@Override
	public boolean calcFunction()
	{
		int i;
		boolean f = true;
		for (i = 0; i != this.inputCount; i++)
		{
			f &= this.getInput(i);
		}
		return f;
	}

	@Override
	public int getElementId()
	{
		return 150;
	}
}
