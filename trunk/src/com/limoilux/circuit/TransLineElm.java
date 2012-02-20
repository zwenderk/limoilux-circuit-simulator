package com.limoilux.circuit;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

class TransLineElm extends CircuitElm
{
	double delay, imped;
	double voltageL[], voltageR[];
	int lenSteps, ptr, width;

	public TransLineElm(int xx, int yy)
	{
		super(xx, yy);
		this.delay = 1000 * CircuitElm.sim.timeStep;
		this.imped = 75;
		this.noDiagonal = true;
		this.reset();
	}

	public TransLineElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.delay = new Double(st.nextToken()).doubleValue();
		this.imped = new Double(st.nextToken()).doubleValue();
		this.width = new Integer(st.nextToken()).intValue();
		// next slot is for resistance (losses), which is not implemented
		st.nextToken();
		this.noDiagonal = true;
		this.reset();
	}

	@Override
	int getDumpType()
	{
		return 171;
	}

	@Override
	int getPostCount()
	{
		return 4;
	}

	@Override
	int getInternalNodeCount()
	{
		return 2;
	}

	@Override
	String dump()
	{
		return super.dump() + " " + this.delay + " " + this.imped + " " + this.width + " " + 0.;
	}

	@Override
	void drag(int xx, int yy)
	{
		xx = CircuitElm.sim.snapGrid(xx);
		yy = CircuitElm.sim.snapGrid(yy);
		int w1 = CircuitElm.max(CircuitElm.sim.gridSize, CircuitElm.abs(yy - this.y));
		int w2 = CircuitElm.max(CircuitElm.sim.gridSize, CircuitElm.abs(xx - this.x));
		if (w1 > w2)
		{
			xx = this.x;
			this.width = w2;
		}
		else
		{
			yy = this.y;
			this.width = w1;
		}
		this.x2 = xx;
		this.y2 = yy;
		this.setPoints();
	}

	Point posts[], inner[];

	@Override
	void reset()
	{
		if (CircuitElm.sim.timeStep == 0)
		{
			return;
		}
		this.lenSteps = (int) (this.delay / CircuitElm.sim.timeStep);
		System.out.println(this.lenSteps + " steps");
		if (this.lenSteps > 100000)
		{
			this.voltageL = this.voltageR = null;
		}
		else
		{
			this.voltageL = new double[this.lenSteps];
			this.voltageR = new double[this.lenSteps];
		}
		this.ptr = 0;
		super.reset();
	}

	@Override
	void setPoints()
	{
		super.setPoints();
		int ds = this.dy == 0 ? CircuitElm.sign(this.dx) : -CircuitElm.sign(this.dy);
		Point p3 = this.interpPoint(this.point1, this.point2, 0, -this.width * ds);
		Point p4 = this.interpPoint(this.point1, this.point2, 1, -this.width * ds);
		int sep = CircuitElm.sim.gridSize / 2;
		Point p5 = this.interpPoint(this.point1, this.point2, 0, -(this.width / 2 - sep) * ds);
		Point p6 = this.interpPoint(this.point1, this.point2, 1, -(this.width / 2 - sep) * ds);
		Point p7 = this.interpPoint(this.point1, this.point2, 0, -(this.width / 2 + sep) * ds);
		Point p8 = this.interpPoint(this.point1, this.point2, 1, -(this.width / 2 + sep) * ds);

		// we number the posts like this because we want the lower-numbered
		// points to be on the bottom, so that if some of them are unconnected
		// (which is often true) then the bottom ones will get automatically
		// attached to ground.
		this.posts = new Point[]
				{ p3, p4, this.point1, this.point2 };
		this.inner = new Point[]
				{ p7, p8, p5, p6 };
	}

	@Override
	void draw(Graphics g)
	{
		this.setBbox(this.posts[0], this.posts[3], 0);
		int segments = (int) (this.dn / 2);
		int ix0 = this.ptr - 1 + this.lenSteps;
		double segf = 1. / segments;
		int i;
		g.setColor(Color.darkGray);
		g.fillRect(this.inner[2].x, this.inner[2].y, this.inner[1].x - this.inner[2].x + 2, this.inner[1].y - this.inner[2].y + 2);
		for (i = 0; i != 4; i++)
		{
			this.setVoltageColor(g, this.volts[i]);
			CircuitElm.drawThickLine(g, this.posts[i], this.inner[i]);
		}
		if (this.voltageL != null)
		{
			for (i = 0; i != segments; i++)
			{
				int ix1 = (ix0 - this.lenSteps * i / segments) % this.lenSteps;
				int ix2 = (ix0 - this.lenSteps * (segments - 1 - i) / segments) % this.lenSteps;
				double v = (this.voltageL[ix1] + this.voltageR[ix2]) / 2;
				this.setVoltageColor(g, v);
				this.interpPoint(this.inner[0], this.inner[1], CircuitElm.ps1, i * segf);
				this.interpPoint(this.inner[2], this.inner[3], CircuitElm.ps2, i * segf);
				g.drawLine(CircuitElm.ps1.x, CircuitElm.ps1.y, CircuitElm.ps2.x, CircuitElm.ps2.y);
				this.interpPoint(this.inner[2], this.inner[3], CircuitElm.ps1, (i + 1) * segf);
				CircuitElm.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
			}
		}
		this.setVoltageColor(g, this.volts[0]);
		CircuitElm.drawThickLine(g, this.inner[0], this.inner[1]);
		this.drawPosts(g);

		this.curCount1 = this.updateDotCount(-this.current1, this.curCount1);
		this.curCount2 = this.updateDotCount(this.current2, this.curCount2);
		if (CircuitElm.sim.dragElm != this)
		{
			this.drawDots(g, this.posts[0], this.inner[0], this.curCount1);
			this.drawDots(g, this.posts[2], this.inner[2], -this.curCount1);
			this.drawDots(g, this.posts[1], this.inner[1], -this.curCount2);
			this.drawDots(g, this.posts[3], this.inner[3], this.curCount2);
		}
	}

	int voltSource1, voltSource2;
	double current1, current2, curCount1, curCount2;

	@Override
	void setVoltageSource(int n, int v)
	{
		if (n == 0)
		{
			this.voltSource1 = v;
		}
		else
		{
			this.voltSource2 = v;
		}
	}

	@Override
	void setCurrent(int v, double c)
	{
		if (v == this.voltSource1)
		{
			this.current1 = c;
		}
		else
		{
			this.current2 = c;
		}
	}

	@Override
	void stamp()
	{
		CircuitElm.sim.stampVoltageSource(this.nodes[4], this.nodes[0], this.voltSource1);
		CircuitElm.sim.stampVoltageSource(this.nodes[5], this.nodes[1], this.voltSource2);
		CircuitElm.sim.stampResistor(this.nodes[2], this.nodes[4], this.imped);
		CircuitElm.sim.stampResistor(this.nodes[3], this.nodes[5], this.imped);
	}

	@Override
	void startIteration()
	{
		// calculate voltages, currents sent over wire
		if (this.voltageL == null)
		{
			CircuitElm.sim.stop("Transmission line delay too large!", this);
			return;
		}
		this.voltageL[this.ptr] = this.volts[2] - this.volts[0] + this.volts[2] - this.volts[4];
		this.voltageR[this.ptr] = this.volts[3] - this.volts[1] + this.volts[3] - this.volts[5];
		// System.out.println(volts[2] + " " + volts[0] + " " +
		// (volts[2]-volts[0]) + " " + (imped*current1) + " " + voltageL[ptr]);
		/*
		 * System.out.println("sending fwd  " + currentL[ptr] + " " + current1);
		 * System.out.println("sending back " + currentR[ptr] + " " + current2);
		 */
		// System.out.println("sending back " + voltageR[ptr]);
		this.ptr = (this.ptr + 1) % this.lenSteps;
	}

	@Override
	void doStep()
	{
		if (this.voltageL == null)
		{
			CircuitElm.sim.stop("Transmission line delay too large!", this);
			return;
		}
		CircuitElm.sim.updateVoltageSource(this.nodes[4], this.nodes[0], this.voltSource1, -this.voltageR[this.ptr]);
		CircuitElm.sim.updateVoltageSource(this.nodes[5], this.nodes[1], this.voltSource2, -this.voltageL[this.ptr]);
		if (Math.abs(this.volts[0]) > 1e-5 || Math.abs(this.volts[1]) > 1e-5)
		{
			CircuitElm.sim.stop("Need to ground transmission line!", this);
			return;
		}
	}

	@Override
	Point getPost(int n)
	{
		return this.posts[n];
	}

	// double getVoltageDiff() { return volts[0]; }
	@Override
	int getVoltageSourceCount()
	{
		return 2;
	}

	@Override
	boolean hasGroundConnection(int n1)
	{
		return false;
	}

	@Override
	boolean getConnection(int n1, int n2)
	{
		return false;
		/*
		 * if (comparePair(n1, n2, 0, 1)) return true; if (comparePair(n1, n2,
		 * 2, 3)) return true; return false;
		 */
	}

	@Override
	void getInfo(String arr[])
	{
		arr[0] = "transmission line";
		arr[1] = CircuitElm.getUnitText(this.imped, CircuitElm.sim.ohmString);
		arr[2] = "length = " + CircuitElm.getUnitText(2.9979e8 * this.delay, "m");
		arr[3] = "delay = " + CircuitElm.getUnitText(this.delay, "s");
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Delay (s)", this.delay, 0, 0);
		}
		if (n == 1)
		{
			return new EditInfo("Impedance (ohms)", this.imped, 0, 0);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.delay = ei.value;
			this.reset();
		}
		if (n == 1)
		{
			this.imped = ei.value;
			this.reset();
		}
	}
}
