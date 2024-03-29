
package com.limoilux.circuit;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

import com.limoilux.circuitsimulator.circuit.CircuitAnalysisException;
import com.limoilux.circuitsimulator.circuit.CircuitElm;
import com.limoilux.circuitsimulator.core.Configs;
import com.limoilux.circuitsimulator.core.CoreUtil;
import com.limoilux.circuitsimulator.ui.DrawUtil;
import com.limoilux.circuitsimulator.ui.EditInfo;

public class VoltageElm extends CircuitElm
{
	static final int FLAG_COS = 2;
	int waveform;
	static final int WF_DC = 0;
	static final int WF_AC = 1;
	static final int WF_SQUARE = 2;
	static final int WF_TRIANGLE = 3;
	static final int WF_SAWTOOTH = 4;
	static final int WF_PULSE = 5;
	static final int WF_VAR = 6;
	double frequency, maxVoltage, freqTimeZero, bias, phaseShift, dutyCycle;

	VoltageElm(int xx, int yy, int wf)
	{
		super(xx, yy);
		this.waveform = wf;
		this.maxVoltage = 5;
		this.frequency = 40;
		this.dutyCycle = .5;
		this.reset();
	}

	public VoltageElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.maxVoltage = 5;
		this.frequency = 40;
		this.waveform = VoltageElm.WF_DC;
		this.dutyCycle = .5;
		try
		{
			this.waveform = new Integer(st.nextToken()).intValue();
			this.frequency = new Double(st.nextToken()).doubleValue();
			this.maxVoltage = new Double(st.nextToken()).doubleValue();
			this.bias = new Double(st.nextToken()).doubleValue();
			this.phaseShift = new Double(st.nextToken()).doubleValue();
			this.dutyCycle = new Double(st.nextToken()).doubleValue();
		}
		catch (Exception e)
		{
		}
		if ((this.flags & VoltageElm.FLAG_COS) != 0)
		{
			this.flags &= ~VoltageElm.FLAG_COS;
			this.phaseShift = Math.PI / 2;
		}
		this.reset();
	}

	@Override
	public int getElementId()
	{
		return 'v';
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.waveform + " " + this.frequency + " " + this.maxVoltage + " " + this.bias
				+ " " + this.phaseShift + " " + this.dutyCycle;
	}

	/*
	 * void setCurrent(double c) { current = c;
	 * System.out.print("v current set to " + c + "\n"); }
	 */

	@Override
	public void reset()
	{
		this.freqTimeZero = 0;
		this.curcount = 0;
	}

	double triangleFunc(double x)
	{
		if (x < Math.PI)
		{
			return x * (2 / Math.PI) - 1;
		}
		return 1 - (x - Math.PI) * (2 / Math.PI);
	}

	@Override
	public void stamp()
	{
		if (this.waveform == VoltageElm.WF_DC)
		{
			CircuitElm.cirSim.circuit.stampVoltageSource(this.nodes[0], this.nodes[1], this.voltSource,
					this.getVoltage());
		}
		else
		{
			CircuitElm.cirSim.circuit.stampVoltageSource(this.nodes[0], this.nodes[1], this.voltSource);
		}
	}

	@Override
	public void doStep() throws CircuitAnalysisException
	{
		if (this.waveform != VoltageElm.WF_DC)
		{
			CircuitElm.cirSim.circuit.updateVoltageSource(this.nodes[0], this.nodes[1], this.voltSource,
					this.getVoltage());
		}
	}

	double getVoltage()
	{
		double w = 2 * Math.PI * (CircuitElm.cirSim.timer.time - this.freqTimeZero) * this.frequency + this.phaseShift;
		switch (this.waveform)
		{
		case WF_DC:
			return this.maxVoltage + this.bias;
		case WF_AC:
			return Math.sin(w) * this.maxVoltage + this.bias;
		case WF_SQUARE:
			return this.bias + (w % (2 * Math.PI) > 2 * Math.PI * this.dutyCycle ? -this.maxVoltage : this.maxVoltage);
		case WF_TRIANGLE:
			return this.bias + this.triangleFunc(w % (2 * Math.PI)) * this.maxVoltage;
		case WF_SAWTOOTH:
			return this.bias + w % (2 * Math.PI) * (this.maxVoltage / Math.PI) - this.maxVoltage;
		case WF_PULSE:
			return w % (2 * Math.PI) < 1 ? this.maxVoltage + this.bias : this.bias;
		default:
			return 0;
		}
	}

	final int circleSize = 17;

	@Override
	public void setPoints()
	{
		super.setPoints();
		this.calcLeads(this.waveform == VoltageElm.WF_DC || this.waveform == VoltageElm.WF_VAR ? 8
				: this.circleSize * 2);
	}

	@Override
	public void draw(Graphics g)
	{
		this.setBbox(this.x, this.y, this.x2, this.y2);
		this.draw2Leads(g);
		if (this.waveform == VoltageElm.WF_DC)
		{
			this.setPowerColor(g, false);
			this.setVoltageColor(g, this.volts[0]);
			CircuitElm.interpPoint2(this.lead1, this.lead2, CircuitElm.ps1, CircuitElm.ps2, 0, 10);
			DrawUtil.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
			this.setVoltageColor(g, this.volts[1]);
			int hs = 16;
			this.setBbox(this.point1, this.point2, hs);
			CircuitElm.interpPoint2(this.lead1, this.lead2, CircuitElm.ps1, CircuitElm.ps2, 1, hs);
			DrawUtil.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
		}
		else
		{
			this.setBbox(this.point1, this.point2, this.circleSize);
			CircuitElm.interpPoint(this.lead1, this.lead2, CircuitElm.ps1, .5);
			this.drawWaveform(g, CircuitElm.ps1);
		}
		this.updateDotCount();
		if (CircuitElm.cirSim.mouseMan.dragElm != this)
		{
			if (this.waveform == VoltageElm.WF_DC)
			{
				DrawUtil.drawDots(g, this.point1, this.point2, this.curcount);
			}
			else
			{
				CircuitElm.drawDots(g, this.point1, this.lead1, this.curcount);
				CircuitElm.drawDots(g, this.point2, this.lead2, -this.curcount);
			}
		}
		this.drawPosts(g);
	}

	void drawWaveform(Graphics g, Point center)
	{
		g.setColor(this.needsHighlight() ? CircuitElm.SELECT_COLOR : Color.gray);
		this.setPowerColor(g, false);
		int xc = center.x;
		int yc = center.y;
		CircuitElm.drawThickCircle(g, xc, yc, this.circleSize);
		int wl = 8;
		this.adjustBbox(xc - this.circleSize, yc - this.circleSize, xc + this.circleSize, yc + this.circleSize);
		int xc2;
		switch (this.waveform)
		{
		case WF_DC:
		{
			break;
		}
		case WF_SQUARE:
			xc2 = (int) (wl * 2 * this.dutyCycle - wl + xc);
			xc2 = Math.max(xc - wl + 3, Math.min(xc + wl - 3, xc2));
			DrawUtil.drawThickLine(g, xc - wl, yc - wl, xc - wl, yc);
			DrawUtil.drawThickLine(g, xc - wl, yc - wl, xc2, yc - wl);
			DrawUtil.drawThickLine(g, xc2, yc - wl, xc2, yc + wl);
			DrawUtil.drawThickLine(g, xc + wl, yc + wl, xc2, yc + wl);
			DrawUtil.drawThickLine(g, xc + wl, yc, xc + wl, yc + wl);
			break;
		case WF_PULSE:
			yc += wl / 2;
			DrawUtil.drawThickLine(g, xc - wl, yc - wl, xc - wl, yc);
			DrawUtil.drawThickLine(g, xc - wl, yc - wl, xc - wl / 2, yc - wl);
			DrawUtil.drawThickLine(g, xc - wl / 2, yc - wl, xc - wl / 2, yc);
			DrawUtil.drawThickLine(g, xc - wl / 2, yc, xc + wl, yc);
			break;
		case WF_SAWTOOTH:
			DrawUtil.drawThickLine(g, xc, yc - wl, xc - wl, yc);
			DrawUtil.drawThickLine(g, xc, yc - wl, xc, yc + wl);
			DrawUtil.drawThickLine(g, xc, yc + wl, xc + wl, yc);
			break;
		case WF_TRIANGLE:
		{
			int xl = 5;
			DrawUtil.drawThickLine(g, xc - xl * 2, yc, xc - xl, yc - wl);
			DrawUtil.drawThickLine(g, xc - xl, yc - wl, xc, yc);
			DrawUtil.drawThickLine(g, xc, yc, xc + xl, yc + wl);
			DrawUtil.drawThickLine(g, xc + xl, yc + wl, xc + xl * 2, yc);
			break;
		}
		case WF_AC:
		{
			int i;
			int xl = 10;
			int ox = -1, oy = -1;
			for (i = -xl; i <= xl; i++)
			{
				int yy = yc + (int) (.95 * Math.sin(i * Math.PI / xl) * wl);
				if (ox != -1)
				{
					DrawUtil.drawThickLine(g, ox, oy, xc + i, yy);
				}
				ox = xc + i;
				oy = yy;
			}
			break;
		}
		}
		if (Configs.SHOW_VALUES)
		{
			String s = CoreUtil.getShortUnitText(this.frequency, "Hz");
			if (this.longueurX == 0 || this.longueurY == 0)
			{
				this.drawValues(g, s, this.circleSize);
			}
		}
	}

	@Override
	public int getVoltageSourceCount()
	{
		return 1;
	}

	@Override
	public double getPower()
	{
		return -this.getVoltageDiff() * this.current;
	}

	@Override
	public double getVoltageDiff()
	{
		return this.volts[1] - this.volts[0];
	}

	@Override
	public void getInfo(String arr[])
	{
		switch (this.waveform)
		{
		case WF_DC:
		case WF_VAR:
			arr[0] = "voltage source";
			break;
		case WF_AC:
			arr[0] = "A/C source";
			break;
		case WF_SQUARE:
			arr[0] = "square wave gen";
			break;
		case WF_PULSE:
			arr[0] = "pulse gen";
			break;
		case WF_SAWTOOTH:
			arr[0] = "sawtooth gen";
			break;
		case WF_TRIANGLE:
			arr[0] = "triangle gen";
			break;
		}
		arr[1] = "I = " + CoreUtil.getCurrentText(this.getCurrent());
		arr[2] = (this instanceof RailElm ? "V = " : "Vd = ") + CoreUtil.getVoltageText(this.getVoltageDiff());
		if (this.waveform != VoltageElm.WF_DC && this.waveform != VoltageElm.WF_VAR)
		{
			arr[3] = "f = " + CoreUtil.getUnitText(this.frequency, "Hz");
			arr[4] = "Vmax = " + CoreUtil.getVoltageText(this.maxVoltage);
			int i = 5;
			if (this.bias != 0)
			{
				arr[i++] = "Voff = " + CoreUtil.getVoltageText(this.bias);
			}
			else if (this.frequency > 500)
			{
				arr[i++] = "wavelength = " + CoreUtil.getUnitText(2.9979e8 / this.frequency, "m");
			}
			arr[i++] = "P = " + CoreUtil.getUnitText(this.getPower(), "W");
		}
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo(this.waveform == VoltageElm.WF_DC ? "Voltage" : "Max Voltage", this.maxVoltage, -20, 20);
		}
		if (n == 1)
		{
			EditInfo ei = new EditInfo("Waveform", this.waveform, -1, -1);
			ei.choice = new Choice();
			ei.choice.add("D/C");
			ei.choice.add("A/C");
			ei.choice.add("Square Wave");
			ei.choice.add("Triangle");
			ei.choice.add("Sawtooth");
			ei.choice.add("Pulse");
			ei.choice.select(this.waveform);
			return ei;
		}
		if (this.waveform == VoltageElm.WF_DC)
		{
			return null;
		}
		if (n == 2)
		{
			return new EditInfo("Frequency (Hz)", this.frequency, 4, 500);
		}
		if (n == 3)
		{
			return new EditInfo("DC Offset (V)", this.bias, -20, 20);
		}
		if (n == 4)
		{
			return new EditInfo("Phase Offset (degrees)", this.phaseShift * 180 / Math.PI, -180, 180)
					.setDimensionless();
		}
		if (n == 5 && this.waveform == VoltageElm.WF_SQUARE)
		{
			return new EditInfo("Duty Cycle", this.dutyCycle * 100, 0, 100).setDimensionless();
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.maxVoltage = ei.value;
		}
		if (n == 3)
		{
			this.bias = ei.value;
		}
		if (n == 2)
		{
			// adjust time zero to maintain continuity ind the waveform
			// even though the frequency has changed.
			double oldfreq = this.frequency;
			this.frequency = ei.value;
			double maxfreq = 1 / (8 * Configs.timeStep);
			if (this.frequency > maxfreq)
			{
				this.frequency = maxfreq;
			}
			double adj = this.frequency - oldfreq;
			this.freqTimeZero = CircuitElm.cirSim.timer.time - oldfreq
					* (CircuitElm.cirSim.timer.time - this.freqTimeZero) / this.frequency;
		}
		if (n == 1)
		{
			int ow = this.waveform;
			this.waveform = ei.choice.getSelectedIndex();
			if (this.waveform == VoltageElm.WF_DC && ow != VoltageElm.WF_DC)
			{
				ei.newDialog = true;
				this.bias = 0;
			}
			else if (this.waveform != VoltageElm.WF_DC && ow == VoltageElm.WF_DC)
			{
				ei.newDialog = true;
			}
			if ((this.waveform == VoltageElm.WF_SQUARE || ow == VoltageElm.WF_SQUARE) && this.waveform != ow)
			{
				ei.newDialog = true;
			}
			this.setPoints();
		}
		if (n == 4)
		{
			this.phaseShift = ei.value * Math.PI / 180;
		}
		if (n == 5)
		{
			this.dutyCycle = ei.value * .01;
		}
	}
}
