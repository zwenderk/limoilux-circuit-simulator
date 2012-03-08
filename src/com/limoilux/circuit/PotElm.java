
package com.limoilux.circuit;

import java.awt.Graphics;
import java.awt.Label;
import java.awt.Point;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.StringTokenizer;

import com.limoilux.circuitsimulator.circuit.CircuitElm;
import com.limoilux.circuitsimulator.core.CircuitSimulator;
import com.limoilux.circuitsimulator.core.Configs;
import com.limoilux.circuitsimulator.ui.DrawUtil;
import com.limoilux.circuitsimulator.ui.EditInfo;

public class PotElm extends CircuitElm implements AdjustmentListener
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
	public int getElementId()
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
		CircuitElm.cirSim.circuit.setNeedAnalysis(true);
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
		if (Math.abs(this.longueurX) > Math.abs(this.longueurY))
		{
			this.longueurX = CircuitElm.cirSim.snapGrid(this.longueurX / 2) * 2;
			this.point2.x = this.x2 = this.point1.x + this.longueurX;
			offset = this.longueurX < 0 ? this.longueurY : -this.longueurY;
			this.point2.y = this.point1.y;
		}
		else
		{
			this.longueurY = CircuitElm.cirSim.snapGrid(this.longueurY / 2) * 2;
			this.point2.y = this.y2 = this.point1.y + this.longueurY;
			offset = this.longueurY > 0 ? this.longueurX : -this.longueurX;
			this.point2.x = this.point1.x;
		}
		if (offset == 0)
		{
			offset = CircuitElm.cirSim.gridSize;
		}
		this.longueur = CircuitElm.distance(this.point1, this.point2);
		int bodyLen = 32;
		this.calcLeads(bodyLen);
		this.position = this.slider.getValue() * .0099 + .005;
		int soff = (int) ((this.position - .5) * bodyLen);
		// int offset2 = offset - sign(offset)*4;
		this.post3 = CircuitElm.interpPoint(this.point1, this.point2, .5, offset);
		this.corner2 = CircuitElm.interpPoint(this.point1, this.point2, soff / this.longueur + .5, offset);
		this.arrowPoint = CircuitElm.interpPoint(this.point1, this.point2, soff / this.longueur + .5,
				8 * CircuitElm.sign(offset));
		this.midpoint = CircuitElm.interpPoint(this.point1, this.point2, soff / this.longueur + .5);
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
		int hs = Configs.EURO_RESISTOR ? 6 : 8;
		double v1 = this.volts[0];
		double v2 = this.volts[1];
		double v3 = this.volts[2];
		this.setBbox(this.point1, this.point2, hs);
		this.draw2Leads(g);
		this.setPowerColor(g, true);
		double segf = 1. / segments;
		int divide = (int) (segments * this.position);
		if (!Configs.EURO_RESISTOR)
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
		DrawUtil.drawThickLine(g, this.post3, this.corner2);
		DrawUtil.drawThickLine(g, this.corner2, this.arrowPoint);
		DrawUtil.drawThickLine(g, this.arrow1, this.arrowPoint);
		DrawUtil.drawThickLine(g, this.arrow2, this.arrowPoint);
		this.curcount1 = CircuitElm.updateDotCount(this.current1, this.curcount1);
		this.curcount2 = CircuitElm.updateDotCount(this.current2, this.curcount2);
		this.curcount3 = CircuitElm.updateDotCount(this.current3, this.curcount3);
		if (CircuitElm.cirSim.mouseMan.dragElm != this)
		{
			DrawUtil.drawDots(g, this.point1, this.midpoint, this.curcount1);
			DrawUtil.drawDots(g, this.point2, this.midpoint, this.curcount2);
			DrawUtil.drawDots(g, this.post3, this.corner2, this.curcount3);
			DrawUtil.drawDots(g, this.corner2, this.midpoint,
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
		CircuitElm.cirSim.circuit.stampResistor(this.nodes[0], this.nodes[2], this.resistance1);
		CircuitElm.cirSim.circuit.stampResistor(this.nodes[2], this.nodes[1], this.resistance2);
	}

	@Override
	public void getInfo(String arr[])
	{
		arr[0] = "potentiometer";
		arr[1] = "Vd = " + CircuitElm.getVoltageDText(this.getVoltageDiff());
		arr[2] = "R1 = " + CircuitElm.getUnitText(this.resistance1, CircuitSimulator.ohmString);
		arr[3] = "R2 = " + CircuitElm.getUnitText(this.resistance2, CircuitSimulator.ohmString);
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
