
package com.limoilux.circuitsimulator.io;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.limoilux.circuitsimulator.circuit.CircuitManager;

public class IOManager
{
	private final AbstractAction saveAct;
	private final AbstractAction saveAsAct;
	private final AbstractAction loadAct;
	private final CircuitManager circuitMan;

	public IOManager(CircuitManager circuitMan)
	{
		this.circuitMan = circuitMan;
		
		this.saveAct = new SaveAction();
		this.saveAsAct = new SaveAsAction();
		this.loadAct = new LoadAction();
	}

	public AbstractAction getSaveAction()
	{
		return this.saveAct;
	}
	
	public AbstractAction getLoadAction()
	{
		return this.saveAsAct;
	}
	
	public AbstractAction getSaveAsAction()
	{
		return this.loadAct;
	}

	public void load()
	{

	}

	public void save()
	{

	}

	public void saveAs()
	{

	}

	private class SaveAction extends AbstractAction
	{
		private SaveAction()
		{
			super("Save");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			IOManager.this.save();
		}
	}

	private class LoadAction extends AbstractAction
	{
		private LoadAction()
		{
			super("Load...");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			IOManager.this.load();
		}
	}

	private class SaveAsAction extends AbstractAction
	{
		private SaveAsAction()
		{
			super("Save as...");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			IOManager.this.saveAs();
		}
	}
}
