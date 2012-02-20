
package com.limoilux.circuit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

class LampElm extends CircuitElm
{
	double resistance;
	final double roomTemp = 300;
	double temp, nom_pow, nom_v, warmTime, coolTime;

	public LampElm(int xx, int yy)
	{
		super(xx, yy);
		this.temp = this.roomTemp;
		this.nom_pow = 100;
		this.nom_v = 120;
		this.warmTime = .4;
		this.coolTime = .4;
	}

	public LampElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.temp = new Double(st.nextToken()).doubleValue();
		this.nom_pow = new Double(st.nextToken()).doubleValue();
		this.nom_v = new Double(st.nextToken()).doubleValue();
		this.warmTime = new Double(st.nextToken()).doubleValue();
		this.coolTime = new Double(st.nextToken()).doubleValue();
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.temp + " " + this.nom_pow + " " + this.nom_v + " " + this.warmTime + " "
				+ this.coolTime;
	}

	@Override
	public int getDumpType()
	{
		return 181;
	}

	Point bulbLead[], filament[], bulb;
	int bulbR;

	@Override
	void reset()
	{
		super.reset();
		this.temp = this.roomTemp;
	}

	final int filament_len = 24;

	@Override
	public void setPoints()
	{
		super.setPoints();
		int llen = 16;
		this.calcLeads(llen);
		this.bulbLead = this.newPointArray(2);
		this.filament = this.newPointArray(2);
		this.bulbR = 20;
		this.filament[0] = this.interpPoint(this.lead1, this.lead2, 0, this.filament_len);
		this.filament[1] = this.interpPoint(this.lead1, this.lead2, 1, this.filament_len);
		double br = this.filament_len - Math.sqrt(this.bulbR * this.bulbR - llen * llen);
		this.bulbLead[0] = this.interpPoint(this.lead1, this.lead2, 0, br);
		this.bulbLead[1] = this.interpPoint(this.lead1, this.lead2, 1, br);
		this.bulb = this.interpPoint(this.filament[0], this.filament[1], .5);
	}

	Color getTempColor()
	{
		if (this.temp < 1200)
		{
			int x = (int) (255 * (this.temp - 800) / 400);
			if (x < 0)
			{
				x = 0;
			}
			return new Color(x, 0, 0);
		}
		if (this.temp < 1700)
		{
			int x = (int) (255 * (this.temp - 1200) / 500);
			if (x < 0)
			{
				x = 0;
			}
			return new Color(255, x, 0);
		}
		if (this.temp < 2400)
		{
			int x = (int) (255 * (this.temp - 1700) / 700);
			if (x < 0)
			{
				x = 0;
			}
			return new Color(255, 255, x);
		}
		return Color.white;
	}

	@Override
	public void draw(Graphics g)
	{
		double v1 = this.volts[0];
		double v2 = this.volts[1];
		this.setBbox(this.point1, this.point2, 4);
		this.adjustBbox(this.bulb.x - this.bulbR, this.bulb.y - this.bulbR, this.bulb.x + this.bulbR, this.bulb.y
				+ this.bulbR);
		// adjustbbox
		this.draw2Leads(g);
		this.setPowerColor(g, true);
		g.setColor(this.getTempColor());
		g.fillOval(this.bulb.x - this.bulbR, this.bulb.y - this.bulbR, this.bulbR * 2, this.bulbR * 2);
		g.setColor(Color.white);
		CircuitElm.drawThickCircle(g, this.bulb.x, this.bulb.y, this.bulbR);
		this.setVoltageColor(g, v1);
		CircuitElm.drawThickLine(g, this.lead1, this.filament[0]);
		this.setVoltageColor(g, v2);
		CircuitElm.drawThickLine(g, this.lead2, this.filament[1]);
		this.setVoltageColor(g, (v1 + v2) * .5);
		CircuitElm.drawThickLine(g, this.filament[0], this.filament[1]);
		this.updateDotCount();
		if (CircuitElm.cirSim.dragElm != this)
		{
			this.drawDots(g, this.point1, this.lead1, this.curcount);
			double cc = this.curcount + (this.dn - 16) / 2;
			this.drawDots(g, this.lead1, this.filament[0], cc);
			cc += this.filament_len;
			this.drawDots(g, this.filament[0], this.filament[1], cc);
			cc += 16;
			this.drawDots(g, this.filament[1], this.lead2, cc);
			cc += this.filament_len;
			this.drawDots(g, this.lead2, this.point2, this.curcount);
		}
		this.drawPosts(g);
	}

	@Override
	void calculateCurrent()
	{
		this.current = (this.volts[0] - this.volts[1]) / this.resistance;
		// System.out.print(this + " res current set to " + current + "\n");
	}

	@Override
	void stamp()
	{
		CircuitElm.cirSim.stampNonLinear(this.nodes[0]);
		CircuitElm.cirSim.stampNonLinear(this.nodes[1]);
	}

	@Override
	boolean nonLinear()
	{
		return true;
	}

	@Override
	public void startIteration()
	{
		// based on http://www.intusoft.com/nlpdf/nl11.pdf
		double nom_r = this.nom_v * this.nom_v / this.nom_pow;
		// this formula doesn't work for values over 5390
		double tp = this.temp > 5390 ? 5390 : this.temp;
		this.resistance = nom_r * (1.26104 - 4.90662 * Math.sqrt(17.1839 / tp - 0.00318794) - 7.8569 / (tp - 187.56));
		double cap = 1.57e-4 * this.nom_pow;
		double capw = cap * this.warmTime / .4;
		double capc = cap * this.coolTime / .4;
		// System.out.println(nom_r + " " + (resistance/nom_r));
		this.temp += this.getPower() * CircuitElm.cirSim.timeStep / capw;
		double cr = 2600 / this.nom_pow;
		this.temp -= CircuitElm.cirSim.timeStep * (this.temp - this.roomTemp) / (capc * cr);
		// System.out.println(capw + " " + capc + " " + temp + " " +resistance);
	}

	@Override
	public void doStep()
	{
		CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[1], this.resistance);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "lamp";
		this.getBasicInfo(arr);
		arr[3] = "R = " + CircuitElm.getUnitText(this.resistance, CircuitElm.cirSim.ohmString);
		arr[4] = "P = " + CircuitElm.getUnitText(this.getPower(), "W");
		arr[5] = "T = " + (int) this.temp + " K";
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		// ohmString doesn't work here on linux
		if (n == 0)
		{
			return new EditInfo("Nominal Power", this.nom_pow, 0, 0);
		}
		if (n == 1)
		{
			return new EditInfo("Nominal Voltage", this.nom_v, 0, 0);
		}
		if (n == 2)
		{
			return new EditInfo("Warmup Time (s)", this.warmTime, 0, 0);
		}
		if (n == 3)
		{
			return new EditInfo("Cooldown Time (s)", this.coolTime, 0, 0);
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0 && ei.value > 0)
		{
			this.nom_pow = ei.value;
		}
		if (n == 1 && ei.value > 0)
		{
			this.nom_v = ei.value;
		}
		if (n == 2 && ei.value > 0)
		{
			this.warmTime = ei.value;
		}
		if (n == 3 && ei.value > 0)
		{
			this.coolTime = ei.value;
		}
	}
}
