
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import com.limoilux.circuit.techno.CircuitAnalysisException;
import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuit.ui.EditInfo;
import com.limoilux.circuitsimulator.core.CircuitSimulator;
import com.limoilux.circuitsimulator.core.CoreUtil;

public class SparkGapElm extends CircuitElm
{
	double resistance, onresistance, offresistance, breakdown, holdcurrent;
	boolean state;

	public SparkGapElm(int xx, int yy)
	{
		super(xx, yy);
		this.offresistance = 1e9;
		this.onresistance = 1e3;
		this.breakdown = 1e3;
		this.holdcurrent = 0.001;
		this.state = false;
	}

	public SparkGapElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.onresistance = new Double(st.nextToken()).doubleValue();
		this.offresistance = new Double(st.nextToken()).doubleValue();
		this.breakdown = new Double(st.nextToken()).doubleValue();
		this.holdcurrent = new Double(st.nextToken()).doubleValue();
	}

	@Override
	public boolean nonLinear()
	{
		return true;
	}

	@Override
	public int getDumpType()
	{
		return 187;
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.onresistance + " " + this.offresistance + " " + this.breakdown + " "
				+ this.holdcurrent;
	}

	Polygon arrow1, arrow2;

	@Override
	public void setPoints()
	{
		super.setPoints();
		int dist = 16;
		int alen = 8;
		this.calcLeads(dist + alen);
		Point p1 = CoreUtil.interpPoint(this.point1, this.point2, (this.dn - alen) / (2 * this.dn));
		this.arrow1 = CircuitElm.calcArrow(this.point1, p1, alen, alen);
		p1 = CoreUtil.interpPoint(this.point1, this.point2, (this.dn + alen) / (2 * this.dn));
		this.arrow2 = CircuitElm.calcArrow(this.point2, p1, alen, alen);
	}

	@Override
	public void draw(Graphics g)
	{
		int i;
		double v1 = this.volts[0];
		double v2 = this.volts[1];
		this.setBbox(this.point1, this.point2, 8);
		this.draw2Leads(g);
		this.setPowerColor(g, true);
		this.setVoltageColor(g, this.volts[0]);
		g.fillPolygon(this.arrow1);
		this.setVoltageColor(g, this.volts[1]);
		g.fillPolygon(this.arrow2);
		if (this.state)
		{
			this.doDots(g);
		}
		this.drawPosts(g);
	}

	@Override
	public void calculateCurrent()
	{
		double vd = this.volts[0] - this.volts[1];
		this.current = vd / this.resistance;
	}

	@Override
	public void reset()
	{
		super.reset();
		this.state = false;
	}

	@Override
	public void startIteration() throws CircuitAnalysisException
	{
		if (Math.abs(this.current) < this.holdcurrent)
		{
			this.state = false;
		}
		double vd = this.volts[0] - this.volts[1];
		if (Math.abs(vd) > this.breakdown)
		{
			this.state = true;
		}
	}

	@Override
	public void doStep() throws CircuitAnalysisException
	{
		this.resistance = this.state ? this.onresistance : this.offresistance;
		CircuitElm.cirSim.circuit.stampResistor(this.nodes[0], this.nodes[1], this.resistance);
	}

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.circuit.stampNonLinear(this.nodes[0]);
		CircuitElm.cirSim.circuit.stampNonLinear(this.nodes[1]);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "spark gap";
		this.getBasicInfo(arr);
		arr[3] = this.state ? "on" : "off";
		arr[4] = "Ron = " + CircuitElm.getUnitText(this.onresistance, CircuitSimulator.ohmString);
		arr[5] = "Roff = " + CircuitElm.getUnitText(this.offresistance, CircuitSimulator.ohmString);
		arr[6] = "Vbreakdown = " + CircuitElm.getUnitText(this.breakdown, "V");
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		// ohmString doesn't work here on linux
		if (n == 0)
		{
			return new EditInfo("On resistance (ohms)", this.onresistance, 0, 0);
		}
		if (n == 1)
		{
			return new EditInfo("Off resistance (ohms)", this.offresistance, 0, 0);
		}
		if (n == 2)
		{
			return new EditInfo("Breakdown voltage", this.breakdown, 0, 0);
		}
		if (n == 3)
		{
			return new EditInfo("Holding current (A)", this.holdcurrent, 0, 0);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (ei.value > 0 && n == 0)
		{
			this.onresistance = ei.value;
		}
		if (ei.value > 0 && n == 1)
		{
			this.offresistance = ei.value;
		}
		if (ei.value > 0 && n == 2)
		{
			this.breakdown = ei.value;
		}
		if (ei.value > 0 && n == 3)
		{
			this.holdcurrent = ei.value;
		}
	}

	@Override
	public boolean needsShortcut()
	{
		return false;
	}
}
