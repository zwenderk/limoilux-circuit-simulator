
package com.limoilux.circuitsimulator.core;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MenuManager
{
	
	// Menu options
	JMenu optionMenu;
	public JCheckBoxMenuItem dotsCheckItem;
	public JCheckBoxMenuItem voltsCheckItem;
	public JCheckBoxMenuItem powerCheckItem;
	public JCheckBoxMenuItem smallGridCheckItem;
	public JCheckBoxMenuItem conductanceCheckItem;
	public JCheckBoxMenuItem euroResistorCheckItem;
	public JCheckBoxMenuItem printableCheckItem;
	public JCheckBoxMenuItem conventionCheckItem;
	public JMenuItem optionsItem;
	
	ItemListener itemList;
	ActionListener actionList;

	
	public MenuManager(ItemListener itemList, ActionListener actionList)
	{
		this.itemList = itemList;
		this.actionList = actionList;
		JMenu menu;
		
		// Menu Option
		menu =  new JMenu("Options");



		this.voltsCheckItem = this.getCheckItem("Show Voltage");
		//this.voltsCheckItem.setState(true);
		menu.add(this.voltsCheckItem);

		this.powerCheckItem = this.getCheckItem("Show Power");
		menu.add(this.powerCheckItem);

		

		
		this.optionMenu = menu;
	}



	public boolean showVolts()
	{
		return this.voltsCheckItem.getState();
	}

	public boolean showPower()
	{
		return this.powerCheckItem.getState();
	}







	public JMenu getOptionMenu()
	{
		return this.optionMenu;
	}
	
	private JCheckBoxMenuItem getCheckItem(String s, String t)
	{
		JCheckBoxMenuItem mi = new JCheckBoxMenuItem(s);
		mi.addItemListener(this.itemList);
		mi.setActionCommand(t);
		return mi;
	}
	
	private JCheckBoxMenuItem getCheckItem(String s)
	{
		JCheckBoxMenuItem mi = new JCheckBoxMenuItem(s);
		mi.addItemListener(this.itemList);
		mi.setActionCommand("");
		return mi;
	}

	private JMenuItem getMenuItem(String s)
	{
		JMenuItem mi = new JMenuItem(s);
		mi.addActionListener(this.actionList);
		return mi;
	}
}
