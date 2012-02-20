package com.limoilux.circuit;
import java.awt.Checkbox;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

class Switch2Elm extends SwitchElm
{
	int link;
	static final int FLAG_CENTER_OFF = 1;

	public Switch2Elm(int xx, int yy)
	{
		super(xx, yy, false);
		this.noDiagonal = true;
	}

	Switch2Elm(int xx, int yy, boolean mm)
	{
		super(xx, yy, mm);
		this.noDiagonal = true;
	}

	public Switch2Elm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
		this.link = new Integer(st.nextToken()).intValue();
		this.noDiagonal = true;
	}

	@Override
	int getDumpType()
	{
		return 'S';
	}

	@Override
	String dump()
	{
		return super.dump() + " " + this.link;
	}

	final int openhs = 16;
	Point swposts[], swpoles[];

	@Override
	void setPoints()
	{
		super.setPoints();
		this.calcLeads(32);
		this.swposts = this.newPointArray(2);
		this.swpoles = this.newPointArray(3);
		this.interpPoint2(this.lead1, this.lead2, this.swpoles[0], this.swpoles[1], 1, this.openhs);
		this.swpoles[2] = this.lead2;
		this.interpPoint2(this.point1, this.point2, this.swposts[0], this.swposts[1], 1, this.openhs);
		this.posCount = this.hasCenterOff() ? 3 : 2;
	}

	@Override
	void draw(Graphics g)
	{
		this.setBbox(this.point1, this.point2, this.openhs);

		// draw first lead
		this.setVoltageColor(g, this.volts[0]);
		CircuitElm.drawThickLine(g, this.point1, this.lead1);

		// draw second lead
		this.setVoltageColor(g, this.volts[1]);
		CircuitElm.drawThickLine(g, this.swpoles[0], this.swposts[0]);

		// draw third lead
		this.setVoltageColor(g, this.volts[2]);
		CircuitElm.drawThickLine(g, this.swpoles[1], this.swposts[1]);

		// draw switch
		if (!this.needsHighlight())
		{
			g.setColor(CircuitElm.whiteColor);
		}
		CircuitElm.drawThickLine(g, this.lead1, this.swpoles[this.position]);

		this.updateDotCount();
		this.drawDots(g, this.point1, this.lead1, this.curcount);
		if (this.position != 2)
		{
			this.drawDots(g, this.swpoles[this.position], this.swposts[this.position], this.curcount);
		}
		this.drawPosts(g);
	}

	@Override
	Point getPost(int n)
	{
		return n == 0 ? this.point1 : this.swposts[n - 1];
	}

	@Override
	int getPostCount()
	{
		return 3;
	}

	@Override
	void calculateCurrent()
	{
		if (this.position == 2)
		{
			this.current = 0;
		}
	}

	@Override
	void stamp()
	{
		if (this.position == 2)
		{
			return;
		}
		CircuitElm.sim.stampVoltageSource(this.nodes[0], this.nodes[this.position + 1], this.voltSource, 0);
	}

	@Override
	int getVoltageSourceCount()
	{
		return this.position == 2 ? 0 : 1;
	}

	@Override
	void toggle()
	{
		super.toggle();
		if (this.link != 0)
		{
			int i;
			for (i = 0; i != CircuitElm.sim.elmList.size(); i++)
			{
				Object o = CircuitElm.sim.elmList.elementAt(i);
				if (o instanceof Switch2Elm)
				{
					Switch2Elm s2 = (Switch2Elm) o;
					if (s2.link == this.link)
					{
						s2.position = this.position;
					}
				}
			}
		}
	}

	@Override
	boolean getConnection(int n1, int n2)
	{
		if (this.position == 2)
		{
			return false;
		}
		return this.comparePair(n1, n2, 0, 1 + this.position);
	}

	@Override
	void getInfo(String arr[])
	{
		arr[0] = this.link == 0 ? "switch (SPDT)" : "switch (DPDT)";
		arr[1] = "I = " + CircuitElm.getCurrentDText(this.getCurrent());
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 1)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Center Off", this.hasCenterOff());
			return ei;
		}
		return super.getEditInfo(n);
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 1)
		{
			this.flags &= ~Switch2Elm.FLAG_CENTER_OFF;
			if (ei.checkbox.getState())
			{
				this.flags |= Switch2Elm.FLAG_CENTER_OFF;
			}
			if (this.hasCenterOff())
			{
				this.momentary = false;
			}
			this.setPoints();
		}
		else
		{
			super.setEditValue(n, ei);
		}
	}

	boolean hasCenterOff()
	{
		return (this.flags & Switch2Elm.FLAG_CENTER_OFF) != 0;
	}
}
