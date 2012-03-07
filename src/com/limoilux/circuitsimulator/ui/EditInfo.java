
package com.limoilux.circuitsimulator.ui;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Scrollbar;
import java.awt.TextField;

public class EditInfo
{
	public String name, text;
	public double value, minval, maxval;
	public TextField textf;
	public Scrollbar bar;
	public Choice choice;
	public Checkbox checkbox;
	public boolean newDialog;
	public boolean forceLargeM;
	public boolean dimensionless;

	public EditInfo(String n, double val, double mn, double mx)
	{
		this.name = n;
		this.value = val;
		if (mn == 0 && mx == 0 && val > 0)
		{
			this.minval = 1e10;
			while (this.minval > val / 100)
			{
				this.minval /= 10.;
			}
			this.maxval = this.minval * 1000;
		}
		else
		{
			this.minval = mn;
			this.maxval = mx;
		}
		this.forceLargeM = this.name.indexOf("(ohms)") > 0 || this.name.indexOf("(Hz)") > 0;
		this.dimensionless = false;
	}

	public EditInfo setDimensionless()
	{
		this.dimensionless = true;
		return this;
	}

}
