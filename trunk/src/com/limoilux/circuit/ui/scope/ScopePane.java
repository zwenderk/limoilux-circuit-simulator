
package com.limoilux.circuit.ui.scope;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;

import com.limoilux.circuit.core.CirSim;
import com.limoilux.circuit.core.CoreUtil;
import com.limoilux.circuit.techno.CircuitAnalysisException;
import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuit.techno.CircuitNode;
import com.limoilux.circuit.techno.CircuitNodeLink;
import com.limoilux.circuit.ui.DrawUtil;

public class ScopePane extends Panel
{
	private static final long serialVersionUID = 6532697895940366402L;
	private final CirSim cirSim;
	public Image scopeImg;
	
	public ScopePane(CirSim cirSim)
	{
		this.cirSim = cirSim;
		
		this.setPreferredSize(new Dimension(0, 150));
		this.setBackground(Color.GREEN);
	}
	
	@Override
	public void update(Graphics g)
	{
		super.update(g);
		
		this.updateCircuit( g);
	}
	
	public void updateCircuit(Graphics realg)
	{
		Graphics g = null;
		CircuitElm realMouseElm;

	



		g = this.scopeImg.getGraphics();
		g.setColor(Color.black);

		g.fillRect(0, 0, this.cirSim.winSize.width, this.cirSim.winSize.height);

		if (this.cirSim.activityManager.isPlaying())
		{
			try
			{
				this.cirSim.runCircuit();
			}
			catch (CircuitAnalysisException e)
			{
				this.cirSim.handleAnalysisException(e);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				this.cirSim.circuit.setNeedAnalysis(true);
				this.repaint();

				return;
			}
		}

		if (this.cirSim.activityManager.isPlaying())
		{
			long sysTime = System.currentTimeMillis();
			if (this.cirSim.timer.lastTime != 0)
			{
				int inc = (int) (sysTime - this.cirSim.timer.lastTime);
				double c = this.cirSim.currentBar.getValue();
				c = Math.exp(c / 3.5 - 14.2);
				CircuitElm.currentMult = 1.7 * inc * c;
				if (!this.cirSim.conventionCheckItem.getState())
				{
					CircuitElm.currentMult = -CircuitElm.currentMult;
				}
			}
			if (sysTime - this.cirSim.timer.secTime >= 1000)
			{
				this.cirSim.timer.secTime = sysTime;
			}
			this.cirSim.timer.lastTime = sysTime;
		}
		else
		{
			this.cirSim.timer.lastTime = 0;
		}

		int i;
		Font oldfont = g.getFont();



		g.setFont(oldfont);

		// Dessinage des scopes
		if (this.cirSim.stopMessage == null)
		{
			this.cirSim.scopeMan.drawScope(g);
		}


		realMouseElm = this.cirSim.mouseElm;
		this.cirSim.mouseElm = realMouseElm;
		/*
		 * g.setColor(Color.white); g.drawString("Framerate: " + framerate, 10,
		 * 10); g.drawString("Steprate: " + steprate, 10, 30);
		 * g.drawString("Steprate/iter: " + (steprate/getIterCount()), 10, 50);
		 * g.drawString("iterc: " + (getIterCount()), 10, 70);
		 */

		realg.drawImage(this.scopeImg, 0, 0, this.cirSim.cirFrame);

		if (this.cirSim.activityManager.isPlaying() && !this.cirSim.circuit.matrix.matrixIsNull())
		{

			long delay = this.cirSim.timer.calculateDelay();

			if (delay > 0)
			{
				try
				{
					Thread.sleep(delay);
				}
				catch (InterruptedException e)
				{
				}
			}

			this.repaint();
		}

	}
}
