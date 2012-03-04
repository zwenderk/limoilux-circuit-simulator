
package com.limoilux.circuit.ui;

import java.awt.Color;
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

public class CircuitPane extends Panel
{
	private static final long serialVersionUID = -3418969740606491502L;

	public Image circuitImage;
	private final CirSim cirSim;

	public CircuitPane(CirSim cirSim)
	{
		this.cirSim = cirSim;

		this.setBackground(Color.BLACK);

	}

	@Override
	public void update(Graphics g)
	{
		super.update(g);
		this.updateCircuit(g);
	}
	
	public void preUpdate()
	{
		
	}

	public void updateCircuit(Graphics realg)
	{
		Graphics g = null;
		CircuitElm realMouseElm;

		g = this.circuitImage.getGraphics();
		g.setColor(Color.black);

		g.fillRect(0, 0, this.cirSim.winSize.width, this.cirSim.winSize.height);

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

		CircuitElm.powerMult = Math.exp(this.cirSim.powerBar.getValue() / 4.762 - 7);

		int i;
		Font oldfont = g.getFont();
		for (i = 0; i != this.cirSim.circuit.getElementCount(); i++)
		{
			if (this.cirSim.powerCheckItem.getState())
			{
				g.setColor(Color.gray);
			}
			/*
			 * else if (conductanceCheckItem.getState())
			 * g.setColor(Color.white);
			 */
			this.cirSim.circuit.getElementAt(i).draw(g);
		}

		if (this.cirSim.tempMouseMode == CirSim.MODE_DRAG_ROW || this.cirSim.tempMouseMode == CirSim.MODE_DRAG_COLUMN
				|| this.cirSim.tempMouseMode == CirSim.MODE_DRAG_POST
				|| this.cirSim.tempMouseMode == CirSim.MODE_DRAG_SELECTED)
		{
			for (i = 0; i != this.cirSim.circuit.getElementCount(); i++)
			{
				CircuitElm ce = this.cirSim.circuit.getElementAt(i);
				DrawUtil.drawPost(g, ce.x, ce.y);
				DrawUtil.drawPost(g, ce.x2, ce.y2);
			}
		}

		int badnodes = 0;

		// find bad connections, nodes not connected to other elements which
		// intersect other elements' bounding boxes
		for (i = 0; i != this.cirSim.circuit.getNodeCount(); i++)
		{
			CircuitNode cn = this.cirSim.circuit.getNodeAt(i);
			if (!cn.isInternal() && cn.getSize() == 1)
			{
				int bb = 0, j;
				CircuitNodeLink cnl = cn.elementAt(0);
				for (j = 0; j != this.cirSim.circuit.getElementCount(); j++)
				{
					if (cnl.elm != this.cirSim.circuit.getElementAt(j)
							&& this.cirSim.circuit.getElementAt(j).boundingBox.contains(cn.x, cn.y))
					{
						bb++;
					}
				}
				if (bb > 0)
				{
					g.setColor(Color.red);
					g.fillOval(cn.x - 3, cn.y - 3, 7, 7);
					badnodes++;
				}
			}
		}
		/*
		 * if (mouseElm != null) { g.setFont(oldfont); g.drawString("+",
		 * mouseElm.x+10, mouseElm.y); }
		 */
		if (this.cirSim.dragElm != null
				&& (this.cirSim.dragElm.x != this.cirSim.dragElm.x2 || this.cirSim.dragElm.y != this.cirSim.dragElm.y2))
		{
			this.cirSim.dragElm.draw(g);
		}

		g.setFont(oldfont);

		g.setColor(CircuitElm.WHITE_COLOR);
		if (this.cirSim.stopMessage != null)
		{
			g.drawString(this.cirSim.stopMessage, 10, this.cirSim.circuit.circuitArea.height);
		}
		else
		{
			if (this.cirSim.circuit.circuitBottom == 0)
			{
				this.cirSim.circuit.calcCircuitBottom();
			}
			String info[] = new String[10];
			if (this.cirSim.mouseElm != null)
			{
				if (this.cirSim.mousePost == -1)
				{
					this.cirSim.mouseElm.getInfo(info);
				}
				else
				{
					info[0] = "V = "
							+ CoreUtil.getUnitText(this.cirSim.mouseElm.getPostVoltage(this.cirSim.mousePost), "V");
					/*
					 * //shownodes for (i = 0; i != mouseElm.getPostCount();
					 * i++) info[0] += " " + mouseElm.nodes[i]; if
					 * (mouseElm.getVoltageSourceCount() > 0) info[0] += ";" +
					 * (mouseElm.getVoltageSource()+nodeList.size());
					 */
				}

			}
			else
			{
				CircuitElm.showFormat.setMinimumFractionDigits(2);
				info[0] = "t = " + CoreUtil.getUnitText(this.cirSim.timer.time, "s");
				CircuitElm.showFormat.setMinimumFractionDigits(0);
			}
			if (this.cirSim.hintType != -1)
			{
				for (i = 0; info[i] != null; i++)
				{
					;
				}
				String s = this.cirSim.getHint();
				if (s == null)
				{
					this.cirSim.hintType = -1;
				}
				else
				{
					info[i] = s;
				}
			}
			int x = 0;

			int ct = this.cirSim.scopeMan.scopeCount;

			if (this.cirSim.stopMessage != null)
			{
				ct = 0;
			}

			if (ct != 0)
			{
				x = this.cirSim.scopeMan.scopes[ct - 1].rightEdge() + 20;
			}

			x = Math.max(x, this.cirSim.winSize.width * 2 / 3);

			// count lines of data
			for (i = 0; info[i] != null; i++)
			{

			}

			if (badnodes > 0)
			{
				if (badnodes == 1)
				{
					info[i++] = badnodes + " bad connection";
				}
				else
				{
					info[i++] = badnodes + " bad connections";
				}
			}

			// find where to show data; below circuit, not too high unless we
			// need it
			int ybase = this.cirSim.winSize.height - 15 * i - 5;
			ybase = Math.min(ybase, this.cirSim.circuit.circuitArea.height);
			ybase = Math.max(ybase, this.cirSim.circuit.circuitBottom);

			for (i = 0; info[i] != null; i++)
			{
				g.drawString(info[i], x, ybase + 15 * (i + 1));
			}

		}

		if (this.cirSim.selectedArea != null)
		{
			g.setColor(CircuitElm.SELECT_COLOR);
			g.drawRect(this.cirSim.selectedArea.x, this.cirSim.selectedArea.y, this.cirSim.selectedArea.width,
					this.cirSim.selectedArea.height);
		}

		realMouseElm = this.cirSim.mouseElm;
		this.cirSim.mouseElm = realMouseElm;
		/*
		 * g.setColor(Color.white); g.drawString("Framerate: " + framerate, 10,
		 * 10); g.drawString("Steprate: " + steprate, 10, 30);
		 * g.drawString("Steprate/iter: " + (steprate/getIterCount()), 10, 50);
		 * g.drawString("iterc: " + (getIterCount()), 10, 70);
		 */

		realg.drawImage(this.circuitImage, 0, 0, this.cirSim.cirFrame);

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
