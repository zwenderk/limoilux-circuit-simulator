
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Graphics;
import java.util.StringTokenizer;

class SweepElm extends CircuitElm
{
	double maxV, maxF, minF, sweepTime, frequency;
	final int FLAG_LOG = 1;
	final int FLAG_BIDIR = 2;

	public SweepElm(int xx, int yy)
	{
		super(xx, yy);
		this.minF = 20;
		this.maxF = 4000;
		this.maxV = 5;
		this.sweepTime = .1;
		this.flags = this.FLAG_BIDIR;
		this.reset();
	}

	public SweepElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.minF = new Double(st.nextToken()).doubleValue();
		this.maxF = new Double(st.nextToken()).doubleValue();
		this.maxV = new Double(st.nextToken()).doubleValue();
		this.sweepTime = new Double(st.nextToken()).doubleValue();
		this.reset();
	}

	@Override
	public int getDumpType()
	{
		return 170;
	}

	@Override
	public int getPostCount()
	{
		return 1;
	}

	final int circleSize = 17;

	@Override
	public String dump()
	{
		return super.dump() + " " + this.minF + " " + this.maxF + " " + this.maxV + " " + this.sweepTime;
	}

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.lead1 = this.interpPoint(this.point1, this.point2, 1 - this.circleSize / this.dn);
	}

	@Override
	public void draw(Graphics g)
	{
		this.setBbox(this.point1, this.point2, this.circleSize);
		this.setVoltageColor(g, this.volts[0]);
		CircuitElm.drawThickLine(g, this.point1, this.lead1);
		g.setColor(this.needsHighlight() ? CircuitElm.selectColor : Color.gray);
		this.setPowerColor(g, false);
		int xc = this.point2.x;
		int yc = this.point2.y;
		CircuitElm.drawThickCircle(g, xc, yc, this.circleSize);
		int wl = 8;
		this.adjustBbox(xc - this.circleSize, yc - this.circleSize, xc + this.circleSize, yc + this.circleSize);
		int i;
		int xl = 10;
		int ox = -1, oy = -1;
		long tm = System.currentTimeMillis();
		// double w = (this == mouseElm ? 3 : 2);
		tm %= 2000;
		if (tm > 1000)
		{
			tm = 2000 - tm;
		}
		double w = 1 + tm * .002;
		if (!CircuitElm.cirSim.stoppedCheck.getState())
		{
			w = 1 + 2 * (this.frequency - this.minF) / (this.maxF - this.minF);
		}
		for (i = -xl; i <= xl; i++)
		{
			int yy = yc + (int) (.95 * Math.sin(i * CircuitElm.PI * w / xl) * wl);
			if (ox != -1)
			{
				CircuitElm.drawThickLine(g, ox, oy, xc + i, yy);
			}
			ox = xc + i;
			oy = yy;
		}
		if (CircuitElm.cirSim.showValuesCheckItem.getState())
		{
			String s = CircuitElm.getShortUnitText(this.frequency, "Hz");
			if (this.dx == 0 || this.dy == 0)
			{
				this.drawValues(g, s, this.circleSize);
			}
		}

		this.drawPosts(g);
		this.curcount = this.updateDotCount(-this.current, this.curcount);
		if (CircuitElm.cirSim.dragElm != this)
		{
			this.drawDots(g, this.point1, this.lead1, this.curcount);
		}
	}

	@Override
	public void stamp()
	{
		CircuitElm.cirSim.stampVoltageSource(0, this.nodes[0], this.voltSource);
	}

	double fadd, fmul, freqTime, savedTimeStep;
	int dir = 1;

	void setParams()
	{
		if (this.frequency < this.minF || this.frequency > this.maxF)
		{
			this.frequency = this.minF;
			this.freqTime = 0;
			this.dir = 1;
		}
		if ((this.flags & this.FLAG_LOG) == 0)
		{
			this.fadd = this.dir * CircuitElm.cirSim.timeStep * (this.maxF - this.minF) / this.sweepTime;
			this.fmul = 1;
		}
		else
		{
			this.fadd = 0;
			this.fmul = Math.pow(this.maxF / this.minF, this.dir * CircuitElm.cirSim.timeStep / this.sweepTime);
		}
		this.savedTimeStep = CircuitElm.cirSim.timeStep;
	}

	@Override
	public void reset()
	{
		this.frequency = this.minF;
		this.freqTime = 0;
		this.dir = 1;
		this.setParams();
	}

	double v;

	@Override
	public void startIteration()
	{
		// has timestep been changed?
		if (CircuitElm.cirSim.timeStep != this.savedTimeStep)
		{
			this.setParams();
		}
		this.v = Math.sin(this.freqTime) * this.maxV;
		this.freqTime += this.frequency * 2 * CircuitElm.PI * CircuitElm.cirSim.timeStep;
		this.frequency = this.frequency * this.fmul + this.fadd;
		if (this.frequency >= this.maxF && this.dir == 1)
		{
			if ((this.flags & this.FLAG_BIDIR) != 0)
			{
				this.fadd = -this.fadd;
				this.fmul = 1 / this.fmul;
				this.dir = -1;
			}
			else
			{
				this.frequency = this.minF;
			}
		}
		if (this.frequency <= this.minF && this.dir == -1)
		{
			this.fadd = -this.fadd;
			this.fmul = 1 / this.fmul;
			this.dir = 1;
		}
	}

	@Override
	public void doStep()
	{
		CircuitElm.cirSim.updateVoltageSource(0, this.nodes[0], this.voltSource, this.v);
	}

	@Override
	public double getVoltageDiff()
	{
		return this.volts[0];
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	boolean hasGroundConnection(int n1)
	{
		return true;
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "sweep " + ((this.flags & this.FLAG_LOG) == 0 ? "(linear)" : "(log)");
		arr[1] = "I = " + CircuitElm.getCurrentDText(this.getCurrent());
		arr[2] = "V = " + CircuitElm.getVoltageText(this.volts[0]);
		arr[3] = "f = " + CircuitElm.getUnitText(this.frequency, "Hz");
		arr[4] = "range = " + CircuitElm.getUnitText(this.minF, "Hz") + " .. "
				+ CircuitElm.getUnitText(this.maxF, "Hz");
		arr[5] = "time = " + CircuitElm.getUnitText(this.sweepTime, "s");
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Min Frequency (Hz)", this.minF, 0, 0);
		}
		if (n == 1)
		{
			return new EditInfo("Max Frequency (Hz)", this.maxF, 0, 0);
		}
		if (n == 2)
		{
			return new EditInfo("Sweep Time (s)", this.sweepTime, 0, 0);
		}
		if (n == 3)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Logarithmic", (this.flags & this.FLAG_LOG) != 0);
			return ei;
		}
		if (n == 4)
		{
			return new EditInfo("Max Voltage", this.maxV, 0, 0);
		}
		if (n == 5)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Bidirectional", (this.flags & this.FLAG_BIDIR) != 0);
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		double maxfreq = 1 / (8 * CircuitElm.cirSim.timeStep);
		if (n == 0)
		{
			this.minF = ei.value;
			if (this.minF > maxfreq)
			{
				this.minF = maxfreq;
			}
		}
		if (n == 1)
		{
			this.maxF = ei.value;
			if (this.maxF > maxfreq)
			{
				this.maxF = maxfreq;
			}
		}
		if (n == 2)
		{
			this.sweepTime = ei.value;
		}
		if (n == 3)
		{
			this.flags &= ~this.FLAG_LOG;
			if (ei.checkbox.getState())
			{
				this.flags |= this.FLAG_LOG;
			}
		}
		if (n == 4)
		{
			this.maxV = ei.value;
		}
		if (n == 5)
		{
			this.flags &= ~this.FLAG_BIDIR;
			if (ei.checkbox.getState())
			{
				this.flags |= this.FLAG_BIDIR;
			}
		}
		this.setParams();
	}
}
