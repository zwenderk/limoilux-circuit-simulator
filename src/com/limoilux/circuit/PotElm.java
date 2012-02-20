
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Label;
import java.awt.Point;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.StringTokenizer;

class PotElm extends CircuitElm implements AdjustmentListener
{
	double position, maxResistance, resistance1, resistance2;
	double current1, current2, current3;
	double curcount1, curcount2, curcount3;
	Scrollbar slider;
	Label label;
	String sliderText;

	public PotElm(int xx, int yy)
	{
		super(xx, yy);
		this.setup();
		this.maxResistance = 1000;
		this.position = .5;
		this.sliderText = "Resistance";
		this.createSlider();
	}

	public PotElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.maxResistance = new Double(st.nextToken()).doubleValue();
		this.position = new Double(st.nextToken()).doubleValue();
		this.sliderText = st.nextToken();
		while (st.hasMoreTokens())
		{
			this.sliderText += ' ' + st.nextToken();
		}
		this.createSlider();
	}

	void setup()
	{
	}

	@Override
	public int getPostCount()
	{
		return 3;
	}

	@Override
	public int getDumpType()
	{
		return 174;
	}

	@Override
	public Point getPost(int n)
	{
		return n == 0 ? this.point1 : n == 1 ? this.point2 : this.post3;
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.maxResistance + " " + this.position + " " + this.sliderText;
	}

	void createSlider()
	{
		CircuitElm.cirSim.mainContainer.add(this.label = new Label(this.sliderText, Label.CENTER));
		int value = (int) (this.position * 100);
		CircuitElm.cirSim.mainContainer.add(this.slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101));
		CircuitElm.cirSim.mainContainer.validate();
		this.slider.addAdjustmentListener(this);
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		CircuitElm.cirSim.analyzeFlag = true;
		this.setPoints();
	}

	@Override
	public void delete()
	{
		CircuitElm.cirSim.mainContainer.remove(this.label);
		CircuitElm.cirSim.mainContainer.remove(this.slider);
	}

	Point post3, corner2, arrowPoint, midpoint, arrow1, arrow2;
	Point ps3, ps4;
	int bodyLen;

	@Override
	public void setPoints()
	{
		super.setPoints();
		int offset = 0;
		if (CircuitElm.abs(this.dx) > CircuitElm.abs(this.dy))
		{
			this.dx = CircuitElm.cirSim.snapGrid(this.dx / 2) * 2;
			this.point2.x = this.x2 = this.point1.x + this.dx;
			offset = this.dx < 0 ? this.dy : -this.dy;
			this.point2.y = this.point1.y;
		}
		else
		{
			this.dy = CircuitElm.cirSim.snapGrid(this.dy / 2) * 2;
			this.point2.y = this.y2 = this.point1.y + this.dy;
			offset = this.dy > 0 ? this.dx : -this.dx;
			this.point2.x = this.point1.x;
		}
		if (offset == 0)
		{
			offset = CircuitElm.cirSim.gridSize;
		}
		this.dn = CircuitElm.distance(this.point1, this.point2);
		int bodyLen = 32;
		this.calcLeads(bodyLen);
		this.position = this.slider.getValue() * .0099 + .005;
		int soff = (int) ((this.position - .5) * bodyLen);
		// int offset2 = offset - sign(offset)*4;
		this.post3 = CircuitElm.interpPoint(this.point1, this.point2, .5, offset);
		this.corner2 = CircuitElm.interpPoint(this.point1, this.point2, soff / this.dn + .5, offset);
		this.arrowPoint = CircuitElm.interpPoint(this.point1, this.point2, soff / this.dn + .5,
				8 * CircuitElm.sign(offset));
		this.midpoint = CircuitElm.interpPoint(this.point1, this.point2, soff / this.dn + .5);
		this.arrow1 = new Point();
		this.arrow2 = new Point();
		double clen = CircuitElm.abs(offset) - 8;
		CircuitElm.interpPoint2(this.corner2, this.arrowPoint, this.arrow1, this.arrow2, (clen - 8) / clen, 8);
		this.ps3 = new Point();
		this.ps4 = new Point();
	}

	@Override
	public void draw(Graphics g)
	{
		int segments = 16;
		int i;
		int ox = 0;
		int hs = CircuitElm.cirSim.euroResistorCheckItem.getState() ? 6 : 8;
		double v1 = this.volts[0];
		double v2 = this.volts[1];
		double v3 = this.volts[2];
		this.setBbox(this.point1, this.point2, hs);
		this.draw2Leads(g);
		this.setPowerColor(g, true);
		double segf = 1. / segments;
		int divide = (int) (segments * this.position);
		if (!CircuitElm.cirSim.euroResistorCheckItem.getState())
		{
			// draw zigzag
			for (i = 0; i != segments; i++)
			{
				int nx = 0;
				switch (i & 3)
				{
				case 0:
					nx = 1;
					break;
				case 2:
					nx = -1;
					break;
				default:
					nx = 0;
					break;
				}
				double v = v1 + (v3 - v1) * i / divide;
				if (i >= divide)
				{
					v = v3 + (v2 - v3) * (i - divide) / (segments - divide);
				}
				this.setVoltageColor(g, v);
				CircuitElm.interpPoint(this.lead1, this.lead2, CircuitElm.ps1, i * segf, hs * ox);
				CircuitElm.interpPoint(this.lead1, this.lead2, CircuitElm.ps2, (i + 1) * segf, hs * nx);
				CircuitElm.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
				ox = nx;
			}
		}
		else
		{
			// draw rectangle
			this.setVoltageColor(g, v1);
			CircuitElm.interpPoint2(this.lead1, this.lead2, CircuitElm.ps1, CircuitElm.ps2, 0, hs);
			CircuitElm.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
			for (i = 0; i != segments; i++)
			{
				double v = v1 + (v3 - v1) * i / divide;
				if (i >= divide)
				{
					v = v3 + (v2 - v3) * (i - divide) / (segments - divide);
				}
				this.setVoltageColor(g, v);
				CircuitElm.interpPoint2(this.lead1, this.lead2, CircuitElm.ps1, CircuitElm.ps2, i * segf, hs);
				CircuitElm.interpPoint2(this.lead1, this.lead2, this.ps3, this.ps4, (i + 1) * segf, hs);
				CircuitElm.drawThickLine(g, CircuitElm.ps1, this.ps3);
				CircuitElm.drawThickLine(g, CircuitElm.ps2, this.ps4);
			}
			CircuitElm.interpPoint2(this.lead1, this.lead2, CircuitElm.ps1, CircuitElm.ps2, 1, hs);
			CircuitElm.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
		}
		this.setVoltageColor(g, v3);
		CircuitElm.drawThickLine(g, this.post3, this.corner2);
		CircuitElm.drawThickLine(g, this.corner2, this.arrowPoint);
		CircuitElm.drawThickLine(g, this.arrow1, this.arrowPoint);
		CircuitElm.drawThickLine(g, this.arrow2, this.arrowPoint);
		this.curcount1 = this.updateDotCount(this.current1, this.curcount1);
		this.curcount2 = this.updateDotCount(this.current2, this.curcount2);
		this.curcount3 = this.updateDotCount(this.current3, this.curcount3);
		if (CircuitElm.cirSim.dragElm != this)
		{
			CircuitElm.drawDots(g, this.point1, this.midpoint, this.curcount1);
			CircuitElm.drawDots(g, this.point2, this.midpoint, this.curcount2);
			CircuitElm.drawDots(g, this.post3, this.corner2, this.curcount3);
			CircuitElm.drawDots(g, this.corner2, this.midpoint,
					this.curcount3 + CircuitElm.distance(this.post3, this.corner2));
		}
		this.drawPosts(g);
	}

	@Override
	public void calculateCurrent()
	{
		this.current1 = (this.volts[0] - this.volts[2]) / this.resistance1;
		this.current2 = (this.volts[1] - this.volts[2]) / this.resistance2;
		this.current3 = -this.current1 - this.current2;
	}

	@Override
	public void stamp()
	{
		this.resistance1 = this.maxResistance * this.position;
		this.resistance2 = this.maxResistance * (1 - this.position);
		CircuitElm.cirSim.stampResistor(this.nodes[0], this.nodes[2], this.resistance1);
		CircuitElm.cirSim.stampResistor(this.nodes[2], this.nodes[1], this.resistance2);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "potentiometer";
		arr[1] = "Vd = " + CircuitElm.getVoltageDText(this.getVoltageDiff());
		arr[2] = "R1 = " + CircuitElm.getUnitText(this.resistance1, CirSim.ohmString);
		arr[3] = "R2 = " + CircuitElm.getUnitText(this.resistance2, CirSim.ohmString);
		arr[4] = "I1 = " + CircuitElm.getCurrentDText(this.current1);
		arr[5] = "I2 = " + CircuitElm.getCurrentDText(this.current2);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		// ohmString doesn't work here on linux
		if (n == 0)
		{
			return new EditInfo("Resistance (ohms)", this.maxResistance, 0, 0);
		}
		if (n == 1)
		{
			EditInfo ei = new EditInfo("Slider Text", 0, -1, -1);
			ei.text = this.sliderText;
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.maxResistance = ei.value;
		}
		if (n == 1)
		{
			this.sliderText = ei.textf.getText();
			this.label.setText(this.sliderText);
		}
	}
}
