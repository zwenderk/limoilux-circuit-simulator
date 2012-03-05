
package com.limoilux.circuitsimulator.core;

import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;

public class MenuManager
{
	
	// Menu options
	public CheckboxMenuItem dotsCheckItem;
	public CheckboxMenuItem voltsCheckItem;
	public CheckboxMenuItem powerCheckItem;
	public CheckboxMenuItem smallGridCheckItem;
	public CheckboxMenuItem showValuesCheckItem;
	public CheckboxMenuItem conductanceCheckItem;
	public CheckboxMenuItem euroResistorCheckItem;
	public CheckboxMenuItem printableCheckItem;
	public CheckboxMenuItem conventionCheckItem;
	public MenuItem optionsItem;
	
	public MenuManager()
	{

	}

	public boolean showDots()
	{
		return this.dotsCheckItem.getState();
	}

	public boolean showVolts()
	{
		return this.voltsCheckItem.getState();
	}

	public boolean showPower()
	{
		return this.powerCheckItem.getState();
	}

	public boolean isSmallGrid()
	{
		return this.smallGridCheckItem.getState();
	}

	public boolean showValues()
	{
		return this.showValuesCheckItem.getState();
	}

	public boolean showconductance()
	{
		return this.conductanceCheckItem.getState();
	}

	public boolean isEuroResistor()
	{
		return this.euroResistorCheckItem.getState();
	}

	public boolean isPrintable()
	{
		return this.printableCheckItem.getState();
	}

	public boolean isConventionnal()
	{
		return this.conventionCheckItem.getState();
	}
}
