
package com.limoilux.circuit;

import java.awt.Label;
import java.awt.Scrollbar;
import java.util.StringTokenizer;

class VarRailElm extends RailElm
{
	Scrollbar slider;
	Label label;
	String sliderText;

	public VarRailElm(int xx, int yy)
	{
		super(xx, yy, VoltageElm.WF_VAR);
		this.sliderText = "Voltage";
		this.frequency = this.maxVoltage;
		this.createSlider();
	}

	public VarRailElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f, st);
		this.sliderText = st.nextToken();
		while (st.hasMoreTokens())
		{
			this.sliderText += ' ' + st.nextToken();
		}
		this.createSlider();
	}

	@Override
	public String dump()
	{
		return super.dump() + " " + this.sliderText;
	}

	@Override
	int getDumpType()
	{
		return 172;
	}

	void createSlider()
	{
		this.waveform = VoltageElm.WF_VAR;
		CircuitElm.cirSim.mainContainer.add(this.label = new Label(this.sliderText, Label.CENTER));
		int value = (int) ((this.frequency - this.bias) * 100 / (this.maxVoltage - this.bias));
		CircuitElm.cirSim.mainContainer.add(this.slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101));
		CircuitElm.cirSim.mainContainer.validate();
	}

	@Override
	double getVoltage()
	{
		this.frequency = this.slider.getValue() * (this.maxVoltage - this.bias) / 100. + this.bias;
		return this.frequency;
	}

	@Override
	public void delete()
	{
		CircuitElm.cirSim.mainContainer.remove(this.label);
		CircuitElm.cirSim.mainContainer.remove(this.slider);
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			return new EditInfo("Min Voltage", this.bias, -20, 20);
		}
		if (n == 1)
		{
			return new EditInfo("Max Voltage", this.maxVoltage, -20, 20);
		}
		if (n == 2)
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
			this.bias = ei.value;
		}
		if (n == 1)
		{
			this.maxVoltage = ei.value;
		}
		if (n == 2)
		{
			this.sliderText = ei.textf.getText();
			this.label.setText(this.sliderText);
		}
	}
}
