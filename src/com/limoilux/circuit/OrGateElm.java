
package com.limoilux.circuit;

import java.awt.Point;
import java.util.StringTokenizer;

import com.limoilux.circuit.techno.CircuitElm;

public class OrGateElm extends GateElm
{
	public OrGateElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public OrGateElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
	}

	@Override
	public String getGateName()
	{
		return "OR gate";
	}

	@Override
	public void setPoints()
	{
		super.setPoints();

		// 0-15 = top curve, 16 = right, 17-32=bottom curve,
		// 33-37 = left curve
		Point triPoints[] = CircuitElm.newPointArray(38);
		if (this instanceof XorGateElm)
		{
			this.linePoints = new Point[5];
		}
		int i;
		for (i = 0; i != 16; i++)
		{
			double a = i / 16.;
			double b = 1 - a * a;
			CircuitElm.interpPoint2(this.lead1, this.lead2, triPoints[i], triPoints[32 - i], .5 + a / 2, b * this.hs2);
		}
		double ww2 = this.ww == 0 ? this.dn * 2 : this.ww * 2;
		for (i = 0; i != 5; i++)
		{
			double a = (i - 2) / 2.;
			double b = 4 * (1 - a * a) - 2;
			CircuitElm.interpPoint(this.lead1, this.lead2, triPoints[33 + i], b / ww2, a * this.hs2);
			if (this instanceof XorGateElm)
			{
				this.linePoints[i] = CircuitElm.interpPoint(this.lead1, this.lead2, (b - 5) / ww2, a * this.hs2);
			}
		}
		triPoints[16] = new Point(this.lead2);
		if (this.isInverting())
		{
			this.pcircle = CircuitElm.interpPoint(this.point1, this.point2, .5 + (this.ww + 4) / this.dn);
			this.lead2 = CircuitElm.interpPoint(this.point1, this.point2, .5 + (this.ww + 8) / this.dn);
		}
		this.gatePoly = CircuitElm.createPolygon(triPoints);
	}

	@Override
	public boolean calcFunction()
	{
		int i;
		boolean f = false;
		for (i = 0; i != this.inputCount; i++)
		{
			f |= this.getInput(i);
		}
		return f;
	}

	@Override
	public int getDumpType()
	{
		return 152;
	}
}
