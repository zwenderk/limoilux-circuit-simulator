package com.limoilux.circuit;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

// 0 = switch
// 1 = switch end 1
// 2 = switch end 2
// ...
// 3n   = coil
// 3n+1 = coil
// 3n+2 = end of coil resistor

class RelayElm extends CircuitElm
{
	double inductance;
	Inductor ind;
	double r_on, r_off, onCurrent;
	Point coilPosts[], coilLeads[], swposts[][], swpoles[][], ptSwitch[];
	Point lines[];
	double coilCurrent, switchCurrent[], coilCurCount, switchCurCount[];
	double d_position, coilR;
	int i_position;
	int poleCount;
	int openhs;
	final int nSwitch0 = 0;
	final int nSwitch1 = 1;
	final int nSwitch2 = 2;
	int nCoil1, nCoil2, nCoil3;
	final int FLAG_SWAP_COIL = 1;

	public RelayElm(int xx, int yy)
	{
		super(xx, yy);
		this.ind = new Inductor(CircuitElm.sim);
		this.inductance = .2;
		this.ind.setup(this.inductance, 0, Inductor.FLAG_BACK_EULER);
		this.noDiagonal = true;
		this.onCurrent = .02;
		this.r_on = .05;
		this.r_off = 1e6;
		this.coilR = 20;
		this.coilCurrent = this.coilCurCount = 0;
		this.poleCount = 1;
		this.setupPoles();
	}

	public RelayElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.poleCount = new Integer(st.nextToken()).intValue();
		this.inductance = new Double(st.nextToken()).doubleValue();
		this.coilCurrent = new Double(st.nextToken()).doubleValue();
		this.r_on = new Double(st.nextToken()).doubleValue();
		this.r_off = new Double(st.nextToken()).doubleValue();
		this.onCurrent = new Double(st.nextToken()).doubleValue();
		this.coilR = new Double(st.nextToken()).doubleValue();
		this.noDiagonal = true;
		this.ind = new Inductor(CircuitElm.sim);
		this.ind.setup(this.inductance, this.coilCurrent, Inductor.FLAG_BACK_EULER);
		this.setupPoles();
	}

	void setupPoles()
	{
		this.nCoil1 = 3 * this.poleCount;
		this.nCoil2 = this.nCoil1 + 1;
		this.nCoil3 = this.nCoil1 + 2;
		if (this.switchCurrent == null || this.switchCurrent.length != this.poleCount)
		{
			this.switchCurrent = new double[this.poleCount];
			this.switchCurCount = new double[this.poleCount];
		}
	}

	@Override
	int getDumpType()
	{
		return 178;
	}

	@Override
	String dump()
	{
		return super.dump() + " " + this.poleCount + " " + this.inductance + " " + this.coilCurrent + " " + this.r_on + " " + this.r_off + " "
				+ this.onCurrent + " " + this.coilR;
	}

	@Override
	void draw(Graphics g)
	{
		int i, p;
		for (i = 0; i != 2; i++)
		{
			this.setVoltageColor(g, this.volts[this.nCoil1 + i]);
			CircuitElm.drawThickLine(g, this.coilLeads[i], this.coilPosts[i]);
		}
		int x = (this.flags & this.FLAG_SWAP_COIL) != 0 ? 1 : 0;
		this.drawCoil(g, this.dsign * 6, this.coilLeads[x], this.coilLeads[1 - x], this.volts[this.nCoil1 + x], this.volts[this.nCoil2 - x]);

		// draw lines
		g.setColor(Color.darkGray);
		for (i = 0; i != this.poleCount; i++)
		{
			if (i == 0)
			{
				this.interpPoint(this.point1, this.point2, this.lines[i * 2], .5, this.openhs * 2 + 5 * this.dsign - i * this.openhs * 3);
			}
			else
			{
				this.interpPoint(this.point1, this.point2, this.lines[i * 2], .5, (int) (this.openhs * (-i * 3 + 3 - .5 + this.d_position)) + 5
						* this.dsign);
			}
			this.interpPoint(this.point1, this.point2, this.lines[i * 2 + 1], .5, (int) (this.openhs * (-i * 3 - .5 + this.d_position)) - 5 * this.dsign);
			g.drawLine(this.lines[i * 2].x, this.lines[i * 2].y, this.lines[i * 2 + 1].x, this.lines[i * 2 + 1].y);
		}

		for (p = 0; p != this.poleCount; p++)
		{
			int po = p * 3;
			for (i = 0; i != 3; i++)
			{
				// draw lead
				this.setVoltageColor(g, this.volts[this.nSwitch0 + po + i]);
				CircuitElm.drawThickLine(g, this.swposts[p][i], this.swpoles[p][i]);
			}

			this.interpPoint(this.swpoles[p][1], this.swpoles[p][2], this.ptSwitch[p], this.d_position);
			// setVoltageColor(g, volts[nSwitch0]);
			g.setColor(Color.lightGray);
			CircuitElm.drawThickLine(g, this.swpoles[p][0], this.ptSwitch[p]);
			this.switchCurCount[p] = this.updateDotCount(this.switchCurrent[p], this.switchCurCount[p]);
			this.drawDots(g, this.swposts[p][0], this.swpoles[p][0], this.switchCurCount[p]);

			if (this.i_position != 2)
			{
				this.drawDots(g, this.swpoles[p][this.i_position + 1], this.swposts[p][this.i_position + 1], this.switchCurCount[p]);
			}
		}

		this.coilCurCount = this.updateDotCount(this.coilCurrent, this.coilCurCount);

		this.drawDots(g, this.coilPosts[0], this.coilLeads[0], this.coilCurCount);
		this.drawDots(g, this.coilLeads[0], this.coilLeads[1], this.coilCurCount);
		this.drawDots(g, this.coilLeads[1], this.coilPosts[1], this.coilCurCount);

		this.drawPosts(g);
		this.setBbox(this.coilPosts[0], this.coilLeads[1], 0);
		this.adjustBbox(this.swpoles[this.poleCount - 1][0], this.swposts[this.poleCount - 1][1]); // XXX
	}

	@Override
	void setPoints()
	{
		super.setPoints();
		this.setupPoles();
		this.allocNodes();
		this.openhs = -this.dsign * 16;

		// switch
		this.calcLeads(32);
		this.swposts = new Point[this.poleCount][3];
		this.swpoles = new Point[this.poleCount][3];
		int i, j;
		for (i = 0; i != this.poleCount; i++)
		{
			for (j = 0; j != 3; j++)
			{
				this.swposts[i][j] = new Point();
				this.swpoles[i][j] = new Point();
			}
			this.interpPoint(this.lead1, this.lead2, this.swpoles[i][0], 0, -this.openhs * 3 * i);
			this.interpPoint(this.lead1, this.lead2, this.swpoles[i][1], 1, -this.openhs * 3 * i - this.openhs);
			this.interpPoint(this.lead1, this.lead2, this.swpoles[i][2], 1, -this.openhs * 3 * i + this.openhs);
			this.interpPoint(this.point1, this.point2, this.swposts[i][0], 0, -this.openhs * 3 * i);
			this.interpPoint(this.point1, this.point2, this.swposts[i][1], 1, -this.openhs * 3 * i - this.openhs);
			this.interpPoint(this.point1, this.point2, this.swposts[i][2], 1, -this.openhs * 3 * i + this.openhs);
		}

		// coil
		this.coilPosts = this.newPointArray(2);
		this.coilLeads = this.newPointArray(2);
		this.ptSwitch = this.newPointArray(this.poleCount);

		int x = (this.flags & this.FLAG_SWAP_COIL) != 0 ? 1 : 0;
		this.interpPoint(this.point1, this.point2, this.coilPosts[0], x, this.openhs * 2);
		this.interpPoint(this.point1, this.point2, this.coilPosts[1], x, this.openhs * 3);
		this.interpPoint(this.point1, this.point2, this.coilLeads[0], .5, this.openhs * 2);
		this.interpPoint(this.point1, this.point2, this.coilLeads[1], .5, this.openhs * 3);

		// lines
		this.lines = this.newPointArray(this.poleCount * 2);
	}

	@Override
	Point getPost(int n)
	{
		if (n < 3 * this.poleCount)
		{
			return this.swposts[n / 3][n % 3];
		}
		return this.coilPosts[n - 3 * this.poleCount];
	}

	@Override
	int getPostCount()
	{
		return 2 + this.poleCount * 3;
	}

	@Override
	int getInternalNodeCount()
	{
		return 1;
	}

	@Override
	void reset()
	{
		super.reset();
		this.ind.reset();
		this.coilCurrent = this.coilCurCount = 0;
		int i;
		for (i = 0; i != this.poleCount; i++)
		{
			this.switchCurrent[i] = this.switchCurCount[i] = 0;
		}
	}

	double a1, a2, a3, a4;

	@Override
	void stamp()
	{
		// inductor from coil post 1 to internal node
		this.ind.stamp(this.nodes[this.nCoil1], this.nodes[this.nCoil3]);
		// resistor from internal node to coil post 2
		CircuitElm.sim.stampResistor(this.nodes[this.nCoil3], this.nodes[this.nCoil2], this.coilR);

		int i;
		for (i = 0; i != this.poleCount * 3; i++)
		{
			CircuitElm.sim.stampNonLinear(this.nodes[this.nSwitch0 + i]);
		}
	}

	@Override
	void startIteration()
	{
		this.ind.startIteration(this.volts[this.nCoil1] - this.volts[this.nCoil3]);

		// magic value to balance operate speed with reset speed
		// semi-realistically
		double magic = 1.3;
		double pmult = Math.sqrt(magic + 1);
		double p = this.coilCurrent * pmult / this.onCurrent;
		this.d_position = Math.abs(p * p) - 1.3;
		if (this.d_position < 0)
		{
			this.d_position = 0;
		}
		if (this.d_position > 1)
		{
			this.d_position = 1;
		}
		if (this.d_position < .1)
		{
			this.i_position = 0;
		}
		else if (this.d_position > .9)
		{
			this.i_position = 1;
		}
		else {
			this.i_position = 2;
			// System.out.println("ind " + this + " " + current + " " + voltdiff);
		}
	}

	// we need this to be able to change the matrix for each step
	@Override
	boolean nonLinear()
	{
		return true;
	}

	@Override
	void doStep()
	{
		double voltdiff = this.volts[this.nCoil1] - this.volts[this.nCoil3];
		this.ind.doStep(voltdiff);
		int p;
		for (p = 0; p != this.poleCount * 3; p += 3)
		{
			CircuitElm.sim.stampResistor(this.nodes[this.nSwitch0 + p], this.nodes[this.nSwitch1 + p], this.i_position == 0 ? this.r_on : this.r_off);
			CircuitElm.sim.stampResistor(this.nodes[this.nSwitch0 + p], this.nodes[this.nSwitch2 + p], this.i_position == 1 ? this.r_on : this.r_off);
		}
	}

	@Override
	void calculateCurrent()
	{
		double voltdiff = this.volts[this.nCoil1] - this.volts[this.nCoil3];
		this.coilCurrent = this.ind.calculateCurrent(voltdiff);

		// actually this isn't correct, since there is a small amount
		// of current through the switch when off
		int p;
		for (p = 0; p != this.poleCount; p++)
		{
			if (this.i_position == 2)
			{
				this.switchCurrent[p] = 0;
			}
			else
			{
				this.switchCurrent[p] = (this.volts[this.nSwitch0 + p * 3] - this.volts[this.nSwitch1 + p * 3 + this.i_position]) / this.r_on;
			}
		}
	}

	@Override
	void getInfo(String arr[])
	{
		arr[0] = this.i_position == 0 ? "relay (off)" : this.i_position == 1 ? "relay (on)" : "relay";
		int i;
		int ln = 1;
		for (i = 0; i != this.poleCount; i++)
		{
			arr[ln++] = "I" + (i + 1) + " = " + CircuitElm.getCurrentDText(this.switchCurrent[i]);
		}
		arr[ln++] = "coil I = " + CircuitElm.getCurrentDText(this.coilCurrent);
		arr[ln++] = "coil Vd = " + CircuitElm.getVoltageDText(this.volts[this.nCoil1] - this.volts[this.nCoil2]);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Inductance (H)", this.inductance, 0, 0);
		}
		if (n == 1)
		{
			return new EditInfo("On Resistance (ohms)", this.r_on, 0, 0);
		}
		if (n == 2)
		{
			return new EditInfo("Off Resistance (ohms)", this.r_off, 0, 0);
		}
		if (n == 3)
		{
			return new EditInfo("On Current (A)", this.onCurrent, 0, 0);
		}
		if (n == 4)
		{
			return new EditInfo("Number of Poles", this.poleCount, 1, 4).setDimensionless();
		}
		if (n == 5)
		{
			return new EditInfo("Coil Resistance (ohms)", this.coilR, 0, 0);
		}
		if (n == 6)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Swap Coil Direction", (this.flags & this.FLAG_SWAP_COIL) != 0);
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0 && ei.value > 0)
		{
			this.inductance = ei.value;
			this.ind.setup(this.inductance, this.coilCurrent, Inductor.FLAG_BACK_EULER);
		}
		if (n == 1 && ei.value > 0)
		{
			this.r_on = ei.value;
		}
		if (n == 2 && ei.value > 0)
		{
			this.r_off = ei.value;
		}
		if (n == 3 && ei.value > 0)
		{
			this.onCurrent = ei.value;
		}
		if (n == 4 && ei.value >= 1)
		{
			this.poleCount = (int) ei.value;
			this.setPoints();
		}
		if (n == 5 && ei.value > 0)
		{
			this.coilR = ei.value;
		}
		if (n == 6)
		{
			if (ei.checkbox.getState())
			{
				this.flags |= this.FLAG_SWAP_COIL;
			}
			else
			{
				this.flags &= ~this.FLAG_SWAP_COIL;
			}
			this.setPoints();
		}
	}

	@Override
	boolean getConnection(int n1, int n2)
	{
		return n1 / 3 == n2 / 3;
	}
}
