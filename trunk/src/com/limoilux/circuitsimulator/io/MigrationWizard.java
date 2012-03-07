
package com.limoilux.circuitsimulator.io;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import com.limoilux.circuit.ui.layout.WizardLayout;

/**
 * Permet de faire des exportation et des importations.
 * 
 * @author Paul Falstad
 * @author David Bernard
 * 
 * @since java 1.6
 * @version 20120220
 */
public class MigrationWizard extends Dialog implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2596556603819783800L;

	private final Frame frame;
	private final JButton importButton;
	private final JButton exportButton;
	private final JButton closeButton;

	private final TextArea textBox;

	private final AbstractAction importAct;
	private final AbstractAction exportAct;
	private final AbstractAction closeAct;

	private final String dumpString;
	private boolean isImport = false;

	public MigrationWizard(Frame frame, String dumpString, Dimension winSize)
	{
		super(frame, "Export - Import", true);

		Point x = null;
		Dimension d = null;

		this.frame = frame;
		this.dumpString = dumpString;

		this.importAct = new ImportAction();
		this.exportAct = new ExportAction();
		this.closeAct = new CancelAction();

		this.setLayout(new WizardLayout());

		this.textBox = new TextArea("", 10, 60, TextArea.SCROLLBARS_BOTH);
		this.add(this.textBox);

		this.importButton = new JButton(this.importAct);
		this.add(this.importButton);

		this.exportButton = new JButton(this.exportAct);
		this.add(this.exportButton);

		this.closeButton = new JButton(this.closeAct);
		this.add(this.closeButton);

		x = frame.getLocationOnScreen();

		this.setSize(400, 300);
		d = this.getSize();

		this.setLocation(x.x + (winSize.width - d.width) / 2, x.y + (winSize.height - d.height) / 2);
	}

	private void dumpText()
	{
		this.textBox.setText(this.dumpString);

		if (this.dumpString.length() > 0)
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

	public boolean isImport()
	{
		return this.isImport;
	}

	private class ImportAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2780013728712155067L;

		private ImportAction()
		{
			super("Import");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			MigrationWizard.this.isImport = true;
			MigrationWizard.this.setVisible(false);
		}
	}

	private class ExportAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -4910456390199779851L;

		private ExportAction()
		{
			super("Export");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			MigrationWizard.this.isImport = false;
			MigrationWizard.this.dumpText();
		}
	}

	private class CancelAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -1340777074044545969L;

		private CancelAction()
		{
			super("Close");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			MigrationWizard.this.isImport = false;
			MigrationWizard.this.setVisible(false);
		}
	}

}
