
package com.limoilux.circuit.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.AbstractAction;

public class ActivityManager
{
	private final List<ActivityListener> listeners;

	private final PlayAction play;
	private final StopAction stop;
	private boolean playing = false;

	public ActivityManager(boolean playing)
	{
		this.listeners = new ArrayList<ActivityListener>();
		this.play = new PlayAction();
		this.stop = new StopAction();

		this.setPlaying(playing);
	}

	public ActivityManager()
	{
		this(true);
	}

	public void setPlaying(boolean playing)
	{
		ListIterator<ActivityListener> ite;
		ActivityListener listener;

		this.play.setEnabled(!playing);
		this.stop.setEnabled(playing);
		this.playing = playing;

		ite = this.listeners.listIterator();
		while (ite.hasNext())
		{
			listener = ite.next();
			listener.stateChanged(playing);
		}
	}

	public boolean isPlaying()
	{
		return this.playing;
	}

	public AbstractAction getPlayAction()
	{
		return this.play;
	}

	public AbstractAction getStopAction()
	{
		return this.stop;
	}

	public void addActivityListener(ActivityListener listener)
	{
		this.listeners.add(listener);
	}

	private class PlayAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3347059575351358150L;

		private PlayAction()
		{
			super(">");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			ActivityManager.this.setPlaying(true);
		}
	}

	private class StopAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6308434130444520010L;

		private StopAction()
		{
			super("||");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			ActivityManager.this.setPlaying(false);
		}
	}
}
