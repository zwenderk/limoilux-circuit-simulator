
package com.limoilux.circuitsimulator.core;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

public class MenuManager
{
	
	// Menu options
	Menu optionMenu;
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
	
	ItemListener itemList;
	ActionListener actionList;

	
	public MenuManager(ItemListener itemList, ActionListener actionList)
	{
		this.itemList = itemList;
		this.actionList = actionList;
		Menu menu;
		
		// Menu Option
		menu =  new Menu("Options");
		this.dotsCheckItem = this.getCheckItem("Show Current");
		this.dotsCheckItem.setState(true);
		menu.add(this.dotsCheckItem);

		this.voltsCheckItem = this.getCheckItem("Show Voltage");
		this.voltsCheckItem.setState(true);
		menu.add(this.voltsCheckItem);

		this.powerCheckItem = this.getCheckItem("Show Power");
		menu.add(this.powerCheckItem);
		
		this.showValuesCheckItem = this.getCheckItem("Show Values");
		this.showValuesCheckItem.setState(true);
		menu.add(this.showValuesCheckItem);

		//conductanceCheckItem = getCheckItem("Show Conductance");
		// m.add(conductanceCheckItem = getCheckItem("Show Conductance"));
		
		this.smallGridCheckItem = this.getCheckItem("Small Grid");
		menu.add(this.smallGridCheckItem);
		
		this.euroResistorCheckItem = this.getCheckItem("European Resistors");
		this.euroResistorCheckItem.setState(false);
		menu.add(this.euroResistorCheckItem);

		this.printableCheckItem = this.getCheckItem("White Background");
		this.printableCheckItem.setState(false);
		menu.add(this.printableCheckItem);

		this.conventionCheckItem = this.getCheckItem("Conventional Current Motion");
		this.conventionCheckItem.setState(true);
		menu.add(this.conventionCheckItem);
	
		this.optionsItem = this.getMenuItem("Other Options...");
		menu.add(this.optionsItem);
		this.optionMenu = menu;
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
	
	public Menu getOptionMenu()
	{
		return this.optionMenu;
	}
	
	private CheckboxMenuItem getCheckItem(String s, String t)
	{
		CheckboxMenuItem mi = new CheckboxMenuItem(s);
		mi.addItemListener(this.itemList);
		mi.setActionCommand(t);
		return mi;
	}
	
	private CheckboxMenuItem getCheckItem(String s)
	{
		CheckboxMenuItem mi = new CheckboxMenuItem(s);
		mi.addItemListener(this.itemList);
		mi.setActionCommand("");
		return mi;
	}

	private MenuItem getMenuItem(String s)
	{
		MenuItem mi = new MenuItem(s);
		mi.addActionListener(this.actionList);
		return mi;
	}
}
