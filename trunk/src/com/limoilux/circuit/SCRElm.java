
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.limoilux.circuitsimulator.circuit.CircuitAnalysisException;
import com.limoilux.circuitsimulator.circuit.CircuitElm;
import com.limoilux.circuitsimulator.ui.EditInfo;

// Silicon-Controlled Rectifier
// 3 nodes, 1 internal node
// 0 = anode, 1 = cathode, 2 = gate
// 0, 3 = variable resistor
// 3, 2 = diode
// 2, 1 = 50 ohm resistor

public class SCRElm extends CircuitElm
{
	final int anode = 0;
	final int cnode = 1;
	final int gnode = 2;
	final int inode = 3;
	Diode diode;

	public SCRElm(int xx, int yy)
	{
		super(xx, yy);
		this.setDefaults();
		this.setup();
	}

	public SCRElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.setDefaults();
		try
		{
			this.lastvac = new Double(st.nextToken()).doubleValue();
			this.lastvag = new Double(st.nextToken()).doubleValue();
			this.volts[this.anode] = 0;
			this.volts[this.cnode] = -this.lastvac;
			this.volts[this.gnode] = -this.lastvag;
			this.triggerI = new Double(st.nextToken()).doubleValue();
			this.holdingI = new Double(st.nextToken()).doubleValue();
			this.cresistance = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
		}
		this.setup();
	}

	void setDefaults()
	{
		this.cresistance = 50;
		this.holdingI = .0082;
		this.triggerI = .01;
	}

	void setup()
	{
		this.diode = new Diode(CircuitElm.cirSim);
		this.diode.setup(.8, 0);
	}

	@Override
	public boolean nonLinear()
	{
		return true;
	}

	@Override
	public void reset()
	{
		this.volts[this.anode] = this.volts[this.cnode] = this.volts[this.gnode] = 0;
		this.diode.reset();
		this.lastvag = this.lastvac = this.curcount_a = this.curcount_c = this.curcount_g = 0;
	}

	@Override
	public int getElementId()
	{
		return 177;
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + (this.volts[this.anode] - this.volts[this.cnode]) + " "
				+ (this.volts[this.anode] - this.volts[this.gnode]) + " " + this.triggerI + " " + this.holdingI + " "
				+ this.cresistance;
	}

	double ia, ic, ig, curcount_a, curcount_c, curcount_g;
	double lastvac, lastvag;
	double cresistance, triggerI, holdingI;

	final int hs = 8;
	Polygon poly;
	Point cathode[], gate[];

	@Override
	public void setPoints()
	{
		super.setPoints();
		int dir = 0;
		if (CircuitElm.abs(this.longueurX) > CircuitElm.abs(this.longueurY))
		{
			dir = -CircuitElm.sign(this.longueurX) * CircuitElm.sign(this.longueurY);
			this.point2.y = this.point1.y;
		}
		else
		{
			dir = CircuitElm.sign(this.longueurY) * CircuitElm.sign(this.longueurX);
			this.point2.x = this.point1.x;
		}
		if (dir == 0)
		{
			dir = 1;
		}
		this.calcLeads(16);
		this.cathode = CircuitElm.newPointArray(2);
		Point pa[] = CircuitElm.newPointArray(2);
		CircuitElm.interpPoint2(this.lead1, this.lead2, pa[0], pa[1], 0, this.hs);
		CircuitElm.interpPoint2(this.lead1, this.lead2, this.cathode[0], this.cathode[1], 1, this.hs);
		this.poly = CircuitElm.createPolygon(pa[0], pa[1], this.lead2);

		this.gate = CircuitElm.newPointArray(2);
		double leadlen = (this.longueur - 16) / 2;
		int gatelen = CircuitElm.cirSim.gridSize;
		gatelen += leadlen % CircuitElm.cirSim.gridSize;
		if (leadlen < gatelen)
		{
			this.x2 = this.x;
			this.y2 = this.y;
			return;
		}
		CircuitElm.interpPoint(this.lead2, this.point2, this.gate[0], gatelen / leadlen, gatelen * dir);
		CircuitElm.interpPoint(this.lead2, this.point2, this.gate[1], gatelen / leadlen, CircuitElm.cirSim.gridSize * 2
				* dir);
	}

	@Override
	public void draw(Graphics g)
	{
		this.setBbox(this.point1, this.point2, this.hs);
		this.adjustBbox(this.gate[0], this.gate[1]);

		double v1 = this.volts[this.anode];
		double v2 = this.volts[this.cnode];

		this.draw2Leads(g);

		// draw arrow thingy
		this.setPowerColor(g, true);
		this.setVoltageColor(g, v1);
		g.fillPolygon(this.poly);

		// draw thing arrow is pointing to
		this.setVoltageColor(g, v2);
		CircuitElm.drawThickLine(g, this.cathode[0], this.cathode[1]);

		CircuitElm.drawThickLine(g, this.lead2, this.gate[0]);
		CircuitElm.drawThickLine(g, this.gate[0], this.gate[1]);

		this.curcount_a = CircuitElm.updateDotCount(this.ia, this.curcount_a);
		this.curcount_c = CircuitElm.updateDotCount(this.ic, this.curcount_c);
		this.curcount_g = CircuitElm.updateDotCount(this.ig, this.curcount_g);
		if (CircuitElm.cirSim.mouseMan.dragElm != this)
		{
			CircuitElm.drawDots(g, this.point1, this.lead2, this.curcount_a);
			CircuitElm.drawDots(g, this.point2, this.lead2, this.curcount_c);
			CircuitElm.drawDots(g, this.gate[1], this.gate[0], this.curcount_g);
			CircuitElm.drawDots(g, this.gate[0], this.lead2,
					this.curcount_g + CircuitElm.distance(this.gate[1], this.gate[0]));
		}
		this.drawPosts(g);
	}

	@Override
	public Point getPost(int n)
	{
		return n == 0 ? this.point1 : n == 1 ? this.point2 : this.gate[1];
	}

	@Override
	public int getPostCount()
	{
		return 3;
	}

	@Override
	public int getInternalNodeCount()
	{
		return 1;
	}

	@Override
	public double getPower()
	{
		return (this.volts[this.anode] - this.volts[this.gnode]) * this.ia
				+ (this.volts[this.cnode] - this.volts[this.gnode]) * this.ic;
	}

	double aresistance;

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.stampNonLinear(this.nodes[this.anode]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[this.cnode]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[this.gnode]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[this.inode]);
		CircuitElm.cirSim.circuit.stampResistor(this.nodes[this.gnode], this.nodes[this.cnode], this.cresistance);
		this.diode.stamp(this.nodes[this.inode], this.nodes[this.gnode]);
	}

	@Override
	public void doStep() throws CircuitAnalysisException
	{
		double vac = this.volts[this.anode] - this.volts[this.cnode]; // typically
		// negative
		double vag = this.volts[this.anode] - this.volts[this.gnode]; // typically
		// positive
		if (Math.abs(vac - this.lastvac) > .01 || Math.abs(vag - this.lastvag) > .01)
		{
			CircuitElm.cirSim.circuit.converged = false;
		}
		this.lastvac = vac;
		this.lastvag = vag;
		this.diode.doStep(this.volts[this.inode] - this.volts[this.gnode]);
		double icmult = 1 / this.triggerI;
		double iamult = 1 / this.holdingI - icmult;
		// System.out.println(icmult + " " + iamult);
		this.aresistance = -icmult * this.ic + this.ia * iamult > 1 ? .0105 : 10e5;
		// System.out.println(vac + " " + vag + " " + sim.converged + " " + ic +
		// " " + ia + " " + aresistance + " " + volts[inode] + " " +
		// volts[gnode] + " " + volts[anode]);
		CircuitElm.cirSim.circuit.stampResistor(this.nodes[this.anode], this.nodes[this.inode], this.aresistance);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "SCR";
		double vac = this.volts[this.anode] - this.volts[this.cnode];
		double vag = this.volts[this.anode] - this.volts[this.gnode];
		double vgc = this.volts[this.gnode] - this.volts[this.cnode];
		arr[1] = "Ia = " + CircuitElm.getCurrentText(this.ia);
		arr[2] = "Ig = " + CircuitElm.getCurrentText(this.ig);
		arr[3] = "Vac = " + CircuitElm.getVoltageText(vac);
		arr[4] = "Vag = " + CircuitElm.getVoltageText(vag);
		arr[5] = "Vgc = " + CircuitElm.getVoltageText(vgc);
	}

	@Override
	public void calculateCurrent()
	{
		this.ic = (this.volts[this.cnode] - this.volts[this.gnode]) / this.cresistance;
		this.ia = (this.volts[this.anode] - this.volts[this.inode]) / this.aresistance;
		this.ig = -this.ic - this.ia;
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		// ohmString doesn't work here on linux
		if (n == 0)
		{
			return new EditInfo("Trigger Current (A)", this.triggerI, 0, 0);
		}
		if (n == 1)
		{
			return new EditInfo("Holding Current (A)", this.holdingI, 0, 0);
		}
		if (n == 2)
		{
			return new EditInfo("Gate-Cathode Resistance (ohms)", this.cresistance, 0, 0);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0 && ei.value > 0)
		{
			this.triggerI = ei.value;
		}
		if (n == 1 && ei.value > 0)
		{
			this.holdingI = ei.value;
		}
		if (n == 2 && ei.value > 0)
		{
			this.cresistance = ei.value;
		}
	}
}
