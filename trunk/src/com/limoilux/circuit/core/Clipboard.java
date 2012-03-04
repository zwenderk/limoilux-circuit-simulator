
package com.limoilux.circuit.core;

import java.awt.MenuItem;
import java.util.Vector;

public class Clipboard
{
	public String clipboard;
	public final Vector<String> undoStack;
	public final Vector<String> redoStack;
	
	public MenuItem undoItem;
	public MenuItem redoItem;

	public Clipboard()
	{
		this.undoStack = new Vector<String>();
		this.redoStack = new Vector<String>();
	}
	
	public void enableUndoRedo()
	{
		this.redoItem.setEnabled(this.redoStack.size() > 0);
		this.undoItem.setEnabled(this.undoStack.size() > 0);
	}
}
