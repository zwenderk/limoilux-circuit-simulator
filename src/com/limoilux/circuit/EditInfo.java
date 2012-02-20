
package com.limoilux.circuit;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Scrollbar;
import java.awt.TextField;

class EditInfo
{
	EditInfo(String n, double val, double mn, double mx)
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

	EditInfo setDimensionless()
	{
		this.dimensionless = true;
		return this;
	}

	String name, text;
	double value, minval, maxval;
	TextField textf;
	Scrollbar bar;
	Choice choice;
	Checkbox checkbox;
	boolean newDialog;
	boolean forceLargeM;
	boolean dimensionless;
}
