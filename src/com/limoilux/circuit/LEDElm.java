
package com.limoilux.circuit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

class LEDElm extends DiodeElm
{
	double colorR, colorG, colorB;

	public LEDElm(int xx, int yy)
	{
		super(xx, yy);
		this.fwdrop = 2.1024259;
		this.setup();
		this.colorR = 1;
		this.colorG = this.colorB = 0;
	}

	public LEDElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
		if ((f & DiodeElm.FLAG_FWDROP) == 0)
		{
			this.fwdrop = 2.1024259;
		}
		this.setup();
		this.colorR = new Double(st.nextToken()).doubleValue();
		this.colorG = new Double(st.nextToken()).doubleValue();
		this.colorB = new Double(st.nextToken()).doubleValue();
	}

	@Override
	public int getDumpType()
	{
		return 162;
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.colorR + " " + this.colorG + " " + this.colorB;
	}

	Point ledLead1, ledLead2, ledCenter;

	@Override
	public void setPoints()
	{
		super.setPoints();
		int cr = 12;
		this.ledLead1 = CircuitElm.interpPoint(this.point1, this.point2, .5 - cr / this.dn);
		this.ledLead2 = CircuitElm.interpPoint(this.point1, this.point2, .5 + cr / this.dn);
		this.ledCenter = CircuitElm.interpPoint(this.point1, this.point2, .5);
	}

	@Override
	public void draw(Graphics g)
	{
		if (this.needsHighlight() || this == CircuitElm.cirSim.dragElm)
		{
			super.draw(g);
			return;
		}
		this.setVoltageColor(g, this.volts[0]);
		CircuitElm.drawThickLine(g, this.point1, this.ledLead1);
		this.setVoltageColor(g, this.volts[1]);
		CircuitElm.drawThickLine(g, this.ledLead2, this.point2);

		g.setColor(Color.gray);
		int cr = 12;
		CircuitElm.drawThickCircle(g, this.ledCenter.x, this.ledCenter.y, cr);
		cr -= 4;
		double w = 255 * this.current / .01;
		if (w > 255)
		{
			w = 255;
		}
		Color cc = new Color((int) (this.colorR * w), (int) (this.colorG * w), (int) (this.colorB * w));
		g.setColor(cc);
		g.fillOval(this.ledCenter.x - cr, this.ledCenter.y - cr, cr * 2, cr * 2);
		this.setBbox(this.point1, this.point2, cr);
		this.updateDotCount();
		CircuitElm.drawDots(g, this.point1, this.ledLead1, this.curcount);
		CircuitElm.drawDots(g, this.point2, this.ledLead2, -this.curcount);
		this.drawPosts(g);
	}

	@Override
	public void getInfo(String arr[])
	{
		super.getInfo(arr);
		arr[0] = "LED";
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return super.getEditInfo(n);
		}
		if (n == 1)
		{
			return new EditInfo("Red Value (0-1)", this.colorR, 0, 1).setDimensionless();
		}
		if (n == 2)
		{
			return new EditInfo("Green Value (0-1)", this.colorG, 0, 1).setDimensionless();
		}
		if (n == 3)
		{
			return new EditInfo("Blue Value (0-1)", this.colorB, 0, 1).setDimensionless();
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			super.setEditValue(0, ei);
		}
		if (n == 1)
		{
			this.colorR = ei.value;
		}
		if (n == 2)
		{
			this.colorG = ei.value;
		}
		if (n == 3)
		{
			this.colorB = ei.value;
		}
	}
}
