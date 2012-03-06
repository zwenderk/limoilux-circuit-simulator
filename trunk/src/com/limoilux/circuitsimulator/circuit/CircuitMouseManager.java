
package com.limoilux.circuitsimulator.circuit;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuitsimulator.core.CircuitSimulator;

public class CircuitMouseManager implements MouseMotionListener, MouseListener
{

	private static final int MODE_ADD_ELM = 0;
	private static final int MODE_DRAG_ALL = 1;
	private static final int MODE_DRAG_ROW = 2;
	private static final int MODE_DRAG_COLUMN = 3;
	private static final int MODE_DRAG_SELECTED = 4;
	private static final int MODE_DRAG_POST = 5;
	private static final int MODE_SELECT = 6;

	public int mousePost = -1;
	public int dragX;
	public int dragY;
	public int tempMouseMode;
	public int mouseMode;
	public String mouseModeStr = "Select";
	public int initDragX;
	public int initDragY;
	public int draggingPost;
	public int scopeSelected = -1;

	public CircuitElm mouseElm;
	public CircuitElm dragElm;

	public CircuitElm plotXElm;
	public CircuitElm plotYElm;

	private final Circuit circuit;

	public CircuitMouseManager(Circuit circuit)
	{
		this.circuit = circuit;

		this.tempMouseMode = CircuitMouseManager.MODE_SELECT;
		this.mouseMode = CircuitMouseManager.MODE_SELECT;
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
		{
			if (this.mouseMode == CircuitMouseManager.MODE_SELECT
					|| this.mouseMode == CircuitMouseManager.MODE_DRAG_SELECTED)
			{
				this.circuit.clearSelection();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{
		this.scopeSelected = -1;
		this.mouseElm = null;

		this.plotXElm = null;
		this.plotYElm = null;
	}

	@Override
	public void mousePressed(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void leftClick(int modifiersEx)
	{
		// left mouse
		this.tempMouseMode = this.mouseMode;
		if ((modifiersEx & InputEvent.ALT_DOWN_MASK) != 0 && (modifiersEx & InputEvent.META_DOWN_MASK) != 0)
		{
			this.tempMouseMode = CircuitMouseManager.MODE_DRAG_COLUMN;
		}
		else if ((modifiersEx & InputEvent.ALT_DOWN_MASK) != 0 && (modifiersEx & InputEvent.SHIFT_DOWN_MASK) != 0)
		{
			this.tempMouseMode = CircuitMouseManager.MODE_DRAG_ROW;
		}
		else if ((modifiersEx & InputEvent.SHIFT_DOWN_MASK) != 0)
		{
			this.tempMouseMode = CircuitMouseManager.MODE_SELECT;
		}
		else if ((modifiersEx & InputEvent.ALT_DOWN_MASK) != 0)
		{
			this.tempMouseMode = CircuitMouseManager.MODE_DRAG_ALL;
		}
		else if ((modifiersEx & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK)) != 0)
		{
			this.tempMouseMode = CircuitMouseManager.MODE_DRAG_POST;
		}
	}

	public boolean rightClick(int modifierEx)
	{
		// right mouse
		if ((modifierEx & InputEvent.SHIFT_DOWN_MASK) != 0)
		{
			tempMouseMode = CircuitSimulator.MODE_DRAG_ROW;
		}
		else if ((modifierEx & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK)) != 0)
		{
			tempMouseMode = CircuitSimulator.MODE_DRAG_COLUMN;
		}
		else
		{
			// TODO à restructurer

			// Existe pour gardé le fonctionnement du programme original.
			return true;
		}

		return false;
	}

	public boolean isModeSelected()
	{
		return tempMouseMode == CircuitMouseManager.MODE_SELECT
				|| tempMouseMode == CircuitMouseManager.MODE_DRAG_SELECTED;
	}
	
	public boolean testMouseMode(String mouseMode)
	{
		return this.mouseModeStr.compareTo(mouseMode) == 0;
	}

}
