
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

import com.limoilux.circuitsimulator.circuit.CircuitElm;
import com.limoilux.circuitsimulator.core.CoreUtil;
import com.limoilux.circuitsimulator.ui.DrawUtil;
import com.limoilux.circuitsimulator.ui.EditInfo;

public class ProbeElm extends CircuitElm
{
	static final int FLAG_SHOWVOLTAGE = 1;
	private Point center;

	public ProbeElm(int xx, int yy)
	{
		super(xx, yy);
	}

	public ProbeElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
	}

	@Override
	public int getElementId()
	{
		return 'p';
	}



	@Override
	public void setPoints()
	{
		super.setPoints();
		
		int x;
		int y;
		
		// swap points so that we subtract higher from lower
		if (this.point2.y < this.point1.y)
		{
			x = this.point1.x;
			y = this.point1.y;
			
			this.point1.setLocation(this.point2);
			this.point2.setLocation(x, y);
		}
		
		this.center = CoreUtil.interpPoint(this.point1, this.point2, .5);
	}

	@Override
	public void draw(Graphics g)
	{
		int hs = 8;
		this.setBbox(this.point1, this.point2, hs);
		boolean selected = this.needsHighlight() || CircuitElm.cirSim.plotYElm == this;
		double len = selected || CircuitElm.cirSim.mouseMan.dragElm == this ? 16 : this.longueur - 32;
		this.calcLeads((int) len);
		this.setVoltageColor(g, this.volts[0]);
		if (selected)
		{
			g.setColor(CircuitElm.SELECT_COLOR);
		}
		DrawUtil.drawThickLine(g, this.point1, this.lead1);
		this.setVoltageColor(g, this.volts[1]);
		if (selected)
		{
			g.setColor(CircuitElm.SELECT_COLOR);
		}
		DrawUtil.drawThickLine(g, this.lead2, this.point2);
		Font f = new Font("SansSerif", Font.BOLD, 14);
		g.setFont(f);
		if (this == CircuitElm.cirSim.mouseMan.plotXElm)
		{
			this.drawCenteredText(g, "X", this.center.x, this.center.y, true);
		}
		if (this == CircuitElm.cirSim.plotYElm)
		{
			this.drawCenteredText(g, "Y", this.center.x, this.center.y, true);
		}
		if (this.mustShowVoltage())
		{
			String s = CoreUtil.getShortUnitText(this.volts[0], "V");
			this.drawValues(g, s, 4);
		}
		this.drawPosts(g);
	}

	boolean mustShowVoltage()
	{
		return (this.flags & ProbeElm.FLAG_SHOWVOLTAGE) != 0;
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "scope probe";
		arr[1] = "Vd = " + CoreUtil.getVoltageText(this.getVoltageDiff());
	}

	@Override
	public boolean getConnection(int n1, int n2)
	{
		return false;
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Show Voltage", this.mustShowVoltage());
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			if (ei.checkbox.getState())
			{
				this.flags = ProbeElm.FLAG_SHOWVOLTAGE;
			}
			else
			{
				this.flags &= ~ProbeElm.FLAG_SHOWVOLTAGE;
			}
		}
	}
}
