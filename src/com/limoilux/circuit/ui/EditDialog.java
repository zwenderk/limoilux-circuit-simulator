
package com.limoilux.circuit.ui;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Label;
import java.awt.Point;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;

import com.limoilux.circuit.core.CirSim;
import com.limoilux.circuit.core.Editable;

public class EditDialog extends Dialog implements AdjustmentListener, ActionListener, ItemListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2448517167834043767L;
	public Editable elm;
	public CirSim cframe;
	public Button applyButton, okButton;
	public EditInfo einfos[];
	public int einfocount;
	public static final int barmax = 1000;
	public NumberFormat noCommaFormat;

	public EditDialog(Editable ce, CirSim f)
	{
		super(f, "Edit Component", false);
		this.cframe = f;
		this.elm = ce;
		this.setLayout(new EditDialogLayout());
		this.einfos = new EditInfo[10];
		this.noCommaFormat = NumberFormat.getInstance();
		this.noCommaFormat.setMaximumFractionDigits(10);
		this.noCommaFormat.setGroupingUsed(false);
		int i;
		for (i = 0;; i++)
		{
			this.einfos[i] = this.elm.getEditInfo(i);
			if (this.einfos[i] == null)
			{
				break;
			}
			EditInfo ei = this.einfos[i];
			this.add(new Label(ei.name));
			if (ei.choice != null)
			{
				this.add(ei.choice);
				ei.choice.addItemListener(this);
			}
			else if (ei.checkbox != null)
			{
				this.add(ei.checkbox);
				ei.checkbox.addItemListener(this);
			}
			else
			{
				this.add(ei.textf = new TextField(this.unitString(ei), 10));
				if (ei.text != null)
				{
					ei.textf.setText(ei.text);
				}
				ei.textf.addActionListener(this);
				if (ei.text == null)
				{
					this.add(ei.bar = new Scrollbar(Scrollbar.HORIZONTAL, 50, 10, 0, EditDialog.barmax + 2));
					this.setBar(ei);
					ei.bar.addAdjustmentListener(this);
				}
			}
		}
		this.einfocount = i;
		this.add(this.applyButton = new Button("Apply"));
		this.applyButton.addActionListener(this);
		this.add(this.okButton = new Button("OK"));
		this.okButton.addActionListener(this);
		Point x = this.cframe.mainContainer.getLocationOnScreen();
		Dimension d = this.getSize();
		this.setLocation(x.x + (this.cframe.winSize.width - d.width) / 2, x.y + (this.cframe.winSize.height - d.height)
				/ 2);
	}

	public String unitString(EditInfo ei)
	{
		double v = ei.value;
		double va = Math.abs(v);
		if (ei.dimensionless)
		{
			return this.noCommaFormat.format(v);
		}
		if (v == 0)
		{
			return "0";
		}
		if (va < 1e-9)
		{
			return this.noCommaFormat.format(v * 1e12) + "p";
		}
		if (va < 1e-6)
		{
			return this.noCommaFormat.format(v * 1e9) + "n";
		}
		if (va < 1e-3)
		{
			return this.noCommaFormat.format(v * 1e6) + "u";
		}
		if (va < 1 && !ei.forceLargeM)
		{
			return this.noCommaFormat.format(v * 1e3) + "m";
		}
		if (va < 1e3)
		{
			return this.noCommaFormat.format(v);
		}
		if (va < 1e6)
		{
			return this.noCommaFormat.format(v * 1e-3) + "k";
		}
		if (va < 1e9)
		{
			return this.noCommaFormat.format(v * 1e-6) + "M";
		}
		return this.noCommaFormat.format(v * 1e-9) + "G";
	}

	public double parseUnits(EditInfo ei) throws java.text.ParseException
	{
		String s = ei.textf.getText();
		s = s.trim();
		int len = s.length();
		char uc = s.charAt(len - 1);
		double mult = 1;
		switch (uc)
		{
		case 'p':
		case 'P':
			mult = 1e-12;
			break;
		case 'n':
		case 'N':
			mult = 1e-9;
			break;
		case 'u':
		case 'U':
			mult = 1e-6;
			break;

		// for ohm values, we assume mega for lowercase m, otherwise milli
		case 'm':
			mult = ei.forceLargeM ? 1e6 : 1e-3;
			break;

		case 'k':
		case 'K':
			mult = 1e3;
			break;
		case 'M':
			mult = 1e6;
			break;
		case 'G':
		case 'g':
			mult = 1e9;
			break;
		}
		if (mult != 1)
		{
			s = s.substring(0, len - 1).trim();
		}
		return this.noCommaFormat.parse(s).doubleValue() * mult;
	}

	public void apply()
	{
		int i;
		for (i = 0; i != this.einfocount; i++)
		{
			EditInfo ei = this.einfos[i];
			if (ei.textf == null)
			{
				continue;
			}
			if (ei.text == null)
			{
				try
				{
					double d = this.parseUnits(ei);
					ei.value = d;
				}
				catch (Exception ex)
				{ /* ignored */
				}
			}
			this.elm.setEditValue(i, ei);
			if (ei.text == null)
			{
				this.setBar(ei);
			}
		}
		this.cframe.needAnalyze();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		int i;
		Object src = e.getSource();
		for (i = 0; i != this.einfocount; i++)
		{
			EditInfo ei = this.einfos[i];
			if (src == ei.textf)
			{
				if (ei.text == null)
				{
					try
					{
						double d = this.parseUnits(ei);
						ei.value = d;
					}
					catch (Exception ex)
					{ /* ignored */
					}
				}
				this.elm.setEditValue(i, ei);
				if (ei.text == null)
				{
					this.setBar(ei);
				}
				this.cframe.needAnalyze();
			}
		}
		if (e.getSource() == this.okButton)
		{
			this.apply();
			this.cframe.mainContainer.requestFocus();
			this.setVisible(false);
			CirSim.editDialog = null;
		}
		if (e.getSource() == this.applyButton)
		{
			this.apply();
		}
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		Object src = e.getSource();
		int i;
		for (i = 0; i != this.einfocount; i++)
		{
			EditInfo ei = this.einfos[i];
			if (ei.bar == src)
			{
				double v = ei.bar.getValue() / 1000.;
				if (v < 0)
				{
					v = 0;
				}
				if (v > 1)
				{
					v = 1;
				}
				ei.value = (ei.maxval - ei.minval) * v + ei.minval;
				/*
				 * if (ei.maxval-ei.minval > 100) ei.value =
				 * Math.round(ei.value); else ei.value =
				 * Math.round(ei.value*100)/100.;
				 */
				ei.value = Math.round(ei.value / ei.minval) * ei.minval;
				this.elm.setEditValue(i, ei);
				ei.textf.setText(this.unitString(ei));
				this.cframe.needAnalyze();
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		Object src = e.getItemSelectable();
		int i;
		boolean changed = false;
		for (i = 0; i != this.einfocount; i++)
		{
			EditInfo ei = this.einfos[i];
			if (ei.choice == src || ei.checkbox == src)
			{
				this.elm.setEditValue(i, ei);
				if (ei.newDialog)
				{
					changed = true;
				}
				this.cframe.needAnalyze();
			}
		}
		if (changed)
		{
			this.setVisible(false);
			CirSim.editDialog = new EditDialog(this.elm, this.cframe);
			CirSim.editDialog.show();
		}
	}

	@Override
	public boolean handleEvent(Event ev)
	{
		if (ev.id == Event.WINDOW_DESTROY)
		{
			this.cframe.mainContainer.requestFocus();
			this.setVisible(false);
			CirSim.editDialog = null;
			return true;
		}
		return super.handleEvent(ev);
	}

	public void setBar(EditInfo ei)
	{
		int x = (int) (EditDialog.barmax * (ei.value - ei.minval) / (ei.maxval - ei.minval));
		ei.bar.setValue(x);
	}
}
