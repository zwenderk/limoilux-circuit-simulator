
package com.limoilux.circuit.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ActivityManager
{
	private final PlayAction play;
	private final StopAction stop;
	private boolean playing = false;

	public ActivityManager(boolean playing)
	{
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
		this.play.setEnabled(playing);
		this.stop.setEnabled(!playing);
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

	private class PlayAction extends AbstractAction
	{
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
