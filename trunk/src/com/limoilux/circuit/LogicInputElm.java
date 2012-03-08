
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Font;
import java.awt.Graphics;
import java.util.StringTokenizer;

import com.limoilux.circuitsimulator.circuit.CircuitElm;
import com.limoilux.circuitsimulator.ui.DrawUtil;
import com.limoilux.circuitsimulator.ui.EditInfo;

public class LogicInputElm extends SwitchElm
{
	final int FLAG_TERNARY = 1;
	final int FLAG_NUMERIC = 2;
	double hiV, loV;

	public LogicInputElm(int xx, int yy)
	{
		super(xx, yy, false);
		this.hiV = 5;
		this.loV = 0;
	}

	public LogicInputElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
		try
		{
			this.hiV = new Double(st.nextToken()).doubleValue();
			this.loV = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
			this.hiV = 5;
			this.loV = 0;
		}
		if (this.isTernary())
		{
			this.posCount = 3;
		}
	}

	boolean isTernary()
	{
		return (this.flags & this.FLAG_TERNARY) != 0;
	}

	boolean isNumeric()
	{
		return (this.flags & (this.FLAG_TERNARY | this.FLAG_NUMERIC)) != 0;
	}

	@Override
	public int getElementId()
	{
		return 'L';
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.hiV + " " + this.loV;
	}

	@Override
	public int getPostCount()
	{
		return 1;
	}

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.lead1 = CircuitElm.interpPoint(this.point1, this.point2, 1 - 12 / this.longueur);
	}

	@Override
	public void draw(Graphics g)
	{
		Font f = new Font("SansSerif", Font.BOLD, 20);
		g.setFont(f);
		g.setColor(this.needsHighlight() ? CircuitElm.SELECT_COLOR : CircuitElm.WHITE_COLOR);
		String s = this.position == 0 ? "L" : "H";
		if (this.isNumeric())
		{
			s = "" + this.position;
		}
		this.setBbox(this.point1, this.lead1, 0);
		this.drawCenteredText(g, s, this.x2, this.y2, true);
		this.setVoltageColor(g, this.volts[0]);
		DrawUtil.drawThickLine(g, this.point1, this.lead1);
		this.updateDotCount();
		DrawUtil.drawDots(g, this.point1, this.lead1, this.curcount);
		this.drawPosts(g);
	}

	@Override
	public void setCurrent(int vs, double c)
	{
		this.current = -c;
	}

	@Override
	public void stamp()
	{
		double v = this.position == 0 ? this.loV : this.hiV;
		if (this.isTernary())
		{
			v = this.position * 2.5;
		}
		CircuitElm.cirSim.circuit.stampVoltageSource(0, this.nodes[0], this.voltSource, v);
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	public double getVoltageDiff()
	{
		return this.volts[0];
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "logic input";
		arr[1] = this.position == 0 ? "low" : "high";
		if (this.isNumeric())
		{
			arr[1] = "" + this.position;
		}
		arr[1] += " (" + CircuitElm.getVoltageText(this.volts[0]) + ")";
		arr[2] = "I = " + CircuitElm.getCurrentText(this.getCurrent());
	}

	@Override
	public boolean hasGroundConnection(int n1)
	{
		return true;
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			EditInfo ei = new EditInfo("", 0, 0, 0);
			ei.checkbox = new Checkbox("Momentary Switch", this.momentary);
			return ei;
		}
		if (n == 1)
		{
			return new EditInfo("High Voltage", this.hiV, 10, -10);
		}
		if (n == 2)
		{
			return new EditInfo("Low Voltage", this.loV, 10, -10);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.momentary = ei.checkbox.getState();
		}
		if (n == 1)
		{
			this.hiV = ei.value;
		}
		if (n == 2)
		{
			this.loV = ei.value;
		}
	}
}
