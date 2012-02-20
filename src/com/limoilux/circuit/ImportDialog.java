package com.limoilux.circuit;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ImportDialog extends Dialog implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2596556603819783800L;
	CirSim cframe;
	Button importButton, closeButton;
	TextArea text;
	boolean isURL;

	ImportDialog(CirSim f, String str, boolean url)
	{
		super(f, str.length() > 0 ? "Export" : "Import", false);
		this.isURL = url;
		this.cframe = f;
		this.setLayout(new ImportDialogLayout());
		this.add(this.text = new TextArea(str, 10, 60, TextArea.SCROLLBARS_BOTH));
		this.importButton = new Button("Import");
		if (!this.isURL)
		{
			this.add(this.importButton);
		}
		this.importButton.addActionListener(this);
		this.add(this.closeButton = new Button("Close"));
		this.closeButton.addActionListener(this);
		Point x = this.cframe.mainContainer.getLocationOnScreen();
		this.resize(400, 300);
		Dimension d = this.getSize();
		this.setLocation(x.x + (this.cframe.winSize.width - d.width) / 2, x.y + (this.cframe.winSize.height - d.height) / 2);
		this.show();
		if (str.length() > 0)
		{
			this.text.selectAll();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		int i;
		Object src = e.getSource();
		if (src == this.importButton)
		{
			this.cframe.readSetup(this.text.getText());
			this.setVisible(false);
		}
		if (src == this.closeButton)
		{
			this.setVisible(false);
		}
	}

	@Override
	public boolean handleEvent(Event ev)
	{
		if (ev.id == Event.WINDOW_DESTROY)
		{
			this.cframe.mainContainer.requestFocus();
			this.setVisible(false);
			this.cframe.impDialog = null;
			return true;
		}
		return super.handleEvent(ev);
	}
}
