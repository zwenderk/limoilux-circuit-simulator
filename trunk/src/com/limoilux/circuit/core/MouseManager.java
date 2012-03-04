package com.limoilux.circuit.core;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuitsimulator.circuit.Circuit;
import com.limoilux.circuitsimulator.core.CircuitSimulator;

public class MouseManager implements MouseMotionListener, MouseListener
{
	private static final int MODE_ADD_ELM = 0;
	private static final int MODE_DRAG_ALL = 1;

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
	
	public Circuit circuit;

	public MouseManager(Circuit circuit)
	{
		this.tempMouseMode = MouseManager.MODE_SELECT;
		this.mouseMode = MouseManager.MODE_SELECT;
	}
	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
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
	public void mouseMoved(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

}
