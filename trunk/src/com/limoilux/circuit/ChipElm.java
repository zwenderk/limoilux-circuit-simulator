package com.limoilux.circuit;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

abstract class ChipElm extends CircuitElm
{
	int csize, cspc, cspc2;
	int bits;
	final int FLAG_SMALL = 1;
	final int FLAG_FLIP_X = 1024;
	final int FLAG_FLIP_Y = 2048;

	public ChipElm(int xx, int yy)
	{
		super(xx, yy);
		if (this.needsBits())
		{
			this.bits = this instanceof DecadeElm ? 10 : 4;
		}
		this.noDiagonal = true;
		this.setupPins();
		this.setSize(CircuitElm.cirSim.smallGridCheckItem.getState() ? 1 : 2);
	}

	public ChipElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		if (this.needsBits())
		{
			this.bits = new Integer(st.nextToken()).intValue();
		}
		this.noDiagonal = true;
		this.setupPins();
		this.setSize((f & this.FLAG_SMALL) != 0 ? 1 : 2);
		int i;
		for (i = 0; i != this.getPostCount(); i++)
		{
			if (this.pins[i].state)
			{
				this.volts[i] = new Double(st.nextToken()).doubleValue();
				this.pins[i].value = this.volts[i] > 2.5;
			}
		}
	}

	boolean needsBits()
	{
		return false;
	}

	void setSize(int s)
	{
		this.csize = s;
		this.cspc = 8 * s;
		this.cspc2 = this.cspc * 2;
		this.flags &= ~this.FLAG_SMALL;
		this.flags |= s == 1 ? this.FLAG_SMALL : 0;
	}

	abstract void setupPins();

	@Override
	void draw(Graphics g)
	{
		this.drawChip(g);
	}

	void drawChip(Graphics g)
	{
		int i;
		Font f = new Font("SansSerif", 0, 10 * this.csize);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		for (i = 0; i != this.getPostCount(); i++)
		{
			Pin p = this.pins[i];
			this.setVoltageColor(g, this.volts[i]);
			Point a = p.post;
			Point b = p.stub;
			CircuitElm.drawThickLine(g, a, b);
			p.curcount = this.updateDotCount(p.current, p.curcount);
			this.drawDots(g, b, a, p.curcount);
			if (p.bubble)
			{
				g.setColor(CircuitElm.cirSim.printableCheckItem.getState() ? Color.white : Color.black);
				CircuitElm.drawThickCircle(g, p.bubbleX, p.bubbleY, 1);
				g.setColor(CircuitElm.lightGrayColor);
				CircuitElm.drawThickCircle(g, p.bubbleX, p.bubbleY, 3);
			}
			g.setColor(CircuitElm.whiteColor);
			int sw = fm.stringWidth(p.text);
			g.drawString(p.text, p.textloc.x - sw / 2, p.textloc.y + fm.getAscent() / 2);
			if (p.lineOver)
			{
				int ya = p.textloc.y - fm.getAscent() / 2;
				g.drawLine(p.textloc.x - sw / 2, ya, p.textloc.x + sw / 2, ya);
			}
		}
		g.setColor(this.needsHighlight() ? CircuitElm.selectColor : CircuitElm.lightGrayColor);
		CircuitElm.drawThickPolygon(g, this.rectPointsX, this.rectPointsY, 4);
		if (this.clockPointsX != null)
		{
			g.drawPolyline(this.clockPointsX, this.clockPointsY, 3);
		}
		for (i = 0; i != this.getPostCount(); i++)
		{
			this.drawPost(g, this.pins[i].post.x, this.pins[i].post.y, this.nodes[i]);
		}
	}

	int rectPointsX[], rectPointsY[];
	int clockPointsX[], clockPointsY[];
	Pin pins[];
	int sizeX, sizeY;
	boolean lastClock;

	@Override
	void drag(int xx, int yy)
	{
		yy = CircuitElm.cirSim.snapGrid(yy);
		if (xx < this.x)
		{
			xx = this.x;
			yy = this.y;
		}
		else
		{
			this.y = this.y2 = yy;
			this.x2 = CircuitElm.cirSim.snapGrid(xx);
		}
		this.setPoints();
	}

	@Override
	void setPoints()
	{
		if (this.x2 - this.x > this.sizeX * this.cspc2 && this == CircuitElm.cirSim.dragElm)
		{
			this.setSize(2);
		}
		int hs = this.cspc;
		int x0 = this.x + this.cspc2;
		int y0 = this.y;
		int xr = x0 - this.cspc;
		int yr = y0 - this.cspc;
		int xs = this.sizeX * this.cspc2;
		int ys = this.sizeY * this.cspc2;
		this.rectPointsX = new int[]
				{ xr, xr + xs, xr + xs, xr };
		this.rectPointsY = new int[]
				{ yr, yr, yr + ys, yr + ys };
		this.setBbox(xr, yr, this.rectPointsX[2], this.rectPointsY[2]);
		int i;
		for (i = 0; i != this.getPostCount(); i++)
		{
			Pin p = this.pins[i];
			switch (p.side)
			{
			case SIDE_N:
				p.setPoint(x0, y0, 1, 0, 0, -1, 0, 0);
				break;
			case SIDE_S:
				p.setPoint(x0, y0, 1, 0, 0, 1, 0, ys - this.cspc2);
				break;
			case SIDE_W:
				p.setPoint(x0, y0, 0, 1, -1, 0, 0, 0);
				break;
			case SIDE_E:
				p.setPoint(x0, y0, 0, 1, 1, 0, xs - this.cspc2, 0);
				break;
			}
		}
	}

	@Override
	Point getPost(int n)
	{
		return this.pins[n].post;
	}

	@Override
	abstract int getVoltageSourceCount(); // output count

	@Override
	void setVoltageSource(int j, int vs)
	{
		int i;
		for (i = 0; i != this.getPostCount(); i++)
		{
			Pin p = this.pins[i];
			if (p.output && j-- == 0)
			{
				p.voltSource = vs;
				return;
			}
		}
		System.out.println("setVoltageSource failed for " + this);
	}

	@Override
	void stamp()
	{
		int i;
		for (i = 0; i != this.getPostCount(); i++)
		{
			Pin p = this.pins[i];
			if (p.output)
			{
				CircuitElm.cirSim.stampVoltageSource(0, this.nodes[i], p.voltSource);
			}
		}
	}

	void execute()
	{
	}

	@Override
	void doStep()
	{
		int i;
		for (i = 0; i != this.getPostCount(); i++)
		{
			Pin p = this.pins[i];
			if (!p.output)
			{
				p.value = this.volts[i] > 2.5;
			}
		}
		this.execute();
		for (i = 0; i != this.getPostCount(); i++)
		{
			Pin p = this.pins[i];
			if (p.output)
			{
				CircuitElm.cirSim.updateVoltageSource(0, this.nodes[i], p.voltSource, p.value ? 5 : 0);
			}
		}
	}

	@Override
	void reset()
	{
		int i;
		for (i = 0; i != this.getPostCount(); i++)
		{
			this.pins[i].value = false;
			this.pins[i].curcount = 0;
			this.volts[i] = 0;
		}
		this.lastClock = false;
	}

	@Override
	String dump()
	{
		int t = this.getDumpType();
		String s = super.dump();
		if (this.needsBits())
		{
			s += " " + this.bits;
		}
		int i;
		for (i = 0; i != this.getPostCount(); i++)
		{
			if (this.pins[i].state)
			{
				s += " " + this.volts[i];
			}
		}
		return s;
	}

	@Override
	void getInfo(String arr[])
	{
		arr[0] = this.getChipName();
		int i, a = 1;
		for (i = 0; i != this.getPostCount(); i++)
		{
			Pin p = this.pins[i];
			if (arr[a] != null)
			{
				arr[a] += "; ";
			}
			else
			{
				arr[a] = "";
			}
			String t = p.text;
			if (p.lineOver)
			{
				t += '\'';
			}
			if (p.clock)
			{
				t = "Clk";
			}
			arr[a] += t + " = " + CircuitElm.getVoltageText(this.volts[i]);
			if (i % 2 == 1)
			{
				a++;
			}
		}
	}

	@Override
	void setCurrent(int x, double c)
	{
		int i;
		for (i = 0; i != this.getPostCount(); i++)
		{
			if (this.pins[i].output && this.pins[i].voltSource == x)
			{
				this.pins[i].current = c;
			}
		}
	}

	String getChipName()
	{
		return "chip";
	}

	@Override
	boolean getConnection(int n1, int n2)
	{
		return false;
	}

	@Override
	boolean hasGroundConnection(int n1)
	{
		return this.pins[n1].output;
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Flip X", (this.flags & this.FLAG_FLIP_X) != 0);
			return ei;
		}
		if (n == 1)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Flip Y", (this.flags & this.FLAG_FLIP_Y) != 0);
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
				this.flags |= this.FLAG_FLIP_X;
			}
			else
			{
				this.flags &= ~this.FLAG_FLIP_X;
			}
			this.setPoints();
		}
		if (n == 1)
		{
			if (ei.checkbox.getState())
			{
				this.flags |= this.FLAG_FLIP_Y;
			}
			else
			{
				this.flags &= ~this.FLAG_FLIP_Y;
			}
			this.setPoints();
		}
	}

	final int SIDE_N = 0;
	final int SIDE_S = 1;
	final int SIDE_W = 2;
	final int SIDE_E = 3;

	class Pin
	{
		Pin(int p, int s, String t)
		{
			this.pos = p;
			this.side = s;
			this.text = t;
		}

		Point post, stub;
		Point textloc;
		int pos, side, voltSource, bubbleX, bubbleY;
		String text;
		boolean lineOver, bubble, clock, output, value, state;
		double curcount, current;

		void setPoint(int px, int py, int dx, int dy, int dax, int day, int sx, int sy)
		{
			if ((ChipElm.this.flags & ChipElm.this.FLAG_FLIP_X) != 0)
			{
				dx = -dx;
				dax = -dax;
				px += ChipElm.this.cspc2 * (ChipElm.this.sizeX - 1);
				sx = -sx;
			}
			if ((ChipElm.this.flags & ChipElm.this.FLAG_FLIP_Y) != 0)
			{
				dy = -dy;
				day = -day;
				py += ChipElm.this.cspc2 * (ChipElm.this.sizeY - 1);
				sy = -sy;
			}
			int xa = px + ChipElm.this.cspc2 * dx * this.pos + sx;
			int ya = py + ChipElm.this.cspc2 * dy * this.pos + sy;
			this.post = new Point(xa + dax * ChipElm.this.cspc2, ya + day * ChipElm.this.cspc2);
			this.stub = new Point(xa + dax * ChipElm.this.cspc, ya + day * ChipElm.this.cspc);
			this.textloc = new Point(xa, ya);
			if (this.bubble)
			{
				this.bubbleX = xa + dax * 10 * ChipElm.this.csize;
				this.bubbleY = ya + day * 10 * ChipElm.this.csize;
			}
			if (this.clock)
			{
				ChipElm.this.clockPointsX = new int[3];
				ChipElm.this.clockPointsY = new int[3];
				ChipElm.this.clockPointsX[0] = xa + dax * ChipElm.this.cspc - dx * ChipElm.this.cspc / 2;
				ChipElm.this.clockPointsY[0] = ya + day * ChipElm.this.cspc - dy * ChipElm.this.cspc / 2;
				ChipElm.this.clockPointsX[1] = xa;
				ChipElm.this.clockPointsY[1] = ya;
				ChipElm.this.clockPointsX[2] = xa + dax * ChipElm.this.cspc + dx * ChipElm.this.cspc / 2;
				ChipElm.this.clockPointsY[2] = ya + day * ChipElm.this.cspc + dy * ChipElm.this.cspc / 2;
			}
		}
	}
}
