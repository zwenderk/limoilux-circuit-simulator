
package com.limoilux.circuit.ui;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.limoilux.circuit.core.CirSim;

public class ImportDialog extends Dialog implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2596556603819783800L;

	@Deprecated
	
	private final Frame frame;
	private final Button importButton;
	private final Button closeButton;
	private final TextArea textBox;
	private String content = null;

	public ImportDialog(CirSim f, Frame frame, String str, Dimension winSize)
	{
		super(frame, str.length() > 0 ? "Export" : "Import", true);


		this.frame = frame;

		this.setLayout(new ImportDialogLayout());

		this.textBox = new TextArea(str, 10, 60, TextArea.SCROLLBARS_BOTH);
		this.add(this.textBox);
		this.importButton = new Button("Import");

		this.add(this.importButton);

		this.importButton.addActionListener(this);

		this.closeButton = new Button("Close");
		this.add(this.closeButton);
		this.closeButton.addActionListener(this);

		Point x = frame.getLocationOnScreen();

		this.setSize(400, 300);
		Dimension d = this.getSize();

		this.setLocation(x.x + (winSize.width - d.width) / 2, x.y + (winSize.height - d.height) / 2);

		if (str.length() > 0)
		{
			this.textBox.selectAll();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		this.setVisible(false);
	}
	
	public String getContent()
	{
		return this.textBox.getText();
	}

	@Override
	public boolean handleEvent(Event ev)
	{
		boolean out = false;
		if (ev.id == Event.WINDOW_DESTROY)
		{
			this.frame.requestFocus();

			this.setVisible(false);

			out = true;
		}
		else
		{
			out = super.handleEvent(ev);
		}
		
		return out;
	}
}
